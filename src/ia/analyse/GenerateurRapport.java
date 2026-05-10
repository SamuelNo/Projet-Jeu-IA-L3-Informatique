package ia.analyse;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ia.IAStats;
import ia.IAFacile;
import ia.IAMoyenne;
import ia.IADifficile;
import test.SimulateIAvIA;

/**
 * Générateur de rapport pour les IA. L'objectif est de lancer une série d'expériences, 
 * de collecter les données et de les formater dans des fichiers CSV structurés ("Tidy Data").
 */
public class GenerateurRapport {

    private static final Path DATA_DIR = Paths.get("data");
    private static final Path PROGRESS_FILE = DATA_DIR.resolve("txt/progress_tournoi.txt");

    public static void main(String[] args) throws Exception {
        Files.createDirectories(DATA_DIR);
        initFiles();

        long campaignStart = System.nanoTime();

        // Expérience 1 : Avantage du trait (Matchs Miroirs)
        analyserAvantageTrait();

        // Expérience 2 : Scénarios asymétriques avec swap (Changement à la mi-temps) ---
        // IAFACILE vs IADIFFICILE
        scenarioFacileDifficileFaibleSwap();
        scenarioFacileDifficileFortSwap();
        // IAFACILE vs IAMOYENNE
        scenarioFacileMoyenneFaibleSwap();
        scenarioFacileMoyenneFortSwap();
        // IAMOYENNE vs IADIFFICILE
        scenarioMoyenneDifficileFaibleSwap();
        scenarioMoyenneDifficileFortSwap();

        // Expérience 3 : Scénarios asymétriques constants (Pas de changement) ---
        // IAFACILE vs IADIFFICILE
        scenarioFacileDifficileFaibleConstant();
        scenarioFacileDifficileFortConstant();
        // IAFACILE vs IAMOYENNE
        scenarioFacileMoyenneFaibleConstant();
        scenarioFacileMoyenneFortConstant();
        // IAMOYENNE vs IADIFFICILE
        scenarioMoyenneDifficileFaibleConstant();
        scenarioMoyenneDifficileFortConstant();
        // Expérience 4 : Analyse Croisée Profondeur vs Heuristique
        analyserImpactProfondeurCroise();

       // Expérience 5 : Performances pures
        runBenchmark(); 

        long totalMs = (System.nanoTime() - campaignStart) / 1_000_000L;
        System.out.println("Campagne terminée en " + totalMs + " ms");
    }

    /** Initialise les fichiers de données maîtres en écrivant les en-têtes. */
    private static void initFiles() throws IOException {
        // 1. Fichier Équité
        try (BufferedWriter w = Files.newBufferedWriter(DATA_DIR.resolve("csv/analyse_premier_joueur.csv"), StandardCharsets.UTF_8)) {
            w.write("Match_ID,VainqueurID,NbTours,TempsTotalMatchMS\n");
        }
        // 2. Fichier Performances
        try (BufferedWriter w = Files.newBufferedWriter(DATA_DIR.resolve("csv/performances_profondeur.csv"), StandardCharsets.UTF_8)) {
            w.write("Heuristique,Profondeur,TempsMoyenCoupMS,TempsMaxCoupMS,NoeudsVisitesMoyen\n");
        }
        // 3. Fichier Maître : Tous les scénarios AVEC Swap
        try (BufferedWriter w = Files.newBufferedWriter(DATA_DIR.resolve("csv/asymetrique_swaps.csv"), StandardCharsets.UTF_8)) {
            w.write("Scenario_Type,Match_ID,IA_J1,IA_J2,Vainqueur,NbTours,TempsTotalPartieMS,TempsMoyenCoup_J1,TempsMoyenCoup_J2\n");
        }
        // 4. Fichier Maître : Tous les scénarios CONSTANTS
        try (BufferedWriter w = Files.newBufferedWriter(DATA_DIR.resolve("csv/asymetrique_constants.csv"), StandardCharsets.UTF_8)) {
            w.write("Scenario_Type,Match_ID,IA_J1,IA_J2,Vainqueur,NbTours,TempsTotalPartieMS,TempsMoyenCoup_J1,TempsMoyenCoup_J2\n");
        }
        // 5. NOUVEAU Fichier : Impact de la profondeur croisée
        try (BufferedWriter w = Files.newBufferedWriter(DATA_DIR.resolve("csv/impact_profondeur_croise.csv"), StandardCharsets.UTF_8)) {
            w.write("Scenario_Type,Match_ID,IA_J1,IA_J2,Vainqueur,NbTours,TempsTotalPartieMS,TempsMoyenCoup_J1,TempsMoyenCoup_J2\n");
        }
        
        Files.writeString(PROGRESS_FILE, "");
    }

    /** Met à jour le fichier de progression. */
    private static void updateProgress(int completed, int total) {
        String s = String.format("completed=%d,total=%d", completed, total);
        try {
            Files.writeString(PROGRESS_FILE, s, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Impossible d'ecrire progress: " + e.getMessage());
        }
    }

    // ========================================================================
    // EXPÉRIENCE 4 : IMPACT PROFONDEUR CROISÉE
    // ========================================================================

    // Expérience 4 : Analyse croisée de l'impact de la profondeur vs l'heuristique. Scénarios miroir et asymétriques, avec et sans swap. Écriture dans impact_profondeur_croise.csv
    public static void analyserImpactProfondeurCroise() throws Exception {
        System.out.println("Lancement expérience 4 : Analyse Croisée (Heuristique vs Profondeur)");
        String csvSwap = "csv/asymetrique_swaps.csv";
        String csvConst = "csv/asymetrique_constants.csv";
        String csvImpact = "csv/impact_profondeur_croise.csv";
        String txtRes = "data/txt/resultats_tournoi.txt";
        String txtNul = "data/txt/details_matchs_nuls.txt";

        // --- DUEL 1 : DIFF_P2 vs MOY_P4 (Le Cerveau vs Les Muscles) ---
        System.out.println("-> Duel : DIFF_P2 vs MOY_P4");
        // Constant : Diff J1 vs Moy J2
        IAStats.reset();
        SimulateIAvIA.main(new String[]{"IADIFFICILE", "IAMOYENNE", "100", "30", txtRes, txtNul, "2", "4"});
        parseAndAppendAsymetrique(txtRes, "DIFF_P2_VS_MOY_P4_CONSTANT", "IADIFFICILE", "IAMOYENNE", csvImpact, 0);

        // Swap (50/50)
        int offset = 0;
        IAStats.reset();
        SimulateIAvIA.main(new String[]{"IADIFFICILE", "IAMOYENNE", "50", "30", txtRes, txtNul, "2", "4"});
        parseAndAppendAsymetrique(txtRes, "DIFF_P2_VS_MOY_P4_SWAP", "IADIFFICILE", "IAMOYENNE", csvImpact, offset);
        offset += 50;
        SimulateIAvIA.main(new String[]{"IAMOYENNE", "IADIFFICILE", "50", "30", txtRes, txtNul, "4", "2"});
        parseAndAppendAsymetrique(txtRes, "DIFF_P2_VS_MOY_P4_SWAP", "IAMOYENNE", "IADIFFICILE", csvImpact, offset);

        // --- DUEL 2 : DIFF_P4 vs MOY_P2 (Domination Totale) ---
        System.out.println("-> Duel : DIFF_P4 vs MOY_P2");
        // Constant : Diff J1 vs Moy J2
        IAStats.reset();
        SimulateIAvIA.main(new String[]{"IADIFFICILE", "IAMOYENNE", "100", "30", txtRes, txtNul, "4", "2"});
        parseAndAppendAsymetrique(txtRes, "DIFF_P4_VS_MOY_P2_CONSTANT", "IADIFFICILE", "IAMOYENNE", csvImpact, 0);

        // Swap (50/50)
        offset = 0;
        IAStats.reset();
        SimulateIAvIA.main(new String[]{"IADIFFICILE", "IAMOYENNE", "50", "30", txtRes, txtNul, "4", "2"});
        parseAndAppendAsymetrique(txtRes, "DIFF_P4_VS_MOY_P2_SWAP", "IADIFFICILE", "IAMOYENNE", csvImpact, offset);
        offset += 50;
        SimulateIAvIA.main(new String[]{"IAMOYENNE", "IADIFFICILE", "50", "30", txtRes, txtNul, "2", "4"});
        parseAndAppendAsymetrique(txtRes, "DIFF_P4_VS_MOY_P2_SWAP", "IAMOYENNE", "IADIFFICILE", csvImpact, offset);

        // --- MIROIRS : Impact pur de la profondeur ---
        System.out.println("-> Miroirs : P2 vs P4");

        // 1. MIROIR MOYENNE (P2 vs P4)
        // Constant : P2 commence toujours
        IAStats.reset();
        SimulateIAvIA.main(new String[]{"IAMOYENNE", "IAMOYENNE", "100", "30", txtRes, txtNul, "2", "4"});
        parseAndAppendAsymetrique(txtRes, "MOY_P2_VS_MOY_P4_CONSTANT", "IAMOYENNE_P2", "IAMOYENNE_P4", csvImpact, 0);

        // Swap (50/50)
        offset = 0;
        IAStats.reset();
        SimulateIAvIA.main(new String[]{"IAMOYENNE", "IAMOYENNE", "50", "30", txtRes, txtNul, "2", "4"});
        parseAndAppendAsymetrique(txtRes, "MOY_P2_VS_MOY_P4_SWAP", "IAMOYENNE_P2", "IAMOYENNE_P4", csvImpact, offset);
        offset += 50;
        SimulateIAvIA.main(new String[]{"IAMOYENNE", "IAMOYENNE", "50", "30", txtRes, txtNul, "4", "2"});
        parseAndAppendAsymetrique(txtRes, "MOY_P2_VS_MOY_P4_SWAP", "IAMOYENNE_P4", "IAMOYENNE_P2", csvImpact, offset);

        // 2. MIROIR DIFFICILE (P2 vs P4)
        // Constant : P2 commence toujours
        IAStats.reset();
        SimulateIAvIA.main(new String[]{"IADIFFICILE", "IADIFFICILE", "100", "30", txtRes, txtNul, "2", "4"});
        parseAndAppendAsymetrique(txtRes, "DIFF_P2_VS_DIFF_P4_CONSTANT", "IADIFFICILE_P2", "IADIFFICILE_P4", csvImpact, 0);

        // Swap (50/50)
        offset = 0;
        IAStats.reset();
        SimulateIAvIA.main(new String[]{"IADIFFICILE", "IADIFFICILE", "50", "30", txtRes, txtNul, "2", "4"});
        parseAndAppendAsymetrique(txtRes, "DIFF_P2_VS_DIFF_P4_SWAP", "IADIFFICILE_P2", "IADIFFICILE_P4", csvImpact, offset);
        offset += 50;
        SimulateIAvIA.main(new String[]{"IADIFFICILE", "IADIFFICILE", "50", "30", txtRes, txtNul, "4", "2"});
        parseAndAppendAsymetrique(txtRes, "DIFF_P2_VS_DIFF_P4_SWAP", "IADIFFICILE_P4", "IADIFFICILE_P2", csvImpact, offset);
}

    // ========================================================================
    // EXPÉRIENCES 1 ET 5
    // ========================================================================

    // Expérience 1 : Analyser l'avantage du trait en match miroir (IAMOYENNE vs IAMOYENNE) et écrire dans analyse_premier_joueur.csv
    public static void analyserAvantageTrait() throws Exception {
        System.out.println("Lancement expérience 1: Match miroir (IAMOYENNE vs IAMOYENNE)");
        IAMoyenne.setProfondeur(3);
        IAStats.reset();
        int nb = 100;
        String outRes = "data/txt/resultats_tournoi.txt";
        String outNuls = "data/txt/details_matchs_nuls.txt";

        AtomicInteger completed = new AtomicInteger(0);
        SimulateIAvIA.progressCallback = (i, nbTotal) -> {
            int done = completed.incrementAndGet();
            updateProgress(done, nb);
        };

        SimulateIAvIA.main(new String[]{"IAMOYENNE", "IAMOYENNE", String.valueOf(nb), String.valueOf(30), outRes, outNuls, "3", "3"});

        try {
            List<String> lines = Files.readAllLines(Paths.get(outRes), StandardCharsets.UTF_8);
            Pattern p = Pattern.compile("Combat (\\d+) .*issue=(\\w+) .*tours=(\\d+) .*duree=([0-9,.]+) (s|ms)");
            int vicJ1 = 0;
            try (BufferedWriter w = Files.newBufferedWriter(DATA_DIR.resolve("csv/analyse_premier_joueur.csv"), StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND)) {
                for (String l : lines) {
                    Matcher m = p.matcher(l);
                    if (m.find()) {
                        String id = m.group(1);
                        String issue = m.group(2);
                        String tours = m.group(3);
                        String duree = m.group(4).replace(',', '.');
                        String unit = m.group(5);
                        double dureeMs = unit.equals("s") ? Double.parseDouble(duree) * 1000.0 : Double.parseDouble(duree);
                        w.write(String.format(Locale.US, "%s,%s,%s,%.0f\n", id, issue, tours, dureeMs));
                        if ("J1".equals(issue)) vicJ1++;
                    }
                }
            }
            double pct = 100.0 * vicJ1 / Math.max(1, nb);
            System.out.println(String.format("Pourcentage victoires Joueur 1: %.2f%%", pct));
        } catch (IOException e) {
            System.err.println("Erreur parsing mirror results: " + e.getMessage());
        }
        SimulateIAvIA.progressCallback = null;
    }

    // Expérience 5 : Benchmark de performances pures (Temps moyen par coup, nombre de noeuds visités) en fonction de la profondeur et de l'heuristique. Écriture dans performances_profondeur.csv
    public static void runBenchmark() throws Exception {
        System.out.println("Lancement expérience 5: Benchmark heuristiques/profondeur");
        String[] heuristiques = {"IAFACILE", "IAMOYENNE", "IADIFFICILE"};
        for (String h : heuristiques) {
            for (int profondeur = 1; profondeur <= 4; profondeur++) {
                if ("IAFACILE".equals(h)) IAFacile.setProfondeur(profondeur);
                if ("IAMOYENNE".equals(h)) IAMoyenne.setProfondeur(profondeur);
                if ("IADIFFICILE".equals(h)) IADifficile.setProfondeur(profondeur);

                int nb = 5;
                IAStats.reset();
                String outRes = "data/txt/resultats_tournoi.txt";
                String outNuls = "data/txt/details_matchs_nuls.txt";

                System.out.println(String.format("Benchmark %s profondeur %d (%d matchs)", h, profondeur, nb));
                SimulateIAvIA.main(new String[]{h, h, String.valueOf(nb), String.valueOf(30), outRes, outNuls, String.valueOf(profondeur), String.valueOf(profondeur)});

                double tempsMoyenCoupMs = IAStats.getMoveCount() == 0 ? 0.0 : (IAStats.getTotalMoveTimeNs() / 1_000_000.0) / IAStats.getMoveCount();
                long noeudsTotal = IAStats.getNodes();
                double noeudsMoyen = nb == 0 ? 0.0 : ((double) noeudsTotal) / nb;

                double tempsMaxAvg = 0.0;
                try {
                    List<String> lines = Files.readAllLines(Paths.get(outRes), StandardCharsets.UTF_8);
                    Pattern p = Pattern.compile("avgCoupJ1=([0-9,.]+)(?: ms)? .*avgCoupJ2=([0-9,.]+)(?: ms)?");
                    for (String l : lines) {
                        Matcher m = p.matcher(l);
                        if (m.find()) {
                            double a1 = Double.parseDouble(m.group(1).replace(',', '.'));
                            double a2 = Double.parseDouble(m.group(2).replace(',', '.'));
                            tempsMaxAvg = Math.max(tempsMaxAvg, Math.max(a1, a2));
                        }
                    }
                } catch (IOException e) {}

                try (BufferedWriter w = Files.newBufferedWriter(DATA_DIR.resolve("csv/performances_profondeur.csv"), StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND)) {
                    w.write(String.format(Locale.US, "%s,%d,%.3f,%.3f,%.1f\n", h, profondeur, tempsMoyenCoupMs, tempsMaxAvg, noeudsMoyen));
                }
            }
        }
    }

    // ========================================================================
    // SCÉNARIOS ASYMÉTRIQUES - SWAPS (Écriture dans asymetrique_swaps.csv)
    // ========================================================================

    // Ces scénarios analysent l'impact du swap (changement de joueur à la mi-temps) dans des duels asymétriques. Chaque scénario est joué en deux phases : d'abord avec une configuration (ex: J1 = Facile, J2 = Difficile), puis avec les rôles inversés (J1 = Difficile, J2 = Facile). Les résultats sont écrits dans asymetrique_swaps.csv avec un offset pour différencier les phases.
    public static void scenarioFacileDifficileFaibleSwap() throws Exception {
        System.out.println("Lancement : FACILE vs DIFF (Faible J1 puis Swap)");
        IAFacile.setProfondeur(3); IADifficile.setProfondeur(3);
        String[][] duels = {{"IAFACILE", "IADIFFICILE"}, {"IADIFFICILE", "IAFACILE"}};
        int offset = 0;
        for (String[] duel : duels) {
            IAStats.reset();
            SimulateIAvIA.main(new String[]{duel[0], duel[1], "50", "30", "data/txt/resultats_tournoi.txt", "data/txt/details_matchs_nuls.txt", "3", "3"});
            parseAndAppendAsymetrique("data/txt/resultats_tournoi.txt", "FACILE_VS_DIFF_FAIBLE_SWAP", duel[0], duel[1], "csv/asymetrique_swaps.csv", offset);
            offset += 50;
        }
    }

    public static void scenarioFacileDifficileFortSwap() throws Exception {
        System.out.println("Lancement : FACILE vs DIFF (Fort J1 puis Swap)");
        IAFacile.setProfondeur(3); IADifficile.setProfondeur(3);
        String[][] duels = {{"IADIFFICILE", "IAFACILE"}, {"IAFACILE", "IADIFFICILE"}};
        int offset = 0;
        for (String[] duel : duels) {
            IAStats.reset();
            SimulateIAvIA.main(new String[]{duel[0], duel[1], "50", "30", "data/txt/resultats_tournoi.txt", "data/txt/details_matchs_nuls.txt", "3", "3"});
            parseAndAppendAsymetrique("data/txt/resultats_tournoi.txt", "FACILE_VS_DIFF_FORT_SWAP", duel[0], duel[1], "csv/asymetrique_swaps.csv", offset);
            offset += 50;
        }
    }

    public static void scenarioFacileMoyenneFaibleSwap() throws Exception {
        System.out.println("Lancement : FACILE vs MOYENNE (Faible J1 puis Swap)");
        IAFacile.setProfondeur(3); IAMoyenne.setProfondeur(3);
        String[][] duels = {{"IAFACILE", "IAMOYENNE"}, {"IAMOYENNE", "IAFACILE"}};
        int offset = 0;
        for (String[] duel : duels) {
            IAStats.reset();
            SimulateIAvIA.main(new String[]{duel[0], duel[1], "50", "30", "data/txt/resultats_tournoi.txt", "data/txt/details_matchs_nuls.txt", "3", "3"});
            parseAndAppendAsymetrique("data/txt/resultats_tournoi.txt", "FACILE_VS_MOYENNE_FAIBLE_SWAP", duel[0], duel[1], "csv/asymetrique_swaps.csv", offset);
            offset += 50;
        }
    }

    public static void scenarioFacileMoyenneFortSwap() throws Exception {
        System.out.println("Lancement : FACILE vs MOYENNE (Fort J1 puis Swap)");
        IAFacile.setProfondeur(3); IAMoyenne.setProfondeur(3);
        String[][] duels = {{"IAMOYENNE", "IAFACILE"}, {"IAFACILE", "IAMOYENNE"}};
        int offset = 0;
        for (String[] duel : duels) {
            IAStats.reset();
            SimulateIAvIA.main(new String[]{duel[0], duel[1], "50", "30", "data/txt/resultats_tournoi.txt", "data/txt/details_matchs_nuls.txt", "3", "3"});
            parseAndAppendAsymetrique("data/txt/resultats_tournoi.txt", "FACILE_VS_MOYENNE_FORT_SWAP", duel[0], duel[1], "csv/asymetrique_swaps.csv", offset);
            offset += 50;
        }
    }

    public static void scenarioMoyenneDifficileFaibleSwap() throws Exception {
        System.out.println("Lancement : MOYENNE vs DIFF (Faible J1 puis Swap)");
        IAMoyenne.setProfondeur(3); IADifficile.setProfondeur(3);
        String[][] duels = {{"IAMOYENNE", "IADIFFICILE"}, {"IADIFFICILE", "IAMOYENNE"}};
        int offset = 0;
        for (String[] duel : duels) {
            IAStats.reset();
            SimulateIAvIA.main(new String[]{duel[0], duel[1], "50", "30", "data/txt/resultats_tournoi.txt", "data/txt/details_matchs_nuls.txt", "3", "3"});
            parseAndAppendAsymetrique("data/txt/resultats_tournoi.txt", "MOYENNE_VS_DIFF_FAIBLE_SWAP", duel[0], duel[1], "csv/asymetrique_swaps.csv", offset);
            offset += 50;
        }
    }

    public static void scenarioMoyenneDifficileFortSwap() throws Exception {
        System.out.println("Lancement : MOYENNE vs DIFF (Fort J1 puis Swap)");
        IAMoyenne.setProfondeur(3); IADifficile.setProfondeur(3);
        String[][] duels = {{"IADIFFICILE", "IAMOYENNE"}, {"IAMOYENNE", "IADIFFICILE"}};
        int offset = 0;
        for (String[] duel : duels) {
            IAStats.reset();
            SimulateIAvIA.main(new String[]{duel[0], duel[1], "50", "30", "data/txt/resultats_tournoi.txt", "data/txt/details_matchs_nuls.txt", "3", "3"});
            parseAndAppendAsymetrique("data/txt/resultats_tournoi.txt", "MOYENNE_VS_DIFF_FORT_SWAP", duel[0], duel[1], "csv/asymetrique_swaps.csv", offset);
            offset += 50;
        }
    }

    // ========================================================================
    // SCÉNARIOS ASYMÉTRIQUES - CONSTANTS (Écriture dans asymetrique_constants.csv)
    // ========================================================================

    // Ces scénarios analysent les duels asymétriques sans swap, c'est-à-dire que le même joueur commence toujours (ex: J1 = Facile, J2 = Difficile). Cela permet d'observer l'impact de l'avantage du trait dans des configurations déséquilibrées. Les résultats sont écrits dans asymetrique_constants.csv.
    public static void scenarioFacileDifficileFaibleConstant() throws Exception {
        System.out.println("Lancement : FACILE vs DIFF (Faible Constant)");
        IAFacile.setProfondeur(3); IADifficile.setProfondeur(3);
        IAStats.reset();
        SimulateIAvIA.main(new String[]{"IAFACILE", "IADIFFICILE", "100", "30", "data/txt/resultats_tournoi.txt", "data/txt/details_matchs_nuls.txt", "3", "3"});
        parseAndAppendAsymetrique("data/txt/resultats_tournoi.txt", "FACILE_VS_DIFF_FAIBLE_CONSTANT", "IAFACILE", "IADIFFICILE", "csv/asymetrique_constants.csv", 0);
    }

    public static void scenarioFacileDifficileFortConstant() throws Exception {
        System.out.println("Lancement : FACILE vs DIFF (Fort Constant)");
        IAFacile.setProfondeur(3); IADifficile.setProfondeur(3);
        IAStats.reset();
        SimulateIAvIA.main(new String[]{"IADIFFICILE", "IAFACILE", "100", "30", "data/txt/resultats_tournoi.txt", "data/txt/details_matchs_nuls.txt", "3", "3"});
        parseAndAppendAsymetrique("data/txt/resultats_tournoi.txt", "FACILE_VS_DIFF_FORT_CONSTANT", "IADIFFICILE", "IAFACILE", "csv/asymetrique_constants.csv", 0);
    }

    public static void scenarioFacileMoyenneFaibleConstant() throws Exception {
        System.out.println("Lancement : FACILE vs MOYENNE (Faible Constant)");
        IAFacile.setProfondeur(3); IAMoyenne.setProfondeur(3);
        IAStats.reset();
        SimulateIAvIA.main(new String[]{"IAFACILE", "IAMOYENNE", "100", "30", "data/txt/resultats_tournoi.txt", "data/txt/details_matchs_nuls.txt", "3", "3"});
        parseAndAppendAsymetrique("data/txt/resultats_tournoi.txt", "FACILE_VS_MOYENNE_FAIBLE_CONSTANT", "IAFACILE", "IAMOYENNE", "csv/asymetrique_constants.csv", 0);
    }

    public static void scenarioFacileMoyenneFortConstant() throws Exception {
        System.out.println("Lancement : FACILE vs MOYENNE (Fort Constant)");
        IAMoyenne.setProfondeur(3); IAFacile.setProfondeur(3);
        IAStats.reset();
        SimulateIAvIA.main(new String[]{"IAMOYENNE", "IAFACILE", "100", "30", "data/txt/resultats_tournoi.txt", "data/txt/details_matchs_nuls.txt", "3", "3"});
        parseAndAppendAsymetrique("data/txt/resultats_tournoi.txt", "FACILE_VS_MOYENNE_FORT_CONSTANT", "IAMOYENNE", "IAFACILE", "csv/asymetrique_constants.csv", 0);
    }

    public static void scenarioMoyenneDifficileFaibleConstant() throws Exception {
        System.out.println("Lancement : MOYENNE vs DIFF (Faible Constant)");
        IAMoyenne.setProfondeur(3); IADifficile.setProfondeur(3);
        IAStats.reset();
        SimulateIAvIA.main(new String[]{"IAMOYENNE", "IADIFFICILE", "100", "30", "data/txt/resultats_tournoi.txt", "data/txt/details_matchs_nuls.txt", "3", "3"});
        parseAndAppendAsymetrique("data/txt/resultats_tournoi.txt", "MOYENNE_VS_DIFF_FAIBLE_CONSTANT", "IAMOYENNE", "IADIFFICILE", "csv/asymetrique_constants.csv", 0);
    }

    public static void scenarioMoyenneDifficileFortConstant() throws Exception {
        System.out.println("Lancement : MOYENNE vs DIFF (Fort Constant)");
        IAMoyenne.setProfondeur(3); IADifficile.setProfondeur(3);
        IAStats.reset();
        SimulateIAvIA.main(new String[]{"IADIFFICILE", "IAMOYENNE", "100", "30", "data/txt/resultats_tournoi.txt", "data/txt/details_matchs_nuls.txt", "3", "3"});
        parseAndAppendAsymetrique("data/txt/resultats_tournoi.txt", "MOYENNE_VS_DIFF_FORT_CONSTANT", "IADIFFICILE", "IAMOYENNE", "csv/asymetrique_constants.csv", 0);
    }

    // ========================================================================
    // MÉTHODE DE PARSING GLOBALE
    // ========================================================================

    /** Parse et ajoute les résultats à un fichier CSV avec un décalage d'ID. */
    private static void parseAndAppendAsymetrique(String resultFile, String scenarioType, String ia1, String ia2, String csvFileName, int idOffset) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(resultFile), StandardCharsets.UTF_8);
            Pattern p = Pattern.compile("Combat (\\d+) .*issue=(\\w+) .*tours=(\\d+) .*duree=([0-9,.]+) (s|ms).*avgCoupJ1=([0-9,.]+)(?: ms)? .*avgCoupJ2=([0-9,.]+)(?: ms)?");
            try (BufferedWriter w = Files.newBufferedWriter(DATA_DIR.resolve(csvFileName), StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND)) {
                for (String l : lines) {
                    Matcher m = p.matcher(l);
                    if (m.find()) {
                        int id = Integer.parseInt(m.group(1)) + idOffset;
                        String issue = m.group(2);
                        String tours = m.group(3);
                        String duree = m.group(4).replace(',', '.');
                        String unit = m.group(5);
                        double dureeMs = unit.equals("s") ? Double.parseDouble(duree) * 1000.0 : Double.parseDouble(duree);
                        String avg1 = m.group(6).replace(',', '.');
                        String avg2 = m.group(7).replace(',', '.');
                        
                        String row = String.format(Locale.US, "%s,%d,%s,%s,%s,%s,%.0f,%.3f,%.3f\n", 
                                scenarioType, id, ia1, ia2, issue, tours, dureeMs, Double.parseDouble(avg1), Double.parseDouble(avg2));
                        w.write(row);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur parsing resultats pour " + csvFileName + ": " + e.getMessage());
        }
    }
}
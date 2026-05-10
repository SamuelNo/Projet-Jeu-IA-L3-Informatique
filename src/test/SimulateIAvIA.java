package test;

import architecture.Arene;
import entite.*;
import ia.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import exception.IllegalAttackException;
import exception.IllegalEnergieException;
import exception.IllegalParadeException;
import exception.IllegalReposException;

/**
 * SimulateIAvIA — outil de simulation de matchs IA vs IA.
 * Lance une série de combats entre deux IA (configurables) et collecte
 * les résultats, durées et statistiques pour générer des rapports.
 * Utilisé par l'UI `TournamentWindow` et par les scripts d'analyse.
 */
public class SimulateIAvIA {
    private static final Random RNG = new Random();
    private static final String IA1_DEFAULT = "IAMOYENNE";
    private static final String IA2_DEFAULT = "IAMOYENNE";
    private static final int NB_COMBATS_DEFAULT = 40;
    private static final int MAX_TOURS_DEFAULT = 30;
    private static final int PROFONDEUR_DEFAULT = 2;
    private static final String FICHIER_RESULTATS_DEFAULT = "data/txt/resultats_tournoi.txt";
    private static final String FICHIER_NULS_DEFAULT = "data/txt/details_matchs_nuls.txt";

    // Classe interne pour stocker les résultats d'un combat individuel
    private static class CombatResult {
        int index;
        int tours;
        String issue;
        Personnage p1;
        Personnage p2;
        String matchup;
        String logComplet;
        long dureeCombatMs;
        long tempsDecisionJ1Ns;
        long tempsDecisionJ2Ns;
        int nbDecisionsJ1;
        int nbDecisionsJ2;
        boolean mortSubite;
    }

    // Progress callback: called after each combat with (combatIndex, nbCombats)
    public static java.util.function.BiConsumer<Integer,Integer> progressCallback = null;

    // profondeurs actives par joueur (remplies depuis main)
    private static int PROFONDEUR_J1 = PROFONDEUR_DEFAULT;
    private static int PROFONDEUR_J2 = PROFONDEUR_DEFAULT;

    // Méthode utilitaire pour générer un personnage aléatoire
    private static Personnage personnageAleatoire() {
        int tirage = RNG.nextInt(3);
        if (tirage == 0) {
            return new Chevalier();
        }
        if (tirage == 1) {
            return new Archere();
        }
        return new Soigneur();
    }

    // Méthode utilitaire pour choisir un coup à partir du nom de l'IA et de l'état courant
    private static Coup choisirCoupParIA(String iaNom, Etat etat) {
        String ia = iaNom == null ? "" : iaNom.trim().toUpperCase();
        switch (ia) {
            case "IAFACILE":
                return IAFacile.choisirCoup(etat, false);
            case "IADIFFICILE":
                return IADifficile.choisirCoup(etat, false);
            case "IAMOYENNE":
            default:
                return IAMoyenne.choisirCoup(etat, false);
        }
    }

    // Méthodes utilitaires pour formater les résultats et les logs de combat
    private static String positionToString(Position p) {
        if (p == null) {
            return "(null)";
        }
        return "(" + p.getLigne() + "," + p.getColonne() + ")";
    }

    // Méthode utilitaire pour convertir la grille en une chaîne de caractères lisible
    private static String grilleToString(int[][] grille) {
        StringBuilder sb = new StringBuilder();
        sb.append("    A B C D E F G H I J\n");
        for (int l = 0; l < grille.length; l++) {
            sb.append((char) ('A' + l)).append("   ");
            for (int c = 0; c < grille[l].length; c++) {
                sb.append(grille[l][c]);
                if (c < grille[l].length - 1) {
                    sb.append(' ');
                }
            }
            sb.append('\n');
        }
        return sb.toString();
    }
    
    // Méthode utilitaire pour formater les statistiques d'un personnage
    private static void ecrireResultatsPartiels(List<CombatResult> tousLesCombats, int victoiresJ1, int victoiresJ2, int nuls,
        String iaJoueur1, String iaJoueur2, int nbCombatsTotal, int maxTours, int profondeurJ1, int profondeurJ2, long dureeTournoiMs,
        long totalDecisionJ1Ns, int totalDecisionsJ1, long totalDecisionJ2Ns, int totalDecisionsJ2,
        List<String> detailsNuls, String fichierResultats, String fichierNuls) {
        try {
            String horodatage = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            List<String> recap = new ArrayList<>();
            recap.add("=== Recapitulatif Tournoi IA ===");
            recap.add("Date: " + horodatage);
            recap.add("IA Joueur 1: " + iaJoueur1);
            recap.add("IA Joueur 2: " + iaJoueur2);
            recap.add("Profondeur J1: " + profondeurJ1 + " | Profondeur J2: " + profondeurJ2);
            recap.add("Nombre de combats: " + tousLesCombats.size() + "/" + nbCombatsTotal);
            recap.add("Tours max par combat: " + maxTours);
            recap.add("");
            recap.add("Victoires J1: " + victoiresJ1);
            recap.add("Defaites J1: " + victoiresJ2);
            recap.add("Victoires J2: " + victoiresJ2);
            recap.add("Defaites J2: " + victoiresJ1);
            recap.add("Matchs nuls: " + nuls);
            recap.add("Temps total tournoi: " + dureeLisible(dureeTournoiMs));
            recap.add("Temps moyen coup J1 (" + iaJoueur1 + "): " + moyenneMs(totalDecisionJ1Ns, totalDecisionsJ1));
            recap.add("Temps moyen coup J2 (" + iaJoueur2 + "): " + moyenneMs(totalDecisionJ2Ns, totalDecisionsJ2));
            recap.add("Temps moyen coup global: " + moyenneMs(totalDecisionJ1Ns + totalDecisionJ2Ns, totalDecisionsJ1 + totalDecisionsJ2));
            recap.add("");
            recap.add("=== Details par combat ===");

            for (CombatResult r : tousLesCombats) {
                recap.add("Combat " + r.index
                    + " | issue=" + r.issue
                    + " | matchup=" + r.matchup
                    + " | profondeurJ1=" + profondeurJ1 + " profondeurJ2=" + profondeurJ2
                    + " | tours=" + r.tours
                    + " | duree=" + dureeLisible(r.dureeCombatMs)
                    + " | avgCoupJ1=" + String.format("%.3f", r.tempsDecisionJ1Ns / 1_000_000.0 / Math.max(1, r.nbDecisionsJ1))
                    + " | avgCoupJ2=" + String.format("%.3f", r.tempsDecisionJ2Ns / 1_000_000.0 / Math.max(1, r.nbDecisionsJ2))
                );
            }

            List<String> nulsLines = new ArrayList<>();
            nulsLines.add("=== Details des matchs nuls ===");
            nulsLines.add("Date: " + horodatage);
            nulsLines.add("IA Joueur 1: " + iaJoueur1 + " | IA Joueur 2: " + iaJoueur2);
            nulsLines.add("Profondeur J1: " + profondeurJ1 + " | Profondeur J2: " + profondeurJ2);
            nulsLines.add("Nombre de matchs nuls: " + nuls);
            nulsLines.add("");
            if (detailsNuls.isEmpty()) {
                nulsLines.add("Aucun match nul sur ce tournoi.");
            } else {
                nulsLines.addAll(detailsNuls);
            }

            Path pathResultats = Paths.get(fichierResultats);
            Path pathNuls = Paths.get(fichierNuls);
            Files.write(pathResultats, recap, StandardCharsets.UTF_8);
            Files.write(pathNuls, nulsLines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture des résultats partiels: " + e.getMessage());
        }
    }

    // Méthode utilitaire pour formater une durée en millisecondes dans un format lisible (ms ou s)
    private static String dureeLisible(long ms) {
        if (ms < 1000) {
            return ms + " ms";
        }
        double s = ms / 1000.0;
        return String.format("%.3f s", s);
    }

    // Méthode utilitaire pour calculer la moyenne en ms à partir du total en ns et du nombre d'occurrences, avec formatage
    private static String moyenneMs(long nsTotal, int nb) {
        if (nb <= 0) {
            return "n/a";
        }
        double ms = (nsTotal / 1_000_000.0) / nb;
        return String.format("%.3f ms", ms);
    }

    // Méthode principale pour simuler un combat entre deux IA, retourner les résultats détaillés et le log complet du combat
    private static CombatResult simulerCombat(int indexCombat, String iaJoueur1, String iaJoueur2, int maxTours) {
        long debutCombatNs = System.nanoTime();
        Personnage p1 = personnageAleatoire();
        Personnage p2 = personnageAleatoire();
        p1.setPosition(new Position(0,0));
        p2.setPosition(new Position(9,9));
        Arene arene = new Arene(p1, p2);
        CombatResult result = new CombatResult();

        StringBuilder log = new StringBuilder();
        log.append("Matchup: ").append(p1.getNom()).append(" vs ").append(p2.getNom()).append('\n');
        log.append("IA J1: ").append(iaJoueur1).append(" | IA J2: ").append(iaJoueur2).append('\n');
        log.append("Grille initiale:\n").append(grilleToString(arene.getGrille()));
        log.append("Etat initial J1: ").append(statPerso(p1)).append(" pos=").append(positionToString(p1.getPosition())).append('\n');
        log.append("Etat initial J2: ").append(statPerso(p2)).append(" pos=").append(positionToString(p2.getPosition())).append("\n\n");

        Personnage joueurActif = p1;
        Personnage adversaire = p2;
        int tour = 0;

        while (p1.estEnVie() && p2.estEnVie() && tour < maxTours) {
            tour++;

            Etat etatIA = new Etat(
                Etat.copieGrille(arene.getGrille()),
                Etat.JoueurEtat.fromPersonnage(joueurActif),
                Etat.JoueurEtat.fromPersonnage(adversaire)
            );

            boolean actifEstJ1 = joueurActif == p1;
            String iaActive = actifEstJ1 ? iaJoueur1 : iaJoueur2;
            // Appliquer la profondeur correspondante au joueur actif juste avant la décision
            if (actifEstJ1) {
                IAFacile.setProfondeur(PROFONDEUR_J1);
                IAMoyenne.setProfondeur(PROFONDEUR_J1);
                IADifficile.setProfondeur(PROFONDEUR_J1);
            } else {
                IAFacile.setProfondeur(PROFONDEUR_J2);
                IAMoyenne.setProfondeur(PROFONDEUR_J2);
                IADifficile.setProfondeur(PROFONDEUR_J2);
            }
            long debutDecisionNs = System.nanoTime();
            Coup coup = choisirCoupParIA(iaActive, etatIA);
            long dureeDecisionNs = System.nanoTime() - debutDecisionNs;

            if (actifEstJ1) {
                result.tempsDecisionJ1Ns += dureeDecisionNs;
                result.nbDecisionsJ1++;
            } else {
                result.tempsDecisionJ2Ns += dureeDecisionNs;
                result.nbDecisionsJ2++;
            }

            log.append("--- Tour ").append(tour).append(" | Actif=").append(joueurActif.getNom())
                .append(" | IA=").append(iaActive).append(" ---\n");
            log.append("Temps decision: ").append(String.format("%.3f ms", dureeDecisionNs / 1_000_000.0)).append('\n');

            Position posAvant = joueurActif.getPosition();
            double hpAdvAvant = adversaire.getHp();
            log.append("Avant: actif ").append(statPerso(joueurActif)).append(" pos=")
                .append(positionToString(joueurActif.getPosition())).append(" | adv ")
                .append(statPerso(adversaire)).append(" pos=").append(positionToString(adversaire.getPosition())).append('\n');

            if (coup != null) {
                Position dest = coup.getDestination();
                int distance = 0;
                if (dest != null && posAvant != null) {
                    distance = Math.abs(dest.getLigne() - posAvant.getLigne())
                             + Math.abs(dest.getColonne() - posAvant.getColonne());
                }
                log.append("Coup: ").append(coup.getAction());
                if (coup.getTypeAttaque() != null) {
                    log.append(" ").append(coup.getTypeAttaque());
                }
                log.append(" | dest=").append(positionToString(dest)).append(" | distance=").append(distance).append('\n');

                if (dest != null) {
                    int caseCible = arene.getGrille()[dest.getLigne()][dest.getColonne()];
                    log.append("Case cible avant deplacement: ").append(caseCible).append('\n');
                    joueurActif.setPosition(dest);
                    arene.updateFullGrille();

                    if (caseCible == 3) {
                        joueurActif.setParade(1);
                        arene.getGrille()[dest.getLigne()][dest.getColonne()] = 0;
                        log.append("Bonus ramasse: PARADE\n");
                    } else if (caseCible == 4) {
                        joueurActif.setEnergie(20.0);
                        arene.getGrille()[dest.getLigne()][dest.getColonne()] = 0;
                        log.append("Bonus ramasse: ENERGIE\n");
                    }
                }

                switch (coup.getAction()) {
                    case ATTAQUE:
                        try {
                            joueurActif.attaquer(adversaire, coup.getTypeAttaque());
                            log.append("Resultat attaque: hp adversaire ")
                                .append(hpAdvAvant).append(" -> ").append(adversaire.getHp()).append('\n');
                        } catch (IllegalAttackException | IllegalEnergieException | IllegalParadeException e) {
                            // On ignore l'exception de combat pour ne pas interrompre le tournoi.
                            log.append("Erreur attaque: ").append(e.getMessage()).append('\n');
                        }
                        break;
                    case PARADE:
                        try {
                            joueurActif.parader();
                            log.append("Parade activee\n");
                        } catch (IllegalParadeException e) {
                            // Action invalide selon l'état courant: on passe.
                            log.append("Erreur parade: ").append(e.getMessage()).append('\n');
                        }
                        break;
                    case REPOS:
                        try {
                            joueurActif.seReposer();
                            log.append("Repos effectue\n");
                        } catch (IllegalReposException e) {
                            // Action invalide selon l'état courant: on passe.
                            log.append("Erreur repos: ").append(e.getMessage()).append('\n');
                        }
                        break;
                    case TERMINER:
                        log.append("Action terminer\n");
                        break;
                }
            } else {
                log.append("Coup: null\n");
            }

            log.append("Apres: actif ").append(statPerso(joueurActif)).append(" pos=")
                .append(positionToString(joueurActif.getPosition())).append(" | adv ")
                .append(statPerso(adversaire)).append(" pos=").append(positionToString(adversaire.getPosition())).append('\n');

            Personnage temp = joueurActif;
            joueurActif = adversaire;
            adversaire = temp;
        }

        result.index = indexCombat;
        result.tours = tour;
        result.p1 = p1;
        result.p2 = p2;
        result.matchup = p1.getNom() + " vs " + p2.getNom();

        if (!p1.estEnVie()) {
            result.issue = "J2";
        } else if (!p2.estEnVie()) {
            result.issue = "J1";
        } else {
            // Mort subite: départage selon PV relatifs, énergie puis parades
            result.mortSubite = true;
            int vainqueur = arene.determineMortSubiteWinner();
            if (vainqueur == 1) {
                result.issue = "J1";
                log.append("Mort Subite: J1 l'emporte (critere PV/energie/parades)\n");
            } else if (vainqueur == 2) {
                result.issue = "J2";
                log.append("Mort Subite: J2 l'emporte (critere PV/energie/parades)\n");
            } else {
                result.issue = "NUL";
                log.append("Mort Subite: égalité parfaite après critères\n");
            }
            log.append("Detail ratios: J1 ratio=" + (p1.getHp()/Math.max(1.0,p1.getMaxHp())) + " J2 ratio=" + (p2.getHp()/Math.max(1.0,p2.getMaxHp())) + "\n");
            log.append("Detail energie: J1=" + p1.getEnergie() + " J2=" + p2.getEnergie() + "\n");
            log.append("Detail parades: J1=" + p1.getNbParades() + " J2=" + p2.getNbParades() + "\n");
        }

        log.append("=== FIN COMBAT ===\n");
        log.append("Tours joues: ").append(tour).append('\n');
        log.append("Issue: ").append(result.issue).append('\n');
        log.append("Duree combat: ").append(dureeLisible((System.nanoTime() - debutCombatNs) / 1_000_000L)).append('\n');
        log.append("Temps moyen coup J1: ").append(moyenneMs(result.tempsDecisionJ1Ns, result.nbDecisionsJ1)).append('\n');
        log.append("Temps moyen coup J2: ").append(moyenneMs(result.tempsDecisionJ2Ns, result.nbDecisionsJ2)).append('\n');
        log.append("Final J1: ").append(statPerso(p1)).append(" pos=").append(positionToString(p1.getPosition())).append('\n');
        log.append("Final J2: ").append(statPerso(p2)).append(" pos=").append(positionToString(p2.getPosition())).append('\n');
        result.logComplet = log.toString();
        result.dureeCombatMs = (System.nanoTime() - debutCombatNs) / 1_000_000L;

        return result;
    }

    // Méthode utilitaire pour afficher la grille dans la console (pour debug)
    private static String valeurOuDefaut(String[] args, int index, String valeurDefaut) {
        if (args.length > index && args[index] != null && !args[index].trim().isEmpty()) {
            return args[index].trim();
        }
        return valeurDefaut;
    }

    // Méthode utilitaire pour parser un entier à partir des arguments, avec une valeur par défaut en cas d'absence ou de format invalide
    private static int intOuDefaut(String[] args, int index, int valeurDefaut) {
        if (args.length > index) {
            try {
                return Integer.parseInt(args[index].trim());
            } catch (NumberFormatException e) {
                return valeurDefaut;
            }
        }
        return valeurDefaut;
    }

    // Méthode utilitaire pour afficher la grille dans la console (pour debug)
    private static String statPerso(Personnage p) {
        return p.getNom() + " PV=" + p.getHp() + " EN=" + p.getEnergie() + " PAR=" + p.getParade();
    }

    // Méthode utilitaire pour afficher la grille dans la console (pour debug)
    public static void main(String[] args) {

        String iaJoueur1 = valeurOuDefaut(args, 0, IA1_DEFAULT);
        String iaJoueur2 = valeurOuDefaut(args, 1, IA2_DEFAULT);
        int nbCombats = Math.max(1, intOuDefaut(args, 2, NB_COMBATS_DEFAULT));
        int maxTours = Math.max(1, intOuDefaut(args, 3, MAX_TOURS_DEFAULT));
        String fichierResultats = valeurOuDefaut(args, 4, FICHIER_RESULTATS_DEFAULT);
        String fichierNuls = valeurOuDefaut(args, 5, FICHIER_NULS_DEFAULT);
        int profondeurJ1 = Math.max(1, intOuDefaut(args, 6, PROFONDEUR_DEFAULT));
        int profondeurJ2 = Math.max(1, intOuDefaut(args, 7, profondeurJ1));
        // Clamp to maximum allowed depth (5)
        profondeurJ1 = Math.min(profondeurJ1, 5);
        profondeurJ2 = Math.min(profondeurJ2, 5);

        int victoiresJ1 = 0;
        int victoiresJ2 = 0;
        int nuls = 0;
        List<String> detailsNuls = new ArrayList<>();
        List<CombatResult> tousLesCombats = new ArrayList<>();
        long debutTournoiNs = System.nanoTime();
        long totalDecisionJ1Ns = 0L;
        long totalDecisionJ2Ns = 0L;
        int totalDecisionsJ1 = 0;
        int totalDecisionsJ2 = 0;

        // Initialiser les profondeurs actives globales pour ce tournoi
        PROFONDEUR_J1 = profondeurJ1;
        PROFONDEUR_J2 = profondeurJ2;

        // Initialiser les fichiers de résultats immédiatement
        ecrireResultatsPartiels(new ArrayList<>(), 0, 0, 0,
            iaJoueur1, iaJoueur2, nbCombats, maxTours, PROFONDEUR_J1, PROFONDEUR_J2, 0L,
            0L, 0, 0L, 0,
            new ArrayList<>(), fichierResultats, fichierNuls);

        for (int i = 1; i <= nbCombats; i++) {
            CombatResult r = simulerCombat(i, iaJoueur1, iaJoueur2, maxTours);
            tousLesCombats.add(r);
            totalDecisionJ1Ns += r.tempsDecisionJ1Ns;
            totalDecisionJ2Ns += r.tempsDecisionJ2Ns;
            totalDecisionsJ1 += r.nbDecisionsJ1;
            totalDecisionsJ2 += r.nbDecisionsJ2;
                if ("J1".equals(r.issue)) {
                    victoiresJ1++;
                } else if ("J2".equals(r.issue)) {
                    victoiresJ2++;
                }

                // On ajoute aux détails des nuls les combats qui ont fini par mort subite
                if (r.mortSubite || "NUL".equals(r.issue)) {
                    if ("NUL".equals(r.issue)) {
                        nuls++;
                    }
                    detailsNuls.add(
                        "Combat " + i
                            + " | profondeurJ1=" + profondeurJ1 + " profondeurJ2=" + profondeurJ2
                        + " | tours=" + r.tours
                        + " | J1(" + iaJoueur1 + "): " + statPerso(r.p1)
                        + " | J2(" + iaJoueur2 + "): " + statPerso(r.p2)
                    );
                    detailsNuls.add(r.logComplet);
                    detailsNuls.add("------------------------------------------------------------");
                }
            
            // Écrire les résultats partiels après chaque combat
            long dureeTournoiPartielMs = (System.nanoTime() - debutTournoiNs) / 1_000_000L;
            ecrireResultatsPartiels(tousLesCombats, victoiresJ1, victoiresJ2, nuls, 
                iaJoueur1, iaJoueur2, nbCombats, maxTours, PROFONDEUR_J1, PROFONDEUR_J2, dureeTournoiPartielMs,
                totalDecisionJ1Ns, totalDecisionsJ1, totalDecisionJ2Ns, totalDecisionsJ2,
                detailsNuls, fichierResultats, fichierNuls);
            // Callback optionnel pour indiquer la progression
            if (progressCallback != null) {
                try { progressCallback.accept(i, nbCombats); } catch (RuntimeException ex) { /* ignore */ }
            }
        }
        // Résultats partiels déjà écrits dans la boucle, pas besoin d'écrire à nouveau
        System.out.println("Tournoi termine.");
    }
}

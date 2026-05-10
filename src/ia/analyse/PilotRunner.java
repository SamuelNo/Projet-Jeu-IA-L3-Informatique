package ia.analyse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import ia.IAStats;
import ia.IAFacile;
import ia.IAMoyenne;
import ia.IADifficile;
import test.SimulateIAvIA;

/**
 * PilotRunner - Classe de lancement pour exécuter une série de matchs entre IA, collecter les statistiques et générer des rapports. Utilisée pour valider les performances des IA et pour alimenter les analyses dans GenerateurRapport.
 */
public class PilotRunner {
    public static void main(String[] args) {
        try {
            Path data = Paths.get("data");
            Files.createDirectories(data);
            Path progress = data.resolve("txt/progress_tournoi.txt");
            Files.writeString(progress, "", StandardCharsets.UTF_8);

            // Set depths to 5
            IAFacile.setProfondeur(5);
            IAMoyenne.setProfondeur(5);
            IADifficile.setProfondeur(5);

            IAStats.reset();

            // progress callback
            SimulateIAvIA.progressCallback = (i, nb) -> {
                try {
                    Files.writeString(progress, String.format("%d/%d\n", i, nb), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    System.err.println("Erreur ecriture progress: " + e.getMessage());
                }
            };

            System.out.println("Pilot: lancer 5 parties profondeur 5 (IADIFFICILE vs IADIFFICILE)");
            // use base filenames, not pilot-specific files
            SimulateIAvIA.main(new String[]{"IADIFFICILE", "IADIFFICILE", "5", "30", "data/txt/resultats_tournoi.txt", "data/txt/details_matchs_nuls.txt", "5", "5"});

            System.out.println("Pilot terminé. IAStats:");
            System.out.println("Noeuds visites total: " + IAStats.getNodes());
            System.out.println("Temps total moves ms: " + (IAStats.getTotalMoveTimeNs()/1_000_000.0));
            System.out.println("Nombre de moves compte: " + IAStats.getMoveCount());

            SimulateIAvIA.progressCallback = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

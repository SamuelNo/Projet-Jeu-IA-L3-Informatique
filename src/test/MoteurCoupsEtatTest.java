package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import entite.Position;
import ia.Coup;
import ia.Etat;
import ia.MoteurCoups;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MoteurCoupsEtatTest {

    @Test
    @DisplayName("Les coups légaux respectent la grille, les obstacles et le pas")
    void genererCoupsLegauxRespecteLesContraintesDeDeplacement() {
        int[][] grille = {
            {0, 0, 0, 0, 0},
            {0, 0, -1, 0, 0},
            {0, 0, 1, 0, 0},
            {0, 0, 0, 0, 0},
            {0, 0, 0, 0, 2}
        };

        Etat.JoueurEtat joueurActif = joueurEtat(new Position(2, 2), 120, 80.0, 150.0, 0, 0, 1, 1, List.of());
        Etat.JoueurEtat adversaire = joueurEtat(new Position(4, 4), 120, 80.0, 150.0, 0, 0, 1, 2, List.of());
        Etat etat = new Etat(grille, joueurActif, adversaire);

        List<Coup> coups = MoteurCoups.genererCoupsLegaux(etat);

        assertFalse(coups.isEmpty(), "Le moteur doit proposer au moins un coup légal");
        for (Coup coup : coups) {
            Position destination = coup.getDestination();
            assertTrue(destination.getLigne() >= 0 && destination.getLigne() < grille.length,
                    "Aucune destination ne doit sortir verticalement de la grille");
            assertTrue(destination.getColonne() >= 0 && destination.getColonne() < grille[0].length,
                    "Aucune destination ne doit sortir horizontalement de la grille");

            int distance = Math.abs(destination.getLigne() - joueurActif.getPosition().getLigne())
                    + Math.abs(destination.getColonne() - joueurActif.getPosition().getColonne());
            assertTrue(distance <= joueurActif.getPas(), "Un coup légal ne doit pas dépasser le pas du joueur");
            assertFalse(destination.getLigne() == 1 && destination.getColonne() == 2,
                    "Le moteur ne doit jamais autoriser un déplacement sur un obstacle (-1)");
        }
    }

    @Test
    @DisplayName("Un bonus de parade incrémente correctement le compteur")
    void simulerCoupRamasseBonusParade() {
        int[][] grille = {
            {0, 0, 0},
            {0, 1, 3},
            {0, 0, 2}
        };

        Etat.JoueurEtat joueurActif = joueurEtat(new Position(1, 1), 120, 80.0, 150.0, 0, 0, 2, 1, List.of());
        Etat.JoueurEtat adversaire = joueurEtat(new Position(2, 2), 120, 80.0, 150.0, 0, 0, 2, 2, List.of());
        Etat etat = new Etat(grille, joueurActif, adversaire);

        Etat suivant = MoteurCoups.simulerCoup(etat, new Coup(new Position(1, 2), Coup.TypeAction.TERMINER, null));

        assertEquals(1, suivant.getAdversaire().getNbParades(), "Le bonus de parade doit augmenter le compteur");
        assertEquals(80.0, suivant.getAdversaire().getEnergie(), 0.0,
                "Le bonus de parade ne doit pas modifier l'énergie");
        assertEquals(1, suivant.getAdversaire().getPosition().getLigne());
        assertEquals(2, suivant.getAdversaire().getPosition().getColonne());
    }

    @Test
    @DisplayName("Un bonus d'énergie augmente l'énergie sans dépasser le plafond")
    void simulerCoupRamasseBonusEnergie() {
        int[][] grille = {
            {0, 0, 0},
            {0, 1, 4},
            {0, 0, 2}
        };

        Etat.JoueurEtat joueurActif = joueurEtat(new Position(1, 1), 120, 140.0, 150.0, 0, 0, 2, 1, List.of());
        Etat.JoueurEtat adversaire = joueurEtat(new Position(2, 2), 120, 80.0, 150.0, 0, 0, 2, 2, List.of());
        Etat etat = new Etat(grille, joueurActif, adversaire);

        Etat suivant = MoteurCoups.simulerCoup(etat, new Coup(new Position(1, 2), Coup.TypeAction.TERMINER, null));

        assertEquals(150.0, suivant.getAdversaire().getEnergie(), 0.0,
                "Le bonus d'énergie doit augmenter l'énergie et respecter le plafond");
    }

    @Test
    @DisplayName("Une attaque inflige des dégâts et consomme l'énergie")
    void simulerCoupAttaqueReduitLesPvEtConsommeLEnergie() {
        int[][] grille = {
            {0, 0, 0},
            {0, 1, 2},
            {0, 0, 0}
        };

        Etat.AttaqueInfo attaqueLegere = new Etat.AttaqueInfo("AL", 15.0, 1);
        Etat.JoueurEtat joueurActif = joueurEtat(new Position(1, 1), 120, 80.0, 150.0, 0, 0, 2, 1, List.of(attaqueLegere));
        Etat.JoueurEtat adversaire = joueurEtat(new Position(1, 2), 120, 80.0, 150.0, 0, 0, 2, 2, List.of());
        Etat etat = new Etat(grille, joueurActif, adversaire);

        Etat suivant = MoteurCoups.simulerCoup(etat, new Coup(new Position(1, 1), Coup.TypeAction.ATTAQUE, "AL"));

        assertEquals(105, suivant.getJoueurActif().getHp(), "L'attaque doit réduire les PV de la cible");
        assertEquals(65.0, suivant.getAdversaire().getEnergie(), 0.0,
                "L'attaque doit consommer exactement l'énergie indiquée par le moteur");
    }

    @Test
    @DisplayName("Le changement de joueur actif réinitialise la parade au bon moment")
    void changerJoueurActifReinitialiseLaParade() {
        int[][] grille = {
            {1, 0},
            {0, 2}
        };

        Etat.JoueurEtat joueur1 = joueurEtat(new Position(0, 0), 120, 80.0, 150.0, 0, 0, 2, 1, List.of());
        Etat.JoueurEtat joueur2 = joueurEtat(new Position(1, 1), 120, 80.0, 150.0, 0, 0, 2, 2, List.of());
        joueur1.setEnParade(true);
        joueur2.setEnParade(true);

        Etat etat = new Etat(grille, joueur1, joueur2);

        etat.changerJoueurActif();
        assertSame(joueur2, etat.getJoueurActif(), "Le joueur actif doit changer après le switch");
        assertFalse(etat.getJoueurActif().isEnParade(), "Le joueur qui commence son tour ne doit pas garder la parade");
        assertTrue(etat.getAdversaire().isEnParade(), "La parade du joueur précédent doit rester active jusqu'au prochain switch");

        etat.changerJoueurActif();
        assertSame(joueur1, etat.getJoueurActif(), "Un second switch doit rendre la main au joueur initial");
        assertFalse(etat.getJoueurActif().isEnParade(), "La parade doit être réinitialisée quand le joueur redevient actif");
    }

    private static Etat.JoueurEtat joueurEtat(Position position, int hp, double energie, double maxEnergie,
            int nbParades, int nbRepos, int pas, int id, List<Etat.AttaqueInfo> attaques) {
        return new Etat.JoueurEtat(position, hp, energie, maxEnergie, nbParades, nbRepos, pas, id, attaques);
    }
}
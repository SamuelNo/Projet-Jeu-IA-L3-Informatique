package ia;

import java.util.List;

/**
 * IA Facile - Le "Berserker".
 * Profondeur 1 : Elle fonce et frappe sans se soucier de la riposte.
 */
public class IAFacile {
    
    private static final int PROFONDEUR = 1; 

    public static Coup choisirCoup(Etat etat) {
        return choisirCoup(etat, false);
    }

    public static Coup choisirCoup(Etat etat, boolean attaqueDejaEffectuee) {
        List<Coup> coupsLegaux = MoteurCoups.genererCoupsLegaux(etat, attaqueDejaEffectuee);
        
        if (coupsLegaux.isEmpty()) {
            return new Coup(null, Coup.TypeAction.TERMINER, null);
        }

        Coup meilleurCoup = coupsLegaux.get(0); 
        int meilleurScore = Integer.MIN_VALUE;

        for (Coup c : coupsLegaux) {
            Etat simulation = MoteurCoups.simulerCoup(etat, c);
            
            int score = minimax(simulation, PROFONDEUR - 1, false);
            
            if (score > meilleurScore) {
                meilleurScore = score;
                meilleurCoup = c;
            }
        }
        return meilleurCoup;
    }

    private static int minimax(Etat etat, int profondeur, boolean estMax) {
        if (profondeur == 0 || etat.estTerminal()) {
            if (estMax) {
                // C'est le tour de l'IA, on évalue normalement.
                return etat.getScoreHeuristique(); 
            } else {
                // CORRECTION MAGIQUE : C'est le tour de l'adversaire dans la simulation.
                // On met temporairement l'IA en "joueur actif" pour qu'elle lise la grille avec ses propres yeux.
                etat.changerJoueurActif();
                int score = etat.getScoreHeuristique();
                etat.changerJoueurActif(); // On remet l'adversaire pour ne rien casser.
                return score;
            }
        }

        if (estMax) {
            int maxEval = Integer.MIN_VALUE;
            List<Coup> coups = MoteurCoups.genererCoupsLegaux(etat, false);
            
            if (coups.isEmpty()) return etat.getScoreHeuristique();
            
            for (Coup c : coups) {
                int eval = minimax(MoteurCoups.simulerCoup(etat, c), profondeur - 1, false);
                maxEval = Math.max(maxEval, eval);
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            List<Coup> coups = MoteurCoups.genererCoupsLegaux(etat, false);
            
            if (coups.isEmpty()) {
                etat.changerJoueurActif();
                int s = etat.getScoreHeuristique();
                etat.changerJoueurActif();
                return s;
            }
            
            for (Coup c : coups) {
                int eval = minimax(MoteurCoups.simulerCoup(etat, c), profondeur - 1, true);
                minEval = Math.min(minEval, eval);
            }
            return minEval;
        }
    }
}
package ia;

import java.util.List;

/**
 * IA Moyenne - Le "Tacticien".
 * Utilise Minimax avec Élagage Alpha-Bêta (Profondeur 4).
 */
public class IAMoyenne {
    
    private static final int PROFONDEUR = 4;

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
        
        // Initialisation de l'Alpha et du Bêta
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        for (Coup c : coupsLegaux) {
            Etat simulation = MoteurCoups.simulerCoup(etat, c);
            int score = minimaxAlphaBeta(simulation, PROFONDEUR - 1, false, alpha, beta);
            
            if (score > meilleurScore) {
                meilleurScore = score;
                meilleurCoup = c;
            }
            alpha = Math.max(alpha, meilleurScore);
        }
        return meilleurCoup;
    }

    private static int minimaxAlphaBeta(Etat etat, int profondeur, boolean estMax, int alpha, int beta) {
        if (profondeur == 0 || etat.estTerminal()) {
            if (estMax) {
                return etat.getScoreHeuristiqueMoyenne();
            } else {
                // CORRECTION MAGIQUE : On inverse pour lire la grille dans le bon sens
                etat.changerJoueurActif();
                int score = etat.getScoreHeuristiqueMoyenne();
                etat.changerJoueurActif();
                return score;
            }
        }

        List<Coup> coups = MoteurCoups.genererCoupsLegaux(etat, false);

        if (estMax) {
            if (coups.isEmpty()) return etat.getScoreHeuristiqueMoyenne();
            
            int maxEval = Integer.MIN_VALUE;
            for (Coup c : coups) {
                int eval = minimaxAlphaBeta(MoteurCoups.simulerCoup(etat, c), profondeur - 1, false, alpha, beta);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break; // Coupure Bêta
            }
            return maxEval;
            
        } else {
            if (coups.isEmpty()) {
                etat.changerJoueurActif();
                int s = etat.getScoreHeuristiqueMoyenne();
                etat.changerJoueurActif();
                return s;
            }
            
            int minEval = Integer.MAX_VALUE;
            for (Coup c : coups) {
                int eval = minimaxAlphaBeta(MoteurCoups.simulerCoup(etat, c), profondeur - 1, true, alpha, beta);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break; // Coupure Alpha
            }
            return minEval;
        }
    }
}
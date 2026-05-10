package ia;

import java.util.Comparator;
import java.util.List;

/**
 * IA Difficile
 * Utilise Minimax avec Élagage Alpha-Bêta à profondeur 4.
 */
public class IADifficile {

    private static int PROFONDEUR_MAX = 3;
    public static void setProfondeur(int p) { PROFONDEUR_MAX = Math.max(1, Math.min(p, 5)); }

    public static Coup choisirCoup(Etat etat) {
        return choisirCoup(etat, false);
    }

    public static Coup choisirCoup(Etat etat, boolean attaqueDejaEffectuee) {
        /**
         * Choisit un coup en utilisant tri des coups + minimax alpha-beta (IA difficile).
         * Optimisé pour l'élagage et la priorité des attaques.
         * @param etat état courant
         * @param attaqueDejaEffectuee indique si l'attaque du tour a déjà été utilisée
         * @return coup choisi
         */
        long t0 = System.nanoTime();
        List<Coup> coupsLegaux = MoteurCoups.genererCoupsLegaux(etat, attaqueDejaEffectuee);

        if (coupsLegaux.isEmpty()) {
            return new Coup(null, Coup.TypeAction.TERMINER, null);
        }

        // TRI DES COUPS : On explore en priorité les attaques.
        // Cela améliore drastiquement l'efficacité de l'élagage Alpha-Bêta.
        coupsLegaux.sort(trierCoupParPriorite());

        Coup meilleurCoup = coupsLegaux.get(0);
        int meilleurScore = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        // On lance l'algorithme avec notre profondeur fixe
        for (Coup c : coupsLegaux) {
            Etat simulation = MoteurCoups.simulerCoup(etat, c);
            // On fait profondeur - 1 car ce premier coup compte comme un niveau
            int score = minimaxAlphaBeta(simulation, PROFONDEUR_MAX - 1, false, alpha, beta);

            if (score > meilleurScore) {
                meilleurScore = score;
                meilleurCoup = c;
            }
            alpha = Math.max(alpha, meilleurScore);
        }

        long t1 = System.nanoTime();
        IAStats.addMoveTimeNs(t1 - t0);
        return meilleurCoup;
    }

    /**
     * Algorithme Minimax avec élagage Alpha-Bêta classique.
     */
    private static int minimaxAlphaBeta(Etat etat, int profondeur, boolean estMax, int alpha, int beta) {
        
        // Condition d'arrêt : on a atteint la profondeur voulue ou la fin du jeu
        if (profondeur == 0 || etat.estTerminal()) {
                IAStats.incNode();
            if (estMax) {
                return etat.getScoreHeuristiqueDifficile();
            } else {
                // On inverse temporairement pour lire la grille du point de vue de l'IA.
                etat.changerJoueurActif();
                int score = etat.getScoreHeuristiqueDifficile();
                etat.changerJoueurActif();
                return score;
            }
        }

        List<Coup> coups = MoteurCoups.genererCoupsLegaux(etat, false);
        coups.sort(trierCoupParPriorite()); // Tri pour couper plus vite

        if (estMax) {
            if (coups.isEmpty()) return etat.getScoreHeuristiqueDifficile();

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
                int s = etat.getScoreHeuristiqueDifficile();
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

    /**
     * Comparateur pour ordonner les coups.
     */
    private static Comparator<Coup> trierCoupParPriorite() {
        return Comparator.comparingInt(c -> {
            switch (c.getAction()) {
                case ATTAQUE:  return 0;
                case PARADE:   return 1;
                case REPOS:    return 2;
                case TERMINER: return 3;
                default:       return 4;
            }
        });
    }
}
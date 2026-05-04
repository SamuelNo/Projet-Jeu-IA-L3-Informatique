package ia;

import java.util.List;

/**
 * IA Facile - Utilise l'algorithme Minimax basique.
 * Se base sur l'heuristique getScoreHeuristique() de la classe Etat.
 */
public class IAFacile {
    
    // Profondeur 2 : l'IA regarde son coup puis la réponse de l'adversaire.
    private static final int PROFONDEUR = 2; 

    public static Coup choisirCoup(Etat etatActuel) {
        List<Coup> coupsLegaux = MoteurCoups.genererCoupsLegaux(etatActuel);
        
        // Sécurité : si aucun coup n'est possible, on termine le tour.
        if (coupsLegaux.isEmpty()) {
            return new Coup(null, Coup.TypeAction.TERMINER, null);
        }

        // On initialise avec le premier coup par défaut (évite les retours null)
        Coup meilleurCoup = coupsLegaux.get(0); 
        int meilleurScore = Integer.MIN_VALUE;

        for (Coup c : coupsLegaux) {
            Etat simulation = MoteurCoups.simulerCoup(etatActuel, c);
            
            // On lance le minimax. 
            // estMax = false car après ce coup, c'est au tour de l'adversaire (MIN).
            int score = minimax(simulation, PROFONDEUR - 1, false);
            
            if (score >= meilleurScore) {
                meilleurScore = score;
                meilleurCoup = c;
            }
        }
        return meilleurCoup;
    }

    /**
     * Algorithme récursif Minimax.
     */
    private static int minimax(Etat etat, int profondeur, boolean estMax) {
        // Condition d'arrêt : fin de partie ou limite de profondeur atteinte.
        if (profondeur == 0 || etat.estTerminal()) {
            // Gestion de la perspective (L'astuce Negamax)
            if (estMax) {
                // C'est au tour de l'IA de jouer, l'heuristique est de son point de vue.
                return etat.getScoreHeuristique(); 
            } else {
                // C'est au tour de l'Humain, on inverse le score pour que l'IA comprenne que c'est une victoire pour elle.
                return -etat.getScoreHeuristique();
            }
        }

        if (estMax) {
            int maxEval = Integer.MIN_VALUE;
            List<Coup> coups = MoteurCoups.genererCoupsLegaux(etat);
            
            if (coups.isEmpty()) return etat.getScoreHeuristique(); // Gère les impasses
            
            for (Coup c : coups) {
                int eval = minimax(MoteurCoups.simulerCoup(etat, c), profondeur - 1, false);
                maxEval = Math.max(maxEval, eval);
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            List<Coup> coups = MoteurCoups.genererCoupsLegaux(etat);
            
            if (coups.isEmpty()) return -etat.getScoreHeuristique(); // Gère les impasses avec inversion
            
            for (Coup c : coups) {
                int eval = minimax(MoteurCoups.simulerCoup(etat, c), profondeur - 1, true);
                minEval = Math.min(minEval, eval);
            }
            return minEval;
        }
    }
}

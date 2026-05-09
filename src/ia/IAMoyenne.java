package ia;

import java.util.List;

/**
 * IA Moyenne - Le "Tacticien".
 * Utilise l'algorithme NegaMax avec Élagage Alpha-Bêta (Profondeur 4).
 */
public class IAMoyenne {
    
    private static final int PROFONDEUR = 2;

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
        
        int alpha = Integer.MIN_VALUE + 1; // +1 pour éviter l'overflow lors de l'inversion de signe
        int beta = Integer.MAX_VALUE;

        for (Coup c : coupsLegaux) {
            Etat simulation = MoteurCoups.simulerCoup(etat, c);
            
            // Le secret du NegaMax : on inverse les bornes et on prend le score négatif de l'adversaire
            int score = -negaMaxAlphaBeta(simulation, PROFONDEUR - 1, -beta, -alpha);
            
            // On maintient la petite pénalité si elle termine bêtement sur sa propre case
            if (c.getAction() == Coup.TypeAction.TERMINER && 
                c.getDestination().getLigne() == etat.getJoueurActif().getPosition().getLigne() &&
                c.getDestination().getColonne() == etat.getJoueurActif().getPosition().getColonne()) {
                score -= 5;
            }
            
            if (score > meilleurScore) {
                meilleurScore = score;
                meilleurCoup = c;
            }
            alpha = Math.max(alpha, meilleurScore);
        }
        return meilleurCoup;
    }

    private static int negaMaxAlphaBeta(Etat etat, int profondeur, int alpha, int beta) {
        if (profondeur == 0 || etat.estTerminal()) {
            // Chaque noeud évalue de SON propre point de vue
            return etat.getScoreHeuristiqueMoyenne();
        }

        List<Coup> coups = MoteurCoups.genererCoupsLegaux(etat, false);

        if (coups.isEmpty()) {
            // Si aucune action n'est possible, on passe le tour virtuellement
            Etat sim = new Etat(etat);
            sim.changerJoueurActif();
            return -negaMaxAlphaBeta(sim, profondeur - 1, -beta, -alpha);
        }
        
        int maxEval = Integer.MIN_VALUE + 1;
        
        for (Coup c : coups) {
            Etat simulation = MoteurCoups.simulerCoup(etat, c);
            int eval = -negaMaxAlphaBeta(simulation, profondeur - 1, -beta, -alpha);
            
            maxEval = Math.max(maxEval, eval);
            alpha = Math.max(alpha, eval);
            
            // Coupure Alpha-Bêta standard
            if (alpha >= beta) {
                break; 
            }
        }
        return maxEval;
    }
}
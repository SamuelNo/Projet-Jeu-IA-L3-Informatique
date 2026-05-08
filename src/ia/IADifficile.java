package ia;

import java.util.Comparator;
import java.util.List;

/**
 * IA Difficile - Le "Stratège".
 * Utilise Minimax avec Élagage Alpha-Bêta et Approfondissement Itératif.
 *
 * ses avantages sur l'IAMoyenne :
 *  - approfondissement itératif : explore profondeur 1, 2, 3... jusqu'à 6
 *    épuisement du temps imparti. Retourne toujours le meilleur coup connu.
 *  - limite de temps (1,5 sec) : garantit une réponse fluide quelle que
 *    soit la complexité de la position.
 *  - tri des coups avant exploration : les attaques et actions prometteuses
 *    sont explorées en premier, ce qui maximise les coupures alpha-bêta.
 *  - heuristique avancée : détecte les coups létaux, pénalise l'adversaire
 *    quand il est en danger, et gère finement le ratio HP/énergie.
 */
public class IADifficile {

    /** Temps maximum alloué par tour en millisecondes. */
    private static final long TEMPS_MAX_MS = 1500;

    /** Profondeur maximale théorique (rarement atteinte dans le temps imparti). */
    private static final int PROFONDEUR_MAX = 8;

    /** Horodatage du début du calcul, partagé entre tous les appels récursifs. */
    private static long tempsDebut;

    /** Indique si la limite de temps a été dépassée pendant la recherche. */
    private static boolean tempsEcoule;

    public static Coup choisirCoup(Etat etat) {
        return choisirCoup(etat, false);
    }

    public static Coup choisirCoup(Etat etat, boolean attaqueDejaEffectuee) {
        List<Coup> coupsLegaux = MoteurCoups.genererCoupsLegaux(etat, attaqueDejaEffectuee);

        if (coupsLegaux.isEmpty()) {
            return new Coup(null, Coup.TypeAction.TERMINER, null);
        }

        // TRI DES COUPS : On explore en priorité les attaques, puis les parades,
        // puis le reste. Cela améliore drastiquement l'efficacité de l'élagage.
        coupsLegaux.sort(trierCoupParPriorite());

        tempsDebut = System.currentTimeMillis();
        tempsEcoule = false;

        // Meilleur coup de secours : toujours le premier coup trié (une attaque si possible)
        Coup meilleurCoup = coupsLegaux.get(0);

        // APPROFONDISSEMENT ITÉRATIF : on cherche à profondeur 1, puis 2, puis 3...
        // Si le temps s'épuise en cours de route, on retourne le meilleur coup
        // trouvé lors de la dernière itération complète.
        for (int profondeur = 1; profondeur <= PROFONDEUR_MAX; profondeur++) {
            Coup meilleurCoupIteration = coupsLegaux.get(0);
            int meilleurScoreIteration = Integer.MIN_VALUE;
            int alpha = Integer.MIN_VALUE;
            int beta = Integer.MAX_VALUE;

            for (Coup c : coupsLegaux) {
                if (tempsEcoule()) break;

                Etat simulation = MoteurCoups.simulerCoup(etat, c);
                int score = minimaxAlphaBeta(simulation, profondeur - 1, false, alpha, beta);

                if (score > meilleurScoreIteration) {
                    meilleurScoreIteration = score;
                    meilleurCoupIteration = c;
                }
                alpha = Math.max(alpha, meilleurScoreIteration);
            }

            // On ne valide l'itération que si elle s'est terminée sans interruption
            if (!tempsEcoule) {
                meilleurCoup = meilleurCoupIteration;
            }

            // Inutile de continuer si le temps est dépassé
            if (tempsEcoule) break;
        }

        return meilleurCoup;
    }

    /**
     * Algorithme Minimax avec élagage Alpha-Bêta.
     * S'interrompt proprement si la limite de temps est dépassée.
     */
    private static int minimaxAlphaBeta(Etat etat, int profondeur, boolean estMax, int alpha, int beta) {
        // VÉRIFICATION DU TEMPS à chaque noeud pour stopper rapidement
        if (tempsEcoule()) return 0;

        if (profondeur == 0 || etat.estTerminal()) {
            if (estMax) {
                return etat.getScoreHeuristiqueDifficile();
            } else {
                // CORRECTION MAGIQUE : On inverse temporairement pour lire la grille
                // du point de vue de l'IA (joueur MAX).
                etat.changerJoueurActif();
                int score = etat.getScoreHeuristiqueDifficile();
                etat.changerJoueurActif();
                return score;
            }
        }

        List<Coup> coups = MoteurCoups.genererCoupsLegaux(etat, false);

        // TRI à chaque nœud pour maximiser les coupures en profondeur
        coups.sort(trierCoupParPriorite());

        if (estMax) {
            if (coups.isEmpty()) return etat.getScoreHeuristiqueDifficile();

            int maxEval = Integer.MIN_VALUE;
            for (Coup c : coups) {
                if (tempsEcoule()) break;
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
                if (tempsEcoule()) break;
                int eval = minimaxAlphaBeta(MoteurCoups.simulerCoup(etat, c), profondeur - 1, true, alpha, beta);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break; // Coupure Alpha
            }
            return minEval;
        }
    }

    /**
     * Vérifie si le temps imparti est écoulé et met à jour le flag en conséquence.
     * @return true si la limite de temps est dépassée
     */
    private static boolean tempsEcoule() {
        if (tempsEcoule) return true;
        if (System.currentTimeMillis() - tempsDebut >= TEMPS_MAX_MS) {
            tempsEcoule = true;
            return true;
        }
        return false;
    }

    /**
     * Comparateur de tri des coups par ordre de priorité stratégique.
     * Ordre : ATTAQUE > PARADE > REPOS > TERMINER.
     * Explorer les attaques en premier augmente significativement le nombre
     * de coupures alpha-bêta et réduit les nœuds explorés.
     */
    private static Comparator<Coup> trierCoupParPriorite() {
        return Comparator.comparingInt(c -> {
            switch (c.getAction()) {
                case ATTAQUE:  return 0; // Priorité maximale
                case PARADE:   return 1;
                case REPOS:    return 2;
                case TERMINER: return 3; // Priorité minimale
                default:       return 4;
            }
        });
    }
}
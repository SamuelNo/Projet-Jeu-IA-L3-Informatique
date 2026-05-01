package ia;

import java.util.List;
import java.util.Random;

/**
 * ia moyenne utilisant l'algorithme minimax basique
 */
public class IAMoyenne {
    
    private static final Random rand = new Random();
    
    /**
     * choisit le meilleur coup en utilisant minimax avec profondeur limitée
     */
    public static Coup choisirCoup(Etat etat) {
        // profondeur de recherche pour l'ia moyenne
        int profondeur = 3;
        
        List<Coup> coupsLegaux = MoteurCoups.genererCoupsLegaux(etat);
        if (coupsLegaux.isEmpty()) {
            return new Coup(null, Coup.TypeAction.TERMINER, null);
        }
        
        Coup meilleurCoup = null;
        double meilleurScore = Double.NEGATIVE_INFINITY;
        
        // on teste chaque coup possible
        for (Coup coup : coupsLegaux) {
            Etat etatSuivant = MoteurCoups.simulerCoup(etat, coup);
            double score = minimax(etatSuivant, profondeur - 1, false, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            
            if (score > meilleurScore) {
                meilleurScore = score;
                meilleurCoup = coup;
            }
        }
        
        return meilleurCoup != null ? meilleurCoup : coupsLegaux.get(0);
    }
    
    /**
     * algorithme minimax récursif
     * @param etat etat actuel du jeu
     * @param profondeur profondeur de recherche restante
     * @param estMax vrai si on cherche le score du joueur max, faux pour min
     * @param alpha valeur alpha pour l'élagage (non utilisée dans la version basique)
     * @param beta valeur beta pour l'élagage (Non utilisée dans la version basique)
     * @return score de l'état évalué
     */
    private static double minimax(Etat etat, int profondeur, boolean estMax, double alpha, double beta) {
        // si on atteint la profondeur maximale ou un état terminal
        if (profondeur == 0 || estEtatTerminal(etat)) {
            return evaluerEtat(etat, estMax);
        }
        
        List<Coup> coupsLegaux = MoteurCoups.genererCoupsLegaux(etat);
        if (coupsLegaux.isEmpty()) {
            return evaluerEtat(etat, estMax);
        }
        
        if (estMax) {
            // tour du joueur max : on cherche le score maximum
            double meilleurScore = Double.NEGATIVE_INFINITY;
            for (Coup coup : coupsLegaux) {
                Etat etatSuivant = MoteurCoups.simulerCoup(etat, coup);
                double score = minimax(etatSuivant, profondeur - 1, false, alpha, beta);
                if (score > meilleurScore) {
                    meilleurScore = score;
                }
            }
            return meilleurScore;
        } else {
            // tour du joueur min : on cherche le score minimum
            double meilleurScore = Double.POSITIVE_INFINITY;
            for (Coup coup : coupsLegaux) {
                Etat etatSuivant = MoteurCoups.simulerCoup(etat, coup);
                double score = minimax(etatSuivant, profondeur - 1, true, alpha, beta);
                if (score < meilleurScore) {
                    meilleurScore = score;
                }
            }
            return meilleurScore;
        }
    }
    
    /**
     * vérifie si l'état est terminal (fin de partie)
     */
    private static boolean estEtatTerminal(Etat etat) {
        return etat.getJoueurActif().getHp() <= 0 || etat.getAdversaire().getHp() <= 0;
    }
    
    /**
     * fonction d'évaluation améliorée pour l'ia moyenne
     * évalue l'état du point de vue du joueur actif
     */
    private static double evaluerEtat(Etat etat, boolean estJoueurActifMax) {
        Etat.JoueurEtat actif = etat.getJoueurActif();
        Etat.JoueurEtat adversaire = etat.getAdversaire();
        
        // si fin de partie, score très élevé ou très bas
        if (actif.getHp() <= 0) {
            return estJoueurActifMax ? -1000 : 1000;
        }
        if (adversaire.getHp() <= 0) {
            return estJoueurActifMax ? 1000 : -1000;
        }
        
        double score = 0;
        
        // différence de pv (plus important)
        score += (actif.getHp() - adversaire.getHp()) * 3;
        
        // énergie (très important pour les attaques)
        score += actif.getEnergie() * 1.0;
        score -= adversaire.getEnergie() * 0.7;
        
        // parades (important pour la défense)
        score += actif.getNbParades() * 15;
        score -= adversaire.getNbParades() * 12;
        
        // analyse des cases de boost autour du joueur actif
        score += evaluerCasesBoost(etat, actif, adversaire) * 5; // poids très élevé pour forcer la prise des bonus
        
        // distance entre les joueurs (plus proche est mieux pour attaquer)
        int distance = Math.abs(actif.getPosition().getLigne() - adversaire.getPosition().getLigne()) +
                     Math.abs(actif.getPosition().getColonne() - adversaire.getPosition().getColonne());
        
        // pénalité pour être trop loin (éviter la fuite)
        if (distance > 5) {
            score -= distance * 3; // pénalité forte si trop loin
        } else {
            score -= distance * 0.5; // pénalité faible si distance raisonnable
        }
        
        // bonus important si le joueur actif est en position d'attaquer
        if (distance <= 2) {
            score += 15; // bonus élevé pour être proche
        } else if (distance <= 4) {
            score += 8; // bonus moyen pour être à portée moyenne
        }
        
        // bonus si le joueur actif a plus de pv que l'adversaire (encourager le combat)
        if (actif.getHp() > adversaire.getHp() + 10) {
            score += 12; // bonus si avantage en pv
        }
        
        // pénalité si le joueur actif fuit alors qu'il a l'avantage
        if (actif.getHp() > adversaire.getHp() && distance > 4) {
            score -= 20; // pénalité forte pour fuite avec avantage
        }
        
        return score;
    }
    
    /**
     * évalue les cases de boost autour du joueur actif
     */
    private static double evaluerCasesBoost(Etat etat, Etat.JoueurEtat actif, Etat.JoueurEtat adversaire) {
        double scoreBoost = 0;
        int[][] grille = etat.getGrille();
        int[][] directions = {{-1,0}, {1,0}, {0,-1}, {0,1}, {-1,-1}, {-1,1}, {1,-1}, {1,1}}; // 8 directions
        
        // vérifier les cases autour du joueur actif
        for (int[] dir : directions) {
            int ligne = actif.getPosition().getLigne() + dir[0];
            int colonne = actif.getPosition().getColonne() + dir[1];
            
            if (ligne >= 0 && ligne < grille.length && colonne >= 0 && colonne < grille[0].length) {
                int caseType = grille[ligne][colonne];
                
                if (caseType == 3) { // case de parade
                    // bonus très élevé pour les cases de parade (priorité absolue)
                    scoreBoost += 50;
                    // bonus supplémentaire si le joueur a besoin de parades
                    if (actif.getNbParades() < 2) {
                        scoreBoost += 25;
                    }
                } else if (caseType == 4) { // case d'énergie
                    // bonus très élevé pour les cases d'énergie (priorité absolue)
                    scoreBoost += 55;
                    // bonus supplémentaire si le joueur a besoin d'énergie
                    if (actif.getEnergie() < 50) {
                        scoreBoost += 30;
                    }
                }
            }
        }
        
        // vérifier les cases de boost accessibles avec les pm restants
        int pmActif = actif.getPas(); // approximation des pm restants
        for (int i = 1; i <= pmActif && i <= 3; i++) { // jusqu'à 3 cases de distance
            for (int[] dir : directions) {
                int ligne = actif.getPosition().getLigne() + dir[0] * i;
                int colonne = actif.getPosition().getColonne() + dir[1] * i;
                
                if (ligne >= 0 && ligne < grille.length && colonne >= 0 && colonne < grille[0].length) {
                    int caseType = grille[ligne][colonne];
                    
                    if (caseType == 3) { // case de parade
                        scoreBoost += 40 / i; // bonus élevé même si loin
                    } else if (caseType == 4) { // case d'énergie
                        scoreBoost += 45 / i; // bonus élevé même si loin
                    }
                }
            }
        }
        
        return scoreBoost;
    }
}

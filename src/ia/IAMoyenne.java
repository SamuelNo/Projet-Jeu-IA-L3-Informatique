package ia;

import java.util.List;
import java.util.Random;
import entite.Position;

/**
 * ia moyenne utilisant l'algorithme minimax basique
 */
public class IAMoyenne {
    
    private static final Random rand = new Random();
    private static final int PROFONDEUR_MAX = 3;
    
    /**
     * choisit le meilleur coup en utilisant minimax avec profondeur limitée
     */
    public static Coup choisirCoup(Etat etat) {
        return choisirCoup(etat, false);
    }

    public static Coup choisirCoup(Etat etat, boolean attaqueDejaEffectuee) {
        // profondeur de recherche pour l'ia moyenne
        int profondeur = 3;
        
        List<Coup> coupsLegaux = MoteurCoups.genererCoupsLegaux(etat, attaqueDejaEffectuee);
        if (coupsLegaux.isEmpty()) {
            return new Coup(null, Coup.TypeAction.REPOS, null);
        }
        
        Coup meilleurCoup = null;
        double meilleureValeur = Double.NEGATIVE_INFINITY;
        
        // for each a in Action(s) do
        //     Value(a) <- MIN_VALUE(result(a, s))
        // return a with highest Value(a)
        for (Coup coup : coupsLegaux) {
            Etat etatSuivant = MoteurCoups.simulerCoup(etat, coup);
            double valeur = minValue(etatSuivant, PROFONDEUR_MAX - 1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            
            if (valeur > meilleureValeur) {
                meilleureValeur = valeur;
                meilleurCoup = coup;
            }
        }
        
        return meilleurCoup;
    }
    
    /**
     * MIN_VALUE(s) returns a utility value
     * if TerminalTest(s) then return Utility(s)
     * v <- +infinity
     * for each a in Action(s) do
     *     v <- min{v, MAX_VALUE(result(a, s))}
     * return v
     */
    private static double minValue(Etat etat, int profondeur, double alpha, double beta) {
        // if TerminalTest(s) then return Utility(s)
        if (profondeur == 0 || estEtatTerminal(etat)) {
            return utility(etat);
        }
        
        double v = Double.POSITIVE_INFINITY;
        
        // for each a in Action(s) do
        //     v <- min{v, MAX_VALUE(result(a, s))}
        List<Coup> coups = MoteurCoups.genererCoupsLegaux(etat);
        for (Coup coup : coups) {
            Etat etatSuivant = MoteurCoups.simulerCoup(etat, coup);
            double maxValueResult = maxValue(etatSuivant, profondeur - 1, alpha, beta);
            v = Math.min(v, maxValueResult);
            
            // élagage alpha-beta pour optimisation
            if (v <= alpha) {
                return v;
            }
            beta = Math.min(beta, v);
        }
        
        return v;
    }
    
    /**
     * MAX_VALUE(s) returns a utility value
     * if TerminalTest(s) then return Utility(s)
     * v <- -infinity
     * for each a in Action(s) do
     *     v <- max{v, MIN_VALUE(result(a, s))}
     * return v
     */
    private static double maxValue(Etat etat, int profondeur, double alpha, double beta) {
        // if TerminalTest(s) then return Utility(s)
        if (profondeur == 0 || estEtatTerminal(etat)) {
            return utility(etat);
        }
        
        double v = Double.NEGATIVE_INFINITY;
        
        // for each a in Action(s) do
        //     v <- max{v, MIN_VALUE(result(a, s))}
        List<Coup> coups = MoteurCoups.genererCoupsLegaux(etat);
        for (Coup coup : coups) {
            Etat etatSuivant = MoteurCoups.simulerCoup(etat, coup);
            double minValueResult = minValue(etatSuivant, profondeur - 1, alpha, beta);
            v = Math.max(v, minValueResult);
            
            // élagage alpha-beta pour optimisation
            if (v >= beta) {
                return v;
            }
            alpha = Math.max(alpha, v);
        }
        
        return v;
    }
    
    /**
     * vérifie si l'état est terminal (fin de partie)
     */
    private static boolean estEtatTerminal(Etat etat) {
        return etat.getJoueurActif().getHp() <= 0 || etat.getAdversaire().getHp() <= 0;
    }
    
    /**
     * fonction d'utilité pour évaluer un état
     * retourne une valeur numérique représentant la qualité de l'état
     * valeurs positives = avantage pour le joueur actif
     * valeurs négatives = avantage pour l'adversaire
     */
    private static double utility(Etat etat) {
        Etat.JoueurEtat actif = etat.getJoueurActif();
        Etat.JoueurEtat adversaire = etat.getAdversaire();
        
        // si fin de partie, retourner une valeur extrême
        if (actif.getHp() <= 0) {
            return -10000.0; // défaite
        }
        if (adversaire.getHp() <= 0) {
            return 10000.0; // victoire
        }
        
        double score = 0.0;
        
        // différence de pv : le plus important
        double differencePV = actif.getHp() - adversaire.getHp();
        score += differencePV * 50.0; // poids très élevé pour les pv
        
        // énergie : important pour pouvoir attaquer
        double energieActif = actif.getEnergie();
        double energieAdversaire = adversaire.getEnergie();

        if (energieActif >= 15) {
            score += 80.0; // bonus pour être en capacité d'attaquer
        } else {
            score += energieActif * 4.0; // pousser à récupérer de l'énergie
            score += (15 - energieActif) * 6.0; // bonus fort quand l'énergie est trop basse
        }

        score -= energieAdversaire * 3.0; // pénaliser l'énergie adverse

        // parades : utile si le joueur a encore des parades, mais moins prioritaire que l'attaque directe
        score += actif.getNbParades() * 10.0;
        score -= adversaire.getNbParades() * 15.0;

        // bonus si une attaque est possible immédiatement
        score += evaluerPotentielAttaqueDirecte(actif, adversaire);

        // plus fort pour se rapprocher des cases d'énergie quand l'IA est faible en énergie
        score += evaluerDistanceBonusEnergie(etat, actif) * 25.0;
        
        // distance : être proche permet d'attaquer
        int distance = calculerDistance(actif.getPosition(), adversaire.getPosition());
        
        if (distance <= 1) {
            score += 200.0; // au contact = ultra prioritaire
        } else if (distance <= 2) {
            score += 100.0; // très proche
        } else if (distance <= 3) {
            score += 50.0; // proche
        } else {
            score -= distance * 20.0; // pénalité si loin
        }
        
        // bonus pour les cases de boost à proximité
        score += evaluerBoostProximite(etat, actif) * 30.0;
        
        return score;
    }
    
    /**
     * calcule la distance de manhattan entre deux positions
     */
    private static int calculerDistance(Position pos1, Position pos2) {
        return Math.abs(pos1.getLigne() - pos2.getLigne()) + 
               Math.abs(pos1.getColonne() - pos2.getColonne());
    }
    
    /**
     * évalue les cases de boost à proximité du joueur
     */
    private static double evaluerBoostProximite(Etat etat, Etat.JoueurEtat joueur) {
        double scoreBoost = 0.0;
        int[][] grille = etat.getGrille();
        int[][] directions = {{-1,0}, {1,0}, {0,-1}, {0,1}, {-1,-1}, {-1,1}, {1,-1}, {1,1}};

        Position posJoueur = joueur.getPosition();

        // vérifier les cases adjacentes
        for (int[] dir : directions) {
            int ligne = posJoueur.getLigne() + dir[0];
            int colonne = posJoueur.getColonne() + dir[1];

            if (ligne >= 0 && ligne < grille.length && colonne >= 0 && colonne < grille[0].length) {
                int caseType = grille[ligne][colonne];

                if (caseType == 3) { // parade
                    if (joueur.getNbParades() < 2) {
                        scoreBoost += 3.0; // bonus plus utile si faible
                    }
                } else if (caseType == 4) { // énergie
                    if (joueur.getEnergie() < 35) {
                        scoreBoost += 5.0; // bonus important quand l'énergie est insuffisante
                    } else {
                        scoreBoost += 2.0; // toujours intéressant
                    }
                }
            }
        }

        return scoreBoost;
    }

    private static double evaluerDistanceBonusEnergie(Etat etat, Etat.JoueurEtat joueur) {
        if (joueur.getEnergie() >= 15) {
            return 0.0;
        }

        int[][] grille = etat.getGrille();
        Position pos = joueur.getPosition();
        int minDist = Integer.MAX_VALUE;

        for (int i = 0; i < grille.length; i++) {
            for (int j = 0; j < grille[i].length; j++) {
                if (grille[i][j] == 4) {
                    int dist = Math.abs(pos.getLigne() - i) + Math.abs(pos.getColonne() - j);
                    minDist = Math.min(minDist, dist);
                }
            }
        }

        if (minDist == Integer.MAX_VALUE) {
            return 0.0;
        }

        return Math.max(0, 10 - minDist);
    }

    private static double evaluerPotentielAttaqueDirecte(Etat.JoueurEtat actif, Etat.JoueurEtat adversaire) {
        double score = 0.0;
        int distance = calculerDistance(actif.getPosition(), adversaire.getPosition());

        for (Etat.AttaqueInfo attaque : actif.getAttaques()) {
            if (actif.getEnergie() >= attaque.getDegat()) {
                if (distance <= attaque.getPortee()) {
                    score += attaque.getDegat() * 120.0;
                    score += 180.0; // grosse priorité si l'attaque est possible maintenant
                } else if (distance <= attaque.getPortee() + 1) {
                    score += attaque.getDegat() * 20.0;
                    score += 40.0; // proche de l'adversaire
                }
            }
        }

        return score;
    }
}


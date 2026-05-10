package ia;

import attaques.Attaques;
import entite.Personnage;
import entite.Position;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente l'état du jeu à un instant T pour les simulations de l'IA.
 */
public class Etat {
    
    private int[][] grille;
    private JoueurEtat joueurActif;
    private JoueurEtat adversaire;

    /**
     * Constructeur standard.
     */
    public Etat(int[][] grille, JoueurEtat joueurActif, JoueurEtat adversaire) {
        this.grille = copierGrille(grille);
        this.joueurActif = joueurActif;
        this.adversaire = adversaire;
    }

    /**
     * Constructeur de copie (Indispensable pour le MoteurCoups et Minimax).
     */
    public Etat(Etat autre) {
        this.grille = copierGrille(autre.grille);
        this.joueurActif = new JoueurEtat(autre.joueurActif);
        this.adversaire = new JoueurEtat(autre.adversaire);
    }

   
    // HEURISTIQUE DE L'IA FACILE

    
    /**
     * Heuristique générale utilisée par l'IA "facile".
     * Fournit un score relatif de la position du point de vue du joueur actif.
     * @return score heuristique (plus élevé = meilleure position)
     */
    public int getScoreHeuristique() {
        // 1. ÉTATS TERMINAUX (Poids de Victoire Absolu)
        if (adversaire.getHp() <= 0) return 1000000; 
        if (joueurActif.getHp() <= 0) return -1000000;

        double score = 0;

        // 2. ANALYSE DES POINTS DE VIE
        score += (joueurActif.getHp() - adversaire.getHp()) * 20;

        // 3. DISTANCE DE MANHATTAN : pénalité renforcée pour forcer l'approche
        int dist = Math.abs(joueurActif.getPosition().getLigne() - adversaire.getPosition().getLigne())
                 + Math.abs(joueurActif.getPosition().getColonne() - adversaire.getPosition().getColonne());
        score -= dist * 5;

        // 4. BONUS SI À PORTÉE D'ATTAQUE : récompense le contact immédiat
        boolean peutAttaquer = false;
        for (AttaqueInfo a : joueurActif.getAttaques()) {
            if (joueurActif.getEnergie() >= a.getDegat() && dist <= a.getPortee()) {
                score += 60;
                peutAttaquer = true;
                break;
            }
        }

        // 5. GESTION DYNAMIQUE DE L'ÉNERGIE
        if (joueurActif.getEnergie() < 10) {
            score += joueurActif.getEnergie() * 5.0; 
        } else {
            score += joueurActif.getEnergie() * 0.1; 
        }
        
        // 6. GESTION DE LA PARADE
        if (joueurActif.isEnParade() && dist <= 3) {
            score += 15;
        } else if (joueurActif.isEnParade() && dist > 3) {
            score -= 5;
        } else {
            score += joueurActif.getNbParades() * 1.0;
        }

        // 7. PÉNALITÉ FORTE POUR L'INACTION : évite les tours passés indéfiniment
        if (joueurActif.getEnergie() > 15 && !joueurActif.isEnParade() && !peutAttaquer) {
            score -= 40;
        }
        
        return (int) score;
    }


    // MÉTHODES DE GESTION DE L'ÉTAT

    // Vérifie si l'état est terminal (victoire de l'un ou l'autre joueur).
    /**
     * Indique si l'état est terminal (une unité est morte).
     * @return true si l'un des joueurs a 0 PV ou moins
     */
    public boolean estTerminal() {
        return joueurActif.getHp() <= 0 || adversaire.getHp() <= 0;
    }

    /**
     * Change le joueur actif dans cet état (utilisé par les simulateurs et le minimax).
     * Remet également enParade à false pour le nouveau joueur actif.
     */
    public void changerJoueurActif() {
        JoueurEtat temp = joueurActif;
        joueurActif = adversaire;
        adversaire = temp;
        joueurActif.setEnParade(false);
    }

    // Méthode utilitaire pour copier une grille (pour éviter les références partagées).
    private int[][] copierGrille(int[][] source) {
        int[][] copie = new int[source.length][source[0].length];
        for (int i = 0; i < source.length; i++) {
            System.arraycopy(source[i], 0, copie[i], 0, source[i].length);
        }
        return copie;
    }

    /**
     * Méthode utilitaire publique statique pour copier une grille (compatibilité).
     */
    public static int[][] copieGrille(int[][] source) {
        int[][] copie = new int[source.length][source[0].length];
        for (int i = 0; i < source.length; i++) {
            System.arraycopy(source[i], 0, copie[i], 0, source[i].length);
        }
        return copie;
    }

    // --- GETTERS & SETTERS ---
    public int[][] getGrille() { return grille; }
    public JoueurEtat getJoueurActif() { return joueurActif; }
    public JoueurEtat getAdversaire() { return adversaire; }

    //HEURISTIQUE DE L'IA MOYENNE 
    
    public int getScoreHeuristiqueMoyenne() {
        if (adversaire.getHp() <= 0) return 1000000;
        if (joueurActif.getHp() <= 0) return -1000000;
        
        double score = 0;
        
        // 1. (1 PV = 100 points)
        score += (joueurActif.getHp() - adversaire.getHp()) * 100.0; 
        
        // 2. L'ÉCONOMIE DU JEU : l'énergie est cruciale pour attaquer, et les parades sont vitales pour survivre.
        // L'énergie permet de frapper, 1 pt d'énergie = 10 points
        score += (joueurActif.getEnergie() - adversaire.getEnergie()) * 10.0; 
        
        // La parade est vitale car il n'y a pas de soin : 1 parade = 4000 points d'écart (équivalent à 40 PV)
        score += (joueurActif.getNbParades() - adversaire.getNbParades()) * 4000.0;
        
        // 3. LA COURSE AU CENTRE (Briser le No Man's Land)
        double centreLigne = (grille.length - 1) / 2.0;
        double centreColonne = (grille[0].length - 1) / 2.0;
        
        double distCentreActif = Math.abs(joueurActif.getPosition().getLigne() - centreLigne) +
                                 Math.abs(joueurActif.getPosition().getColonne() - centreColonne);
        double distCentreAdversaire = Math.abs(adversaire.getPosition().getLigne() - centreLigne) +
                                      Math.abs(adversaire.getPosition().getColonne() - centreColonne);
                                   

        score += (distCentreAdversaire - distCentreActif) * 500.0; 
        score += (joueurActif.getPosition().getLigne() * 0.001) + (joueurActif.getPosition().getColonne() * 0.0001);
        return (int) score;
    }
    

    // HEURISTIQUE DE L'IA DIFFICILE


    public int getScoreHeuristiqueDifficile() {
        // 1. ÉTATS TERMINAUX (Poids de Victoire Absolu)
        if (adversaire.getHp() <= 0) return 1000000;
        if (joueurActif.getHp() <= 0) return -1000000;

        double score = 0;

        // 2. POINTS DE VIE : Priorité absolue, pondération élevée
        score += (joueurActif.getHp() - adversaire.getHp()) * 80;

        // 3. DISTANCE : pénalité très forte pour forcer une pression constante
        int distance = distanceDifficile(joueurActif.getPosition(), adversaire.getPosition());
        score -= distance * 12;

        // 4. DÉTECTION DES COUPS LÉTAUX : Bonus massif si l'adversaire
        //    peut être tué dès ce tour (énergie suffisante + portée ok)
        boolean peutAttaquer = false;
        for (AttaqueInfo a : joueurActif.getAttaques()) {
            if (joueurActif.getEnergie() >= a.getDegat() && distance <= a.getPortee()) {
                peutAttaquer = true;
                if (adversaire.getHp() <= a.getDegat()) {
                    score += 500000; // Coup létal détecté : on le priorise absolument
                    break;
                }
                // Bonus de position offensive même sans coup létal
                score += a.getDegat() * 3;
            }
        }

        // 5. PÉNALITÉ D'ÉVITEMENT : l'IA ne doit JAMAIS fuir si elle peut attaquer
        if (!peutAttaquer && joueurActif.getEnergie() >= 15) {
            score -= 80;
        }

        // 6. GESTION FINE DE L'ÉNERGIE
        if (joueurActif.getEnergie() < 15) {
            score -= 150; // Alerte rouge : plus assez d'énergie pour attaquer
        } else if (joueurActif.getEnergie() <= 60) {
            score += joueurActif.getEnergie() * 1.2;
        } else {
            score += 72; // Plafond : inutile de farmer au-delà de 60
        }

        // 7. LE SECRET DES PARADES (Importé de l'IA Moyenne)
        // La parade est vitale car il n'y a pas de soin.
        score += (joueurActif.getNbParades() - adversaire.getNbParades()) * 4000.0;

        // 8. LA COURSE AU CENTRE ET LE MICRO-GRADIENT
        double centreLigne = (grille.length - 1) / 2.0;
        double centreColonne = (grille[0].length - 1) / 2.0;
        
        double distCentreActif = Math.abs(joueurActif.getPosition().getLigne() - centreLigne) +
                                 Math.abs(joueurActif.getPosition().getColonne() - centreColonne);
        double distCentreAdversaire = Math.abs(adversaire.getPosition().getLigne() - centreLigne) +
                                      Math.abs(adversaire.getPosition().getColonne() - centreColonne);
                                   
        // Dominer le centre du plateau
        score += (distCentreAdversaire - distCentreActif) * 500.0;

        // 9. PÉNALITÉ SI L'ADVERSAIRE PEUT NOUS TUER AU PROCHAIN TOUR
        for (AttaqueInfo a : adversaire.getAttaques()) {
            if (adversaire.getEnergie() >= a.getDegat()
                    && distance <= a.getPortee()
                    && joueurActif.getHp() <= a.getDegat()) {
                score -= 200000; // Danger immédiat : fuir ou parader en priorité
                break;
            }
        }
        // 10. TIE-BREAKING DÉTERMINISTE (Le Micro-Gradient pour éviter les boucles)
        score += (joueurActif.getPosition().getLigne() * 0.001) + (joueurActif.getPosition().getColonne() * 0.0001);
        return (int) score;
    }


    private int distanceDifficile(Position p1, Position p2) {
        return Math.abs(p1.getLigne() - p2.getLigne()) + Math.abs(p1.getColonne() - p2.getColonne());
    }

    // CLASSES INTERNES DE DONNÉES

    public static class JoueurEtat {
        private Position position;
        private int hp;
        private double energie;
        private double maxEnergie;
        private int nbParades;
        private int nbRepos;
        private boolean enParade;
        private int pas;
        private int id;
        private List<AttaqueInfo> attaques;

        // Constructeur standard
        public JoueurEtat(Position position, int hp, double energie, double maxEnergie, int nbParades, int nbRepos, int pas, int id, List<AttaqueInfo> attaques) {
            this.position = position;
            this.hp = hp;
            this.energie = energie;
            this.maxEnergie = maxEnergie;
            this.nbParades = nbParades;
            this.nbRepos = nbRepos;
            this.enParade = false;
            this.pas = pas;
            this.id = id;
            this.attaques = attaques;
        }

        // Constructeur de copie
        public JoueurEtat(JoueurEtat autre) {
            this.position = new Position(autre.position.getLigne(), autre.position.getColonne());
            this.hp = autre.hp;
            this.energie = autre.energie;
            this.maxEnergie = autre.maxEnergie;
            this.nbParades = autre.nbParades;
            this.nbRepos = autre.nbRepos;
            this.enParade = autre.enParade;
            this.pas = autre.pas;
            this.id = autre.id;
            this.attaques = new ArrayList<>(autre.attaques);
        }

        /**
         * Méthode utilitaire pour convertir un Personnage en JoueurEtat, en extrayant les informations nécessaires.
         */
        public static JoueurEtat fromPersonnage(Personnage p, int idJoueur) {
            List<AttaqueInfo> infos = new ArrayList<>();
            for (Attaques a : p.getAttaques()) {
                infos.add(new AttaqueInfo(a.getType_attaque(), a.getDegat(), a.getPortee()));
            }
            return new JoueurEtat(p.getPosition(), (int) Math.max(0, p.getHp()), p.getEnergie(), p.getMaxEnergie(), p.getNbParades(), p.getNbRepos(), p.getPas(), idJoueur, infos);
        }

        /**
         * Surcharge utilitaire pour conserver l'API existante où aucun id n'est fourni.
         */
        public static JoueurEtat fromPersonnage(Personnage p) {
            return fromPersonnage(p, 0);
        }

        // --- Getters & Setters ---
        public Position getPosition() { return position; }
        public void setPosition(Position position) { this.position = position; }
        public int getHp() { return hp; }
        public void setHp(int hp) { this.hp = hp; }
        public double getEnergie() { return energie; }
        public void setEnergie(double energie) { 
            this.energie = energie;
            if (this.energie < 0) this.energie = 0;
            if (this.energie > this.maxEnergie) this.energie = this.maxEnergie;
        }
        public double getMaxEnergie() { return maxEnergie; }
        public int getNbParades() { return nbParades; }
        public void setNbParades(int nbParades) { this.nbParades = nbParades; }
        public int getNbRepos() { return nbRepos; }
        public void setNbRepos(int nbRepos) { this.nbRepos = nbRepos; }
        public boolean isEnParade() { return enParade; }
        public void setEnParade(boolean enParade) { this.enParade = enParade; }
        public int getPas() { return pas; }
        public int getId() { return id; }
        public List<AttaqueInfo> getAttaques() { return attaques; }
    }

    public static class AttaqueInfo {
        private String type;
        private double degat;
        private int portee;

        public AttaqueInfo(String type, double degat, int portee) {
            this.type = type;
            this.degat = degat;
            this.portee = portee;
        }

        public String getType() { return type; }
        public double getDegat() { return degat; }
        public int getPortee() { return portee; }
    }
}
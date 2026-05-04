package ia;

import entite.Position;
import entite.Personnage;
import attaques.Attaques;
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

    // =========================================================================
    // 🧠 HEURISTIQUE DE L'IA FACILE
    // =========================================================================
    
    public int getScoreHeuristique() {
        // 1. ÉTATS TERMINAUX (Poids de Victoire Absolu)
        if (adversaire.getHp() <= 0) return 1000000; 
        if (joueurActif.getHp() <= 0) return -1000000;

        double score = 0;

        // 2. ANALYSE DES POINTS DE VIE
        score += (joueurActif.getHp() - adversaire.getHp()) * 20;

        // 3. DISTANCE DE MANHATTAN
        int dist = Math.abs(joueurActif.getPosition().getLigne() - adversaire.getPosition().getLigne())
                 + Math.abs(joueurActif.getPosition().getColonne() - adversaire.getPosition().getColonne());
        score -= dist * 2;

        // 4. GESTION DYNAMIQUE DE L'ÉNERGIE
        if (joueurActif.getEnergie() < 10) {
            score += joueurActif.getEnergie() * 5.0; 
        } else {
            score += joueurActif.getEnergie() * 0.1; 
        }
        
        // 5. GESTION DE LA PARADE
        if (joueurActif.isEnParade() && dist <= 3) {
            score += 15;
        } else if (joueurActif.isEnParade() && dist > 3) {
            score -= 5;
        } else {
            score += joueurActif.getNbParades() * 1.0;
        }

        // 6. PÉNALITÉ POUR L'INACTION
        if (joueurActif.getEnergie() > 15 && !joueurActif.isEnParade()) {
            score -= 10;
        }

        return (int) score;
    }

    // =========================================================================
    // ⚙️ MÉTHODES DE GESTION DE L'ÉTAT
    // =========================================================================

    public boolean estTerminal() {
        return joueurActif.getHp() <= 0 || adversaire.getHp() <= 0;
    }

    public void changerJoueurActif() {
        JoueurEtat temp = joueurActif;
        joueurActif = adversaire;
        adversaire = temp;
        
        // CORRECTION VITALE : La parade ne dure qu'un tour !
        joueurActif.setEnParade(false);
    }

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

    // =========================================================================
    // 👤 CLASSES INTERNES DE DONNÉES
    // =========================================================================

    public static class JoueurEtat {
        private Position position;
        private int hp;
        private double energie;
        private int nbParades;
        private int nbRepos;
        private boolean enParade;
        private int pas;
        private int id;
        private List<AttaqueInfo> attaques;

        // Constructeur standard
        public JoueurEtat(Position position, int hp, double energie, int nbParades, int nbRepos, int pas, int id, List<AttaqueInfo> attaques) {
            this.position = position;
            this.hp = hp;
            this.energie = energie;
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
            this.nbParades = autre.nbParades;
            this.nbRepos = autre.nbRepos;
            this.enParade = autre.enParade;
            this.pas = autre.pas;
            this.id = autre.id;
            this.attaques = new ArrayList<>(autre.attaques);
        }

        /**
         * CORRECTION : Ajout du paramètre idJoueur pour l'affichage correct sur la grille (ex: 1 ou 2).
         */
        public static JoueurEtat fromPersonnage(Personnage p, int idJoueur) {
            List<AttaqueInfo> infos = new ArrayList<>();
            for (Attaques a : p.getAttaques()) {
                infos.add(new AttaqueInfo(a.getType_attaque(), a.getDegat(), a.getPortee()));
            }
            return new JoueurEtat(p.getPosition(), (int) Math.max(0, p.getHp()), p.getEnergie(), p.getNbParades(), p.getNbRepos(), p.getPas(), idJoueur, infos);
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
        public void setEnergie(double energie) { this.energie = energie; }
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

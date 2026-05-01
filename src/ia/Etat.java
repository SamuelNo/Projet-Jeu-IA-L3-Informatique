package ia;

import attaques.Attaques;
import entite.Personnage;
import entite.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Etat abstrait du jeu pour les algorithmes IA.
 */
public class Etat {
    public static class AttaqueInfo {
        private final String type;
        private final double degat;
        private final int portee;

        public AttaqueInfo(String type, double degat, int portee) {
            this.type = type;
            this.degat = degat;
            this.portee = portee;
        }

        public String getType() {
            return type;
        }

        public double getDegat() {
            return degat;
        }

        public int getPortee() {
            return portee;
        }
    }

    public static class JoueurEtat {
        private final String nom;
        private double hp;
        private double energie;
        private final int pas;
        private int nbParades;
        private int nbRepos;
        private boolean enParade;
        private Position position;
        private final List<AttaqueInfo> attaques;

        public JoueurEtat(
                String nom,
                double hp,
                double energie,
                int pas,
                int nbParades,
                int nbRepos,
                boolean enParade,
                Position position,
                List<AttaqueInfo> attaques
        ) {
            this.nom = nom;
            this.hp = hp;
            this.energie = energie;
            this.pas = pas;
            this.nbParades = nbParades;
            this.nbRepos = nbRepos;
            this.enParade = enParade;
            this.position = position;
            this.attaques = attaques;
        }

        public JoueurEtat(JoueurEtat other) {
            this.nom = other.nom;
            this.hp = other.hp;
            this.energie = other.energie;
            this.pas = other.pas;
            this.nbParades = other.nbParades;
            this.nbRepos = other.nbRepos;
            this.enParade = other.enParade;
            this.position = new Position(other.position.getLigne(), other.position.getColonne());
            this.attaques = new ArrayList<>(other.attaques);
        }

        public static JoueurEtat fromPersonnage(Personnage p) {
            List<AttaqueInfo> attaques = new ArrayList<>();
            for (Attaques att : p.getAttaques()) {
                attaques.add(new AttaqueInfo(att.getType_attaque(), att.getDegat(), att.getPortee()));
            }
            return new JoueurEtat(
                    p.getNom(),
                    p.getHp(),
                    p.getEnergie(),
                    p.getPas(),
                    p.getNbParades(),
                    p.getNbRepos(),
                    p.isEnParade(),
                    new Position(p.getPosition().getLigne(), p.getPosition().getColonne()),
                    attaques
            );
        }

        public String getNom() { return nom; }
        public double getHp() { return hp; }
        public void setHp(double hp) { this.hp = hp; }
        public double getEnergie() { return energie; }
        public void setEnergie(double energie) { this.energie = energie; }
        public int getPas() { return pas; }
        public int getNbParades() { return nbParades; }
        public void setNbParades(int nbParades) { this.nbParades = nbParades; }
        public int getNbRepos() { return nbRepos; }
        public void setNbRepos(int nbRepos) { this.nbRepos = nbRepos; }
        public boolean isEnParade() { return enParade; }
        public void setEnParade(boolean enParade) { this.enParade = enParade; }
        public Position getPosition() { return position; }
        public void setPosition(Position position) { this.position = position; }
        public List<AttaqueInfo> getAttaques() { return attaques; }
    }

    private final int[][] grille;
    private JoueurEtat joueurActif;
    private JoueurEtat adversaire;

    public Etat(int[][] grille, JoueurEtat joueurActif, JoueurEtat adversaire) {
        this.grille = grille;
        this.joueurActif = joueurActif;
        this.adversaire = adversaire;
    }

    public Etat(Etat other) {
        this.grille = copieGrille(other.grille);
        this.joueurActif = new JoueurEtat(other.joueurActif);
        this.adversaire = new JoueurEtat(other.adversaire);
    }

    public static int[][] copieGrille(int[][] source) {
        int[][] copie = new int[source.length][source[0].length];
        for (int i = 0; i < source.length; i++) {
            System.arraycopy(source[i], 0, copie[i], 0, source[i].length);
        }
        return copie;
    }

    public int[][] getGrille() {
        return grille;
    }

    public JoueurEtat getJoueurActif() {
        return joueurActif;
    }

    public JoueurEtat getAdversaire() {
        return adversaire;
    }

    public void changerJoueurActif() {
        JoueurEtat tmp = joueurActif;
        joueurActif = adversaire;
        adversaire = tmp;
    }
}

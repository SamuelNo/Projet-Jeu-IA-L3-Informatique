package architecture;

import entite.*;
import java.util.Random; 

/**
 * Représente l'arène de combat et l'état de sa grille.
 */
public class Arene {
    private int grille[][];
    private static final int TAILLE = 10;
    private Personnage joueur1;
    private Personnage joueur2;
    private String indice;

    /**
     * Crée une arène pour deux joueurs et initialise la grille.
     * @param joueur1 premier joueur
     * @param joueur2 second joueur
     */
    public Arene(Personnage joueur1, Personnage joueur2) {
        grille = new int[TAILLE][TAILLE];
        indice = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        this.joueur1 = joueur1;
        this.joueur2 = joueur2;
        initialiserGrille();
        genererObstacles(10); 
        genererBonus(3, 3); // 3 parades, 3 energies
    }

    /**
     * Initialise complètement la grille et place les deux joueurs aux coins.
     */
    public void initialiserGrille() {
        for (int i = 0; i < TAILLE; i++) {
            for (int j = 0; j < TAILLE; j++) {
                grille[i][j] = 0;
            }
        }
        joueur1.setPosition(new Position(0, 0));
        joueur2.setPosition(new Position(TAILLE - 1, TAILLE - 1));
        grille[joueur1.getPosition().getLigne()][joueur1.getPosition().getColonne()] = 1;
        grille[joueur2.getPosition().getLigne()][joueur2.getPosition().getColonne()] = 2;
    }
    /**
     * Génère aléatoirement un nombre donné d'obstacles sur la grille, sans écraser les joueurs ni les bonus.
     * @param nbObstacles
     */
    private void genererObstacles(int nbObstacles) {
        Random rand = new Random();
        int obstaclesPlaces = 0;
        
        while (obstaclesPlaces < nbObstacles) {
            int l = rand.nextInt(TAILLE);
            int c = rand.nextInt(TAILLE);
            
            if (grille[l][c] == 0) {
                grille[l][c] = -1;
                obstaclesPlaces++;
            }
        }
    }

        /**
        * Génère aléatoirement un nombre donné de bonus de parade et d'énergie sur la grille, sans écraser les joueurs ni les obstacles.
        * @param nbParades nombre de bonus de parade à générer
        * @param nbEnergie nombre de bonus d'énergie à générer
        */
    private void genererBonus(int nbParades, int nbEnergie) {
        Random rand = new Random();
        int places = 0;
        while (places < nbParades) {
            int l = rand.nextInt(TAILLE);
            int c = rand.nextInt(TAILLE);
            if (grille[l][c] == 0) { grille[l][c] = 3; places++; }
        }
        places = 0;
        while (places < nbEnergie) {
            int l = rand.nextInt(TAILLE);
            int c = rand.nextInt(TAILLE);
            if (grille[l][c] == 0) { grille[l][c] = 4; places++; }
        }
    }

    /**
     * Met à jour la grille après un coup, en effaçant les anciennes positions des joueurs et en plaçant les nouveaux.
     * Les obstacles et bonus restent inchangés.
     */
    public void updateFullGrille() {
        for (int i = 0; i < TAILLE; i++) {
            for (int j = 0; j < TAILLE; j++) {
                // on ne supprime pas les obstacles (-1) ni les bonus (3, 4)
                if (grille[i][j] != -1 && grille[i][j] != 3 && grille[i][j] != 4) 
                    grille[i][j] = 0;
            }
        }
        grille[joueur1.getPosition().getLigne()][joueur1.getPosition().getColonne()] = 1;
        grille[joueur2.getPosition().getLigne()][joueur2.getPosition().getColonne()] = 2;
    }
    
    /**
     * Affiche l'état actuel de la grille dans la console.
     */
    public void getArene() {
        String[] indiceGrille = indice.split("");
        System.out.print("    ");
        for (int i = 0; i < TAILLE; i++) {
            System.out.print(indiceGrille[i] + " ");
        }
        System.out.println("\n");
        for (int i = 0; i < TAILLE; i++) {
            System.out.print(indiceGrille[i] + "   ");
            for (int j = 0; j < TAILLE; j++) {
                System.out.print(grille[i][j] + " ");
            }
            System.out.println();
        }
    }

    /**
     * Retourne le premier joueur.
     * @return joueur 1
     */
    public Personnage getJoueur1() {
        return joueur1;
    }

    /**
     * Retourne le second joueur.
     * @return joueur 2
     */
    public Personnage getJoueur2() {
        return joueur2;
    }
    
    /**
     * Affiche les informations principales des deux joueurs.
     */
    public void afficherJoueur() {
        System.out.println("Joueur 1 : " + joueur1.infoPersoString() + "\n");
        System.out.println("Joueur 2 : " + joueur2.infoPersoString() + "\n");
    }
    
    public int[][] getGrille() {
        return grille;
    }

    /**
     * Applique la règle de Mort Subite pour départager deux joueurs vivants.
     * Retourne 1 si joueur1 l'emporte, 2 si joueur2 l'emporte, 0 si égalité parfaite.
     * Critères (dans l'ordre): ratio PV actuels / PV max, énergie restante, nombre de parades.
     */
    public int determineMortSubiteWinner() {
        double ratio1 = joueur1.getHp() / Math.max(1.0, joueur1.getMaxHp());
        double ratio2 = joueur2.getHp() / Math.max(1.0, joueur2.getMaxHp());
        if (Double.compare(ratio1, ratio2) > 0) return 1;
        if (Double.compare(ratio2, ratio1) > 0) return 2;

        // égalité sur les PV relatifs -> comparer l'énergie
        double e1 = joueur1.getEnergie();
        double e2 = joueur2.getEnergie();
        if (Double.compare(e1, e2) > 0) return 1;
        if (Double.compare(e2, e1) > 0) return 2;

        // égalité sur l'énergie -> comparer le nombre de parades restantes
        int p1 = joueur1.getNbParades();
        int p2 = joueur2.getNbParades();
        if (p1 > p2) return 1;
        if (p2 > p1) return 2;

        // parfaite égalité
        return 0;
    }
}
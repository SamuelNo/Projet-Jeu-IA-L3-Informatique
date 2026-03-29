package architecture;

import entite.*;

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
    }

    // Initialisation de la grille: toutes les cases sont pour l'instant à 0 (vide);
    // case = 1 (Joueur 1); case = 2 (Joueur 2)
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
     * Recalcule la grille selon les positions courantes des joueurs.
     */
    public void updateFullGrille() {
        // 1. Tout remettre à zéro (ou laisser les murs/objets fixes)
        for (int i = 0; i < TAILLE; i++) {
            for (int j = 0; j < TAILLE; j++) {
                // La valeur -1 est réservée aux obstacles fixes éventuels.
                if (grille[i][j] != -1) // Si c'est pas un mur
                    grille[i][j] = 0;
            }
        }
        // 2. Placer les entités vivantes
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
    
}
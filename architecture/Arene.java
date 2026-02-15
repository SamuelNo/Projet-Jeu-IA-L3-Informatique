package architecture;
import entites.*;

public class Arene {
    private int grille[][];
    private static final int TAILLE = 10;
    private Personnage joueur1;
    private Personnage joueur2;
    private String indice;

    public Arene(Personnage joueur1,Personnage joueur2){
        grille = new int[TAILLE][TAILLE];
        indice = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        this.joueur1 = joueur1;
        this.joueur2 = joueur2;
        initialiserGrille();
    }

    // Initialisation de la grille: toutes les cases sont pour l'instant à 0 (vide); case = 1 (Joueur 1); case = 2 (Joueur 2)
    public void initialiserGrille(){
        for(int i = 0; i < TAILLE; i++){
            for(int j = 0; j< TAILLE; j++){
                grille[i][j] = 0;
            }
        }
        joueur1.setPosition(new Position(0 , 0));
        joueur2.setPosition( new Position(TAILLE - 1, TAILLE - 1));
        grille[joueur1.getPosition().getLigne()][joueur1.getPosition().getColonne()] = 1;
        grille[joueur2.getPosition().getLigne()][joueur2.getPosition().getColonne()] = 2;

    }

    public void updatePosition(){
        grille[joueur1.getPosition().getLigne()][joueur1.getPosition().getColonne()] = 1;
        grille[joueur2.getPosition().getLigne()][joueur2.getPosition().getColonne()] = 2;
    }

    public void getArene(){
        String[] indiceGrille = indice.split("");
        System.out.print("    ");
        for(int i = 0; i < TAILLE ; i++){
            System.out.print(indiceGrille[i]+" ");
        }
        System.out.print("\n\n");
        for(int i = 0; i < TAILLE; i++ ){
            System.out.print(indiceGrille[i]+"   ");
            for(int j = 0; j < TAILLE; j++){
                System.out.print(grille[i][j]+" ");
            }
            System.out.print("\n");
        }
    }



    public static void main(String[] args){
        Arene a = new Arene(new Personnage("Samuel"), new Personnage("Brahim"));
        a.getArene();

        Personnage p = new Personnage("Samuel");
        System.out.println(p.estEnVie());
    }
}

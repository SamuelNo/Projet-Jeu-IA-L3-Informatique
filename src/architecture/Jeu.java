package architecture;

import entite.*;
import exception.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Jeu {

    private List<Personnage> listePersonnages;
    private Arene arene;
    private Scanner sc;

    public Jeu() {
        sc = new Scanner(System.in);
        listePersonnages = new ArrayList<>();
        listePersonnages.add(new Chevalier());
        listePersonnages.add(new Archere());
        listePersonnages.add(new Soigneur());
        initialisation();
    }

    private Personnage choixPersonnage()throws NullPointerException{
        Personnage persoChoisi = null;
        System.out.println("\nEntrer le nom du personnage de votre choix :");
        String nom = sc.nextLine();

        if (nom.equalsIgnoreCase("chevalier"))
            persoChoisi = new Chevalier();
        if (nom.equalsIgnoreCase("archère"))
            persoChoisi = new Archere();
        if (nom.equalsIgnoreCase("soigneur"))
            persoChoisi = new Soigneur();

        if(persoChoisi == null)
            throw new NullPointerException("Personnage non reconnu, veuillez réessayer.");

        return persoChoisi; 
    }

    private void initialisation(){
        System.out.println("\nG L A D I U S \n");
        boolean choixValide = false;
        while(!choixValide){
            try{
                System.out.println("Liste des personnages :\n");
                for (int i = 0; i < listePersonnages.size(); i++) {
                    System.out.println(listePersonnages.get(i).infoPersoString() + "\n");
                }
                System.out.println("Joueur N°1\nChoix du personnage");
                Personnage perso1 = choixPersonnage();
                System.out.println("\nJoueur N°2\nChoix du personnage");
                Personnage perso2 = choixPersonnage();
                choixValide = true;
                arene = new Arene(perso1, perso2);
                System.out.println("\nChoix validé, voici votre arène :\n");
                arene.getArene();
                arene.afficherJoueur();
                
                FenetreArene.lancerFenetre(arene);
                
            } catch (NullPointerException e) {
                System.out.println("\nErreur : " + e.getMessage() + "\n");
             }
        }
    }

    public void tour(Personnage attaquant, Personnage defenseur) {
        boolean tourReussi = false;
        while (!tourReussi) {
            try {
                System.out.println("\nChoisissez une case de destination pour votre personnage (ex : A1, B3, etc) :");
                String destination = sc.nextLine().toUpperCase();
                
                // parsing a1 b3 pour la grille
                if (destination.length() >= 2) {
                    int ligne = destination.charAt(0) - 'A';
                    int colonne = destination.charAt(1) - '0';
                    
                    if (ligne >= 0 && ligne < 10 && colonne >= 0 && colonne < 10 && arene.getGrille()[ligne][colonne] != -1) {
                        attaquant.setPosition(new Position(ligne, colonne));
                        arene.updateFullGrille();
                        FenetreArene.rafraichir(); 
                    } else {
                        System.out.println("\ndéplacement impossible (hors carte ou obstacle).");
                    }
                }

                System.out.println("\nChoix d'action :\nA : Attaque\nP : Parade\nR : Repos");
                String action = sc.nextLine();
                switch (action.toLowerCase()) {

                    case "a":
                        System.out.println("\nChoix de l'attaque :\nAL : Attaque légère\nAD : Attaque à distance\nALD : Attaque lourde");
                        String attaque = sc.nextLine();
                        attaquant.attaquer(defenseur, attaque);
                        tourReussi = true;
                        break;
                        
                    case "p":
                        attaquant.parader();
                        tourReussi = true;
                        break;

                    case "r":
                        attaquant.seReposer();
                        tourReussi = true;  
                        break;
                }
            } catch (IllegalActionException e) {
                System.out.println("\nAction impossible : " + e.getMessage() + "\n");
            }
        }
    }

    public void star() {
        while (arene.getJoueur1().getHp() > 0 && arene.getJoueur2().getHp() > 0) {
            arene.getArene();
            System.out.println("\nJoueur 1, à vous de jouer : " + arene.getJoueur1().getNom() + "\n");
            System.out.println(arene.getJoueur1().statsJoueurString() + "\n");
            tour(arene.getJoueur1(), arene.getJoueur2());
            if (arene.getJoueur2().getHp() <= 0) {
                System.out.println("\nJoueur 1 a gagné !\n");
                break;
            }
            System.out.println("\nJoueur 2, à vous de jouer : " + arene.getJoueur2().getNom() + "\n");
            System.out.println(arene.getJoueur2().statsJoueurString() + "\n");
            tour(arene.getJoueur2(), arene.getJoueur1());
            if (arene.getJoueur1().getHp() <= 0) {
                System.out.println("\nJoueur 2 a gagné !\n");
                break;
            }
        }
    }

    public void fermer() {
        sc.close();
    }
}
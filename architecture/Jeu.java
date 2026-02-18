package architecture;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import entites.Archere;
import entites.Chevalier;
import entites.Personnage;
import entites.Soigneur;


public class Jeu {

    private List<Personnage> listePersonnages;
    private Arene arene;
    private static Scanner sc;

    public Jeu(){
        listePersonnages = new ArrayList<>();
        listePersonnages.add(new Chevalier());
        listePersonnages.add(new Archere());
        listePersonnages.add(new Soigneur());
        initialisation();
    }

    private Personnage choixPersonnage(){
        Personnage perso = null;
        System.out.println("Entrer le nom du personnage de votre choix: ");
        sc = new Scanner(System.in);
        String nom = sc.nextLine();

        for(Personnage p : listePersonnages){
            if(nom.toLowerCase().equals(p.getNom().toLowerCase())){
                perso = p;
            }
        }

        return perso;
    }

    private void initialisation(){
        System.out.println("\nG L A D I U S \n");
        System.out.println("Liste des personnages: \n");
        for(int i = 0; i<listePersonnages.size();i++){
            System.out.println(listePersonnages.get(i).toString()+"\n");
        }
        System.out.println("Joueur N°1\nChoix du personnage ");
        Personnage perso1 = choixPersonnage();
        System.out.println("Joueur N°2\nChoix du personnage");
        Personnage perso2 = choixPersonnage();
        arene = new Arene(perso1,perso2);
        sc.close();
        System.out.println("\nChoix validé, voici votre arène: \n");
        arene.getArene();
        arene.getJoueur();

    }
}

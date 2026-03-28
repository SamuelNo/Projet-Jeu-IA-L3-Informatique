package architecture;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import entite.*;

public class Jeu {

    private List<Personnage> listePersonnages;
    private Arene arene;
    private static Scanner sc;

    public Jeu() {
        listePersonnages = new ArrayList<>();
        listePersonnages.add(new Chevalier());
        listePersonnages.add(new Archere());
        listePersonnages.add(new Soigneur());
        initialisation();
    }

    private Personnage choixPersonnage() {
        System.out.println("Entrer le nom du personnage de votre choix: ");
        String nom = sc.nextLine().toLowerCase();

        // Au lieu de retourner l'objet de la liste, on en crée un nouveau
        if (nom.equalsIgnoreCase("chevalier"))
            return new Chevalier();
        if (nom.equalsIgnoreCase("archere"))
            return new Archere();
        if (nom.equalsIgnoreCase("soigneur"))
            return new Soigneur();

        return null; // Ou gérer l'erreur si le nom est faux
    }

    private void initialisation() {
        System.out.println("\nG L A D I U S \n");
        System.out.println("Liste des personnages: \n");
        for (int i = 0; i < listePersonnages.size(); i++) {
            System.out.println(listePersonnages.get(i).toString() + "\n");
        }
        System.out.println("Joueur N°1\nChoix du personnage ");
        Personnage perso1 = choixPersonnage();
        System.out.println("Joueur N°2\nChoix du personnage");
        Personnage perso2 = choixPersonnage();
        arene = new Arene(perso1, perso2);
        sc.close();
        System.out.println("\nChoix validé, voici votre arène: \n");
        arene.getArene();
        arene.afficherJoueur();

    }

    private void infligerDegats(Personnage Defenseur, double degat){
        Defenseur.setHp(-degat);
    }

    public void star() {
        while (arene.getJoueur1().getHp() > 0 && arene.getJoueur2().getHp() > 0) {
            Boolean paradeJ1 = false;
            Boolean paradeJ2 = false;
            System.out.println("\nJoueur 1, à vous de jouer : " + arene.getJoueur1().toString() + "\n");
            System.out.println("Choix d'action : D : Déplacement, A : Attaque, P : Parade, R : Repos");
            sc = new Scanner(System.in);
            String action = sc.nextLine();
            switch (action.toLowerCase()) {
                case "d":
                    System.out.println("Saisir la case de destination (ex: A1, B3 etc): ");
                    String destination = sc.nextLine();
                    break;

                case "a":
                    System.out.println("Choix de l'attaque :\n Attaque de base : 'ALD' \nAttaque à distance : AL 3\n Attaque lourde : 'ALD");
                    String attaque = sc.nextLine();
                    if(paradeJ2){
                        System.out.println("Attaque parée, aucun dégât infligé");
                    } else {
                        infligerDegats(arene.getJoueur2(),arene.getJoueur1().choixAttaque(attaque));
                        System.out.println("Attaque réussie, " + arene.getJoueur2().getHp() + " points de vie restants");
                    }
                    break;

                case "p":
                    paradeJ1 = true;
                    arene.getJoueur1().setParade(-1);
                    break;

                case "r":
                    arene.getJoueur1().setRepos(-1);
                    arene.getJoueur1().setEnergie(20.0);
                    break;
            }
        }
    }
}

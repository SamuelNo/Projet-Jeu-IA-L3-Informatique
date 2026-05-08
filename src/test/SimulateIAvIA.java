package test;

import architecture.Arene;
import entite.*;
import ia.*;

public class SimulateIAvIA {
    public static void main(String[] args) {
        Personnage p1 = new Chevalier();
        Personnage p2 = new Chevalier();
        p1.setPosition(new Position(0,0));
        p2.setPosition(new Position(9,9));
        Arene arene = new Arene(p1, p2);

        Personnage joueurActif = p1;
        Personnage adversaire = p2;
        int turn = 0;
        int maxTurns = 100;

        System.out.println("--- Début simulation IA Moyenne vs IA Moyenne ---");
        arene.getArene();

        while (p1.estEnVie() && p2.estEnVie() && turn < maxTurns) {
            turn++;
            System.out.println("\n--- Tour " + turn + " - " + joueurActif.getNom() + " ---");

            Etat etatIA = new Etat(Etat.copieGrille(arene.getGrille()), Etat.JoueurEtat.fromPersonnage(joueurActif), Etat.JoueurEtat.fromPersonnage(adversaire));
            Coup coup = IAMoyenne.choisirCoup(etatIA, false);
            if (coup == null) {
                System.out.println("IA n'a trouvé aucun coup. Termine le tour.");
            } else {
                Position dest = coup.getDestination();
                if (dest != null) {
                    int dist = Math.abs(dest.getLigne() - joueurActif.getPosition().getLigne()) + Math.abs(dest.getColonne() - joueurActif.getPosition().getColonne());
                    System.out.println("Déplacement choisi vers (" + dest.getLigne() + "," + dest.getColonne() + ") distance=" + dist);
                    joueurActif.setPosition(dest);
                    arene.updateFullGrille();
                    int caseCible = arene.getGrille()[dest.getLigne()][dest.getColonne()];
                    if (caseCible == 3) {
                        joueurActif.setParade(1);
                        arene.getGrille()[dest.getLigne()][dest.getColonne()] = 0;
                        System.out.println("Récupéré bonus Parade");
                    } else if (caseCible == 4) {
                        joueurActif.setEnergie(20.0);
                        arene.getGrille()[dest.getLigne()][dest.getColonne()] = 0;
                        System.out.println("Récupéré bonus Energie");
                    }
                }

                switch (coup.getAction()) {
                    case ATTAQUE:
                        System.out.println("Action: ATTAQUE " + coup.getTypeAttaque());
                        try {
                            double hpAvant = adversaire.getHp();
                            joueurActif.attaquer(adversaire, coup.getTypeAttaque());
                            if (adversaire.getHp() < hpAvant) {
                                System.out.println("Attaque réussie. HP adversaire=" + adversaire.getHp());
                            }
                        } catch (Exception e) {
                            System.out.println("Erreur attaque: " + e.getMessage());
                        }
                        break;
                    case PARADE:
                        System.out.println("Action: PARADE");
                        try {
                            joueurActif.parader();
                            System.out.println("Parade activée. Energie=" + joueurActif.getEnergie());
                        } catch (Exception e) {
                            System.out.println("Erreur parade: " + e.getMessage());
                        }
                        break;
                    case REPOS:
                        System.out.println("Action: REPOS");
                        try {
                            joueurActif.seReposer();
                            System.out.println("Repos effectué. Energie=" + joueurActif.getEnergie());
                        } catch (Exception e) {
                            System.out.println("Erreur repos: " + e.getMessage());
                        }
                        break;
                    case TERMINER:
                        System.out.println("Action: TERMINER");
                        break;
                }
            }

            // afficher état
            System.out.println(joueurActif.getNom() + " - PV:" + joueurActif.getHp() + " EN:" + joueurActif.getEnergie() + " Pm:" + joueurActif.getPas() + " Parades:" + joueurActif.getParade());
            System.out.println(adversaire.getNom() + " - PV:" + adversaire.getHp() + " EN:" + adversaire.getEnergie() + " Parades:" + adversaire.getParade());

            // switch
            Personnage temp = joueurActif;
            joueurActif = adversaire;
            adversaire = temp;
        }

        System.out.println("--- Fin simulation après " + turn + " tours ---");
        if (!p1.estEnVie()) System.out.println("Joueur 2 gagne");
        else if (!p2.estEnVie()) System.out.println("Joueur 1 gagne");
        else System.out.println("Match nul / Limite tours atteinte");
    }
}

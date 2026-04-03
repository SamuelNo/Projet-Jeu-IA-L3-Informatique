package test;

import architecture.FenetreArene;
import architecture.Jeu;

public class TestGeneral {
    public static void main(String[] args) {
        Jeu j = new Jeu();
        j.start(); 

        FenetreArene fenetre = new FenetreArene(j.getArene());
    }
}
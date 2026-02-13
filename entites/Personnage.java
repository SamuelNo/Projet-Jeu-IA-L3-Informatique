package entites;
import java.util.ArrayList;
import java.util.List;

import attaque.Attaques;
import attaque.Attaques_Distance;
import attaque.Attaques_Legere;
import attaque.Attaques_Lourde;

public class Personnage {
    protected String nom;
    protected double hp;
    protected double energie;
    protected int pas;
    protected int nbparades;
    protected int nbrepos;
    protected Position position;
    protected List<Attaques> attaques;
    protected Actions action;
    // protected String statut;
    // Pour connaître le statut du personnage en jeu, genre "Etourdi", "En parade" etc etc

    public Personnage(String nom){
        this.nom = nom;
        this.hp = 100;
        this.energie = 50;
        this.pas = 4;
        this.nbparades = 3;
        this.nbrepos = 0;
        this.position = null;
        this.attaques = new ArrayList();
        this.action = new Actions();
        initialisationAttaque();
    }


    public Personnage(double hp, double energie, int pas, int nbparades, int nbrepos){
        this.hp = hp;
        this.energie = energie;
        this.pas = pas;
        this.nbparades = nbparades;
        this.nbrepos = nbrepos;
        this.attaques = new ArrayList<>();
        this.action = new Actions();
        this.position = null;
        initialisationAttaque();
    }

    public double getHp(){
        return this.hp;
    }

    public double getEnergie(){
        return this.energie;
    }

    public int getPas(){
        return this.pas;
    }

    public int getNbParades(){
        return this.nbparades;
    }

    public int getNbRepos(){
        return this.nbrepos;
    }
  
    
    public Position getPosition(){
        return position;
    }

    public void setPosition(Position position){
        this.position = position;
    }

    public boolean estEnVie(){
        if (this.hp <= 0){
            return false;
        }
        else {
            return true;
        }
    }

    public void initialisationAttaque(){
        attaques.add(new Attaques_Legere());
        attaques.add(new Attaques_Lourde());
        attaques.add(new Attaques_Distance());
    }


    public static void main(String[] args) {
        Personnage p1 = new Personnage("Legias");
        System.out.println(p1.attaques.get(1).getDegat());
        System.out.println(p1.estEnVie());
    }
}
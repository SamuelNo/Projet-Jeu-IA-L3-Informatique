package entites;
import java.util.ArrayList;
import java.util.List;

import attaques.Attaques;
import attaques.Attaques_Distance;
import attaques.Attaques_Legere;
import attaques.Attaques_Lourde;

public class Personnage {
    private String nom;
    private double hp;
    private double energie;
    private int pas;
    private int nbparades;
    private int nbrepos;
    private Position position;
    private List<Attaques> attaques;
    private Arme arme;
    private Actions action;
    // private String statut;
    // Pour connaître le statut du personnage en jeu, genre "Etourdi", "En parade" etc etc

    public Personnage(String nom, Arme arme){
        this.nom = nom;
        this.arme = arme;
        this.hp = 120;
        this.energie = 80;
        this.pas = 4;
        this.nbparades = 3;
        this.nbrepos = 0;
        this.position = null;
        this.attaques = new ArrayList();
        this.action = new Actions();
        initialisationAttaque();
    }



    public Personnage(String nom, Arme arme, double hp, double energie, int pas, int nbparades, int nbrepos){
        this.nom = nom;
        this.arme = arme;
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

    public void setHp(double supplement){
        this.hp += supplement;
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

    public String getNom(){
        return nom;
    }

    public Arme getArme(){
        return arme;
    }

    public List<Attaques> getAttaques(){
        return attaques;
    }

    public boolean estEnVie(){
        if (this.hp <= 0){
            return false;
        }
        else {
            return true;
        }
    }

    private void initialisationAttaque(){
        attaques.add(new Attaques_Legere());
        attaques.add(new Attaques_Lourde());
        attaques.add(new Attaques_Distance());
    }

    @Override 
    public String toString(){
        StringBuffer sb = new StringBuffer("");
        sb.append("Nom: "+nom+" -> ");
        sb.append("Arme: "+ arme.getNom()+", ");
        for(int i = 0; i<attaques.size();i++){
            sb.append(attaques.get(i).toString()+" ; ");
        }
        sb.append(" PV : "+hp);

        return sb.toString();
    }
}
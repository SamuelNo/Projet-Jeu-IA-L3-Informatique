package entite;

public class Arme {
    
    private String nom;
    private int supplement;

    public Arme(String nom, int supplement){
        this.nom = nom;
        this.supplement = supplement;

    }

    public String getNom(){
        return nom;
    }

    public int getSupplement(){
        return supplement;
    }
}

package entite;

/**
 * Représente une arme et son bonus associé.
 */
public class Arme {
    
    private String nom;
    private int supplement;

    /**
     * Crée une arme.
     * @param nom nom de l'arme
     * @param supplement bonus appliqué selon la classe/attaque
     */
    public Arme(String nom, int supplement){
        this.nom = nom;
        this.supplement = supplement;

    }

    /**
     * Retourne le nom de l'arme.
     * @return nom de l'arme
     */
    public String getNom(){
        return nom;
    }

    /**
     * Retourne le bonus de l'arme.
     * @return valeur du bonus
     */
    public int getSupplement(){
        return supplement;
    }
}

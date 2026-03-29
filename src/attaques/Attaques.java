package attaques;

/**
 * Représente une attaque disponible pour un personnage.
 */
public class Attaques {

    private String type_attaque;
    private double degat;
    private int portee;
    
    /**
     * Crée une attaque avec son type, ses dégâts et sa portée.
     * @param type_attaque code de l'attaque
     * @param degat dégâts infligés
     * @param portee portée maximale
     */
    public Attaques(String type_attaque, double degat, int portee){
        this.type_attaque = type_attaque;
        this.degat = degat;
        this.portee = portee;
    }

    /**
     * Retourne les dégâts de l'attaque.
     * @return dégâts actuels
     */
    public double getDegat(){
        return degat;
    }

    /**
     * Augmente les dégâts de l'attaque.
     * @param supplement bonus de dégâts
     */
    public void setDegat(double supplement){
        this.degat += supplement;
    }

    /**
     * Retourne le type de l'attaque.
     * @return type d'attaque
     */
    public String getType_attaque(){
        return type_attaque;
    }

    /**
     * Retourne la portée maximale de l'attaque.
     * @return portée de l'attaque
     */
    public  int getPortee(){
        return portee;
    }

    /**
     * Retourne une représentation textuelle de l'attaque.
     * @return description de l'attaque
     */
    @Override
    public String toString(){
        return type_attaque+" : "+degat+" de dégats, "+"portée : "+portee;
    }
}
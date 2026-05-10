package entite;

/**
 * Classe Soigneur — spécialisation de `Personnage`.
 * Convertit le supplément d'arme en points de vie supplémentaires.
 */
public class Soigneur extends Personnage{
    
    /**
     * Construit un `Soigneur` et applique son bonus de PV.
     */
    public Soigneur(){
        super("Soigneur", new Arme("Batôn magique",30));
        ajoutSupplement();
    }

    /**
     * Convertit le supplément d'arme en PV initiaux.
     * Méthode privée appelée lors de l'initialisation.
     */
    private void ajoutSupplement(){
       // Ici le bonus n'affecte pas les dégâts: il est converti en survivabilité.
       super.setHp((double)(super.getArme().getSupplement()));
    }
}

package entite;

/**
 * Classe Soigneur: bonus converti en points de vie supplémentaires.
 */
public class Soigneur extends Personnage{
    
    /**
     * Crée un soigneur puis applique son bonus spécifique.
     */
    public Soigneur(){
        super("Soigneur", new Arme("Batôn magique",30));
        ajoutSupplement();
    }

    /**
     * Le soigneur utilise son supplément d'arme pour augmenter ses PV de départ.
     */
    private void ajoutSupplement(){
       // Ici le bonus n'affecte pas les dégâts: il est converti en survivabilité.
       super.setHp((double)(super.getArme().getSupplement()));
    }
}

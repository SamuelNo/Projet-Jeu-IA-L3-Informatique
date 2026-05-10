package entite;

import attaques.Attaques;
import attaques.Attaques_Distance;

/**
 * Classe Archère — spécialisation de `Personnage`.
 * Donne un bonus de dégâts aux attaques à distance via l'arme "Arc".
 */
public class Archere extends Personnage{
    
    /**
     * Construit une `Archere` avec l'arme appropriée et applique son bonus.
     */
    public Archere(){
        super("Archère", new Arme("Arc",30));
        ajoutSupplement();
    }

    /**
     * Applique le supplément d'arme uniquement sur l'attaque à distance.
     * Méthode interne utilisée lors de l'initialisation.
     */
    private void ajoutSupplement(){
        for(Attaques a : super.getAttaques()){
            // Seule l'attaque de type distance reçoit le bonus spécifique de l'arc.
            if(a instanceof Attaques_Distance){
                a.setDegat(super.getArme().getSupplement());
            }
        }
    }
}

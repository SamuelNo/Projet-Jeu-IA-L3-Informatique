package entite;

import attaques.Attaques;
import attaques.Attaques_Distance;

/**
 * Classe Archère: bonus appliqué sur les attaques à distance.
 */
public class Archere extends Personnage{
    
    /**
     * Crée une archère avec un arc et applique les bonus d'arme.
     */
    public Archere(){
        super("Archère", new Arme("Arc",30));
        ajoutSupplement();
    }

    /**
     * Applique le supplément d'arme uniquement sur l'attaque à distance.
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

package entite;

import attaques.*;

/**
 * Classe Chevalier: bonus appliqué sur les attaques lourdes.
 */
public class Chevalier extends Personnage{
    
    /**
     * Crée un chevalier avec une épée et applique les bonus d'arme.
     */
    public Chevalier(){
        super("Chevalier", new Arme("Épée",20));
        ajoutSupplement();
    }

    /**
     * Applique le supplément d'arme uniquement sur l'attaque lourde.
     */
    private void ajoutSupplement(){
        for(Attaques a : super.getAttaques()){
            // Le chevalier renforce sa frappe lourde, pas ses autres attaques.
            if(a instanceof Attaques_Lourde){
                a.setDegat(super.getArme().getSupplement());
            }
        }
    }
}


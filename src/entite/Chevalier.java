package entite;

import attaques.*;

/**
 * Classe Chevalier — spécialisation de `Personnage`.
 * Le `Chevalier` reçoit un bonus sur ses attaques lourdes via l'arme "Épée".
 */
public class Chevalier extends Personnage{
    
    /**
     * Construit un `Chevalier` et applique son bonus d'arme.
     */
    public Chevalier(){
        super("Chevalier", new Arme("Épée",20));
        ajoutSupplement();
    }

    /**
     * Applique le supplément d'arme uniquement sur l'attaque lourde.
     * Méthode privée invoquée à l'initialisation.
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


package entites;

import attaques.*;

public class Chevalier extends Personnage{
    
    public Chevalier(){
        super("Chevalier", new Arme("Épée",20));
        ajoutSupplement();
    }

    private void ajoutSupplement(){
        for(Attaques a : super.getAttaques()){
            if(a instanceof Attaques_Lourde){
                a.setDegat(super.getArme().getSupplement());
            }
        }
    }
}


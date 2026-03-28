package entite;

import attaques.Attaques;
import attaques.Attaques_Distance;


public class Archere extends Personnage{
    
    public Archere(){
        super("Archère", new Arme("Arc",30));
        ajoutSupplement();
    }

    private void ajoutSupplement(){
        for(Attaques a : super.getAttaques()){
            if(a instanceof Attaques_Distance){
                a.setDegat(super.getArme().getSupplement());
            }
        }
    }
}

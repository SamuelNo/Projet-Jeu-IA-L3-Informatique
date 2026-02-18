package entites;


public class Soigneur extends Personnage{
    
    public Soigneur(){
        super("Soigneur", new Arme("Batôn magique",30));
        ajoutSupplement();
    }

    private void ajoutSupplement(){
       super.setHp((double)(super.getArme().getSupplement()));
    }
}

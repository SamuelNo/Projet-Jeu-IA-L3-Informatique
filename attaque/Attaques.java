package attaque;

public abstract class Attaques {

    protected String type_attaque;
    protected int degat;
    protected  int portee;
    

    public Attaques(String type_attaque, int degat, int portee){
        this.type_attaque = type_attaque;
        this.degat = degat;
        this.portee = portee;
    }

    public abstract int getDegat();

    public abstract String getType_attaque();

    public abstract int getPortee();

    public String toString(){
        return type_attaque+", "+degat+"de dégats, "+"portée: "+portee;
    }
}
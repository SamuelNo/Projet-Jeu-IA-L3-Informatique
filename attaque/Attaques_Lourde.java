package attaque;

public class Attaques_Lourde extends Attaques {

    public Attaques_Lourde(){
        super("ALD",30,1);
    }

    public  int getDegat(){
        return degat;
    }

    public  String getType_attaque(){
        return type_attaque;
    }

    public int getPortee(){
        return portee;
    }
}   
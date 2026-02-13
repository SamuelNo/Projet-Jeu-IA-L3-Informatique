package attaque;

public class Attaques_Legere extends Attaques{

    public Attaques_Legere(){
        super("AL",15,1);
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
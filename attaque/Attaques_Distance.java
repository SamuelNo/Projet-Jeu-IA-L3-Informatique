package attaque;

public class Attaques_Distance extends Attaques {
    public Attaques_Distance(){
        super("AD",20,3);
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


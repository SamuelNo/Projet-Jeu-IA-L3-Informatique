package attaques;

public class Attaques {

    private String type_attaque;
    private int degat;
    private int portee;
    

    public Attaques(String type_attaque, int degat, int portee){
        this.type_attaque = type_attaque;
        this.degat = degat;
        this.portee = portee;
    }

    public int getDegat(){
        return degat;
    }

    public void setDegat(int supplement){
        this.degat += supplement;
    }

    public String getType_attaque(){
        return type_attaque;
    }

    public  int getPortee(){
        return portee;
    }

    public String toString(){
        return type_attaque+" : "+degat+" de dégats, "+"portée : "+portee;
    }
}
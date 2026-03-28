package attaques;

public class Attaques {

    private String type_attaque;
    private double degat;
    private int portee;
    

    public Attaques(String type_attaque, double degat, int portee){
        this.type_attaque = type_attaque;
        this.degat = degat;
        this.portee = portee;
    }

    public double getDegat(){
        return degat;
    }

    public void setDegat(double supplement){
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
package entites;
public class Actions {
    private String attaque;
    private String parade;
    private String repos;

    public Actions(){
        this.attaque = "Attaque";
        this.parade = "Parade";
        this.repos = "Repos";
    }

    public String getAttaque(){
        return attaque;
    }

    public String getParade(){
        return parade;
    }

    public String getRepos(){
        return repos;
    }
}



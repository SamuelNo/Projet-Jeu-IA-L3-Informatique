package entite;
import java.util.ArrayList;
import java.util.List;
import exception.*;
import attaques.*;


/**
 * Classe représentant un personnage jouable dans le jeu.
 * Gère les attributs du personnage (HP, énergie, position, etc.)
 * et les actions associées (attaque, parade, repos, mouvement).
 */
public class Personnage {
    private String nom;
    private double hp;
    private double energie;
    private int pas;
    private int nbparades;
    private boolean enParade; // 
    private int nbrepos;
    private Position position;
    private List<Attaques> attaques;
    private Arme arme;
    // private String statut;
    // Pour connaître le statut du personnage en jeu, genre "Etourdi", "En parade" etc etc

    /**
     * Constructeur par défaut d'un personnage avec les stats de base.
     * @param nom Le nom du personnage
     * @param arme L'arme équipée du personnage
     */
    public Personnage(String nom, Arme arme){
        this.nom = nom;
        this.arme = arme;
        this.hp = 120.0;
        this.energie = 80.0;
        this.pas = 4;
        this.nbparades = 3;
        this.enParade = false;
        this.nbrepos = 1;
        this.position = null;
        this.attaques = new ArrayList<>();
        initialisationAttaque();
    }



    /**
     * Constructeur personnalisé d'un personnage avec des stats personnalisées.
     * @param nom Le nom du personnage
     * @param arme L'arme équipée du personnage
     * @param hp Les points de vie initiaux
     * @param energie Les points d'énergie initiaux
     * @param pas Le nombre de pas de mouvement
     * @param nbparades Le nombre de parades disponibles
     * @param enParade Le statut de parade du personnage
     * @param nbrepos Le nombre de repos
     */
    public Personnage(String nom, Arme arme, double hp, double energie, int pas, int nbparades, int nbrepos){
        this.nom = nom;
        this.arme = arme;
        this.hp = hp;
        this.energie = energie;
        this.pas = pas;
        this.nbparades = nbparades;
        this.enParade = false;
        this.nbrepos = nbrepos;
        this.position = null;
        this.attaques = new ArrayList<>();
        initialisationAttaque();
    }

    /**
     * Retourne les points de vie actuels du personnage.
     * @return Les points de vie
     */
    public double getHp(){
        return hp;
    }

    /**
     * Modifie les points de vie en ajoutant un supplément.
     * @param supplement La valeur à ajouter aux PV (peut être négatif pour infliger des dégâts)
     */
    public void setHp(double supplement){
        this.hp += supplement;
    }

    /**
     * Retourne les points d'énergie actuels du personnage.
     * @return Les points d'énergie
     */
    public double getEnergie(){
        return energie;
    }

    /**
     * Modifie les points d'énergie en ajoutant un supplément.
     * @param supplement La valeur à ajouter à l'énergie
     */
    public void setEnergie(double supplement){
        this.energie += supplement;
    }

    public int getParade() {
        return this.nbparades;
    }

    /**
     * Augmente le nombre de repos effectués.
     * @param supplement Le nombre de repos à ajouter
     */
    public void setNbRepos(int supplement){
        this.nbrepos += supplement;
    }

   

    /**
     * Retourne le nombre de pas de mouvement disponibles.
     * @return Le nombre de pas
     */
    public int getPas(){
        return pas;
    }

    /**
     * Retourne le nombre de parades disponibles.
     * @return Le nombre de parades
     */
    public int getNbParades(){
        return nbparades;
    }

    /**
     * Modifie le nombre de parades en ajoutant un supplément.
     * @param supplement La valeur à ajouter au nombre de parades
     */
    public void setParade(int supplement){
        this.nbparades += supplement;
    }

    /**
     * Retourne le statut de parade du personnage.
     * @return true si le personnage est en parade, false sinon
     */    
    public boolean isEnParade() {
        return enParade;
    }
    
    /**
     * Modifie le statut de parade du personnage.
     * @param enParade Le nouveau statut de parade
     */
    public void setEnParade(boolean enParade) {
        this.enParade = enParade;
    }



    /**
     * Retourne le nombre total de repos effectués.
     * @return Le nombre de repos
     */
    public int getNbRepos(){
        return nbrepos;
    }

    /**
     * Retourne la position actuelle du personnage sur l'arène.
     * @return La position du personnage
     */
    public Position getPosition(){
        return position;
    }

    /**
     * Définit la position du personnage sur l'arène.
     * @param position La nouvelle position
     */
    public void setPosition(Position position){
        this.position = position;
    }

    /**
     * Retourne le nom du personnage.
     * @return Le nom
     */
    public String getNom(){
        return nom;
    }

    /**
     * Retourne l'arme équipée du personnage.
     * @return L'arme
     */
    public Arme getArme(){
        return arme;
    }

    /**
     * Retourne la liste des attaques disponibles du personnage.
     * @return La liste des attaques
     */
    public List<Attaques> getAttaques(){
        return attaques;
    }

   

        /**
        * Effectue une attaque sur une cible en utilisant un type d'attaque spécifique.
        * Vérifie les conditions d'attaque (type d'attaque valide, énergie suffisante, parade de la cible).
        * @param cible Le personnage cible de l'attaque
        * @param typeAttaque Le type d'attaque à utiliser
        * @throws IllegalAttackException Si le type d'attaque est inconnu
        * @throws IllegalEnergieException Si l'énergie est insuffisante pour effectuer l'attaque
        */
    public void attaquer(Personnage cible, String typeAttaque) throws IllegalAttackException, IllegalEnergieException , IllegalParadeException{
        double degat = 0;
        for (int i = 0; i < attaques.size(); i++){
            // On parcourt la liste pour trouver exactement le type d'attaque demandé.
            if (attaques.get(i).getType_attaque().equalsIgnoreCase(typeAttaque)){
                degat = attaques.get(i).getDegat();
            }
        }
        if (degat == 0){
            throw new IllegalAttackException("Attention! Attaque inconnue");
        }
        if (energie < 0 || energie < degat){
            throw new IllegalEnergieException("Attention! Énergie insuffisante");
        }
        if(cible.isEnParade()){
            System.out.println("\nAttaque parée, aucun dégât subi\n");
            cible.setEnParade(false); // La parade ne dure que pour une attaque, elle est désactivée après avoir été utilisée.
        } else {
            cible.setHp(-degat);
            System.out.println("\nAttaque réussie, " + cible.getHp() + " points de vie restants\n");
        }
        energie -= degat;
        
    }

    /**
     * Active l'état de parade pour le tour en cours.
     * @throws IllegalParadeException si le quota de parades est épuisé
     */
    public void parader() throws IllegalParadeException{
        if (nbparades <= 0){
            throw new IllegalParadeException("Attention! Nombre de parades épuisé");
        }
        System.out.println("\nParade activée pour le prochain tour\n");
        setEnParade(true);
        setParade(-1);
    }

    /**
     * Déclenche un repos pour regagner de l'énergie.
     * @throws IllegalReposException si le quota de repos est épuisé
     */
    public void seReposer() throws IllegalReposException{
        System.out.println("\nRepos activé, énergie régénérée pour le prochain tour\n");
        setEnergie(20.0); // Exemple de régénération d'énergie
    }

    /**
     * Vérifie si le personnage est encore en vie.
     * @return true si les points de vie sont supérieurs à 0, false sinon
     */
    public boolean estEnVie(){
        if (this.hp <= 0){
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Initialise la liste des attaques disponibles du personnage.
     */
    private void initialisationAttaque(){
        attaques.add(new Attaques_Legere());
        attaques.add(new Attaques_Lourde());
        attaques.add(new Attaques_Distance());
    }

    /**
     * Retourne les statistiques essentielles du joueur prêtes à afficher.
     * @return statistiques formatées sur plusieurs lignes
     */
    public String statsJoueurString(){
        StringBuffer sb = new StringBuffer("");
        sb.append("PV : " + hp + "\n");
        sb.append("Energie : " + energie + "\n");
        sb.append("Parades restantes : " + nbparades + "\n");
        sb.append("Repos : illimité");
        return sb.toString();
    }

    /**
     * Retourne une représentation textuelle du personnage.
     * @return Une chaîne contenant le nom, l'arme, les attaques, les PV et l'énergie
     */
    public String infoPersoString(){
        StringBuffer sb = new StringBuffer("");
        sb.append("Nom: "+nom+" -> ");
        sb.append("Arme: "+ arme.getNom()+", ");
        for(int i = 0; i<attaques.size();i++){
            // Concatène dynamiquement la liste des attaques disponibles.
            sb.append(attaques.get(i).toString()+" ; ");
        }
        sb.append(" PV : "+hp);
        sb.append(", Energie : "+energie);

        return sb.toString();
    }
}
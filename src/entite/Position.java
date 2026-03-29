package entite;

/**
 * Représente une position (ligne, colonne) dans l'arène.
 */
public class Position {
    
    private int ligne;
    private int colonne;

    /**
     * Crée une position.
     * @param ligne indice de ligne
     * @param colonne indice de colonne
     */
    public Position(int ligne, int colonne){
        this.ligne = ligne;
        this.colonne = colonne;
    }

    /**
     * Met à jour la position.
     * @param ligne nouvelle ligne
     * @param colonne nouvelle colonne
     */
    public void setPostion(int ligne, int colonne){
        this.ligne = ligne;
        this.colonne = colonne;
    }

    /**
     * Retourne la ligne.
     * @return ligne courante
     */
    public int getLigne(){
        return ligne;
    }

    /**
     * Retourne la colonne.
     * @return colonne courante
     */
    public int getColonne(){
        return colonne;
    }

    /**
     * Retourne une description de la position.
     * @return position sous forme de texte
     */
    public String donnerPosition(){
        // Format centralisé pour simplifier les affichages de coordonnées dans le jeu.
        String pos = "position en ("+ligne+","+colonne+")";
        return pos;
    }
}

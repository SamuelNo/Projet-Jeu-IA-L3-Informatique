package exception;

// Action du joueur interdite: exemple(Regarder les autres exceptions fille)

/**
 * Exception mère pour toutes les actions interdites du jeu.
 */
public class IllegalActionException extends Exception{

    /**
     * Crée une exception d'action illégale.
     * @param message détail de l'erreur
     */
    public IllegalActionException(String message){
        super(message);
    }

}
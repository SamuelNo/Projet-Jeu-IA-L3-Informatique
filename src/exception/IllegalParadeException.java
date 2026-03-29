package exception;

// Parade du joueur interdit: exemple(Utilisation d'une parade alors que le quota de parade est déja épuisé)

/**
 * Exception levée quand une parade n'est plus disponible.
 */
public class IllegalParadeException extends IllegalActionException {

    /**
     * Crée une exception de parade invalide.
     * @param message détail de l'erreur
     */
    public IllegalParadeException(String message){
        super(message);
    }
}

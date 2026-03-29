package exception;

// Utilisation d'un repos du joueur interdit: exemple(Utilisation d'un repos alors que le quota de repos est épuisé)

/**
 * Exception levée quand un repos supplémentaire est interdit.
 */
public class IllegalReposException extends IllegalActionException{

    /**
     * Crée une exception de repos invalide.
     * @param message détail de l'erreur
     */
    public IllegalReposException(String message){
        super(message);
    }
}

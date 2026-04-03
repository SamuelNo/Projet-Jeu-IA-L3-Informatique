package exception;

// Déplacement du joueur interdit: exemple(Dépassement du nombre de pas possible)

/**
 * Exception levée quand un déplacement est interdit.
 */
public class IllegalMoveException extends IllegalActionException{
    
    /**
     * Crée une exception de déplacement invalide.
     * @param message détail de l'erreur
     */
    public IllegalMoveException(String message){
        super(message);
    }
}
package exception;

// Attaque du joueur interdite: exemple(Distance de l'attaque non respectée)

/**
 * Exception levée quand une attaque n'est pas autorisée.
 */
public class IllegalAttackException extends IllegalActionException{

    /**
     * Crée une exception d'attaque invalide.
     * @param message détail de l'erreur
     */
    public IllegalAttackException(String message){
        super(message);
    }
}

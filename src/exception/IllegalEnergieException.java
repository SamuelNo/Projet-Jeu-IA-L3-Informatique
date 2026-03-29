package exception;

// Dépassement d'énergie du joueur interdit: exemple(Dépassement du quota d'énergie après une attaque)

/**
 * Exception levée quand l'énergie est insuffisante.
 */
public class IllegalEnergieException extends IllegalActionException {

    /**
     * Crée une exception d'énergie insuffisante.
     * @param message détail de l'erreur
     */
    public IllegalEnergieException(String message){
        super(message);
    }
}

package exception;

// Dépassement d'énergie du joueur interdit: exemple(Dépassement du quota d'énergie après une attaque)

public class IllegalEnergieException extends IllegalActionException {

    public IllegalEnergieException(){
        super("Attention! Énergie insuffisante");
    }
}

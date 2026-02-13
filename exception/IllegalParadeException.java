package exception;

// Parade du joueur interdit: exemple(Utilisation d'une parade alors que le quota de parade est déja épuisé)

public class IllegalParadeException extends IllegalActionException {

    public IllegalParadeException(){
        super("Attention! Nombre de parade déja épuisé");
    }
}

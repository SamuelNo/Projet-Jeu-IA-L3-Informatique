package exception;

// Utilisation d'un repos du joueur interdit: exemple(Utilisation d'un repos alors que le quota de repos est épuisé)

public class IllegalReposException extends IllegalActionException{

    public IllegalReposException(){
        super("Attention! Nombre de repos épuisé");
    }
}

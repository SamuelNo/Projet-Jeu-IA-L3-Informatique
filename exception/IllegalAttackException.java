package exception;

// Attaque du joueur interdite: exemple(Distance de l'attaque non respectée)

public class IllegalAttackException extends IllegalActionException{

    public IllegalAttackException(){
        super("Attention! La distance de l'attaque n'est pas respectée");
    }
}

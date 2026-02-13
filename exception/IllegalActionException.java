package exception;

// Action du joueur interdite: exemple(Regarder les autres exceptions fille)

public class IllegalActionException extends Exception{

    public IllegalActionException(String message){
        super(message);
    }

}
package ia;

import entite.Position;

/**
 * Représente un coup complet d'un tour IA.
 * Un coup contient un déplacement puis une action.
 */
public class Coup {
    public enum TypeAction {
        ATTAQUE,
        PARADE,
        REPOS,
        TERMINER
    }

    private final Position destination;
    private final TypeAction action;
    private final String typeAttaque;

    public Coup(Position destination, TypeAction action, String typeAttaque) {
        this.destination = destination;
        this.action = action;
        this.typeAttaque = typeAttaque;
    }

    public Position getDestination() {
        return destination;
    }

    public TypeAction getAction() {
        return action;
    }

    public String getTypeAttaque() {
        return typeAttaque;
    }
}

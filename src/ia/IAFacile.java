package ia;

import java.util.List;
import java.util.Random;

/**
 * IA Facile - Stratégie aléatoire avec quelques heuristiques simples.
 */
public class IAFacile {
    private static final Random rand = new Random();
    
    /**
     * Choisit un coup aléatoire parmi les coups légaux.
     */
    public static Coup choisirCoup(Etat etat) {
        List<Coup> coupsLegaux = MoteurCoups.genererCoupsLegaux(etat);
        
        if (coupsLegaux.isEmpty()) {
            return new Coup(null, Coup.TypeAction.TERMINER, null);
        }
        
        // Stratégie très simple : choix aléatoire
        return coupsLegaux.get(rand.nextInt(coupsLegaux.size()));
    }
}

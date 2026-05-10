package ia;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Classe utilitaire pour collecter des statistiques sur les performances de l'IA.
 * Permet de compter le nombre de noeuds visités et le temps total passé à choisir des coups.
 */
public class IAStats {
    private static final AtomicLong nodesVisited = new AtomicLong(0);
    private static final AtomicLong totalMoveTimeNs = new AtomicLong(0);
    private static final AtomicLong moveCount = new AtomicLong(0);

    // Réinitialiser les statistiques avant un nouveau tournoi ou une nouvelle série de tests
    public static void reset() {
        nodesVisited.set(0);
        totalMoveTimeNs.set(0);
        moveCount.set(0);
    }

    // Incrémenter le nombre de noeuds visités
    public static void incNode() {
        nodesVisited.incrementAndGet();
    }

    // Ajouter du temps en nanosecondes pour un coup
    public static void addNodes(long n) { nodesVisited.addAndGet(n); }

    // Getters pour les statistiques
    public static long getNodes() { return nodesVisited.get(); }

    public static void addMoveTimeNs(long ns) { totalMoveTimeNs.addAndGet(ns); moveCount.incrementAndGet(); }

    public static long getTotalMoveTimeNs() { return totalMoveTimeNs.get(); }

    public static long getMoveCount() { return moveCount.get(); }
}

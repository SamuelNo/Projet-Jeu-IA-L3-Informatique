package ia;

import entite.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Générateur/simulateur de coups pour les futures IA Minimax/Alpha-Bêta.
 */
public class MoteurCoups {

    public static List<Coup> genererCoupsLegaux(Etat s) {
        List<Coup> coups = new ArrayList<>();
        Etat.JoueurEtat actif = s.getJoueurActif();
        Etat.JoueurEtat adversaire = s.getAdversaire();

        List<Position> destinations = genererDestinationsLegales(s, actif, adversaire);
        for (Position dest : destinations) {
            // Actions toujours disponibles sous conditions.
            coups.add(new Coup(dest, Coup.TypeAction.TERMINER, null));
            if (actif.getNbParades() > 0) {
                coups.add(new Coup(dest, Coup.TypeAction.PARADE, null));
            }
            coups.add(new Coup(dest, Coup.TypeAction.REPOS, null));

            int distApresDeplacement = distanceManhattan(dest, adversaire.getPosition());
            for (Etat.AttaqueInfo attaque : actif.getAttaques()) {
                if (actif.getEnergie() >= attaque.getDegat() && distApresDeplacement <= attaque.getPortee()) {
                    coups.add(new Coup(dest, Coup.TypeAction.ATTAQUE, attaque.getType()));
                }
            }
        }

        return coups;
    }

    public static Etat simulerCoup(Etat s, Coup c) {
        Etat suivant = new Etat(s);
        Etat.JoueurEtat actif = suivant.getJoueurActif();
        Etat.JoueurEtat adversaire = suivant.getAdversaire();

        appliquerDeplacementEtBonus(suivant, actif, adversaire, c.getDestination());

        switch (c.getAction()) {
            case PARADE:
                if (actif.getNbParades() > 0) {
                    actif.setEnParade(true);
                    actif.setNbParades(actif.getNbParades() - 1);
                }
                break;
            case REPOS:
                actif.setEnergie(actif.getEnergie() + 20.0);
                break;
            case ATTAQUE:
                appliquerAttaque(actif, adversaire, c.getTypeAttaque());
                break;
            case TERMINER:
                break;
        }

        suivant.changerJoueurActif();
        return suivant;
    }

    private static List<Position> genererDestinationsLegales(Etat s, Etat.JoueurEtat actif, Etat.JoueurEtat adversaire) {
        List<Position> positions = new ArrayList<>();
        int[][] grille = s.getGrille();
        int taille = grille.length;
        Position origine = actif.getPosition();

        for (int l = 0; l < taille; l++) {
            for (int c = 0; c < taille; c++) {
                int dist = Math.abs(l - origine.getLigne()) + Math.abs(c - origine.getColonne());
                if (dist > actif.getPas()) {
                    continue;
                }
                if (l == adversaire.getPosition().getLigne() && c == adversaire.getPosition().getColonne()) {
                    continue;
                }
                if (grille[l][c] == -1) {
                    continue;
                }
                positions.add(new Position(l, c));
            }
        }
        return positions;
    }

    private static void appliquerDeplacementEtBonus(Etat etat, Etat.JoueurEtat actif, Etat.JoueurEtat adversaire, Position destination) {
        int[][] grille = etat.getGrille();
        Position ancienne = actif.getPosition();

        // Nettoyage de l'ancienne case si c'était une case joueur.
        if (grille[ancienne.getLigne()][ancienne.getColonne()] == 1 || grille[ancienne.getLigne()][ancienne.getColonne()] == 2) {
            grille[ancienne.getLigne()][ancienne.getColonne()] = 0;
        }

        int caseCible = grille[destination.getLigne()][destination.getColonne()];
        if (caseCible == 3) {
            actif.setNbParades(actif.getNbParades() + 1);
            grille[destination.getLigne()][destination.getColonne()] = 0;
        } else if (caseCible == 4) {
            actif.setEnergie(actif.getEnergie() + 20.0);
            grille[destination.getLigne()][destination.getColonne()] = 0;
        }

        actif.setPosition(new Position(destination.getLigne(), destination.getColonne()));
        grille[actif.getPosition().getLigne()][actif.getPosition().getColonne()] = 1;
        grille[adversaire.getPosition().getLigne()][adversaire.getPosition().getColonne()] = 2;
    }

    private static void appliquerAttaque(Etat.JoueurEtat actif, Etat.JoueurEtat adversaire, String typeAttaque) {
        Etat.AttaqueInfo attaque = null;
        for (Etat.AttaqueInfo a : actif.getAttaques()) {
            if (a.getType().equalsIgnoreCase(typeAttaque)) {
                attaque = a;
                break;
            }
        }
        if (attaque == null) {
            return;
        }

        int distance = distanceManhattan(actif.getPosition(), adversaire.getPosition());
        if (distance > attaque.getPortee() || actif.getEnergie() < attaque.getDegat()) {
            return;
        }

        if (adversaire.isEnParade()) {
            adversaire.setEnParade(false);
        } else {
            adversaire.setHp(adversaire.getHp() - attaque.getDegat());
        }
        actif.setEnergie(actif.getEnergie() - attaque.getDegat());
    }

    private static int distanceManhattan(Position p1, Position p2) {
        return Math.abs(p1.getLigne() - p2.getLigne()) + Math.abs(p1.getColonne() - p2.getColonne());
    }
}

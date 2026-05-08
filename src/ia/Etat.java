package ia;

import attaques.Attaques;
import entite.Personnage;
import entite.Position;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente l'état du jeu à un instant T pour les simulations de l'IA.
 */
public class Etat {
    
    private int[][] grille;
    private JoueurEtat joueurActif;
    private JoueurEtat adversaire;

    /**
     * Constructeur standard.
     */
    public Etat(int[][] grille, JoueurEtat joueurActif, JoueurEtat adversaire) {
        this.grille = copierGrille(grille);
        this.joueurActif = joueurActif;
        this.adversaire = adversaire;
    }

    /**
     * Constructeur de copie (Indispensable pour le MoteurCoups et Minimax).
     */
    public Etat(Etat autre) {
        this.grille = copierGrille(autre.grille);
        this.joueurActif = new JoueurEtat(autre.joueurActif);
        this.adversaire = new JoueurEtat(autre.adversaire);
    }

    // =========================================================================
    // 🧠 HEURISTIQUE DE L'IA FACILE
    // =========================================================================
    
    public int getScoreHeuristique() {
        // 1. ÉTATS TERMINAUX (Poids de Victoire Absolu)
        if (adversaire.getHp() <= 0) return 1000000; 
        if (joueurActif.getHp() <= 0) return -1000000;

        double score = 0;

        // 2. ANALYSE DES POINTS DE VIE
        score += (joueurActif.getHp() - adversaire.getHp()) * 20;

        // 3. DISTANCE DE MANHATTAN : pénalité renforcée pour forcer l'approche
        int dist = Math.abs(joueurActif.getPosition().getLigne() - adversaire.getPosition().getLigne())
                 + Math.abs(joueurActif.getPosition().getColonne() - adversaire.getPosition().getColonne());
        score -= dist * 5;

        // 4. BONUS SI À PORTÉE D'ATTAQUE : récompense le contact immédiat
        boolean peutAttaquer = false;
        for (AttaqueInfo a : joueurActif.getAttaques()) {
            if (joueurActif.getEnergie() >= a.getDegat() && dist <= a.getPortee()) {
                score += 60;
                peutAttaquer = true;
                break;
            }
        }

        // 5. GESTION DYNAMIQUE DE L'ÉNERGIE
        if (joueurActif.getEnergie() < 10) {
            score += joueurActif.getEnergie() * 5.0; 
        } else {
            score += joueurActif.getEnergie() * 0.1; 
        }
        
        // 6. GESTION DE LA PARADE
        if (joueurActif.isEnParade() && dist <= 3) {
            score += 15;
        } else if (joueurActif.isEnParade() && dist > 3) {
            score -= 5;
        } else {
            score += joueurActif.getNbParades() * 1.0;
        }

        // 7. PÉNALITÉ FORTE POUR L'INACTION : évite les tours passés indéfiniment
        if (joueurActif.getEnergie() > 15 && !joueurActif.isEnParade() && !peutAttaquer) {
            score -= 40;
        }
        
        return (int) score;
    }

    // =========================================================================
    // ⚙️ MÉTHODES DE GESTION DE L'ÉTAT
    // =========================================================================

    public boolean estTerminal() {
        return joueurActif.getHp() <= 0 || adversaire.getHp() <= 0;
    }

    public void changerJoueurActif() {
        JoueurEtat temp = joueurActif;
        joueurActif = adversaire;
        adversaire = temp;
        
        // CORRECTION VITALE : La parade ne dure qu'un tour !
        joueurActif.setEnParade(false);
    }

    private int[][] copierGrille(int[][] source) {
        int[][] copie = new int[source.length][source[0].length];
        for (int i = 0; i < source.length; i++) {
            System.arraycopy(source[i], 0, copie[i], 0, source[i].length);
        }
        return copie;
    }

    /**
     * Méthode utilitaire publique statique pour copier une grille (compatibilité).
     */
    public static int[][] copieGrille(int[][] source) {
        int[][] copie = new int[source.length][source[0].length];
        for (int i = 0; i < source.length; i++) {
            System.arraycopy(source[i], 0, copie[i], 0, source[i].length);
        }
        return copie;
    }

    // --- GETTERS & SETTERS ---
    public int[][] getGrille() { return grille; }
    public JoueurEtat getJoueurActif() { return joueurActif; }
    public JoueurEtat getAdversaire() { return adversaire; }

    // =========================================================================
    // 🧠 HEURISTIQUE DE L'IA MOYENNE (Le Tacticien)
    // =========================================================================
    
    public int getScoreHeuristiqueMoyenne() {
        if (adversaire.getHp() <= 0) return 1000000;
        if (joueurActif.getHp() <= 0) return -1000000;
        
        double score = 0;
        
        // 1. POINTS DE VIE (Priorité absolue)
        score += (joueurActif.getHp() - adversaire.getHp()) * 50;

        // 2. DISTANCE : pénalité forte pour éviter le jeu défensif passif
        int distance = Math.abs(joueurActif.getPosition().getLigne() - adversaire.getPosition().getLigne()) +
                       Math.abs(joueurActif.getPosition().getColonne() - adversaire.getPosition().getColonne());
        score -= distance * 8.0;

        // 3. BONUS SI À PORTÉE : récompense massivement le fait d'être en position d'attaque
        boolean peutAttaquer = false;
        for (AttaqueInfo a : joueurActif.getAttaques()) {
            if (joueurActif.getEnergie() >= a.getDegat() && distance <= a.getPortee()) {
                score += 100;
                peutAttaquer = true;
                break;
            }
        }

        // 4. PÉNALITÉ D'ÉVITEMENT : si l'IA peut bouger et attaquer mais ne le fait pas
        if (!peutAttaquer && joueurActif.getEnergie() >= 15) {
            score -= 50;
        }

        // 5. GESTION DE L'ÉNERGIE (Anti-Farming plafonné)
        if (joueurActif.getEnergie() <= 50) {
            score += joueurActif.getEnergie() * 1.5;
        } else {
            score += 75; // Plafond : se reposer au-delà de 50 ne rapporte PLUS de points !
        }
        
        // Alerte rouge : l'IA est obligée de se reposer si elle n'a même plus de quoi faire une attaque légère
        if (joueurActif.getEnergie() < 15) {
            score -= 100;
        }
        
        // 6. PARADES EN INVENTAIRE (pondération réduite pour ne pas surpasser le combat)
        score += Math.min(joueurActif.getNbParades(), 3) * 10;
        score -= Math.min(adversaire.getNbParades(), 3) * 8;
        
        // 7. BONUS CASES : attrait réduit pour ne pas détourner l'IA du combat
        score += evaluerCasesBoostMoyenne(this, joueurActif);

        return (int) score;
    }
    
    private double evaluerCasesBoostMoyenne(Etat etat, JoueurEtat actif) {
        double scoreBoost = 0;
        int[][] grille = etat.getGrille();
        Position pos = actif.getPosition();
        
        // Rayon réduit à 3 : l'IA ne se détourne du combat que pour des bonus très proches
        int rayon = 3;
        
        for(int l = Math.max(0, pos.getLigne() - rayon); l <= Math.min(grille.length - 1, pos.getLigne() + rayon); l++) {
            for(int c = Math.max(0, pos.getColonne() - rayon); c <= Math.min(grille[0].length - 1, pos.getColonne() + rayon); c++) {
                
                int dist = Math.abs(pos.getLigne() - l) + Math.abs(pos.getColonne() - c);
                
                if (dist > 0 && dist <= rayon) {
                    // Attrait fortement réduit (80->30, 60->20) pour ne jamais surpasser le combat
                    if (grille[l][c] == 3 && actif.getNbParades() < 2) {
                        scoreBoost += 30.0 / dist;
                    } 
                    else if (grille[l][c] == 4 && actif.getEnergie() <= 30) {
                        scoreBoost += 20.0 / dist;
                    }
                }
            }
        }
        return scoreBoost;
    }

    // =========================================================================
    // 🧠 HEURISTIQUE DE L'IA DIFFICILE (Le Stratège)
    // =========================================================================

    public int getScoreHeuristiqueDifficile() {
        // 1. ÉTATS TERMINAUX (Poids de Victoire Absolu)
        if (adversaire.getHp() <= 0) return 1000000;
        if (joueurActif.getHp() <= 0) return -1000000;

        double score = 0;

        // 2. POINTS DE VIE : Priorité absolue, pondération élevée
        score += (joueurActif.getHp() - adversaire.getHp()) * 80;

        // 3. DISTANCE : pénalité très forte pour forcer une pression constante
        int distance = distanceDifficile(joueurActif.getPosition(), adversaire.getPosition());
        score -= distance * 12;

        // 4. DÉTECTION DES COUPS LÉTAUX : Bonus massif si l'adversaire
        //    peut être tué dès ce tour (énergie suffisante + portée ok)
        boolean peutAttaquer = false;
        for (AttaqueInfo a : joueurActif.getAttaques()) {
            if (joueurActif.getEnergie() >= a.getDegat() && distance <= a.getPortee()) {
                peutAttaquer = true;
                if (adversaire.getHp() <= a.getDegat()) {
                    score += 500000; // Coup létal détecté : on le priorise absolument
                    break;
                }
                // Bonus de position offensive même sans coup létal
                score += a.getDegat() * 3;
            }
        }

        // 5. PÉNALITÉ D'ÉVITEMENT : l'IA ne doit JAMAIS fuir si elle peut attaquer
        if (!peutAttaquer && joueurActif.getEnergie() >= 15) {
            score -= 80;
        }

        // 6. GESTION FINE DE L'ÉNERGIE
        if (joueurActif.getEnergie() < 15) {
            score -= 150; // Alerte rouge : plus assez d'énergie pour attaquer
        } else if (joueurActif.getEnergie() <= 60) {
            score += joueurActif.getEnergie() * 1.2;
        } else {
            score += 72; // Plafond : inutile de farmer au-delà de 60
        }

        // 7. PARADES EN STOCK : avantage défensif sur l'adversaire
        score += Math.min(joueurActif.getNbParades(), 3) * 25;
        score -= Math.min(adversaire.getNbParades(), 3) * 20;

        // 8. BONUS CASES (radar très court) : ne se détourne du combat que si le bonus
        //    est vraiment sur son chemin (rayon 2 au lieu de 5)
        score += evaluerCasesBoostDifficile();

        // 9. PÉNALITÉ SI L'ADVERSAIRE PEUT NOUS TUER AU PROCHAIN TOUR
        for (AttaqueInfo a : adversaire.getAttaques()) {
            if (adversaire.getEnergie() >= a.getDegat()
                    && distance <= a.getPortee()
                    && joueurActif.getHp() <= a.getDegat()) {
                score -= 200000; // Danger immédiat : fuir ou parader en priorité
                break;
            }
        }

        return (int) score;
    }

    private double evaluerCasesBoostDifficile() {
        double scoreBoost = 0;
        Position pos = joueurActif.getPosition();
        // Rayon réduit à 2 : le combat prime toujours sur la collecte de bonus
        int rayon = 2;

        for (int l = Math.max(0, pos.getLigne() - rayon); l <= Math.min(grille.length - 1, pos.getLigne() + rayon); l++) {
            for (int c = Math.max(0, pos.getColonne() - rayon); c <= Math.min(grille[0].length - 1, pos.getColonne() + rayon); c++) {
                int dist = Math.abs(pos.getLigne() - l) + Math.abs(pos.getColonne() - c);
                if (dist > 0 && dist <= rayon) {
                    // Attrait très faible pour ne jamais surpasser l'intérêt d'attaquer
                    if (grille[l][c] == 3 && joueurActif.getNbParades() < 2) {
                        scoreBoost += 20.0 / dist;
                    } else if (grille[l][c] == 4 && joueurActif.getEnergie() <= 20) {
                        scoreBoost += 15.0 / dist;
                    }
                }
            }
        }
        return scoreBoost;
    }

    private int distanceDifficile(Position p1, Position p2) {
        return Math.abs(p1.getLigne() - p2.getLigne()) + Math.abs(p1.getColonne() - p2.getColonne());
    }

        // =========================================================================
    // 👤 CLASSES INTERNES DE DONNÉES
    // =========================================================================

    public static class JoueurEtat {
        private Position position;
        private int hp;
        private double energie;
        private double maxEnergie;
        private int nbParades;
        private int nbRepos;
        private boolean enParade;
        private int pas;
        private int id;
        private List<AttaqueInfo> attaques;

        // Constructeur standard
        public JoueurEtat(Position position, int hp, double energie, double maxEnergie, int nbParades, int nbRepos, int pas, int id, List<AttaqueInfo> attaques) {
            this.position = position;
            this.hp = hp;
            this.energie = energie;
            this.maxEnergie = maxEnergie;
            this.nbParades = nbParades;
            this.nbRepos = nbRepos;
            this.enParade = false;
            this.pas = pas;
            this.id = id;
            this.attaques = attaques;
        }

        // Constructeur de copie
        public JoueurEtat(JoueurEtat autre) {
            this.position = new Position(autre.position.getLigne(), autre.position.getColonne());
            this.hp = autre.hp;
            this.energie = autre.energie;
            this.maxEnergie = autre.maxEnergie;
            this.nbParades = autre.nbParades;
            this.nbRepos = autre.nbRepos;
            this.enParade = autre.enParade;
            this.pas = autre.pas;
            this.id = autre.id;
            this.attaques = new ArrayList<>(autre.attaques);
        }

        /**
         * CORRECTION : Ajout du paramètre idJoueur pour l'affichage correct sur la grille (ex: 1 ou 2).
         */
        public static JoueurEtat fromPersonnage(Personnage p, int idJoueur) {
            List<AttaqueInfo> infos = new ArrayList<>();
            for (Attaques a : p.getAttaques()) {
                infos.add(new AttaqueInfo(a.getType_attaque(), a.getDegat(), a.getPortee()));
            }
            return new JoueurEtat(p.getPosition(), (int) Math.max(0, p.getHp()), p.getEnergie(), p.getMaxEnergie(), p.getNbParades(), p.getNbRepos(), p.getPas(), idJoueur, infos);
        }

        /**
         * Surcharge utilitaire pour conserver l'API existante où aucun id n'est fourni.
         */
        public static JoueurEtat fromPersonnage(Personnage p) {
            return fromPersonnage(p, 0);
        }

        // --- Getters & Setters ---
        public Position getPosition() { return position; }
        public void setPosition(Position position) { this.position = position; }
        public int getHp() { return hp; }
        public void setHp(int hp) { this.hp = hp; }
        public double getEnergie() { return energie; }
        public void setEnergie(double energie) { 
            this.energie = energie;
            if (this.energie < 0) this.energie = 0;
            if (this.energie > this.maxEnergie) this.energie = this.maxEnergie;
        }
        public double getMaxEnergie() { return maxEnergie; }
        public int getNbParades() { return nbParades; }
        public void setNbParades(int nbParades) { this.nbParades = nbParades; }
        public int getNbRepos() { return nbRepos; }
        public void setNbRepos(int nbRepos) { this.nbRepos = nbRepos; }
        public boolean isEnParade() { return enParade; }
        public void setEnParade(boolean enParade) { this.enParade = enParade; }
        public int getPas() { return pas; }
        public int getId() { return id; }
        public List<AttaqueInfo> getAttaques() { return attaques; }
    }

    public static class AttaqueInfo {
        private String type;
        private double degat;
        private int portee;

        public AttaqueInfo(String type, double degat, int portee) {
            this.type = type;
            this.degat = degat;
            this.portee = portee;
        }

        public String getType() { return type; }
        public double getDegat() { return degat; }
        public int getPortee() { return portee; }
    }
}
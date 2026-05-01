package architecture;

import entite.*;
import exception.IllegalActionException;
import exception.IllegalAttackException;
import ia.Etat;
import ia.IAFacile;
import ia.IAMoyenne;
import ia.Coup;
import ia.MoteurCoups;
import attaques.*;
import java.awt.*;
import javax.swing.*;

public class Jeu {
    private Arene arene;
    private Personnage joueur1, joueur2;
    private Personnage joueurActif, adversaire;
    
    private String etat = "INIT"; 
    private String attaqueEnCours = "";
    private int pmRestants = 0;
    private boolean attaqueDejaEffectuee = false;
    
    // Variable temporaire pour la sélection
    private String classChoisie = null;
    private String modeJeu = "PVP"; // PVP, PVIA_FACILE, PVIA_MOYEN, PVIA_DIFFICILE, IAVIA
    private String difficulteIA1 = "FACILE"; // Difficulté de l'IA 1 en mode IAVIA
    private String difficulteIA2 = "FACILE"; // Difficulté de l'IA 2 en mode IAVIA

    public Jeu() {
        this("PVP"); // Mode par défaut pour compatibilité
    }
    
    public Jeu(String mode) {
        this.modeJeu = mode;
        
        // parser les modes de jeu avec difficultés spécifiques pour chaque ia
        if (mode.startsWith("IAVIA_")) {
            String[] parties = mode.split("_");
            if (parties.length >= 3) {
                this.modeJeu = "IAVIA";
                this.difficulteIA1 = parties[1];
                this.difficulteIA2 = parties[2];
            } else {
                // mode par défaut si le parsing échoue
                this.modeJeu = "IAVIA";
                this.difficulteIA1 = "FACILE";
                this.difficulteIA2 = "FACILE";
            }
        }
        
        // On lance d'abord la fenêtre principale en arrière-plan
        FenetreArene.lancerFenetre(this);
        demarrerJeu();
    }

    private void demarrerJeu() {
        switch (modeJeu) {
            case "PVP":
                demarrerJoueurVsJoueur();
                break;
            case "PVIA_FACILE":
            case "PVIA_MOYEN":
            case "PVIA_DIFFICILE":
                demarrerJoueurVsIA();
                break;
            case "IAVIA":
                demarrerIAVsIA();
                break;
            default:
                demarrerJoueurVsJoueur(); // Mode par défaut
        }
    }
    
    private void demarrerJoueurVsJoueur() {
        String choix1 = demanderClasse("Joueur 1 (Bleu), choisissez votre classe :");
        joueur1 = instancierPerso(choix1);

        String choix2 = demanderClasse("Joueur 2 (Rouge), choisissez votre classe :");
        joueur2 = instancierPerso(choix2);

        if (joueur1 == null || joueur2 == null) {
            System.exit(0); 
        }

        initialiserPartie();
    }
    
    private void demarrerJoueurVsIA() {
        String choix1 = demanderClasse("Joueur 1 (Bleu), choisissez votre classe :");
        joueur1 = instancierPerso(choix1);

        // L'IA choisit une classe aléatoirement pour l'instant
        String[] classesIA = {"Chevalier", "Archère", "Soigneur"};
        String choixIA = classesIA[(int)(Math.random() * classesIA.length)];
        joueur2 = instancierPerso(choixIA);
        // Le nom reste celui par défaut de la classe

        if (joueur1 == null || joueur2 == null) {
            System.exit(0); 
        }

        initialiserPartie();
    }
    
    private void demarrerIAVsIA() {
        // Les deux IA choisissent des classes aléatoirement
        String[] classesIA = {"Chevalier", "Archère", "Soigneur"};
        
        String choixIA1 = classesIA[(int)(Math.random() * classesIA.length)];
        joueur1 = instancierPerso(choixIA1);
        // Le nom reste celui par défaut de la classe
        
        String choixIA2 = classesIA[(int)(Math.random() * classesIA.length)];
        joueur2 = instancierPerso(choixIA2);
        // Le nom reste celui par défaut de la classe

        initialiserPartie();
    }
    
    private void initialiserPartie() {
        arene = new Arene(joueur1, joueur2);
        FenetreArene.setArene(arene);
        
        joueurActif = joueur1;
        adversaire = joueur2;
        etat = "MOUVEMENT";
        pmRestants = joueurActif.getPas();
        attaqueDejaEffectuee = false;
        
        FenetreArene.MAJStats(joueur1, joueur2, joueurActif);
        String modeAffiche = getModeAffichage();
        FenetreArene.MAJTexte(
                modeAffiche + " - Tour de " + joueurActif.getNom() + " : " + pmRestants + " PM disponibles."
        );
        FenetreArene.rafraichir();
        
        // Si c'est à l'IA de jouer dès le début, on lance son tour automatiquement
        if (estTourIA()) {
            SwingUtilities.invokeLater(() -> {
                try {
                    Thread.sleep(1000); // Petite pause pour la lisibilité
                    jouerTourIA();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }
    
    private String getModeAffichage() {
        switch (modeJeu) {
            case "PVP": return "Joueur vs Joueur";
            case "PVIA_FACILE": return "Joueur vs IA (Facile)";
            case "PVIA_MOYEN": return "Joueur vs IA (Moyen)";
            case "PVIA_DIFFICILE": return "Joueur vs IA (Difficile)";
            case "IAVIA": 
                if (joueurActif == joueur1) {
                    return "IA vs IA - Joueur 1: " + difficulteIA1 + " | Joueur 2: " + difficulteIA2;
                } else {
                    return "IA vs IA - Joueur 1: " + difficulteIA1 + " | Joueur 2: " + difficulteIA2;
                }
            default: return "Combat";
        }
    }

    // --- NOUVEAU MENU DE SÉLECTION PROPRE ---
    private String demanderClasse(String titre) {
        classChoisie = null; // reset
        
        JDialog dialog = new JDialog((Frame)null, "Sélection du Personnage", true);
        dialog.setSize(450, 200);
        dialog.setLocationRelativeTo(null); // centre sur l'écran
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(new Color(40, 42, 54));

        JLabel lblTitre = new JLabel(titre, SwingConstants.CENTER);
        lblTitre.setForeground(Color.WHITE);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitre.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        dialog.add(lblTitre, BorderLayout.NORTH);

        JPanel panelClasses = new JPanel(new GridLayout(1, 3, 15, 0));
        panelClasses.setBackground(new Color(40, 42, 54));
        panelClasses.setBorder(BorderFactory.createEmptyBorder(10, 20, 30, 20));

        String[] classes = {"Chevalier", "Archère", "Soigneur"};
        for (String c : classes) {
            JButton btn = new JButton(c);
            btn.setBackground(new Color(60, 62, 74));
            btn.setForeground(Color.BLACK);
            btn.setFocusPainted(false);
            btn.setFont(new Font("Arial", Font.BOLD, 14));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Effet Hover
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new Color(100, 102, 120)); // plus clair au survol
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new Color(60, 62, 74)); // retour normal
                }
            });

            btn.addActionListener(e -> {
                classChoisie = c;
                dialog.dispose(); // ferme la fenêtre quand on clique
            });
            panelClasses.add(btn);
        }

        dialog.add(panelClasses, BorderLayout.CENTER);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // force à choisir
        dialog.setVisible(true); 

        return classChoisie == null ? "Chevalier" : classChoisie; // sécurité
    }

    private Personnage instancierPerso(String nom) {
        if (nom == null) return new Chevalier();
        if (nom.equals("Archère")) return new Archere();
        if (nom.equals("Soigneur")) return new Soigneur();
        return new Chevalier(); 
    }
    
    public void clicSurCase(int ligne, int colonne) {
        if (etat.equals("MOUVEMENT")) {
            int caseCible = arene.getGrille()[ligne][colonne];
            
            if (caseCible == 1 || caseCible == 2) {
                JOptionPane.showMessageDialog(null, "Case déjà occupée par un joueur !");
                return;
            }
            if (caseCible == -1) {
                JOptionPane.showMessageDialog(null, "Case bloquée par un obstacle !");
                return;
            }

            int distMouv = Math.abs(ligne - joueurActif.getPosition().getLigne()) + Math.abs(colonne - joueurActif.getPosition().getColonne());
            if (distMouv == 0) return;
            if (distMouv > pmRestants) {
                JOptionPane.showMessageDialog(null, "Déplacement trop grand (PM restants : " + pmRestants + ")");
                return;
            }

            // prendre les bonus sur le trajet de déplacement
            Position depart = joueurActif.getPosition();
            int bonusPris = 0;
            
            // parcourir le trajet case par case (manhattan)
            int ligneActuelle = depart.getLigne();
            int colonneActuelle = depart.getColonne();
            
            // déplacement horizontal puis vertical (ou inversement)
            while (ligneActuelle != ligne || colonneActuelle != colonne) {
                // déplacement horizontal
                if (colonneActuelle != colonne) {
                    colonneActuelle += (colonne > colonneActuelle) ? 1 : -1;
                } else {
                    ligneActuelle += (ligne > ligneActuelle) ? 1 : -1;
                }
                
                // vérifier si la case contient un bonus
                int caseType = arene.getGrille()[ligneActuelle][colonneActuelle];
                if (caseType == 3) {
                    joueurActif.setParade(1);
                    arene.getGrille()[ligneActuelle][colonneActuelle] = 0;
                    bonusPris++;
                } else if (caseType == 4) {
                    joueurActif.setEnergie(20.0);
                    arene.getGrille()[ligneActuelle][colonneActuelle] = 0;
                    bonusPris++;
                }
            }

            joueurActif.setPosition(new Position(ligne, colonne));
            pmRestants -= distMouv;
            arene.updateFullGrille();
            
            etat = "MOUVEMENT";
            FenetreArene.MAJStats(joueur1, joueur2, joueurActif); 
            String infoAttaque = attaqueDejaEffectuee ? " Attaque déjà utilisée." : " Vous pouvez encore attaquer.";
            String messageBonus = bonusPris > 0 ? " " + bonusPris + " bonus collectés !" : "";
            FenetreArene.MAJTexte(
                    joueurActif.getNom() + " s'est déplacé (" + pmRestants + " PM restants)." + infoAttaque + messageBonus
            );
            FenetreArene.rafraichir();

        } else if (etat.equals("CIBLE")) {
            if (adversaire.getPosition().getLigne() == ligne && adversaire.getPosition().getColonne() == colonne) {
                executerAttaque();
            } else {
                etat = "MOUVEMENT";
                FenetreArene.MAJTexte("Ciblage annulé. Choisissez une action.");
                FenetreArene.rafraichir();
            }
        }
    }

    public void clicAction(String action) throws IllegalAttackException {
        boolean estAttaque = "AL".equals(action) || "ALD".equals(action) || "AD".equals(action);
        // En mode CIBLE, on autorise de changer d'attaque à la volée.
        if (!etat.equals("ACTION") && !etat.equals("MOUVEMENT") && !(etat.equals("CIBLE") && estAttaque)) return; 

        try {
            switch (action) {
                case "AL":
                case "ALD":
                case "AD":
                    if (attaqueDejaEffectuee) {
                        throw new IllegalActionException("Attaque déjà utilisée ce tour.");
                    }
                    attaqueEnCours = action;
                    
                    int portee = 0;
                    double coutEnergie = -1;
                    for (Attaques att : joueurActif.getAttaques()) {
                        if (att.getType_attaque().equalsIgnoreCase(action)) {
                            portee = att.getPortee();
                            coutEnergie = att.getDegat();
                        }
                    }

                    if (portee == 0) {
                        throw new IllegalAttackException("Type d'attaque inconnu.");
                    }
                    if (joueurActif.getEnergie() < coutEnergie) {
                        throw new IllegalAttackException(
                                "Énergie insuffisante pour " + action + " (coût : " + coutEnergie + ")."
                        );
                    }

                    etat = "CIBLE";
                    FenetreArene.MAJTexte(
                            "Attaque " + action + " sélectionnée (portée " + portee + "). Cliquez une cible."
                    );
                    FenetreArene.rafraichir();
                    break;
                    
                case "P":
                    joueurActif.parader();
                    finDeTour();
                    break;

                case "R":
                    joueurActif.seReposer(); 
                    finDeTour(); 
                    break;
                default:
                    throw new IllegalActionException("Action inconnue : " + action);
            }
        } catch (IllegalActionException e) {
            JOptionPane.showMessageDialog(null, "Action impossible : " + e.getMessage());
        }
    }

    private void executerAttaque() {
        try {
            int distance = Math.abs(adversaire.getPosition().getLigne() - joueurActif.getPosition().getLigne()) +
                    Math.abs(adversaire.getPosition().getColonne() - joueurActif.getPosition().getColonne());
            int portee = getPorteeAttaque(attaqueEnCours);
            if (portee <= 0) {
                throw new IllegalAttackException("Type d'attaque invalide.");
            }
            if (distance > portee) {
                throw new IllegalAttackException(
                        "Cible hors de portée pour " + attaqueEnCours + " (portée max : " + portee + ")."
                );
            }

            double hpAvant = adversaire.getHp();
            boolean paradeAvant = adversaire.isEnParade();
            joueurActif.attaquer(adversaire, attaqueEnCours);
            attaqueDejaEffectuee = true;
            attaqueEnCours = "";
            FenetreArene.MAJStats(joueur1, joueur2, joueurActif); 
            
            if (adversaire.getHp() <= 0) {
                etat = "FIN";
                JOptionPane.showMessageDialog(null, "Victoire ! " + joueurActif.getNom() + " a remporté le combat !");
                System.exit(0);
            } else {
                etat = "MOUVEMENT";
                if (paradeAvant && adversaire.getHp() == hpAvant) {
                    JOptionPane.showMessageDialog(null, "Parade réussie ! " + adversaire.getNom() + " ne perd aucun PV.");
                } else {
                    JOptionPane.showMessageDialog(null, "Attaque réussie ! Il reste " + adversaire.getHp() + " PV à " + adversaire.getNom());
                }
                FenetreArene.MAJTexte(
                        "Attaque terminée. " + pmRestants + " PM restants pour te repositionner, ou Repos pour finir le tour."
                );
                FenetreArene.rafraichir();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erreur attaque : " + e.getMessage());
            etat = "MOUVEMENT";
            FenetreArene.MAJTexte("Choisissez une action via les boutons.");
        }
    }

    private void finDeTour() {
        Personnage temp = joueurActif;
        joueurActif = adversaire;
        adversaire = temp;
        etat = "MOUVEMENT";
        // Une parade ne peut pas survivre au-delà du prochain tour du joueur.
        joueurActif.setEnParade(false);
        attaqueEnCours = "";
        attaqueDejaEffectuee = false;
        pmRestants = joueurActif.getPas();
        
        String numJ = (joueurActif == joueur1) ? "(J1)" : "(J2)";
        FenetreArene.MAJStats(joueur1, joueur2, joueurActif);
        FenetreArene.MAJTexte(
                joueurActif.getNom() + " " + numJ + " : " + pmRestants + " PM disponibles"
        );
        FenetreArene.rafraichir();
        
        // Si c'est à l'IA de jouer, on lance son tour automatiquement
        if (estTourIA()) {
            SwingUtilities.invokeLater(() -> {
                try {
                    Thread.sleep(1000); // Petite pause pour la lisibilité
                    jouerTourIA();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }
    
    /**
     * Vérifie si c'est le tour de l'IA selon le mode de jeu
     */
    private boolean estTourIA() {
        return (modeJeu.equals("PVIA_FACILE") && joueurActif == joueur2) ||
               (modeJeu.equals("PVIA_MOYEN") && joueurActif == joueur2) ||
               (modeJeu.equals("PVIA_DIFFICILE") && joueurActif == joueur2) ||
               (modeJeu.equals("IAVIA"));
    }
    
    /**
     * Fait jouer l'IA un tour complet
     */
    private void jouerTourIA() {
        try {
            Etat etatIA = exporterEtatIA();
            Coup coupChoisi;
            
            // choix de l'ia selon le mode de jeu et la difficulté spécifique
            if (modeJeu.equals("PVIA_FACILE")) {
                coupChoisi = IAFacile.choisirCoup(etatIA);
            } else if (modeJeu.equals("PVIA_MOYEN")) {
                coupChoisi = IAMoyenne.choisirCoup(etatIA);
            } else if (modeJeu.equals("PVIA_DIFFICILE")) {
                // todo : implémenter ia difficile
                coupChoisi = IAFacile.choisirCoup(etatIA);
            } else if (modeJeu.equals("IAVIA")) {
                // mode ia vs ia avec difficultés spécifiques
                String difficulteActuelle;
                if (joueurActif == joueur1) {
                    difficulteActuelle = difficulteIA1;
                } else {
                    difficulteActuelle = difficulteIA2;
                }
                
                if (difficulteActuelle.equals("FACILE")) {
                    coupChoisi = IAFacile.choisirCoup(etatIA);
                } else if (difficulteActuelle.equals("MOYEN")) {
                    coupChoisi = IAMoyenne.choisirCoup(etatIA);
                } else if (difficulteActuelle.equals("DIFFICILE")) {
                    // todo : implémenter ia difficile
                    coupChoisi = IAFacile.choisirCoup(etatIA);
                } else {
                    coupChoisi = IAFacile.choisirCoup(etatIA);
                }
            } else {
                coupChoisi = IAFacile.choisirCoup(etatIA);
            }
            
            if (coupChoisi == null) {
                finDeTour();
                return;
            }
            
            // Exécuter le déplacement
            if (coupChoisi.getDestination() != null) {
                Position dest = coupChoisi.getDestination();
                int dist = Math.abs(dest.getLigne() - joueurActif.getPosition().getLigne()) + 
                          Math.abs(dest.getColonne() - joueurActif.getPosition().getColonne());
                
                // Appliquer le déplacement
                int caseCible = arene.getGrille()[dest.getLigne()][dest.getColonne()];
                if (caseCible == 3) {
                    joueurActif.setParade(1);
                    arene.getGrille()[dest.getLigne()][dest.getColonne()] = 0;
                } else if (caseCible == 4) {
                    joueurActif.setEnergie(20.0);
                    arene.getGrille()[dest.getLigne()][dest.getColonne()] = 0;
                }
                
                joueurActif.setPosition(dest);
                pmRestants -= dist;
                arene.updateFullGrille();
                FenetreArene.rafraichir();
                
                // afficher un message si un bonus a été pris
                if (caseCible == 3) {
                    FenetreArene.MAJTexte("Bonus de parade obtenu !");
                } else if (caseCible == 4) {
                    FenetreArene.MAJTexte("Bonus d'énergie obtenu !");
                }
                
                Thread.sleep(500);
            }
            
            // Exécuter l'action
            switch (coupChoisi.getAction()) {
                case ATTAQUE:
                    if (coupChoisi.getTypeAttaque() != null) {
                        attaqueEnCours = coupChoisi.getTypeAttaque();
                        executerAttaque();
                        // Forcer la fin du tour après l'attaque
                        Thread.sleep(1000);
                        finDeTour();
                    }
                    break;
                case PARADE:
                    joueurActif.parader();
                    Thread.sleep(500);
                    finDeTour();
                    break;
                case REPOS:
                    joueurActif.seReposer();
                    Thread.sleep(500);
                    finDeTour();
                    break;
                case TERMINER:
                    Thread.sleep(500);
                    finDeTour();
                    break;
            }
        } catch (Exception e) {
            System.err.println("Erreur pendant le tour de l'IA : " + e.getMessage());
            finDeTour();
        }
    }

    public void start() {}
    public void fermer() {}
    public Arene getArene() { return arene; }
    public Personnage getJoueurActif() { return joueurActif; }
    public Personnage getAdversaire() { return adversaire; }
    public String getEtat() { return etat; }
    public String getAttaqueEnCours() { return attaqueEnCours; }
    public int getPmRestants() { return pmRestants; }
    public String getModeJeu() {
        return modeJeu;
    }
    
    public String getDifficulteIA1() {
        return difficulteIA1;
    }
    
    public String getDifficulteIA2() {
        return difficulteIA2;
    }

    public int getPorteeAttaque(String typeAttaque) {
        if (typeAttaque == null || typeAttaque.isEmpty() || joueurActif == null) return 0;
        for (Attaques att : joueurActif.getAttaques()) {
            if (att.getType_attaque().equalsIgnoreCase(typeAttaque)) {
                return att.getPortee();
            }
        }
        return 0;
    }

    /**
     * Exporte l'état courant du jeu vers une représentation dédiée à l'IA.
     */
    public Etat exporterEtatIA() {
        return new Etat(
                Etat.copieGrille(arene.getGrille()),
                Etat.JoueurEtat.fromPersonnage(joueurActif),
                Etat.JoueurEtat.fromPersonnage(adversaire)
        );
    }
}
package architecture;

import entite.*;
import attaques.*;
import java.awt.*;
import javax.swing.*;

public class Jeu {
    private Arene arene;
    private Personnage joueur1, joueur2;
    private Personnage joueurActif, adversaire;
    
    private String etat = "INIT"; 
    private String attaqueEnCours = "";
    
    // Variable temporaire pour la sélection
    private String classChoisie = null;

    public Jeu() {
        // On lance d'abord la fenêtre principale en arrière-plan
        FenetreArene.lancerFenetre(this);
        demarrerJeu();
    }

    private void demarrerJeu() {
        // Remplacement du vieux JOptionPane par notre menu stylé
        String choix1 = demanderClasse("Joueur 1 (Bleu), choisissez votre classe :");
        joueur1 = instancierPerso(choix1);

        String choix2 = demanderClasse("Joueur 2 (Rouge), choisissez votre classe :");
        joueur2 = instancierPerso(choix2);

        if (joueur1 == null || joueur2 == null) {
            System.exit(0); 
        }

        arene = new Arene(joueur1, joueur2);
        FenetreArene.setArene(arene);
        
        joueurActif = joueur1;
        adversaire = joueur2;
        etat = "MOUVEMENT";
        
        FenetreArene.MAJStats(joueur1, joueur2, joueurActif);
        FenetreArene.MAJTexte("Tour de " + joueurActif.getNom() + " (J1) : Cliquez sur une case pour bouger.");
        FenetreArene.rafraichir();
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
            btn.setForeground(Color.WHITE);
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
            if (distMouv > joueurActif.getPas()) {
                JOptionPane.showMessageDialog(null, "Déplacement trop grand (max " + joueurActif.getPas() + " pas).");
                return;
            }

            if (caseCible == 3) {
                joueurActif.setParade(1); 
                arene.getGrille()[ligne][colonne] = 0;
            } else if (caseCible == 4) {
                joueurActif.setEnergie(20.0);
                arene.getGrille()[ligne][colonne] = 0;
            }

            joueurActif.setPosition(new Position(ligne, colonne));
            arene.updateFullGrille();
            
            etat = "ACTION";
            FenetreArene.MAJStats(joueur1, joueur2, joueurActif); 
            FenetreArene.MAJTexte(joueurActif.getNom() + " s'est déplacé. Choisissez une action en bas.");
            FenetreArene.rafraichir();

        } else if (etat.equals("CIBLE")) {
            if (adversaire.getPosition().getLigne() == ligne && adversaire.getPosition().getColonne() == colonne) {
                executerAttaque();
            } else {
                JOptionPane.showMessageDialog(null, "Vous devez cliquer sur " + adversaire.getNom() + " !");
                etat = "ACTION"; 
                FenetreArene.MAJTexte("Action annulée. Choisissez une attaque via les boutons.");
            }
        }
    }

    public void clicAction(String action) {
        if (!etat.equals("ACTION") && !etat.equals("MOUVEMENT")) return; 

        try {
            switch (action) {
                case "AL":
                case "ALD":
                case "AD":
                    attaqueEnCours = action;
                    
                    int distAtt = Math.abs(adversaire.getPosition().getLigne() - joueurActif.getPosition().getLigne()) + 
                                  Math.abs(adversaire.getPosition().getColonne() - joueurActif.getPosition().getColonne());
                    int portee = 0;
                    for (Attaques att : joueurActif.getAttaques()) {
                        if (att.getType_attaque().equalsIgnoreCase(action)) portee = att.getPortee();
                    }
                    
                    if (portee > 0 && distAtt > portee) {
                        JOptionPane.showMessageDialog(null, "Cible hors de portée pour cette attaque ! (portée max : " + portee + ")");
                        return;
                    }
                    
                    etat = "CIBLE";
                    FenetreArene.MAJTexte("Cliquez sur " + adversaire.getNom() + " sur la grille pour l'attaquer.");
                    break;
                    
                case "P":
                    joueurActif.parader();
                    finDeTour();
                    break;

                case "R":
                    joueurActif.seReposer(); 
                    finDeTour(); 
                    break;

                case "T":
                    finDeTour();
                    break;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Action impossible : " + e.getMessage());
        }
    }

    private void executerAttaque() {
        try {
            joueurActif.attaquer(adversaire, attaqueEnCours);
            FenetreArene.MAJStats(joueur1, joueur2, joueurActif); 
            
            if (adversaire.getHp() <= 0) {
                etat = "FIN";
                JOptionPane.showMessageDialog(null, "Coup de grâce ! " + joueurActif.getNom() + " a gagné le combat !");
                System.exit(0);
            } else {
                JOptionPane.showMessageDialog(null, "Attaque réussie ! Il reste " + adversaire.getHp() + " PV à " + adversaire.getNom());
                finDeTour();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erreur attaque : " + e.getMessage());
            etat = "ACTION";
            FenetreArene.MAJTexte("Choisissez une action via les boutons.");
        }
    }

    private void finDeTour() {
        Personnage temp = joueurActif;
        joueurActif = adversaire;
        adversaire = temp;
        etat = "MOUVEMENT";
        
        String numJ = (joueurActif == joueur1) ? "(J1)" : "(J2)";
        FenetreArene.MAJStats(joueur1, joueur2, joueurActif);
        FenetreArene.MAJTexte("Tour de " + joueurActif.getNom() + " " + numJ + " : Cliquez sur une case pour bouger.");
        FenetreArene.rafraichir();
    }

    public void start() {}
    public void fermer() {}
    public Arene getArene() { return arene; }
}
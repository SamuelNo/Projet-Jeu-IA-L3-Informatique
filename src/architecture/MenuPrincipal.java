package architecture;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Menu principal du jeu Gladius
 */
public class MenuPrincipal extends JFrame {
    private static final Color COULEUR_FOND = new Color(245, 245, 245);
    private static final Color COULEUR_BOUTON = new Color(220, 220, 220);
    private static final Color COULEUR_BOUTON_SURVOL = new Color(200, 200, 200);
    private static final Color COULEUR_TEXTE = new Color(50, 50, 50);
    private static final Color COULEUR_BORDURE = new Color(180, 180, 180);
    
    public MenuPrincipal() {
        setTitle("Jeu de Combat IA");
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COULEUR_FOND);
        setResizable(false);
        
        // Panneau principal
        JPanel panneauPrincipal = new JPanel(new BorderLayout());
        panneauPrincipal.setBackground(COULEUR_FOND);
        panneauPrincipal.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        
        // Titre stylé
        JLabel titre = new JLabel("Jeu de Combat IA", SwingConstants.CENTER);
        titre.setForeground(COULEUR_TEXTE);
        titre.setFont(new Font("Arial", Font.BOLD, 36));
        titre.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        
        // Sous-titre
        JLabel sousTitre = new JLabel("Choisissez votre mode de jeu", SwingConstants.CENTER);
        sousTitre.setForeground(COULEUR_TEXTE);
        sousTitre.setFont(new Font("Arial", Font.ITALIC, 18));
        sousTitre.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0));
        
        // Panneau des boutons avec espacement amélioré
        JPanel panneauBoutons = new JPanel(new GridLayout(4, 1, 20, 20));
        panneauBoutons.setBackground(COULEUR_FOND);
        panneauBoutons.setBorder(BorderFactory.createEmptyBorder(0, 80, 0, 80));
        
        // Création des boutons stylés
        JButton btnPvP = creerBoutonClassique("Joueur vs Joueur");
        JButton btnPvIA = creerBoutonClassique("Joueur vs IA");
        JButton btnIAvIA = creerBoutonClassique("IA vs IA (Spectateur)");
        JButton btnQuitter = creerBoutonClassique("Quitter");
        
        // Actions des boutons
        btnPvP.addActionListener(e -> lancerModeJeu("PVP"));
        btnPvIA.addActionListener(e -> selectionnerDifficulteIA());
        btnIAvIA.addActionListener(e -> selectionnerDifficulteIAvIA());
        btnQuitter.addActionListener(e -> System.exit(0));
        
        panneauBoutons.add(btnPvP);
        panneauBoutons.add(btnPvIA);
        panneauBoutons.add(btnIAvIA);
        panneauBoutons.add(btnQuitter);
        
        // Assemblage du panneau titre
        JPanel panneauTitre = new JPanel(new GridLayout(2, 1, 10, 0));
        panneauTitre.setBackground(COULEUR_FOND);
        panneauTitre.add(titre);
        panneauTitre.add(sousTitre);
        
        // panneau pour le footer avec infos
        JPanel panneauFooter = new JPanel();
        panneauFooter.setBackground(COULEUR_FOND);
        JLabel footer = new JLabel("v1.0 | Projet L3 Informatique", SwingConstants.CENTER);
        footer.setForeground(new Color(158, 158, 158));
        footer.setFont(new Font("Arial", Font.PLAIN, 12));
        panneauFooter.add(footer);
        
        panneauPrincipal.add(panneauTitre, BorderLayout.NORTH);
        panneauPrincipal.add(panneauBoutons, BorderLayout.CENTER);
        panneauPrincipal.add(panneauFooter, BorderLayout.SOUTH);
        
        add(panneauPrincipal);
    }
    
    private JButton creerBouton(String texte) {
        JButton bouton = new JButton(texte);
        bouton.setBackground(COULEUR_BOUTON);
        bouton.setForeground(COULEUR_TEXTE);
        bouton.setFont(new Font("Arial", Font.BOLD, 16));
        bouton.setFocusPainted(false);
        bouton.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        bouton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Effet de survol
        bouton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bouton.setBackground(COULEUR_BOUTON_SURVOL);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bouton.setBackground(COULEUR_BOUTON);
            }
        });
        
        return bouton;
    }
    
    /**
     * crée un bouton classique et ésthétique
     */
    private JButton creerBoutonClassique(String texte) {
        JButton bouton = new JButton(texte);
        bouton.setBackground(Color.WHITE);
        bouton.setForeground(COULEUR_TEXTE);
        bouton.setFocusPainted(false);
        bouton.setFont(new Font("Arial", Font.PLAIN, 16));
        bouton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bouton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COULEUR_BORDURE, 1),
            BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));
        bouton.setPreferredSize(new Dimension(250, 45));
        
        // effets hover subtils
        bouton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bouton.setBackground(COULEUR_BOUTON_SURVOL);
                bouton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COULEUR_TEXTE, 1),
                    BorderFactory.createEmptyBorder(12, 20, 12, 20)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bouton.setBackground(Color.WHITE);
                bouton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COULEUR_BORDURE, 1),
                    BorderFactory.createEmptyBorder(12, 20, 12, 20)
                ));
            }
        });
        
        return bouton;
    }
    
    /**
     * crée un bouton stylé avec une couleur personnalisée
     */
    private JButton creerBoutonStile(String texte, Color couleur) {
        JButton bouton = new JButton(texte);
        bouton.setBackground(couleur);
        bouton.setForeground(Color.WHITE);
        bouton.setFocusPainted(false);
        bouton.setFont(new Font("Arial", Font.BOLD, 16));
        bouton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bouton.setBorder(BorderFactory.createLineBorder(couleur.darker(), 2));
        bouton.setPreferredSize(new Dimension(300, 50));
        
        // effets hover
        bouton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bouton.setBackground(couleur.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bouton.setBackground(couleur);
            }
        });
        
        return bouton;
    }
    
    // Sélection de la difficulté de l'IA
    private void selectionnerDifficulteIA() {
        JDialog dialogDifficulte = new JDialog(this, "Sélection de la difficulté de l'IA", true);
        dialogDifficulte.setSize(500, 400);
        dialogDifficulte.setLocationRelativeTo(this);
        dialogDifficulte.setLayout(new BorderLayout());
        dialogDifficulte.getContentPane().setBackground(COULEUR_FOND);
        dialogDifficulte.setResizable(false);
        
        // titre stylé
        JLabel titre = new JLabel("Choisissez la difficulté de l'IA", SwingConstants.CENTER);
        titre.setForeground(COULEUR_TEXTE);
        titre.setFont(new Font("Arial", Font.BOLD, 22));
        titre.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        
        // panneau pour les boutons avec design amélioré
        JPanel panneauBoutons = new JPanel(new GridLayout(2, 1, 15, 15));
        panneauBoutons.setBackground(COULEUR_FOND);
        panneauBoutons.setBorder(BorderFactory.createEmptyBorder(0, 60, 30, 60));
        
        JButton btnFacile = creerBoutonClassique("Facile");
        JButton btnMoyenne = creerBoutonClassique("Moyenne");
        
        // variables pour stocker la sélection
        final String[] difficulteChoisie = {null};
        final JButton[] boutonSelectionne = {null};
        
        btnFacile.addActionListener(e -> {
            // réinitialiser le style du bouton précédemment sélectionné
            if (boutonSelectionne[0] != null) {
                boutonSelectionne[0].setBackground(Color.WHITE);
                boutonSelectionne[0].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COULEUR_BORDURE, 1),
                    BorderFactory.createEmptyBorder(12, 20, 12, 20)
                ));
            }
            
            // mettre en évidence le bouton sélectionné
            btnFacile.setBackground(new Color(230, 240, 255)); // bleu très clair
            btnFacile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 150, 255), 3),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)
            ));
            btnFacile.setFont(new Font("Arial", Font.BOLD, 16));
            boutonSelectionne[0] = btnFacile;
            difficulteChoisie[0] = "PVIA_FACILE";
        });
        
        btnMoyenne.addActionListener(e -> {
            if (boutonSelectionne[0] != null) {
                boutonSelectionne[0].setBackground(Color.WHITE);
                boutonSelectionne[0].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COULEUR_BORDURE, 1),
                    BorderFactory.createEmptyBorder(12, 20, 12, 20)
                ));
            }
            
            btnMoyenne.setBackground(new Color(255, 248, 220)); // jaune très clair
            btnMoyenne.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 200, 0), 3),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)
            ));
            btnMoyenne.setFont(new Font("Arial", Font.BOLD, 16));
            boutonSelectionne[0] = btnMoyenne;
            difficulteChoisie[0] = "PVIA_MOYEN";
        });
        
        panneauBoutons.add(btnFacile);
        panneauBoutons.add(btnMoyenne);
        
        // bouton de validation
        JButton btnValider = creerBoutonClassique("Lancer le combat");
        btnValider.setEnabled(false); // désactivé tant qu'aucune difficulté n'est choisie
        
        JPanel panelValidation = new JPanel();
        panelValidation.setBackground(COULEUR_FOND);
        panelValidation.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        panelValidation.add(btnValider);
        
        // écouteur pour activer/désactiver le bouton valider
        ActionListener[] listeners = {btnFacile.getActionListeners()[0], btnMoyenne.getActionListeners()[0]};
        for (ActionListener listener : listeners) {
            // créer un nouveau listener pour chaque bouton
            if (listener == btnFacile.getActionListeners()[0]) {
                btnFacile.addActionListener(e -> btnValider.setEnabled(true));
            } else if (listener == btnMoyenne.getActionListeners()[0]) {
                btnMoyenne.addActionListener(e -> btnValider.setEnabled(true));
            }
        }
        
        btnValider.addActionListener(e -> {
            if (difficulteChoisie[0] != null) {
                dialogDifficulte.dispose();
                lancerModeJeu(difficulteChoisie[0]);
            }
        });
        
        dialogDifficulte.add(titre, BorderLayout.NORTH);
        dialogDifficulte.add(panneauBoutons, BorderLayout.CENTER);
        dialogDifficulte.add(panelValidation, BorderLayout.SOUTH);
        dialogDifficulte.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialogDifficulte.setVisible(true);
    }
    
    // Sélection de la difficulté pour chaque IA en mode IA vs IA
    private void selectionnerDifficulteIAvIA() {
        JDialog dialogDifficulte = new JDialog(this, "Configuration IA vs IA", true);
        dialogDifficulte.setSize(600, 500);
        dialogDifficulte.setLocationRelativeTo(this);
        dialogDifficulte.setLayout(new BorderLayout());
        dialogDifficulte.getContentPane().setBackground(COULEUR_FOND);
        dialogDifficulte.setResizable(false);
        
        // titre stylé
        JLabel titre = new JLabel("Configuration du combat IA", SwingConstants.CENTER);
        titre.setForeground(COULEUR_TEXTE);
        titre.setFont(new Font("Arial", Font.BOLD, 22));
        titre.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        JPanel panneauIA = new JPanel(new GridLayout(2, 1, 20, 20));
        panneauIA.setBackground(COULEUR_FOND);
        panneauIA.setBorder(BorderFactory.createEmptyBorder(0, 60, 20, 60));
        
        // variables pour stocker les sélections
        final String[] difficulteIA1Choisie = {null};
        final String[] difficulteIA2Choisie = {null};
        final JButton[] boutonSelectionneIA1 = {null};
        final JButton[] boutonSelectionneIA2 = {null};
        
        // IA 1 (Joueur 1)
        JPanel panelIA1 = new JPanel(new BorderLayout());
        panelIA1.setBackground(COULEUR_FOND);
        JLabel lblIA1 = new JLabel("IA 1 (Bleu) :", SwingConstants.LEFT);
        lblIA1.setForeground(COULEUR_TEXTE);
        lblIA1.setFont(new Font("Arial", Font.BOLD, 18));
        lblIA1.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panelIA1.add(lblIA1, BorderLayout.NORTH);
        
        JPanel boutonsIA1 = new JPanel(new GridLayout(1, 3, 10, 0));
        boutonsIA1.setBackground(COULEUR_FOND);
        JButton btnIA1Facile = creerBoutonClassique("Facile");
        JButton btnIA1Moyenne = creerBoutonClassique("Moyenne");
        
        btnIA1Facile.addActionListener(e -> {
            if (boutonSelectionneIA1[0] != null) {
                boutonSelectionneIA1[0].setBackground(Color.WHITE);
                boutonSelectionneIA1[0].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COULEUR_BORDURE, 1),
                    BorderFactory.createEmptyBorder(12, 20, 12, 20)
                ));
            }
            btnIA1Facile.setBackground(new Color(230, 240, 255)); // bleu très clair
            btnIA1Facile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 150, 255), 3),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)
            ));
            btnIA1Facile.setFont(new Font("Arial", Font.BOLD, 16));
            boutonSelectionneIA1[0] = btnIA1Facile;
            difficulteIA1Choisie[0] = "FACILE";
        });
        
        btnIA1Moyenne.addActionListener(e -> {
            if (boutonSelectionneIA1[0] != null) {
                boutonSelectionneIA1[0].setBackground(Color.WHITE);
                boutonSelectionneIA1[0].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COULEUR_BORDURE, 1),
                    BorderFactory.createEmptyBorder(12, 20, 12, 20)
                ));
            }
            btnIA1Moyenne.setBackground(new Color(255, 248, 220)); // jaune très clair
            btnIA1Moyenne.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 200, 0), 3),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)
            ));
            btnIA1Moyenne.setFont(new Font("Arial", Font.BOLD, 16));
            boutonSelectionneIA1[0] = btnIA1Moyenne;
            difficulteIA1Choisie[0] = "MOYEN";
        });
        
        boutonsIA1.add(btnIA1Facile);
        boutonsIA1.add(btnIA1Moyenne);
        panelIA1.add(boutonsIA1, BorderLayout.CENTER);
        
        // IA 2 (Joueur 2)
        JPanel panelIA2 = new JPanel(new BorderLayout());
        panelIA2.setBackground(COULEUR_FOND);
        JLabel lblIA2 = new JLabel("IA 2 (Rouge) :", SwingConstants.LEFT);
        lblIA2.setForeground(COULEUR_TEXTE);
        lblIA2.setFont(new Font("Arial", Font.BOLD, 18));
        lblIA2.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panelIA2.add(lblIA2, BorderLayout.NORTH);
        
        JPanel boutonsIA2 = new JPanel(new GridLayout(1, 3, 10, 0));
        boutonsIA2.setBackground(COULEUR_FOND);
        JButton btnIA2Facile = creerBoutonClassique("Facile");
        JButton btnIA2Moyenne = creerBoutonClassique("Moyenne");
        
        btnIA2Facile.addActionListener(e -> {
            if (boutonSelectionneIA2[0] != null) {
                boutonSelectionneIA2[0].setBackground(Color.WHITE);
                boutonSelectionneIA2[0].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COULEUR_BORDURE, 1),
                    BorderFactory.createEmptyBorder(12, 20, 12, 20)
                ));
            }
            btnIA2Facile.setBackground(new Color(230, 240, 255)); // bleu très clair
            btnIA2Facile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 150, 255), 3),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)
            ));
            btnIA2Facile.setFont(new Font("Arial", Font.BOLD, 16));
            boutonSelectionneIA2[0] = btnIA2Facile;
            difficulteIA2Choisie[0] = "FACILE";
        });
        
        btnIA2Moyenne.addActionListener(e -> {
            if (boutonSelectionneIA2[0] != null) {
                boutonSelectionneIA2[0].setBackground(Color.WHITE);
                boutonSelectionneIA2[0].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COULEUR_BORDURE, 1),
                    BorderFactory.createEmptyBorder(12, 20, 12, 20)
                ));
            }
            btnIA2Moyenne.setBackground(new Color(255, 248, 220)); // jaune très clair
            btnIA2Moyenne.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 200, 0), 3),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)
            ));
            btnIA2Moyenne.setFont(new Font("Arial", Font.BOLD, 16));
            boutonSelectionneIA2[0] = btnIA2Moyenne;
            difficulteIA2Choisie[0] = "MOYEN";
        });
        
        boutonsIA2.add(btnIA2Facile);
        boutonsIA2.add(btnIA2Moyenne);
        panelIA2.add(boutonsIA2, BorderLayout.CENTER);
        
        panneauIA.add(panelIA1);
        panneauIA.add(panelIA2);
        
        // Bouton de validation
        JButton btnValider = creerBoutonClassique("Lancer le combat");
        btnValider.setEnabled(false); // désactivé tant que les difficultés ne sont pas choisies
        
        JPanel panelValidation = new JPanel();
        panelValidation.setBackground(COULEUR_FOND);
        panelValidation.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        panelValidation.add(btnValider);
        
        // activer le bouton valider quand les deux ia sont configurées
        ActionListener[] listenersIA1 = {btnIA1Facile.getActionListeners()[0], btnIA1Moyenne.getActionListeners()[0]};
        ActionListener[] listenersIA2 = {btnIA2Facile.getActionListeners()[0], btnIA2Moyenne.getActionListeners()[0]};
        
        for (ActionListener listener : listenersIA1) {
            btnIA1Facile.addActionListener(e -> verifierEtActiver(btnValider, difficulteIA1Choisie, difficulteIA2Choisie));
            btnIA1Moyenne.addActionListener(e -> verifierEtActiver(btnValider, difficulteIA1Choisie, difficulteIA2Choisie));
        }
        
        for (ActionListener listener : listenersIA2) {
            btnIA2Facile.addActionListener(e -> verifierEtActiver(btnValider, difficulteIA1Choisie, difficulteIA2Choisie));
            btnIA2Moyenne.addActionListener(e -> verifierEtActiver(btnValider, difficulteIA1Choisie, difficulteIA2Choisie));
        }
        
        btnValider.addActionListener(e -> {
            if (difficulteIA1Choisie[0] != null && difficulteIA2Choisie[0] != null) {
                dialogDifficulte.dispose();
                lancerModeJeu("IAVIA_" + difficulteIA1Choisie[0] + "_" + difficulteIA2Choisie[0]);
            }
        });
        
        dialogDifficulte.add(titre, BorderLayout.NORTH);
        dialogDifficulte.add(panneauIA, BorderLayout.CENTER);
        dialogDifficulte.add(panelValidation, BorderLayout.SOUTH);
        dialogDifficulte.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialogDifficulte.setVisible(true);
    }
    
    /**
     * vérifie si les deux ia sont configurées et active le bouton valider
     */
    private void verifierEtActiver(JButton btnValider, String[] diff1, String[] diff2) {
        btnValider.setEnabled(diff1[0] != null && diff2[0] != null);
    }
    
    private void lancerModeJeu(String mode) {
        this.dispose(); // Ferme le menu principal
        
        // Lancement du jeu avec le mode sélectionné
        SwingUtilities.invokeLater(() -> {
            try {
                new Jeu(mode);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, 
                    "Erreur lors du lancement du jeu : " + e.getMessage(), 
                    "Erreur", 
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                // En cas d'erreur, on relance le menu
                new MenuPrincipal().setVisible(true);
            }
        });
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MenuPrincipal menu = new MenuPrincipal();
            menu.setVisible(true);
        });
    }
}

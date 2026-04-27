package architecture;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Menu principal du jeu Gladius avec interface stylisée
 */
public class MenuPrincipal extends JFrame {
    private static final Color COULEUR_FOND = new Color(40, 42, 54);
    private static final Color COULEUR_BOUTON = new Color(68, 71, 90);
    private static final Color COULEUR_BOUTON_SURVOL = new Color(88, 91, 110);
    private static final Color COULEUR_TEXTE = Color.WHITE;
    
    public MenuPrincipal() {
        setTitle("Gladius - Jeu Tactique IA");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        initUI();
    }
    
    private void initUI() {
        // Panneau principal
        JPanel panneauPrincipal = new JPanel(new BorderLayout());
        panneauPrincipal.setBackground(COULEUR_FOND);
        
        // Titre
        JLabel titre = new JLabel("GLADIUS", SwingConstants.CENTER);
        titre.setFont(new Font("Arial Black", Font.BOLD, 48));
        titre.setForeground(COULEUR_TEXTE);
        titre.setBorder(BorderFactory.createEmptyBorder(40, 0, 20, 0));
        
        JLabel sousTitre = new JLabel("Jeu Tactique au Tour par Tour", SwingConstants.CENTER);
        sousTitre.setFont(new Font("Arial", Font.ITALIC, 18));
        sousTitre.setForeground(new Color(150, 150, 150));
        sousTitre.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0));
        
        // Panneau des boutons
        JPanel panneauBoutons = new JPanel(new GridLayout(4, 1, 0, 15));
        panneauBoutons.setBackground(COULEUR_FOND);
        panneauBoutons.setBorder(BorderFactory.createEmptyBorder(0, 80, 40, 80));
        
        // Création des boutons
        JButton btnPvP = creerBouton("Joueur vs Joueur");
        JButton btnPvIA = creerBouton("Joueur vs IA (Facile)");
        JButton btnIAvIA = creerBouton("IA vs IA (Spectateur)");
        JButton btnQuitter = creerBouton("Quitter");
        
        // Actions des boutons
        btnPvP.addActionListener(e -> lancerModeJeu("PVP"));
        btnPvIA.addActionListener(e -> lancerModeJeu("PVIA_FACILE"));
        btnIAvIA.addActionListener(e -> lancerModeJeu("IAVIA"));
        btnQuitter.addActionListener(e -> System.exit(0));
        
        panneauBoutons.add(btnPvP);
        panneauBoutons.add(btnPvIA);
        panneauBoutons.add(btnIAvIA);
        panneauBoutons.add(btnQuitter);
        
        // Assemblage
        JPanel panneauTitre = new JPanel(new GridLayout(2, 1));
        panneauTitre.setBackground(COULEUR_FOND);
        panneauTitre.add(titre);
        panneauTitre.add(sousTitre);
        
        panneauPrincipal.add(panneauTitre, BorderLayout.NORTH);
        panneauPrincipal.add(panneauBoutons, BorderLayout.CENTER);
        
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

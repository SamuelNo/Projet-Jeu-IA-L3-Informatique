package architecture;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import entite.Personnage;

public class FenetreArene extends JPanel {
    private Arene arene;
    private Jeu jeu;
    private static final int TAILLE_CASE = 60; 
    private static final int MARGE = 30; 
    private static FenetreArene instance; 
    
    // elements d'interface
    private static JLabel infoLabel;
    private static JLabel statsJ1;
    private static JLabel statsJ2;
    
    // pour l'effet de survol (hover)
    private int hoverLigne = -1;
    private int hoverCol = -1;

    public FenetreArene(Jeu jeu) {
        this.jeu = jeu;
        setPreferredSize(new Dimension(10 * TAILLE_CASE + MARGE * 2, 10 * TAILLE_CASE + MARGE * 2));
        setBackground(new Color(40, 42, 54)); // fond sombre

        // ecouteur de clics
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (instance.arene == null) return;
                int col = (e.getX() - MARGE) / TAILLE_CASE;
                int ligne = (e.getY() - MARGE) / TAILLE_CASE;
                if (ligne >= 0 && ligne < 10 && col >= 0 && col < 10) {
                    jeu.clicSurCase(ligne, col);
                }
            }
        });

        // ecouteur de mouvements (pour le hover et les infobulles)
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (instance.arene == null) return;
                int col = (e.getX() - MARGE) / TAILLE_CASE;
                int ligne = (e.getY() - MARGE) / TAILLE_CASE;
                
                if (ligne >= 0 && ligne < 10 && col >= 0 && col < 10) {
                    // On n'active le hover QUE sur les cases vides (0) ou bonus (3, 4)
                    int caseCible = arene.getGrille()[ligne][col];
                    if (caseCible == 0 || caseCible == 3 || caseCible == 4) {
                        if (hoverLigne != ligne || hoverCol != col) {
                            hoverLigne = ligne;
                            hoverCol = col;
                            rafraichir();
                        }
                    } else {
                        // Si on passe sur un mur (-1) ou un joueur (1, 2), on annule le hover
                        if (hoverLigne != -1) {
                            hoverLigne = -1;
                            hoverCol = -1;
                            rafraichir();
                        }
                    }
                } else {
                    if (hoverLigne != -1) {
                        hoverLigne = -1;
                        hoverCol = -1;
                        rafraichir();
                    }
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (arene == null) return; 
        
        int[][] grille = arene.getGrille(); 
        g.setFont(new Font("Arial", Font.BOLD, 14));

        // labels lignes et colonnes
        g.setColor(Color.LIGHT_GRAY);
        char[] lettres = "ABCDEFGHIJ".toCharArray();
        for (int i = 0; i < 10; i++) {
            g.drawString(String.valueOf(i), MARGE + i * TAILLE_CASE + (TAILLE_CASE / 2) - 5, MARGE - 10);
            g.drawString(String.valueOf(lettres[i]), MARGE - 20, MARGE + i * TAILLE_CASE + (TAILLE_CASE / 2) + 5);
        }

        // dessin de la grille
        for (int ligne = 0; ligne < 10; ligne++) {
            for (int col = 0; col < 10; col++) {
                int x = col * TAILLE_CASE + MARGE;
                int y = ligne * TAILLE_CASE + MARGE;

                // couleurs des cases
                if (grille[ligne][col] == -1) g.setColor(new Color(50, 50, 60)); // obstacle
                else if (grille[ligne][col] == 1) g.setColor(new Color(65, 105, 225)); // joueur 1
                else if (grille[ligne][col] == 2) g.setColor(new Color(220, 20, 60)); // joueur 2
                else if (grille[ligne][col] == 3) g.setColor(new Color(255, 193, 7)); // bonus parade
                else if (grille[ligne][col] == 4) g.setColor(new Color(76, 175, 80)); // bonus energie
                else g.setColor(new Color(90, 92, 106)); // case vide sombre
                
                g.fillRect(x, y, TAILLE_CASE, TAILLE_CASE);
                
                // ajout d'un icone texte pour les bonus (pour faire plus propre)
                if (grille[ligne][col] == 3) {
                    g.setColor(Color.BLACK);
                    g.drawString("P", x + 25, y + 35);
                } else if (grille[ligne][col] == 4) {
                    g.setColor(Color.BLACK);
                    g.drawString("E", x + 25, y + 35);
                }

                // effet de survol (hover)
                if (ligne == hoverLigne && col == hoverCol) {
                    g.setColor(new Color(255, 255, 255, 60)); // voile blanc leger
                    g.fillRect(x, y, TAILLE_CASE, TAILLE_CASE);
                }

                // contour de la case
                g.setColor(new Color(30, 32, 40));
                g.drawRect(x, y, TAILLE_CASE, TAILLE_CASE);
            }
        }

        // DESSIN DE L'INFOBULLE (Tooltip) par-dessus la grille
        if (hoverLigne != -1 && hoverCol != -1) {
            int caseHover = grille[hoverLigne][hoverCol];
            if (caseHover == 3 || caseHover == 4) {
                String texteTooltip = (caseHover == 3) ? " Bonus : +1 Parade " : " Bonus : +20 Énergie ";
                int mouseX = hoverCol * TAILLE_CASE + MARGE + TAILLE_CASE / 2;
                int mouseY = hoverLigne * TAILLE_CASE + MARGE - 15; // affiché au-dessus de la case
                
                g.setFont(new Font("Arial", Font.BOLD, 12));
                FontMetrics fm = g.getFontMetrics();
                int largeurTexte = fm.stringWidth(texteTooltip);
                
                g.setColor(new Color(20, 20, 20, 220)); // fond noir transparent
                g.fillRect(mouseX - largeurTexte / 2 - 5, mouseY - 20, largeurTexte + 10, 25);
                g.setColor(Color.WHITE);
                g.drawRect(mouseX - largeurTexte / 2 - 5, mouseY - 20, largeurTexte + 10, 25);
                g.drawString(texteTooltip, mouseX - largeurTexte / 2, mouseY - 3);
            }
        }
    }

    public static void lancerFenetre(Jeu jeu) {
        JFrame fenetre = new JFrame("GLADIUS - Arène Tactique");
        fenetre.setLayout(new BorderLayout());
        fenetre.getContentPane().setBackground(new Color(40, 42, 54));

        instance = new FenetreArene(jeu);
        fenetre.add(instance, BorderLayout.CENTER);

        // --- PANNEAU DU BAS (ACTIONS) ---
        JPanel panelActions = new JPanel();
        panelActions.setBackground(new Color(30, 32, 40));
        panelActions.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton btnAL = new JButton("Att. Légère");
        JButton btnALo = new JButton("Att. Lourde");
        JButton btnAD = new JButton("Att. Distance");
        JButton btnParade = new JButton("Parade");
        JButton btnRepos = new JButton("Se Reposer (+ Énergie)");
        JButton btnPasser = new JButton("Terminer son tour"); // Bouton separé

        // Style des boutons
        JButton[] boutons = {btnAL, btnALo, btnAD, btnParade, btnRepos, btnPasser};
        for (JButton b : boutons) {
            b.setBackground(new Color(60, 62, 74));
            b.setForeground(Color.WHITE);
            b.setFocusPainted(false);
            b.setFont(new Font("Arial", Font.BOLD, 12));
            panelActions.add(b);
        }

        btnAL.addActionListener(e -> jeu.clicAction("AL"));
        btnALo.addActionListener(e -> jeu.clicAction("ALD"));
        btnAD.addActionListener(e -> jeu.clicAction("AD"));
        btnParade.addActionListener(e -> jeu.clicAction("P"));
        btnRepos.addActionListener(e -> jeu.clicAction("R"));
        btnPasser.addActionListener(e -> jeu.clicAction("T")); // 'T' pour Terminer

        fenetre.add(panelActions, BorderLayout.SOUTH);

        // --- PANNEAU LATERAL DROIT (STATS) ---
        JPanel panelStats = new JPanel(new GridLayout(2, 1, 15, 15));
        panelStats.setPreferredSize(new Dimension(220, 0));
        panelStats.setBackground(new Color(40, 42, 54));
        panelStats.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 20));
        
        statsJ1 = new JLabel("<html><div style='color:white;'><b>Joueur 1</b><br>En attente...</div></html>");
        statsJ1.setFont(new Font("Arial", Font.PLAIN, 14));
        statsJ1.setOpaque(true);
        statsJ1.setBackground(new Color(50, 52, 64));
        statsJ1.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(65, 105, 225), 3),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        statsJ2 = new JLabel("<html><div style='color:white;'><b>Joueur 2</b><br>En attente...</div></html>");
        statsJ2.setFont(new Font("Arial", Font.PLAIN, 14));
        statsJ2.setOpaque(true);
        statsJ2.setBackground(new Color(50, 52, 64));
        statsJ2.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 20, 60), 3),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        panelStats.add(statsJ1);
        panelStats.add(statsJ2);
        fenetre.add(panelStats, BorderLayout.EAST);

        // --- BARRE D'INFOS (HAUT) ---
        infoLabel = new JLabel("Bienvenue dans Gladius !", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Arial", Font.BOLD, 16));
        infoLabel.setForeground(new Color(220, 220, 220));
        infoLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        fenetre.add(infoLabel, BorderLayout.NORTH);

        fenetre.pack();
        fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fenetre.setLocationRelativeTo(null); 
        fenetre.setVisible(true);
    }

    public static void setArene(Arene a) {
        if (instance != null) instance.arene = a;
    }

    public static void MAJTexte(String texte) {
        if (infoLabel != null) infoLabel.setText(texte);
    }
    
    public static void MAJStats(Personnage j1, Personnage j2, Personnage actif) {
        if (statsJ1 != null && j1 != null) {
            String tourIndic = (actif == j1) ? "<br><br><font color='#4169e1'><b>▶ À TON TOUR</b></font>" : "";
            statsJ1.setText("<html><div style='color:#E0E0E0;'><b>" + j1.getNom() + " (J1)</b><hr>"
                    + "PV : " + j1.getHp() + "<br>"
                    + "Énergie : " + j1.getEnergie() + "<br>"
                    + "Parades : " + j1.getParade() + "<br>"
                    + "Pas max : " + j1.getPas() 
                    + tourIndic + "</div></html>");
        }
        if (statsJ2 != null && j2 != null) {
            String tourIndic = (actif == j2) ? "<br><br><font color='#dc143c'><b>▶ À TON TOUR</b></font>" : "";
            statsJ2.setText("<html><div style='color:#E0E0E0;'><b>" + j2.getNom() + " (J2)</b><hr>"
                    + "PV : " + j2.getHp() + "<br>"
                    + "Énergie : " + j2.getEnergie() + "<br>"
                    + "Parades : " + j2.getParade() + "<br>"
                    + "Pas max : " + j2.getPas() 
                    + tourIndic + "</div></html>");
        }
    }

    public static void rafraichir() {
        if (instance != null) instance.repaint();
    }
}
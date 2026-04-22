package architecture;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import entite.Personnage;
import exception.*;

public class FenetreArene extends JPanel {
    private Arene arene;
    private Jeu jeu;
    private static final int TAILLE_CASE = 60; 
    private static final int MARGE = 30; 
    private static final Color COULEUR_FOND = new Color(40, 42, 54);
    private static final Color COULEUR_CASE_VIDE = new Color(90, 92, 106);
    private static final Color COULEUR_CASE_HIGHLIGHT = new Color(70, 130, 180, 95);
    private static final Color COULEUR_CASE_CIBLE = new Color(220, 20, 60, 130);
    private static final Color COULEUR_PORTEE_ATTAQUE = new Color(255, 140, 0, 95);
    private static final Color COULEUR_CASE_SELECTION = new Color(255, 255, 255, 70);
    private static final Color COULEUR_UI_PANNEAU = new Color(28, 24, 35);
    private static FenetreArene instance; 
    private static BufferedImage tuileSolA;
    private static BufferedImage tuileSolB;
    private static BufferedImage tuileObstacle;
    private static BufferedImage tuileParade;
    private static BufferedImage tuileEnergie;
    private static BufferedImage chevalierBleu;
    private static BufferedImage chevalierRouge;
    private static BufferedImage archereBleu;
    private static BufferedImage archereRouge;
    private static BufferedImage soigneurBleu;
    private static BufferedImage soigneurRouge;
    
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
        setBackground(COULEUR_FOND); // fond sombre
        initialiserAssetsPixel();

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
                    if (hoverLigne != ligne || hoverCol != col) {
                        hoverLigne = ligne;
                        hoverCol = col;
                        rafraichir();
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
        String etatJeu = jeu.getEtat();

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

                // couleurs de base
                dessinerTuile(g, grille[ligne][col], ligne, col, x, y);

                // Surbrillance des cases accessibles en phase mouvement.
                if ("MOUVEMENT".equals(etatJeu) && caseAccessible(ligne, col)) {
                    g.setColor(COULEUR_CASE_HIGHLIGHT);
                    g.fillRect(x, y, TAILLE_CASE, TAILLE_CASE);
                }

                // Cible clairement marquée en phase ciblage.
                if ("CIBLE".equals(etatJeu) && estCaseAdversaire(ligne, col)) {
                    g.setColor(COULEUR_CASE_CIBLE);
                    g.fillRect(x, y, TAILLE_CASE, TAILLE_CASE);
                }

                // Prévisualisation de portée de l'attaque sélectionnée.
                if ("CIBLE".equals(etatJeu) && caseDansPorteeAttaque(ligne, col)) {
                    g.setColor(COULEUR_PORTEE_ATTAQUE);
                    g.fillRect(x, y, TAILLE_CASE, TAILLE_CASE);
                }
                
                // effet de survol (hover)
                if (ligne == hoverLigne && col == hoverCol) {
                    g.setColor(COULEUR_CASE_SELECTION); // voile blanc leger
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
            String texteTooltip = null;
            if (caseHover == 3) texteTooltip = " Bonus : +1 Parade ";
            else if (caseHover == 4) texteTooltip = " Bonus : +20 Énergie ";
            else if ("MOUVEMENT".equals(etatJeu) && caseAccessible(hoverLigne, hoverCol)) texteTooltip = " Déplacement valide ";
            else if ("CIBLE".equals(etatJeu) && estCaseAdversaire(hoverLigne, hoverCol)) texteTooltip = " Cible ennemie ";
            else if ("CIBLE".equals(etatJeu) && caseDansPorteeAttaque(hoverLigne, hoverCol)) texteTooltip = " Dans la portée ";

            if (texteTooltip != null) {
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
        fenetre.getContentPane().setBackground(COULEUR_FOND);

        instance = new FenetreArene(jeu);
        fenetre.add(instance, BorderLayout.CENTER);

        // --- PANNEAU DU BAS (ACTIONS) ---
        JPanel panelActions = new JPanel();
        panelActions.setBackground(COULEUR_UI_PANNEAU);
        panelActions.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton btnAL = new JButton("AL");
        JButton btnALo = new JButton("ALD");
        JButton btnAD = new JButton("AD");
        JButton btnParade = new JButton("PARADE");
        JButton btnRepos = new JButton("REPOS");
        btnAL.setToolTipText("Attaque légère");
        btnALo.setToolTipText("Attaque lourde");
        btnAD.setToolTipText("Attaque à distance");
        btnParade.setToolTipText("Active la parade pour la prochaine attaque reçue");
        btnRepos.setToolTipText("Régénère de l'énergie et termine le tour");

        // Style des boutons
        JButton[] boutons = {btnAL, btnALo, btnAD, btnParade, btnRepos};
        for (JButton b : boutons) {
            b.setBackground(new Color(60, 62, 74));
            b.setForeground(new Color(235, 235, 235));
            b.setFocusPainted(false);
            b.setFont(new Font("Monospaced", Font.BOLD, 14));
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(129, 95, 203), 2),
                    BorderFactory.createEmptyBorder(8, 14, 8, 14)
            ));
            b.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    b.setBackground(new Color(85, 88, 104));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    b.setBackground(new Color(60, 62, 74));
                }
            });
            panelActions.add(b);
        }

        btnAL.addActionListener(e -> {
            try {
                jeu.clicAction("AL");
            } catch (IllegalActionException ex) {
                JOptionPane.showMessageDialog(null, "Action impossible : " + ex.getMessage());
            }
        });
        btnALo.addActionListener(e -> {
            try {
                jeu.clicAction("ALD");
            } catch (IllegalActionException ex) {
                JOptionPane.showMessageDialog(null, "Action impossible : " + ex.getMessage());
            }
        });
        btnAD.addActionListener(e -> {
            try {
                jeu.clicAction("AD");
            } catch (IllegalActionException ex) {
                JOptionPane.showMessageDialog(null, "Action impossible : " + ex.getMessage());
            }
        });
        btnParade.addActionListener(e -> {
            try {
                jeu.clicAction("P");
            } catch (IllegalActionException ex) {
                JOptionPane.showMessageDialog(null, "Action impossible : " + ex.getMessage());
            }
        });
        btnRepos.addActionListener(e -> {
            try {
                jeu.clicAction("R");
            } catch (IllegalActionException ex) {
                JOptionPane.showMessageDialog(null, "Action impossible : " + ex.getMessage());
            }
        });
        fenetre.add(panelActions, BorderLayout.SOUTH);

        // --- PANNEAU LATERAL DROIT (STATS) ---
        JPanel panelStats = new JPanel(new GridLayout(2, 1, 15, 15));
        panelStats.setPreferredSize(new Dimension(220, 0));
        panelStats.setBackground(COULEUR_FOND);
        panelStats.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 20));
        
        statsJ1 = new JLabel("<html><div style='color:white;'><b>Joueur 1</b><br>En attente...</div></html>");
        statsJ1.setFont(new Font("Arial", Font.PLAIN, 14));
        statsJ1.setOpaque(true);
        statsJ1.setBackground(COULEUR_UI_PANNEAU);
        statsJ1.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(65, 105, 225), 3),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        statsJ2 = new JLabel("<html><div style='color:white;'><b>Joueur 2</b><br>En attente...</div></html>");
        statsJ2.setFont(new Font("Arial", Font.PLAIN, 14));
        statsJ2.setOpaque(true);
        statsJ2.setBackground(COULEUR_UI_PANNEAU);
        statsJ2.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 20, 60), 3),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        panelStats.add(statsJ1);
        panelStats.add(statsJ2);
        fenetre.add(panelStats, BorderLayout.EAST);

        // --- BARRE D'INFOS (HAUT) ---
        infoLabel = new JLabel("Bienvenue dans Gladius !", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        infoLabel.setForeground(new Color(220, 220, 220));
        infoLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        infoLabel.setOpaque(true);
        infoLabel.setBackground(COULEUR_UI_PANNEAU);
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
                    + "PV : " + barre(j1.getHp(), 150, "#4caf50") + " " + (int) j1.getHp() + "<br>"
                    + "Énergie : " + barre(j1.getEnergie(), 150, "#ffca28") + " " + (int) j1.getEnergie() + "<br>"
                    + "Parades : " + j1.getParade() + "<br>"
                    + "PM max : " + j1.getPas()
                    + ((actif == j1 && instance != null && instance.jeu != null) ? "<br>PM restants : " + instance.jeu.getPmRestants() : "")
                    + tourIndic + "</div></html>");
        }
        if (statsJ2 != null && j2 != null) {
            String tourIndic = (actif == j2) ? "<br><br><font color='#dc143c'><b>▶ À TON TOUR</b></font>" : "";
            statsJ2.setText("<html><div style='color:#E0E0E0;'><b>" + j2.getNom() + " (J2)</b><hr>"
                    + "PV : " + barre(j2.getHp(), 150, "#4caf50") + " " + (int) j2.getHp() + "<br>"
                    + "Énergie : " + barre(j2.getEnergie(), 150, "#ffca28") + " " + (int) j2.getEnergie() + "<br>"
                    + "Parades : " + j2.getParade() + "<br>"
                    + "PM max : " + j2.getPas()
                    + ((actif == j2 && instance != null && instance.jeu != null) ? "<br>PM restants : " + instance.jeu.getPmRestants() : "")
                    + tourIndic + "</div></html>");
        }
    }

    public static void rafraichir() {
        if (instance != null) instance.repaint();
    }

    private boolean estCaseAdversaire(int ligne, int col) {
        Personnage adv = jeu.getAdversaire();
        if (adv == null || adv.getPosition() == null) return false;
        return adv.getPosition().getLigne() == ligne && adv.getPosition().getColonne() == col;
    }

    private boolean caseAccessible(int ligne, int col) {
        if (jeu.getJoueurActif() == null || jeu.getAdversaire() == null) return false;
        int[][] grille = arene.getGrille();
        if (ligne < 0 || col < 0 || ligne >= grille.length || col >= grille[0].length) return false;
        if (grille[ligne][col] == -1 || grille[ligne][col] == 1 || grille[ligne][col] == 2) return false;

        int dist = Math.abs(ligne - jeu.getJoueurActif().getPosition().getLigne())
                + Math.abs(col - jeu.getJoueurActif().getPosition().getColonne());
        return dist > 0 && dist <= jeu.getPmRestants();
    }

    private boolean caseDansPorteeAttaque(int ligne, int col) {
        if (jeu.getJoueurActif() == null) return false;
        String attaque = jeu.getAttaqueEnCours();
        int portee = jeu.getPorteeAttaque(attaque);
        if (portee <= 0) return false;

        int dist = Math.abs(ligne - jeu.getJoueurActif().getPosition().getLigne())
                + Math.abs(col - jeu.getJoueurActif().getPosition().getColonne());
        return dist <= portee;
    }

    private static String barre(double valeur, double max, String couleur) {
        int largeur = 10;
        int remplie = (int) Math.round(Math.max(0, Math.min(max, valeur)) / max * largeur);
        StringBuilder sb = new StringBuilder();
        sb.append("<font color='").append(couleur).append("'>");
        for (int i = 0; i < remplie; i++) sb.append("■");
        sb.append("</font><font color='#555'>");
        for (int i = remplie; i < largeur; i++) sb.append("■");
        sb.append("</font>");
        return sb.toString();
    }

    private void dessinerTuile(Graphics g, int valeurCase, int ligne, int col, int x, int y) {
        BufferedImage tuile;
        if (valeurCase == -1) tuile = tuileObstacle;
        else if (valeurCase == 3) tuile = tuileParade;
        else if (valeurCase == 4) tuile = tuileEnergie;
        else tuile = ((ligne + col) % 2 == 0) ? tuileSolA : tuileSolB;
        g.drawImage(tuile, x, y, TAILLE_CASE, TAILLE_CASE, null);

        if (valeurCase == 1 && arene != null) {
            BufferedImage sprite = spritePour(arene.getJoueur1(), true);
            g.drawImage(sprite, x + 6, y + 6, TAILLE_CASE - 12, TAILLE_CASE - 12, null);
        }
        if (valeurCase == 2 && arene != null) {
            BufferedImage sprite = spritePour(arene.getJoueur2(), false);
            g.drawImage(sprite, x + 6, y + 6, TAILLE_CASE - 12, TAILLE_CASE - 12, null);
        }
    }

    private static void initialiserAssetsPixel() {
        if (tuileSolA != null) return;
        tuileSolA = creerTuile(new Color(92, 78, 66), new Color(108, 92, 76));
        tuileSolB = creerTuile(new Color(84, 70, 58), new Color(100, 84, 70));
        tuileObstacle = creerTuileMur();
        tuileParade = creerTuile(new Color(179, 129, 38), new Color(214, 159, 58));
        tuileEnergie = creerTuile(new Color(36, 122, 90), new Color(48, 156, 116));
        chevalierBleu = creerSpriteChevalier(new Color(91, 131, 255), new Color(213, 224, 255));
        chevalierRouge = creerSpriteChevalier(new Color(220, 74, 74), new Color(255, 214, 214));
        archereBleu = creerSpriteArchere(new Color(91, 131, 255), new Color(213, 224, 255));
        archereRouge = creerSpriteArchere(new Color(220, 74, 74), new Color(255, 214, 214));
        soigneurBleu = creerSpriteSoigneur(new Color(91, 131, 255), new Color(213, 224, 255));
        soigneurRouge = creerSpriteSoigneur(new Color(220, 74, 74), new Color(255, 214, 214));
    }

    private static BufferedImage creerTuile(Color c1, Color c2) {
        int size = 16;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                boolean motif = ((x / 4) + (y / 4)) % 2 == 0;
                img.setRGB(x, y, (motif ? c1 : c2).getRGB());
            }
        }
        return img;
    }

    private static BufferedImage creerTuileMur() {
        int s = 16;
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        // Fond pierre clair
        for (int y = 0; y < s; y++) {
            for (int x = 0; x < s; x++) {
                boolean motif = ((x / 4) + (y / 4)) % 2 == 0;
                Color c = motif ? new Color(78, 86, 104) : new Color(98, 108, 126);
                img.setRGB(x, y, c.getRGB());
            }
        }
        // Joints de briques
        g2.setColor(new Color(58, 64, 78));
        g2.drawLine(0, 5, 15, 5);
        g2.drawLine(0, 10, 15, 10);
        g2.drawLine(3, 0, 3, 5);
        g2.drawLine(11, 0, 11, 5);
        g2.drawLine(7, 5, 7, 10);
        g2.drawLine(14, 5, 14, 10);
        g2.drawLine(2, 10, 2, 15);
        g2.drawLine(9, 10, 9, 15);
        // Symbole inaccessible
        g2.setColor(new Color(196, 58, 58));
        g2.fillRect(4, 4, 8, 8);
        g2.setColor(new Color(242, 242, 242));
        g2.fillRect(5, 7, 6, 2);
        // Bordure claire
        g2.setColor(new Color(180, 188, 205));
        g2.drawRect(0, 0, 15, 15);
        g2.dispose();
        return img;
    }

    private static BufferedImage creerSpriteChevalier(Color principal, Color accent) {
        int s = 16;
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(new Color(0, 0, 0, 0));
        g2.fillRect(0, 0, s, s);
        // Casque
        g2.setColor(new Color(165, 170, 180));
        g2.fillRect(5, 2, 6, 3);
        g2.setColor(new Color(120, 126, 140));
        g2.fillRect(4, 4, 8, 2);
        // Armure
        g2.setColor(principal);
        g2.fillRect(4, 6, 8, 6);
        g2.setColor(accent);
        g2.fillRect(6, 8, 4, 2);
        // Épée
        g2.setColor(new Color(190, 195, 205));
        g2.fillRect(11, 6, 2, 5);
        g2.setColor(new Color(135, 105, 65));
        g2.fillRect(10, 10, 3, 1);
        g2.setColor(new Color(30, 30, 30));
        g2.fillRect(5, 12, 2, 2);
        g2.fillRect(9, 12, 2, 2);
        g2.dispose();
        return img;
    }

    private static BufferedImage creerSpriteArchere(Color principal, Color accent) {
        int s = 16;
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(new Color(0, 0, 0, 0));
        g2.fillRect(0, 0, s, s);
        // Capuche
        g2.setColor(new Color(72, 82, 56));
        g2.fillRect(5, 2, 6, 3);
        // Tunique
        g2.setColor(principal);
        g2.fillRect(4, 5, 8, 7);
        g2.setColor(accent);
        g2.fillRect(6, 7, 4, 2);
        // Arc
        g2.setColor(new Color(133, 92, 53));
        g2.fillRect(2, 5, 1, 7);
        g2.fillRect(3, 4, 1, 1);
        g2.fillRect(3, 12, 1, 1);
        g2.setColor(new Color(220, 220, 220));
        g2.fillRect(3, 5, 1, 7);
        g2.setColor(new Color(30, 30, 30));
        g2.fillRect(5, 12, 2, 2);
        g2.fillRect(9, 12, 2, 2);
        g2.dispose();
        return img;
    }

    private static BufferedImage creerSpriteSoigneur(Color principal, Color accent) {
        int s = 16;
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(new Color(0, 0, 0, 0));
        g2.fillRect(0, 0, s, s);
        // Coiffe
        g2.setColor(new Color(205, 190, 120));
        g2.fillRect(5, 2, 6, 2);
        // Robe
        g2.setColor(principal);
        g2.fillRect(4, 4, 8, 8);
        g2.setColor(accent);
        g2.fillRect(6, 7, 4, 3);
        // Bâton
        g2.setColor(new Color(134, 97, 63));
        g2.fillRect(12, 4, 1, 8);
        g2.setColor(new Color(120, 224, 170));
        g2.fillRect(11, 3, 3, 2);
        g2.setColor(new Color(30, 30, 30));
        g2.fillRect(5, 12, 2, 2);
        g2.fillRect(9, 12, 2, 2);
        g2.dispose();
        return img;
    }

    private static BufferedImage spritePour(Personnage p, boolean equipeBleue) {
        if (p == null || p.getNom() == null) return equipeBleue ? chevalierBleu : chevalierRouge;
        String nom = p.getNom().toLowerCase();
        if (nom.contains("arch")) return equipeBleue ? archereBleu : archereRouge;
        if (nom.contains("soign")) return equipeBleue ? soigneurBleu : soigneurRouge;
        return equipeBleue ? chevalierBleu : chevalierRouge;
    }
}
package architecture;

import java.awt.*;
import javax.swing.*;

public class FenetreArene extends JPanel {
    private Arene arene;
    private static final int TAILLE_CASE = 60; 
    private static final int MARGE = 30; // marge pour caler les labels
    private static FenetreArene instance; 

    public FenetreArene(Arene arene) {
        this.arene = arene;
        setPreferredSize(new Dimension(10 * TAILLE_CASE + MARGE, 10 * TAILLE_CASE + MARGE));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        int[][] grille = arene.getGrille(); 
        g.setFont(new Font("Arial", Font.BOLD, 14));

        // labels des colonnes (0 a 9)
        for (int col = 0; col < 10; col++) {
            g.setColor(Color.BLACK);
            g.drawString(String.valueOf(col), MARGE + col * TAILLE_CASE + (TAILLE_CASE / 2) - 5, MARGE - 10);
        }

        // labels des lignes (a a j)
        char[] lettres = "ABCDEFGHIJ".toCharArray();
        for (int ligne = 0; ligne < 10; ligne++) {
            g.setColor(Color.BLACK);
            g.drawString(String.valueOf(lettres[ligne]), MARGE - 20, MARGE + ligne * TAILLE_CASE + (TAILLE_CASE / 2) + 5);
        }

        for (int ligne = 0; ligne < 10; ligne++) {
            for (int col = 0; col < 10; col++) {
                int x = col * TAILLE_CASE + MARGE;
                int y = ligne * TAILLE_CASE + MARGE;

                if (grille[ligne][col] == -1) {
                    g.setColor(Color.DARK_GRAY); 
                    g.fillRect(x, y, TAILLE_CASE, TAILLE_CASE);
                } else if (grille[ligne][col] == 1) {
                    g.setColor(Color.BLUE); 
                    g.fillRect(x, y, TAILLE_CASE, TAILLE_CASE);
                } else if (grille[ligne][col] == 2) {
                    g.setColor(Color.RED); 
                    g.fillRect(x, y, TAILLE_CASE, TAILLE_CASE);
                } else {
                    g.setColor(Color.LIGHT_GRAY); 
                    g.fillRect(x, y, TAILLE_CASE, TAILLE_CASE);
                }

                g.setColor(Color.BLACK);
                g.drawRect(x, y, TAILLE_CASE, TAILLE_CASE);
            }
        }
    }

    public static void lancerFenetre(Arene arene) {
        JFrame fenetre = new JFrame("G L A D I U S - Arène");
        FenetreArene panel = new FenetreArene(arene);
        instance = panel; 
        
        fenetre.add(panel);
        fenetre.pack();
        fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fenetre.setLocationRelativeTo(null); 
        fenetre.setVisible(true);
    }

    public static void rafraichir() {
        if (instance != null) {
            instance.repaint();
        }
    }
}
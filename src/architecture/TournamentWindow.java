package architecture;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Fenêtre de configuration et de lancement du tournoi IA vs IA, affichant les résultats et les statistiques après chaque tournoi. Permet de choisir les IA, le nombre de combats, les profondeurs, et d'interrompre le tournoi en cours.
 */

public class TournamentWindow extends JFrame {
    private JComboBox<String> cbIA1;
    private JComboBox<String> cbIA2;
    private JSpinner spNbMatches;
    private JSpinner spProfondeur1;
    private JSpinner spProfondeur2;
    private JButton btnLancer;
    private JButton btnFinir;
    private JTabbedPane onglets;
    private JTable tableResultats;
    private DefaultTableModel tableModel;
    private JTextArea taStats;
    private JLabel lblStatus;
    private Process currentProcess;
    private String fichierResultatsActuel = "data/txt/resultats_tournoi.txt";
    private String fichierNulsActuel = "data/txt/details_matchs_nuls.txt";

    public TournamentWindow() {
        /**
         * Construit la fenêtre de configuration du tournoi IA vs IA.
         * Permet de choisir les IA, profondeurs et lancer/arrêter les tournois.
         */
        setTitle("Tournoi IA vs IA");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setResizable(true);

        JPanel config = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.WEST;
        config.add(new JLabel("IA Joueur 1:"), c);
        c.gridx = 1; cbIA1 = new JComboBox<>(new String[]{"IAFACILE","IAMOYENNE","IADIFFICILE"});
        config.add(cbIA1, c);

        c.gridx = 0; c.gridy = 1; config.add(new JLabel("IA Joueur 2:"), c);
        c.gridx = 1; cbIA2 = new JComboBox<>(new String[]{"IAFACILE","IAMOYENNE","IADIFFICILE"});
        config.add(cbIA2, c);

        c.gridx = 0; c.gridy = 2; config.add(new JLabel("Nombre de combats:"), c);
        c.gridx = 1;
        spNbMatches = new JSpinner(new SpinnerNumberModel(40, 1, 9999, 1));
        JComponent editor = spNbMatches.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) editor).getTextField().setColumns(6);
        }
        config.add(spNbMatches, c);

        c.gridx = 0; c.gridy = 3; config.add(new JLabel("Profondeur J1:"), c);
        c.gridx = 1;
        spProfondeur1 = new JSpinner(new SpinnerNumberModel(2, 1, 5, 1));
        JComponent depthEditor1 = spProfondeur1.getEditor();
        if (depthEditor1 instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) depthEditor1).getTextField().setColumns(4);
        }
        config.add(spProfondeur1, c);

        c.gridx = 0; c.gridy = 4; config.add(new JLabel("Profondeur J2:"), c);
        c.gridx = 1;
        spProfondeur2 = new JSpinner(new SpinnerNumberModel(2, 1, 5, 1));
        JComponent depthEditor2 = spProfondeur2.getEditor();
        if (depthEditor2 instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) depthEditor2).getTextField().setColumns(4);
        }
        config.add(spProfondeur2, c);

        c.gridx = 0; c.gridy = 5; c.gridwidth = 2; c.anchor = GridBagConstraints.CENTER;
        btnLancer = new JButton("Lancer le tournoi");
        btnFinir = new JButton("Finir Tournoi");
        btnFinir.setEnabled(false);
        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        pnlBtns.add(btnLancer);
        pnlBtns.add(btnFinir);
        config.add(pnlBtns, c);

        add(config, BorderLayout.NORTH);

        onglets = new JTabbedPane();
        taStats = new JTextArea(); taStats.setEditable(false);

        lblStatus = new JLabel("");
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        add(lblStatus, BorderLayout.SOUTH);

        // Table model pour le recapitulatif par combat
        String[] colonnes = new String[]{"Combat","Issue","Matchup","Profondeur J1/J2","Tours","Duree","avgCoupJ1","avgCoupJ2"};
        tableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableResultats = new JTable(tableModel);
        tableResultats.setAutoCreateRowSorter(true);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        tableResultats.setRowSorter(sorter);
        onglets.addTab("Tableau/Recap", new JScrollPane(tableResultats));
        // onglet Journal des nuls supprimé : le fichier details_matchs_nuls.txt est conservé sur disque
        onglets.addTab("Statistiques", new JScrollPane(taStats));

        add(onglets, BorderLayout.CENTER);

        btnLancer.addActionListener((ActionEvent e) -> lancerTournoi());
        btnFinir.addActionListener((ActionEvent e) -> {
            finirTournoi();
        });
    }

    //** Lancement du tournoi dans un thread séparé pour ne pas bloquer l'interface, avec gestion de la configuration, des fichiers de résultats et de l'état des boutons. */
    private void lancerTournoi() {
        final String ia1 = (String) cbIA1.getSelectedItem();
        final String ia2 = (String) cbIA2.getSelectedItem();
        System.out.println("[TOURNOI] === LANCEMENT D'UN NOUVEAU TOURNOI ===");
        System.out.println("[TOURNOI] IA1: " + ia1 + ", IA2: " + ia2);
        try {
            spNbMatches.commitEdit();
            spProfondeur1.commitEdit();
            spProfondeur2.commitEdit();
        } catch (java.text.ParseException ex) {
            JOptionPane.showMessageDialog(this,
                "Valeur invalide, valeur précédente conservée.",
                "Tournoi", JOptionPane.WARNING_MESSAGE);
        }
        final int nb = ((Number) spNbMatches.getValue()).intValue();
        final int profondeur1 = ((Number) spProfondeur1.getValue()).intValue();
        final int profondeur2 = ((Number) spProfondeur2.getValue()).intValue();
        // Use base filenames (no per-depth files)
        final String fichierResultats = "data/txt/resultats_tournoi.txt";
        final String fichierNuls = "data/txt/details_matchs_nuls.txt";
        fichierResultatsActuel = fichierResultats;
        fichierNulsActuel = fichierNuls;
        System.out.println("[TOURNOI] Nombre de combats: " + nb);
        System.out.println("[TOURNOI] Profondeur J1: " + profondeur1 + " Profondeur J2: " + profondeur2);
        
        // Supprimer les anciens fichiers de résultats
        System.out.println("[TOURNOI] Suppression des anciens fichiers...");
        // remove base files to start fresh
        new File("data/txt/resultats_tournoi.txt").delete();
        new File("data/txt/details_matchs_nuls.txt").delete();
        System.out.println("[TOURNOI] Fichiers supprimés");

        btnLancer.setEnabled(false);
        btnFinir.setEnabled(true);
        onglets.setEnabled(false);
        lblStatus.setText("Combat en cours... " + nb + " partie(s) prévues. ProfondeurJ1=" + profondeur1 + " ProfondeurJ2=" + profondeur2);
        System.out.println("[TOURNOI] Statut mis à jour dans l'UI");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    String javaCmd = System.getProperty("java.home") + "/bin/java";
                    System.out.println("[TOURNOI] Commande Java: " + javaCmd);
                    String workingDir = System.getProperty("user.dir", ".");
                    System.out.println("[TOURNOI] Répertoire de travail: " + workingDir);
                    
                    ProcessBuilder pb = new ProcessBuilder(
                        javaCmd, "-cp", "bin", "test.SimulateIAvIA",
                        ia1, ia2, String.valueOf(nb), String.valueOf(30), fichierResultats, fichierNuls, String.valueOf(profondeur1), String.valueOf(profondeur2)
                    );
                    System.out.println("[TOURNOI] Commande: " + String.join(" ", pb.command()));
                    pb.directory(new File(workingDir));
                    pb.redirectErrorStream(true);
                    currentProcess = pb.start();
                    System.out.println("[TOURNOI] Processus lancé avec PID: " + currentProcess.pid());

                    System.out.println("[TOURNOI] Lecture de la sortie du processus...");
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(currentProcess.getInputStream(), StandardCharsets.UTF_8))) {
                        int lineCount = 0;
                        while (reader.readLine() != null) {
                            lineCount++;
                        }
                        System.out.println("[TOURNOI] Sortie consommée (" + lineCount + " lignes)");
                    }

                    int exitCode = currentProcess.waitFor();
                    System.out.println("[TOURNOI] Processus terminé avec code: " + exitCode);
                } catch (IOException | InterruptedException ex) {
                    System.out.println("[TOURNOI] ERREUR lors du lancement: " + ex.getMessage());
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> taStats.append("Erreur processus: " + ex.getMessage() + "\n"));
                }
                return null;
            }

            @Override
            protected void done() {
                    System.out.println("[TOURNOI] SwingWorker terminé, chargement des résultats...");
                    btnLancer.setEnabled(true);
                    btnFinir.setEnabled(false);
                    onglets.setEnabled(true);
                    lblStatus.setText("");
                    chargerEtAfficherResultats();
                    System.out.println("[TOURNOI] === FIN DU TOURNOI ===\n");
                    currentProcess = null;
            }
        };
        worker.execute();
    }

    //** Gestion de l'arrêt du tournoi : envoie un signal de termination au processus en cours, attend sa fin, met à jour l'interface et charge les résultats partiels. */
    private void finirTournoi() {
        if (currentProcess != null) {
            System.out.println("[TOURNOI] Arrêt du tournoi demandé par l'utilisateur...");
            // Envoyer SIGTERM au processus (destroy() sur Unix envoie SIGTERM)
            currentProcess.destroy();
            System.out.println("[TOURNOI] Signal de termination envoyé, attente du processus (10s max)...");
            
            // Attendre que le processus se termine gracefully (max 10 secondes)
            try {
                boolean terminated = currentProcess.waitFor(10, java.util.concurrent.TimeUnit.SECONDS);
                if (terminated) {
                    System.out.println("[TOURNOI] Processus terminé gracefully");
                } else {
                    System.out.println("[TOURNOI] Processus n'a pas répondu au SIGTERM, forçage de l'arrêt...");
                    currentProcess.destroyForcibly();
                    Thread.sleep(500);
                    System.out.println("[TOURNOI] Processus arrêté de force");
                }
            } catch (InterruptedException ex) {
                System.out.println("[TOURNOI] Erreur lors de l'attente: " + ex.getMessage());
                currentProcess.destroyForcibly();
            }
            
            lblStatus.setText("Tournoi interrompu par l'utilisateur.");
            btnFinir.setEnabled(false);
            btnLancer.setEnabled(true);
            onglets.setEnabled(true);
            // Charger ce qui a été écrit jusqu'ici
            System.out.println("[TOURNOI] Chargement des résultats partiels...");
            chargerEtAfficherResultats();
            currentProcess = null;
            System.out.println("[TOURNOI] === ARRÊT TERMINÉ ===");
        }
    }

    //** Lit les fichiers de résultats et de nuls, extrait les statistiques et les détails par combat, et met à jour l'interface avec ces informations. */
    private void chargerEtAfficherResultats() {
        Path pathRes = Paths.get(fichierResultatsActuel);
        Path pathNuls = Paths.get(fichierNulsActuel);
        try {
            // Clear previous table
            tableModel.setRowCount(0);
            if (Files.exists(pathRes)) {
                java.util.List<String> lines = Files.readAllLines(pathRes, StandardCharsets.UTF_8);
                // Cherche la section 'Details par combat'
                int start = -1;
                for (int i = 0; i < lines.size(); i++) {
                    if (lines.get(i).trim().equalsIgnoreCase("=== Details par combat ===")) {
                        start = i + 1;
                        break;
                    }
                }
                if (start != -1) {
                    for (int i = start; i < lines.size(); i++) {
                        String line = lines.get(i).trim();
                        if (line.isEmpty()) continue;
                        if (!line.startsWith("Combat ")) continue;
                        // Exemple: Combat 1 | issue=J2 | matchup=Chevalier vs Chevalier | tours=16 | duree=1,113 s | avgCoupJ1=0,356 ms | avgCoupJ2=134,965 ms
                        String[] parts = line.split(" \\| ");
                        String combat = parts.length > 0 ? parts[0].replace("Combat ", "").trim() : "";
                        String issue = "";
                        String matchup = "";
                        String profondeur = "";
                        String tours = "";
                        String duree = "";
                        String avg1 = "";
                        String avg2 = "";
                        for (String p : parts) {
                            p = p.trim();
                            if (p.startsWith("issue=")) issue = p.substring(6);
                            else if (p.startsWith("matchup=")) matchup = p.substring(8);
                            else if (p.startsWith("profondeurJ1=")) {
                                String pj1 = p.substring(12);
                                // try to find profondeurJ2 in same part list later
                                // temporarily store as pj1/pj2
                                profondeur = pj1;
                            } else if (p.startsWith("profondeurJ2=")) {
                                String pj2 = p.substring(12);
                                if (profondeur.isEmpty()) profondeur = "?/" + pj2; else profondeur = profondeur + "/" + pj2;
                            } else if (p.startsWith("profondeur=")) {
                                profondeur = p.substring(11);
                            }
                            else if (p.startsWith("tours=")) tours = p.substring(6);
                            else if (p.startsWith("duree=")) duree = p.substring(6);
                            else if (p.startsWith("avgCoupJ1=")) avg1 = p.substring(10);
                            else if (p.startsWith("avgCoupJ2=")) avg2 = p.substring(10);
                        }
                        tableModel.addRow(new Object[]{combat, issue, matchup, profondeur, tours, duree, avg1, avg2});
                    }
                } else {
                    // pas de section détails, rien à remplir
                }
            } else {
                tableModel.setRowCount(0);
            }
            // Ne pas afficher le journal des nuls dans l'UI ; le fichier reste disponible sur le disque.

            // Remplir statistiques simples depuis resultats (premières lignes)
            if (Files.exists(pathRes)) {
                java.util.List<String> lines = Files.readAllLines(pathRes, StandardCharsets.UTF_8);
                StringBuilder stats = new StringBuilder();
                for (String l : lines) {
                    if (l.startsWith("Profondeur J1") || l.startsWith("Profondeur IA") || l.startsWith("Victoires J1") || l.startsWith("Victoires J2") || l.startsWith("Matchs nuls") || l.startsWith("Temps total tournoi") || l.startsWith("Temps moyen coup")) {
                        stats.append(l).append('\n');
                    }
                }
                taStats.setText(stats.toString());
            }
        } catch (IOException e) {
            taStats.setText("Erreur lecture fichiers: " + e.getMessage());
        }
    }
}

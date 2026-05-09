package architecture;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TournamentWindow extends JFrame {
    private JComboBox<String> cbIA1;
    private JComboBox<String> cbIA2;
    private JSpinner spNbMatches;
    private JButton btnLancer;
    private JButton btnFinir;
    private JTabbedPane onglets;
    private JTable tableResultats;
    private DefaultTableModel tableModel;
    private JTextArea taStats;
    private JLabel lblStatus;
    private Process currentProcess;

    public TournamentWindow() {
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

        c.gridx = 0; c.gridy = 3; c.gridwidth = 2; c.anchor = GridBagConstraints.CENTER;
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
        String[] colonnes = new String[]{"Combat","Issue","Matchup","Tours","Duree","avgCoupJ1","avgCoupJ2"};
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

    private void lancerTournoi() {
        final String ia1 = (String) cbIA1.getSelectedItem();
        final String ia2 = (String) cbIA2.getSelectedItem();
        try {
            spNbMatches.commitEdit();
        } catch (java.text.ParseException ex) {
            JOptionPane.showMessageDialog(this,
                "Nombre de combats invalide, valeur précédente conservée.",
                "Tournoi", JOptionPane.WARNING_MESSAGE);
        }
        final int nb = ((Number) spNbMatches.getValue()).intValue();

        btnLancer.setEnabled(false);
        btnFinir.setEnabled(true);
        onglets.setEnabled(false);
        lblStatus.setText("Combat en cours... " + nb + " partie(s) prévues.");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    // Construire la commande java pour lancer SimulateIAvIA dans un processus séparé
                    String javaCmd = System.getProperty("java.home") + "/bin/java";
                    ProcessBuilder pb = new ProcessBuilder(
                        javaCmd, "-cp", "bin", "test.SimulateIAvIA",
                        ia1, ia2, String.valueOf(nb), String.valueOf(50), "resultats_tournoi.txt", "details_matchs_nuls.txt"
                    );
                    String workingDir = System.getProperty("user.dir", ".");
                    pb.directory(new File(workingDir));
                    pb.redirectErrorStream(true);
                    currentProcess = pb.start();

                    // Lire la sortie du processus et l'afficher dans le journal en temps réel
                    // Consommer la sortie du processus pour éviter blocage, sans l'afficher
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(currentProcess.getInputStream(), StandardCharsets.UTF_8))) {
                        while (reader.readLine() != null) {
                            // lecture et jet
                        }
                    }

                    currentProcess.waitFor();
                } catch (IOException | InterruptedException ex) {
                    SwingUtilities.invokeLater(() -> taStats.append("Erreur processus: " + ex.getMessage() + "\n"));
                }
                return null;
            }

            @Override
            protected void done() {
                    btnLancer.setEnabled(true);
                    btnFinir.setEnabled(false);
                    onglets.setEnabled(true);
                    lblStatus.setText("");
                    chargerEtAfficherResultats();
                    currentProcess = null;
            }
        };
        worker.execute();
    }

    private void finirTournoi() {
        if (currentProcess != null) {
            currentProcess.destroy();
            lblStatus.setText("Tournoi interrompu par l'utilisateur.");
            btnFinir.setEnabled(false);
            btnLancer.setEnabled(true);
            onglets.setEnabled(true);
            // Charger ce qui a été écrit jusqu'ici
            chargerEtAfficherResultats();
            currentProcess = null;
        }
    }

    private void chargerEtAfficherResultats() {
        Path pathRes = Paths.get("resultats_tournoi.txt");
        Path pathNuls = Paths.get("details_matchs_nuls.txt");
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
                        String tours = "";
                        String duree = "";
                        String avg1 = "";
                        String avg2 = "";
                        for (String p : parts) {
                            p = p.trim();
                            if (p.startsWith("issue=")) issue = p.substring(6);
                            else if (p.startsWith("matchup=")) matchup = p.substring(8);
                            else if (p.startsWith("tours=")) tours = p.substring(6);
                            else if (p.startsWith("duree=")) duree = p.substring(6);
                            else if (p.startsWith("avgCoupJ1=")) avg1 = p.substring(10);
                            else if (p.startsWith("avgCoupJ2=")) avg2 = p.substring(10);
                        }
                        tableModel.addRow(new Object[]{combat, issue, matchup, tours, duree, avg1, avg2});
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
                    if (l.startsWith("Victoires J1") || l.startsWith("Victoires J2") || l.startsWith("Matchs nuls") || l.startsWith("Temps total tournoi") || l.startsWith("Temps moyen coup")) {
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

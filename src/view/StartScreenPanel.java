package view;

import model.Hasil;
import viewmodel.GameViewModel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StartScreenPanel extends JPanel {
    private final MainWindow mainWindow;
    private final GameViewModel viewModel;
    private final JTextField usernameField;
    private final JTable scoreTable;
    private final DefaultTableModel tableModel;

    public StartScreenPanel(MainWindow mainWindow, GameViewModel viewModel) {
        this.mainWindow = mainWindow;
        this.viewModel = viewModel;
        this.setLayout(new BorderLayout(10, 10));
        this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("COLLECT THE SKILL BALLS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        this.add(titleLabel, BorderLayout.NORTH);

        // Center Panel (Input and Table)
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        // Username Input
        JPanel usernamePanel = new JPanel();
        usernamePanel.add(new JLabel("Username:"));
        usernameField = new JTextField(15);
        usernamePanel.add(usernameField);
        centerPanel.add(usernamePanel, BorderLayout.NORTH);

        // Score Table
        String[] columnNames = {"Username", "Score", "Count"};
        tableModel = new DefaultTableModel(columnNames, 0);
        scoreTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(scoreTable); // Table with a scroll bar
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        this.add(centerPanel, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton playButton = new JButton("Play");
        JButton quitButton = new JButton("Quit");
        buttonPanel.add(playButton);
        buttonPanel.add(quitButton);
        this.add(buttonPanel, BorderLayout.SOUTH);

        // Action Listeners
        playButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a username.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // The game only starts if the Play button is clicked
            viewModel.startGame(username, mainWindow.getWidth(), mainWindow.getHeight());
            mainWindow.showPanel("GamePanel");
        });

        quitButton.addActionListener(e -> System.exit(0));

        // Populate table when panel is shown
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                populateScoreTable();
            }
        });

        // Panggil sekali saat pertama kali dibuat untuk memastikan data tampil saat startup
        populateScoreTable();
    }

    private void populateScoreTable() {
        tableModel.setRowCount(0); // Clear existing data
        List<Hasil> scores = viewModel.getAllScores();
        for (Hasil hasil : scores) {
            tableModel.addRow(new Object[]{hasil.getUsername(), hasil.getSkor(), hasil.getCount()});
        }
    }
}
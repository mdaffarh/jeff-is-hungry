package view;

import viewmodel.GameViewModel;
import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final GameViewModel viewModel;
    private final GamePanel gamePanel;
    private final StartScreenPanel startScreenPanel;

    public MainWindow() {
        this.viewModel = new GameViewModel();
        this.cardLayout = new CardLayout();
        this.mainPanel = new JPanel(cardLayout);

        // Create panels
        this.startScreenPanel = new StartScreenPanel(this, viewModel);
        this.gamePanel = new GamePanel(this, viewModel);

        // Add panels to the main card layout
        mainPanel.add(startScreenPanel, "StartScreen");
        mainPanel.add(gamePanel, "GamePanel");

        this.add(mainPanel);
        this.setTitle("Shark Food Collector");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 600);
        this.setLocationRelativeTo(null); // Center the window
        this.setVisible(true);
    }

    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
        if (panelName.equals("GamePanel")) {
            gamePanel.requestFocusInWindow();
        }
    }

    public static void main(String[] args) {
        // Run the GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(MainWindow::new);
    }
}
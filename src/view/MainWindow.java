package view;

import viewmodel.GameViewModel;
import javax.swing.*;
import java.awt.*;
import audio.AudioManager; // Import AudioManager

public class MainWindow extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final GameViewModel viewModel;
    private final GamePanel gamePanel;
    private final StartScreenPanel startScreenPanel;

    private Cursor menuCursor, gameCursor;

    public MainWindow() {
        this.viewModel = new GameViewModel();

        loadAssetsAndAudio(); // Panggil metode baru

        this.cardLayout = new CardLayout();
        this.mainPanel = new JPanel(cardLayout);

        // Create panels
        this.startScreenPanel = new StartScreenPanel(this, viewModel);
        this.gamePanel = new GamePanel(this, viewModel);



        // Add panels to the main card layout
        mainPanel.add(startScreenPanel, "StartScreen");
        mainPanel.add(gamePanel, "GamePanel");

        this.add(mainPanel);
        this.setTitle("Jeff is Hungry");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(900, 700);
        this.setLocationRelativeTo(null); // Center the window

        showPanel("StartScreen"); // Tampilkan menu awal dan putar musik

        this.setVisible(true);
    }

    private void loadAssetsAndAudio() {
        // --- Memuat Ikon Jendela ---
        try {
            Image icon = new ImageIcon(getClass().getResource("/icon.png")).getImage();
            this.setIconImage(icon);
        } catch (Exception e) {
            System.err.println("Favicon not found.");
        }

        // --- Memuat Kursor Kustom ---
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        try {
            Image menuCursorImg = new ImageIcon(getClass().getResource("/cursor/menu-arrow.png")).getImage();
            menuCursor = toolkit.createCustomCursor(menuCursorImg, new Point(0, 0), "menu_cursor");
        } catch (Exception e) { menuCursor = Cursor.getDefaultCursor(); }

        try {
            // 1. Buat ImageIcon dulu untuk memastikan gambar termuat penuh ke memori.
            ImageIcon cursorIcon = new ImageIcon(getClass().getResource("/cursor/cross-ingame.png"));
            Image gameCursorImg = cursorIcon.getImage();

            // 2. Sekarang, getWidth() dan getHeight() lebih aman untuk dipanggil.
            Point hotspot = new Point(gameCursorImg.getWidth(null) / 2, gameCursorImg.getHeight(null) / 2);

            // 3. Buat kursor dengan gambar dan hotspot yang sudah dihitung.
            gameCursor = toolkit.createCustomCursor(gameCursorImg, hotspot, "game_cursor");
        } catch (Exception e) { gameCursor = Cursor.getDefaultCursor(); }

        // --- Memuat Semua Audio ---
        AudioManager audio = AudioManager.getInstance();
        audio.loadSound("menu_music", "/audio/background-music.wav");
        audio.loadSound("button_click", "/audio/button-click.wav");
        audio.loadSound("footstep", "/audio/sand-footstep.wav");
        audio.loadSound("eat_sound", "/audio/eat-sound.wav");
    }

    public void showPanel(String panelName) {
        // Logika untuk memutar/menghentikan musik saat berganti panel
        if (panelName.equals("GamePanel")) {
            AudioManager.getInstance().stopSound("menu_music");
            this.setCursor(gameCursor);
        } else {
            AudioManager.getInstance().loopSound("menu_music");
            this.setCursor(menuCursor);
        }

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
//        Saya Muhammad Daffa Rizmawan Harahap mengerjakan evaluasi Tugas Masa Depan dalam mata kuliah
//        Desain dan Pemrograman Berorientasi Objek untuk keberkahanNya maka saya
//        tidak melakukan kecurangan seperti yang telah dispesifikasikan. Aamiin.

package view;

import viewmodel.GameViewModel;
import audio.AudioManager;
import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final GameViewModel viewModel;
    private final GamePanel gamePanel;
    private final StartScreenPanel startScreenPanel;

    private Cursor menuCursor, gameCursor;

    public MainWindow() {
        this.viewModel = new GameViewModel();

        // load asset dan audio
        loadAssetsAndAudio();

        // buat panel utama
        this.cardLayout = new CardLayout();
        this.mainPanel = new JPanel(cardLayout);

        // buat panel start dan panel game
        this.startScreenPanel = new StartScreenPanel(this, viewModel);
        this.gamePanel = new GamePanel(this, viewModel);

        // tambahkan panel ke panel utama
        mainPanel.add(startScreenPanel, "StartScreen");
        mainPanel.add(gamePanel, "GamePanel");
        this.add(mainPanel);

        // settingan general game
        this.setTitle("Jeff is Hungry");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(900, 700);
        this.setLocationRelativeTo(null); // Center the window

        // tampilkan start screen
        showPanel("StartScreen"); // Tampilkan menu awal dan putar musik

        this.setVisible(true);
    }

    // method untuk load asset dan audio
    private void loadAssetsAndAudio() {
        // icon di window
        try {
            Image icon = new ImageIcon(getClass().getResource("/icon.png")).getImage();
            this.setIconImage(icon);
        } catch (Exception e) {
            System.err.println("Favicon not found.");
        }

        // cursor custom
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        try {
            Image menuCursorImg = new ImageIcon(getClass().getResource("/cursor/menu-arrow.png")).getImage();
            menuCursor = toolkit.createCustomCursor(menuCursorImg, new Point(0, 0), "menu_cursor");
        } catch (Exception e) { menuCursor = Cursor.getDefaultCursor(); }

        try {
            ImageIcon cursorIcon = new ImageIcon(getClass().getResource("/cursor/cross-ingame.png"));
            Image gameCursorImg = cursorIcon.getImage();

            gameCursor = toolkit.createCustomCursor(gameCursorImg, new Point(0, 0), "game_cursor");
        } catch (Exception e) { gameCursor = Cursor.getDefaultCursor(); }

        // load semua audio
        AudioManager audio = AudioManager.getInstance();
        audio.loadSound("menu_music", "/audio/background-music.wav");
        audio.loadSound("button_click", "/audio/button-click.wav");
        audio.loadSound("footstep", "/audio/sand-footstep.wav");
        audio.loadSound("eat_sound", "/audio/eat-sound.wav");
    }

    // mengatur panel yang ditampilkan
    public void showPanel(String panelName) {
        // logika untuk memutar/menghentikan musik saat berganti panel
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
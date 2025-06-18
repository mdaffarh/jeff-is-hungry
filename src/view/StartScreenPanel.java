package view;

import audio.AudioManager;
import model.Result;
import viewmodel.GameViewModel;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.awt.FontFormatException;

/**
 * StartScreenPanel adalah panel yang berfungsi sebagai menu utama permainan.
 * Panel ini menampilkan judul, papan peringkat (leaderboard), input username,
 * dan tombol-tombol navigasi seperti Play, Credit, dan Exit.
 */

public class StartScreenPanel extends JPanel {
    // Dependensi & Komponen Utama
    private final MainWindow mainWindow;
    private final GameViewModel viewModel;

    // Komponen UI
    private JTextField usernameField;
    private JTable scoreTable;
    private DefaultTableModel tableModel;
    private JButton muteToggleButton;

    // Aset Gambar & Font
    private Image menuBackgroundImage, titleImage, playButtonImage, creditButtonImage, exitButtonImage;
    private ImageIcon soundIcon, mutedIcon;
    private Font pixelifyFont;

    /**
     * Constructor untuk StartScreenPanel.
     * @param mainWindow Referensi ke jendela utama aplikasi.
     * @param viewModel Referensi ke ViewModel yang berisi logika game.
     */
    public StartScreenPanel(MainWindow mainWindow, GameViewModel viewModel) {
        this.mainWindow = mainWindow;
        this.viewModel = viewModel;
        loadAssets();
        initUI();
    }

    /**
     * Memuat semua aset gambar dan font dari folder resources.
     * Dipanggil sekali saat inisialisasi.
     */
    private void loadAssets() {
        try {
            // Memuat Aset Gambar UI
            menuBackgroundImage = ImageIO.read(getClass().getResourceAsStream("/images/menu/menu-background.png"));
            titleImage = ImageIO.read(getClass().getResourceAsStream("/images/menu/title.png"));
            playButtonImage = ImageIO.read(getClass().getResourceAsStream("/images/menu/play.png"));
            creditButtonImage = ImageIO.read(getClass().getResourceAsStream("/images/menu/credit.png"));
            exitButtonImage = ImageIO.read(getClass().getResourceAsStream("/images/menu/exit.png"));
            soundIcon = new ImageIcon(getClass().getResource("/images/menu/sound.png"));
            mutedIcon = new ImageIcon(getClass().getResource("/images/menu/muted.png"));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Gagal memuat salah satu aset gambar menu!");
            e.printStackTrace();
        }

        try (InputStream is = getClass().getResourceAsStream("/font/Pixelify.ttf")) {
            // Memuat Aset Font Kustom
            if (is == null) throw new IOException("File font tidak ditemukan: Pixelify.ttf");
            pixelifyFont = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(pixelifyFont);
        } catch (IOException | FontFormatException e) {
            System.err.println("Gagal memuat font kustom. Menggunakan font default.");
            pixelifyFont = new Font("Arial", Font.PLAIN, 12);
        }
    }

    /**
     * Menginisialisasi dan menata semua komponen UI menggunakan GridBagLayout untuk responsivitas.
     */
    private void initUI() {
        this.setLayout(new BorderLayout());

        // Panel konten utama dibuat transparan agar background panel utama terlihat
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        // Judul Game (Gambar)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // Membentang 2 kolom
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.insets = new Insets(20, 10, 20, 10);
        if (titleImage != null) {
            contentPanel.add(new JLabel(new ImageIcon(titleImage)), gbc);
        }

        // Panel Papan Peringkat (kiri)
        JScrollPane leaderboardPane = createLeaderboardComponent();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.6; // Mengambil 60% lebar
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 50, 50, 10);
        contentPanel.add(leaderboardPane, gbc);

        // Panel Aksi (kanan)
        JPanel rightPanel = createRightPanelComponent();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.4; // Mengambil 40% lebar
        gbc.insets = new Insets(10, 10, 50, 0);
        contentPanel.add(rightPanel, gbc);

        this.add(contentPanel, BorderLayout.CENTER);

        // Panggil metode-metode ini setelah semua komponen UI dibuat
        populateScoreTable();
        addListeners();
        updateMuteButtonIcon();
    }

    /**
     * Membuat dan menata komponen JScrollPane yang berisi tabel papan peringkat.
     * @return JScrollPane yang sudah di-style.
     */
    private JScrollPane createLeaderboardComponent() {
        tableModel = new DefaultTableModel(new String[]{"Username", "Score", "Count"}, 0);
        scoreTable = new JTable(tableModel);
        styleTable(scoreTable);
        JScrollPane scrollPane = new JScrollPane(scoreTable);
        styleScrollPane(scrollPane);
        return scrollPane;
    }

    /**
     * Membuat dan menata panel kanan yang berisi input username dan semua tombol aksi.
     * @return JPanel yang sudah berisi semua komponen kanan.
     */
    private JPanel createRightPanelComponent() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 0);

        // Input Username
        usernameField = new JTextField("ENTER USERNAME");
        styleTextField(usernameField);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(usernameField, gbc);

        // Tombol Play
        JButton playButton = createImageButton(playButtonImage);
        playButton.addActionListener(e -> onPlayButtonClicked());
        gbc.gridy = 1;
        panel.add(playButton, gbc);

        // Tombol Credit
        JButton creditButton = createImageButton(creditButtonImage);
        creditButton.addActionListener(e -> onCreditButtonClicked());
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        panel.add(creditButton, gbc);

        // Tombol Exit
        JButton exitButton = createImageButton(exitButtonImage);
        exitButton.addActionListener(e -> System.exit(0));
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(exitButton, gbc);

        // Tombol Mute
        muteToggleButton = createImageButton(soundIcon != null ? soundIcon.getImage() : null);
        muteToggleButton.addActionListener(e -> onMuteButtonClicked());
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        panel.add(muteToggleButton, gbc);

        return panel;
    }

    /**
     * Menambahkan listener yang tidak terkait langsung dengan pembuatan tombol,
     * seperti listener untuk tabel, text field, dan panel itu sendiri.
     */
    private void addListeners() {
        scoreTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = scoreTable.getSelectedRow();
                if (selectedRow != -1 && selectedRow < scoreTable.getRowCount()) {
                    String username = scoreTable.getValueAt(selectedRow, 0).toString();
                    usernameField.setText(username);
                }
            }
        });

        usernameField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (usernameField.getText().equals("ENTER USERNAME")) {
                    usernameField.setText("");
                }
            }
        });

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                populateScoreTable();
            }
        });
    }

    // Logika Aksi Tombol
    private void onPlayButtonClicked() {
        AudioManager.getInstance().playSound("button_click");
        String username = usernameField.getText().trim();
        if (username.isEmpty() || username.equals("ENTER USERNAME")) {
            JOptionPane.showMessageDialog(this, "Please enter a username.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        viewModel.startGame(username, mainWindow.getWidth(), mainWindow.getHeight());
        mainWindow.showPanel("GamePanel");
    }

    private void onCreditButtonClicked() {
        AudioManager.getInstance().playSound("button_click");
        CreditsDialog creditsDialog = new CreditsDialog(mainWindow, pixelifyFont);
        creditsDialog.setVisible(true);
    }

    private void onMuteButtonClicked() {
        AudioManager audio = AudioManager.getInstance();
        audio.setMuted(!audio.isMuted());
        updateMuteButtonIcon();
    }

    /**
     * Mengupdate ikon tombol mute dan memutar/menghentikan musik sesuai status.
     */
    private void updateMuteButtonIcon() {
        AudioManager audio = AudioManager.getInstance();
        if (audio.isMuted()) {
            muteToggleButton.setIcon(mutedIcon);
            audio.stopSound("menu_music");
        } else {
            muteToggleButton.setIcon(soundIcon);
            audio.loopSound("menu_music");
        }
    }

    /**
     * Memuat data skor dari ViewModel dan menampilkannya di tabel.
     */
    private void populateScoreTable() {
        tableModel.setRowCount(0);
        List<Result> scores = viewModel.getAllScores();
        if (scores == null) return;
        for (Result result : scores) {
            tableModel.addRow(new Object[]{result.getUsername(), result.getSkor(), result.getCount()});
        }
    }

    // Metode helper untuk styling komponen
    private JButton createImageButton(Image img) {
        JButton button = new JButton(new ImageIcon(img));
        button.setBorder(null);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        return button;
    }

    private void styleTextField(JTextField tf) {
        tf.setFont(pixelifyFont.deriveFont(24f));
        tf.setForeground(new Color(60, 42, 42)); // Warna teks coklat gelap
        tf.setBackground(new Color(240, 220, 180, 200)); // Warna krem semi-transparan
        tf.setHorizontalAlignment(JTextField.CENTER);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 42, 42), 2),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }

    private void styleTable(JTable table) {
        table.setFont(pixelifyFont.deriveFont(16f));
        // Memberi warna latar pada tabel
        table.setBackground(new Color(245, 222, 179, 230)); // Warna krem gandum, sedikit transparan
        table.setForeground(new Color(87, 58, 24)); // Coklat tua
        table.setRowHeight(25);
        table.setGridColor(new Color(60, 42, 42, 100)); // Warna grid coklat transparan

        // Style untuk Header
        table.getTableHeader().setFont(pixelifyFont.deriveFont(Font.BOLD, 18f));
        table.getTableHeader().setBackground(new Color(60, 42, 42)); // Coklat tua solid
        table.getTableHeader().setForeground(Color.WHITE);
    }

    // style untuk Scrollbar
    private void styleScrollPane(JScrollPane sp) {
        sp.getViewport().setBackground(new Color(245, 222, 179, 230));
        sp.setBorder(BorderFactory.createLineBorder(new Color(60, 42, 42), 3));

        // Terapkan UI Kustom ke Scrollbar Vertikal
        sp.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        // Atur juga untuk scrollbar horizontal jika muncul
        sp.getHorizontalScrollBar().setUI(new CustomScrollBarUI());
    }


    /**
     * Menggambar background utama panel.
     * Komponen lain digambar oleh Swing, bukan di sini.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (menuBackgroundImage != null) {
            g.drawImage(menuBackgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
        }
    }

    /**
     * Kelas kustom untuk membuat UI scrollbar yang sesuai dengan tema pixel art.
     */
    private static class CustomScrollBarUI extends BasicScrollBarUI {
        private final Color thumbColor = new Color(87, 58, 24); // Coklat tua untuk pegangan
        private final Color trackColor = new Color(245, 222, 179); // Krem untuk jalur
        private final Dimension zeroDim = new Dimension(0, 0);

        // Sembunyikan tombol panah atas dan bawah
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return new JButton() {
                @Override
                public Dimension getPreferredSize() {
                    return zeroDim;
                }
            };
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return new JButton() {
                @Override
                public Dimension getPreferredSize() {
                    return zeroDim;
                }
            };
        }

        // Atur warna jalur (track) scrollbar
        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(trackColor);
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }

        // Atur warna dan bentuk pegangan (thumb) scrollbar
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }
            g.setColor(thumbColor);
            // Gambar persegi panjang sebagai pegangan, tanpa sudut melengkung
            g.fillRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height);
        }
    }
}
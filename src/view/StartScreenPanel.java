package view;

import audio.AudioManager;
import model.Hasil;
import viewmodel.GameViewModel;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.awt.FontFormatException;
import javax.swing.plaf.basic.BasicScrollBarUI; // <-- Import baru untuk UI Scrollbar

public class StartScreenPanel extends JPanel {
    private final MainWindow mainWindow;
    private final GameViewModel viewModel;

    // Komponen UI
    private JTextField usernameField;
    private JTable scoreTable;
    private DefaultTableModel tableModel;

    // Aset Gambar & Font
    private Image menuBackgroundImage, titleImage, playButtonImage, creditButtonImage, exitButtonImage;
    private Font pixelifyFont;

    //-- 1. Variabel baru untuk tombol dan ikon mute
    private JButton muteToggleButton;
    private ImageIcon soundIcon, mutedIcon;

    public StartScreenPanel(MainWindow mainWindow, GameViewModel viewModel) {
        this.mainWindow = mainWindow;
        this.viewModel = viewModel;

        // Hanya panggil dua metode ini di constructor
        loadAssets();
        initUI();
    }

    private void loadAssets() {
        try {
            // Memuat font kustom
            try (InputStream is = getClass().getResourceAsStream("/font/Pixelify.ttf")) {
                if (is == null) throw new IOException("File font tidak ditemukan: Pixelify.ttf");
                pixelifyFont = Font.createFont(Font.TRUETYPE_FONT, is);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(pixelifyFont);
            } catch (FontFormatException e) {
                e.printStackTrace();
                pixelifyFont = new Font("Arial", Font.PLAIN, 12);
            }

            // Memuat semua gambar UI
            menuBackgroundImage = ImageIO.read(getClass().getResourceAsStream("/images/menu/menu-background.png"));
            titleImage = ImageIO.read(getClass().getResourceAsStream("/images/menu/title.png"));
            playButtonImage = ImageIO.read(getClass().getResourceAsStream("/images/menu/play.png"));
            creditButtonImage = ImageIO.read(getClass().getResourceAsStream("/images/menu/credit.png"));
            exitButtonImage = ImageIO.read(getClass().getResourceAsStream("/images/menu/exit.png"));

            //-- 2. Muat ikon untuk tombol mute
            soundIcon = new ImageIcon(getClass().getResource("/images/menu/sound.png"));
            mutedIcon = new ImageIcon(getClass().getResource("/images/menu/muted.png"));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Gagal memuat salah satu aset menu!");
        }
    }

    private void initUI() {
        setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        // 1. Judul Game
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 20, 10);
        gbc.anchor = GridBagConstraints.PAGE_START;
        if (titleImage != null) {
            contentPanel.add(new JLabel(new ImageIcon(titleImage)), gbc);
        }

        // 2. Tabel Skor
        tableModel = new DefaultTableModel(new String[]{"Username", "Score", "Count"}, 0);
        scoreTable = new JTable(tableModel);
        styleTable(scoreTable);
        JScrollPane scrollPane = new JScrollPane(scoreTable);
        styleScrollPane(scrollPane);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.6;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 50, 50, 10);
        contentPanel.add(scrollPane, gbc);

        // 3. Panel Kanan (memanggil metode helper yang berisi pembuatan tombol)
        JPanel rightPanel = createRightPanel();
        rightPanel.setOpaque(false);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.4;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 50, 0);
        contentPanel.add(rightPanel, gbc);

        this.add(contentPanel, BorderLayout.CENTER);

        // Panggil metode-metode ini setelah semua komponen UI dibuat
        populateScoreTable();
        addListeners();

        // Panggil metode untuk mengatur ikon tombol awal sesuai status AudioManager
        updateMuteButtonIcon();
    }

    // Metode ini hanya bertanggung jawab membuat panel kanan dan tombol-tombolnya
    // Pastikan listener hanya ditambahkan di sini.
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        usernameField = new JTextField("ENTER USERNAME");
        styleTextField(usernameField);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(usernameField, gbc);

        JButton playButton = createImageButton(playButtonImage);
        playButton.addActionListener(e -> {
            AudioManager.getInstance().playSound("button_click");
            onPlayButtonClicked();
        });
        gbc.gridy = 1;
        panel.add(playButton, gbc);

        JButton creditButton = createImageButton(creditButtonImage);
        // HANYA ADA SATU addActionListener UNTUK TOMBOL KREDIT DI SINI
        creditButton.addActionListener(e -> {
            AudioManager.getInstance().playSound("button_click");
            onCreditButtonClicked();
        });
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        panel.add(creditButton, gbc);

        JButton exitButton = createImageButton(exitButtonImage);
        exitButton.addActionListener(e -> {
            AudioManager.getInstance().playSound("button_click");
            System.exit(0);
        });
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(exitButton, gbc);

        //-- KETERANGAN: Tombol Mute ditambahkan di sini
        muteToggleButton = createImageButton(soundIcon.getImage());
        gbc.gridx = 1; // Kolom kanan
        gbc.gridy = 3; // Baris baru di paling bawah
        gbc.weighty = 1.0; // Memberi sisa ruang vertikal ke baris ini
        gbc.fill = GridBagConstraints.NONE; // Tidak meregangkan tombol
        gbc.anchor = GridBagConstraints.SOUTHEAST; // Posisi di pojok kanan bawah sel
        panel.add(muteToggleButton, gbc);

        return panel;
    }

    private void addListeners() {
        // Metode ini HANYA untuk listener yang tidak terkait tombol
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

        //-- 4. Tambahkan ActionListener untuk tombol mute
        muteToggleButton.addActionListener(e -> {
            AudioManager audio = AudioManager.getInstance();
            // Balik status mute saat ini
            audio.setMuted(!audio.isMuted());
            // Perbarui ikon dan putar/hentikan musik
            updateMuteButtonIcon();
        });
    }

    //-- 5. Metode helper baru untuk mengupdate ikon dan musik
    private void updateMuteButtonIcon() {
        AudioManager audio = AudioManager.getInstance();
        if (audio.isMuted()) {
            muteToggleButton.setIcon(mutedIcon);
            audio.stopSound("menu_music"); // Pastikan musik berhenti jika di-mute
        } else {
            muteToggleButton.setIcon(soundIcon);
            audio.loopSound("menu_music"); // Putar musik jika tidak di-mute
        }
    }

    private void onPlayButtonClicked() {
        String username = usernameField.getText().trim();
        if (username.isEmpty() || username.equals("ENTER USERNAME")) {
            JOptionPane.showMessageDialog(this, "Please enter a username.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        viewModel.startGame(username, mainWindow.getWidth(), mainWindow.getHeight());
        mainWindow.showPanel("GamePanel");
    }

    private void onCreditButtonClicked() {
        CreditsDialog creditsDialog = new CreditsDialog(mainWindow, pixelifyFont);
        creditsDialog.setVisible(true);
    }

    private void populateScoreTable() {
        tableModel.setRowCount(0);
        List<Hasil> scores = viewModel.getAllScores();
        for (Hasil hasil : scores) {
            tableModel.addRow(new Object[]{hasil.getUsername(), hasil.getSkor(), hasil.getCount()});
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
        // KETERANGAN: Memberi warna latar pada tabel
        table.setBackground(new Color(245, 222, 179, 230)); // Warna krem gandum, sedikit transparan
        table.setForeground(new Color(87, 58, 24)); // Coklat tua
        table.setRowHeight(25);
        table.setGridColor(new Color(60, 42, 42, 100)); // Warna grid coklat transparan

        // Style untuk Header
        table.getTableHeader().setFont(pixelifyFont.deriveFont(Font.BOLD, 18f));
        table.getTableHeader().setBackground(new Color(60, 42, 42)); // Coklat tua solid
        table.getTableHeader().setForeground(Color.WHITE);
    }

    //-- KETERANGAN: Metode ini sekarang juga mengatur style untuk Scrollbar
    private void styleScrollPane(JScrollPane sp) {
        sp.getViewport().setBackground(new Color(245, 222, 179, 230));
        sp.setBorder(BorderFactory.createLineBorder(new Color(60, 42, 42), 3));

        // Terapkan UI Kustom ke Scrollbar Vertikal
        sp.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        // Atur juga untuk scrollbar horizontal jika muncul
        sp.getHorizontalScrollBar().setUI(new CustomScrollBarUI());
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Gambar background terlebih dahulu agar menutupi seluruh panel
        if (menuBackgroundImage != null) {
            g.drawImage(menuBackgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
        }
        // KETERANGAN: Gambar judul dan placeholder "LEADERBOARD" tidak lagi digambar di sini,
        // karena sudah menjadi komponen (JLabel) yang dikelola oleh Layout Manager.
    }

    //-- KETERANGAN: Kelas baru ditambahkan di sini untuk mendefinisikan tampilan Scrollbar --
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
package view;

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

    public StartScreenPanel(MainWindow mainWindow, GameViewModel viewModel) {
        this.mainWindow = mainWindow;
        this.viewModel = viewModel;

        loadAssets();
        initUI(); // Menginisialisasi semua komponen dan layout
    }

    private void loadAssets() {
        try {
            // Memuat font kustom
            try (InputStream is = getClass().getResourceAsStream("/fonts/Pixelify.ttf")) {
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
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Gagal memuat salah satu aset menu!");
        }
    }

    private void initUI() {
        // Menggunakan BorderLayout untuk panel utama agar background bisa digambar
        setLayout(new BorderLayout());

        // Membuat panel konten utama dengan GridBagLayout untuk fleksibilitas
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false); // Buat panel transparan agar background terlihat
        GridBagConstraints gbc = new GridBagConstraints();

        // --- Konfigurasi GridBagLayout ---

        // 1. Judul Game (title.png)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // Membentang 2 kolom
        gbc.insets = new Insets(20, 10, 20, 10); // Jarak atas, kiri, bawah, kanan
        gbc.anchor = GridBagConstraints.PAGE_START;
        contentPanel.add(new JLabel(new ImageIcon(titleImage)), gbc);

        // 2. Tabel Skor (Leaderboard) di sisi kiri
        tableModel = new DefaultTableModel(new String[]{"Username", "Score", "Count"}, 0);
        scoreTable = new JTable(tableModel);
        styleTable(scoreTable);
        JScrollPane scrollPane = new JScrollPane(scoreTable);
        styleScrollPane(scrollPane);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1; // Kembali ke 1 kolom
        gbc.weightx = 0.6; // Mengambil 60% dari lebar ruang ekstra
        gbc.weighty = 1.0; // Mengambil semua tinggi ruang ekstra
        gbc.fill = GridBagConstraints.BOTH; // Memenuhi selnya
        gbc.insets = new Insets(10, 50, 50, 10);
        contentPanel.add(scrollPane, gbc);

        // 3. Panel Kanan (Input dan Tombol)
        JPanel rightPanel = createRightPanel();
        rightPanel.setOpaque(false);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.4; // Mengambil 40% dari lebar
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 50, 50);
        contentPanel.add(rightPanel, gbc);

        // Menambahkan panel konten ke panel utama
        this.add(contentPanel, BorderLayout.CENTER);

        // Memuat data ke tabel dan menambahkan listeners
        populateScoreTable();
        addListeners();

        //-- KETERANGAN: TAMBAHKAN KODE INI UNTUK MEMPERBAIKI MASALAH REFRESH --
        // Listener ini akan berjalan setiap kali panel ini ditampilkan (termasuk saat kembali dari game)
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                // Memuat ulang data dari database ke tabel
                populateScoreTable();
            }
        });
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

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
        gbc.gridwidth = 1; // Kembali ke 1 kolom
        panel.add(creditButton, gbc);

        // Tombol Exit
        JButton exitButton = createImageButton(exitButtonImage);
        exitButton.addActionListener(e -> System.exit(0));
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(exitButton, gbc);

        return panel;
    }

    private void addListeners() {
        // Fitur klik nama di tabel untuk mengisi input username
        scoreTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = scoreTable.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    String username = (String) tableModel.getValueAt(row, 0);
                    usernameField.setText(username);
                }
            }
        });

        // Menghapus teks placeholder saat input diklik
        usernameField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (usernameField.getText().equals("ENTER USERNAME")) {
                    usernameField.setText("");
                }
            }
        });
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
package view;

import model.Food;
import model.Lasso;
import model.Player;
import viewmodel.GameViewModel;
import viewmodel.GameViewModel.GameState;
import javax.imageio.ImageIO; // <-- Import untuk memuat gambar
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException; // <-- Import untuk menangani error I/O
import java.io.InputStream; // <-- Import untuk InputStream
import java.awt.FontFormatException; // <-- Import untuk error font
import java.util.HashMap;
import java.util.Map;

public class GamePanel extends JPanel implements ActionListener {
    private final MainWindow mainWindow;
    private final GameViewModel viewModel;
    private final Timer gameLoop;

    // --- LANGKAH 1: Tambahkan variabel untuk menampung gambar background ---
    private Image backgroundImage;
    private Image basketImage;
    //-- 1. Tambahkan variabel untuk font kustom Anda
    private Font customFont;

    //-- 1. Variabel baru untuk menyimpan semua gambar makanan
    private Map<String, Image> foodImages;
    private Image emptyPlateImage;

    public GamePanel(MainWindow mainWindow, GameViewModel viewModel) {
        this.mainWindow = mainWindow;
        this.viewModel = viewModel;
        this.setFocusable(true);

        loadAssets();

        // --- LANGKAH 2: Muat gambar dari folder resources di constructor ---
        try {
            // Pastikan nama file "background.png" sesuai dengan nama file Anda
            backgroundImage = ImageIO.read(getClass().getResourceAsStream("/images/background.png"));
            basketImage = ImageIO.read(getClass().getResourceAsStream("/images/basket.png"));
        } catch (IOException e) {
            System.err.println("Gagal memuat gambar background!");
            e.printStackTrace();
        }

        //-- 2. Muat dan daftarkan Font Kustom
        try (InputStream is = getClass().getResourceAsStream("/fonts/Pixelify.ttf")) {
            // Ganti "MyCoolFont.ttf" dengan nama file font Anda
            if (is == null) {
                throw new IOException("File font tidak ditemukan!");
            }
            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(baseFont);
            // Simpan font dasar untuk digunakan nanti
            customFont = baseFont;
        } catch (IOException | FontFormatException e) {
            System.err.println("Gagal memuat font kustom. Menggunakan font default.");
            e.printStackTrace();
            customFont = null; // Pastikan null jika gagal
        }

        gameLoop = new Timer(16, this);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e.getKeyCode());
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (viewModel.getGameState() == GameState.PLAYING) {
                    viewModel.fireLasso(e.getPoint());
                }
            }
        });

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                gameLoop.start();
                requestFocusInWindow();
            }
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                gameLoop.stop();
            }
        });
    }

    //-- 2. Metode baru untuk memuat semua aset
    private void loadAssets() {
        try {
            backgroundImage = ImageIO.read(getClass().getResourceAsStream("/images/background.png"));
            basketImage = ImageIO.read(getClass().getResourceAsStream("/images/basket.png"));

            // Muat gambar piring kosong
            emptyPlateImage = ImageIO.read(getClass().getResourceAsStream("/images/foods/plate.png"));

            // Inisialisasi map dan muat semua gambar makanan
            foodImages = new HashMap<>();
            String[] foodNames = {
                    // Makanan Positif
                    "steak", "hotdog", "hamburger", "cheesecake", "chocolate", "pudding", "sushi",
                    // Makanan Negatif (Sayuran)
                    "carrot", "green", "corn", "eggplant", "cucumber"
            };

            for (String name : foodNames) {
                Image img = ImageIO.read(getClass().getResourceAsStream("/images/foods/" + name + ".png"));
                if (img != null) {
                    foodImages.put(name, img);
                } else {
                    System.err.println("Gagal memuat gambar: " + name + ".png");
                }
            }

        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Gagal memuat gambar aset!");
            e.printStackTrace();
        }

        try (InputStream is = getClass().getResourceAsStream("/fonts/Pixelify.ttf")) {
            if (is == null) throw new IOException("File font tidak ditemukan!");
            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(baseFont);
            customFont = baseFont;
        } catch (IOException | FontFormatException e) {
            System.err.println("Gagal memuat font kustom.");
            customFont = null;
        }
    }

    private void handleKeyPress(int keyCode) {
        // ... (kode ini tidak berubah)
        if (viewModel.getPlayer() == null) return;
        int speed = 10;
        switch (keyCode) {
            case KeyEvent.VK_UP: viewModel.getPlayer().move(0, -speed); break;
            case KeyEvent.VK_DOWN: viewModel.getPlayer().move(0, speed); break;
            case KeyEvent.VK_LEFT: viewModel.getPlayer().move(-speed, 0); break;
            case KeyEvent.VK_RIGHT: viewModel.getPlayer().move(speed, 0); break;
            case KeyEvent.VK_SPACE:
                viewModel.stopGameAndSave();
                mainWindow.showPanel("StartScreen");
                break;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(viewModel.getGameState() == GameState.PLAYING) {
            viewModel.updateGame(getWidth(), getHeight());
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(viewModel.getGameState() != GameState.PLAYING) return;

        // --- LANGKAH 3: Gambar background terlebih dahulu ---
        if (backgroundImage != null) {
            // Gambar akan di-scale agar selalu pas dengan ukuran panel
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //-- 3. Logika baru untuk menggambar makanan
        for (Food food : viewModel.getFoodItems()) {
            Image imageToDraw;
            // Jika bola sedang dianimasikan ke keranjang, tampilkan piring kosong
            if (food.getState() == Food.FoodState.ANIMATING_TO_BASKET) {
                imageToDraw = emptyPlateImage;
            } else {
                // Jika tidak, tampilkan gambar makanan sesuai namanya
                imageToDraw = foodImages.get(food.getName());
            }

            if (imageToDraw != null) {
                // Gambar makanan/piring di tengah posisinya
                int imgSize = 64; // Ukuran gambar makanan
                g.drawImage(imageToDraw, food.getPosition().x - imgSize / 2, food.getPosition().y - imgSize / 2, imgSize, imgSize, this);
            }
        }

        Player player = viewModel.getPlayer();
        if (player != null) {
            Lasso lasso = viewModel.getLasso();
            if (lasso.getState() != Lasso.LassoState.IDLE) {
                g2d.setColor(Color.YELLOW);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(lasso.getStartPoint().x, lasso.getStartPoint().y, lasso.getEndPoint().x, lasso.getEndPoint().y);
            }

            g2d.setColor(Color.DARK_GRAY);
            Point pPos = player.getPosition();
            int[] xPoints = {pPos.x, pPos.x - 20, pPos.x + 20};
            int[] yPoints = {pPos.y - 20, pPos.y + 20, pPos.y + 20};
            g2d.fillPolygon(xPoints, yPoints, 3);
        }

        //-- 3. Logika baru untuk menggambar Keranjang Skor dan Teksnya
        if (basketImage != null) {

// Tentukan ukuran dan posisi keranjang di KANAN-TENGAH

            int basketWidth = 260;
            int basketHeight = 220; // Sesuaikan ukuran jika perlu

            int basketX = getWidth() - basketWidth; // 20px dari tepi kanan
            int basketY = (getHeight() - basketHeight) / 2; // Tepat di tengah secara vertikal
// Gambar keranjang

            g.drawImage(basketImage, basketX, basketY, basketWidth, basketHeight, this);



// Atur font dan warna teks agar kontras dengan keranjang cream

            g2d.setColor(new Color(255, 255, 255)); // Warna coklat tua



//-- 3. Terapkan Font Kustom pada Teks Skor

            g2d.setFont(customFont.deriveFont(Font.BOLD, 18f));
// Posisikan teks di atas gambar keranjang
            String scoreText = "Score: " + viewModel.getCurrentScore();

            String countText = "Count: " + viewModel.getCurrentCount();
            g2d.drawString(scoreText, basketX + 87, basketY + 120);
            g2d.drawString(countText, basketX + 87, basketY + 145);
        }
    }
}
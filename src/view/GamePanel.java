package view;

import model.FloatingScore;
import model.Food;
import model.Lasso;
import model.Player;
import viewmodel.GameViewModel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.InputStream;
import java.awt.FontFormatException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * GamePanel adalah komponen utama tempat semua elemen permainan digambar dan loop game berjalan.
 * Kelas ini bertanggung jawab untuk rendering visual dan menangani input dari pengguna.
 */
public class GamePanel extends JPanel implements ActionListener {

    // Konstanta untuk durasi animasi agar mudah diubah
    private static final int IDLE_FRAME_DURATION = 40;
    private static final int WALK_FRAME_DURATION = 15;

    // Referensi ke komponen utama dan logika game
    private final MainWindow mainWindow;
    private final GameViewModel viewModel;
    private final Timer gameLoop;

    // Aset Gambar (Images)
    private Image backgroundImage, basketImage, emptyPlateImage;
    private Image playerStandImage, playerJumpImage, playerWalkImage, playerEatImage;
    private Image tongueBaseImage, tongueMiddleImage, tongueTipImage;
    private Map<String, Image> foodImages;

    // Aset Font
    private Font customFont;

    // Variabel untuk State Animasi
    private int idleAnimCounter = 0;
    private boolean isIdleJumpFrame = false;
    private int walkAnimCounter = 0;
    private boolean isWalkFrame = false;

    // Variabel untuk State Input
    private final Set<Integer> activeKeys = new HashSet<>();

    /**
     * Constructor untuk GamePanel.
     * @param mainWindow Referensi ke jendela utama aplikasi.
     * @param viewModel Referensi ke ViewModel yang berisi logika game.
     */
    public GamePanel(MainWindow mainWindow, GameViewModel viewModel) {
        this.mainWindow = mainWindow;
        this.viewModel = viewModel;
        this.setFocusable(true);

        loadAssets();
        addListeners();

        this.gameLoop = new Timer(16, this); // Mengatur game loop untuk berjalan sekitar 60 FPS
    }

    /**
     * Memuat semua aset gambar dan font dari folder resources.
     * Metode ini dipanggil sekali saat inisialisasi.
     */
    private void loadAssets() {
        try {
            // Memuat Aset UI dan Background
            backgroundImage = ImageIO.read(getClass().getResourceAsStream("/images/background.png"));
            basketImage = ImageIO.read(getClass().getResourceAsStream("/images/basket.png"));
            emptyPlateImage = ImageIO.read(getClass().getResourceAsStream("/images/foods/plate.png"));

            // Memuat Aset Makanan
            foodImages = new HashMap<>();
            String[] foodNames = {
                    "steak", "hotdog", "hamburger", "cheesecake", "chocolate", "pudding", "sushi",
                    "carrot", "green", "corn", "eggplant", "cucumber"
            };
            for (String name : foodNames) {
                Image img = ImageIO.read(getClass().getResourceAsStream("/images/foods/" + name + ".png"));
                if (img != null) foodImages.put(name, img);
                else System.err.println("Gagal memuat gambar makanan: " + name + ".png");
            }

            // Memuat Aset Animasi Karakter
            playerStandImage = ImageIO.read(getClass().getResourceAsStream("/images/jeff/stand.png"));
            playerJumpImage = ImageIO.read(getClass().getResourceAsStream("/images/jeff/jump.png"));
            playerWalkImage = ImageIO.read(getClass().getResourceAsStream("/images/jeff/walk.png"));
            playerEatImage = ImageIO.read(getClass().getResourceAsStream("/images/jeff/eat.png"));

            // Memuat Aset Laso (Lidah)
            tongueBaseImage = ImageIO.read(getClass().getResourceAsStream("/images/tongue/tongue_base.png"));
            tongueMiddleImage = ImageIO.read(getClass().getResourceAsStream("/images/tongue/tongue_middle.png"));
            tongueTipImage = ImageIO.read(getClass().getResourceAsStream("/images/tongue/tongue_tip.png"));

        } catch (IOException | IllegalArgumentException e) {
            System.err.println("KRITIS: Gagal memuat salah satu aset gambar!");
            e.printStackTrace();
        }

        // Memuat Aset Font
        try (InputStream is = getClass().getResourceAsStream("/font/Pixelify.ttf")) {
            if (is == null) throw new IOException("File font tidak ditemukan: Pixelify.ttf");
            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(baseFont);
            customFont = baseFont;
        } catch (IOException | FontFormatException e) {
            System.err.println("Gagal memuat font kustom. Menggunakan font default.");
            customFont = null;
        }
    }

    /**
     * Menambahkan semua listener input (keyboard, mouse, komponen) ke panel.
     */
    private void addListeners() {
        KeyAdapter keyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                GameViewModel.GameState currentState = viewModel.getGameState();

                if (currentState == GameViewModel.GameState.PLAYING) {
                    if (isMovementKey(keyCode)) {
                        activeKeys.add(keyCode);
                    } else if (keyCode == KeyEvent.VK_SPACE) {
                        viewModel.pauseGame();
                    }
                } else if (currentState == GameViewModel.GameState.PAUSED) {
                    if (keyCode == KeyEvent.VK_ENTER) {
                        viewModel.resumeGame();
                    } else if (keyCode == KeyEvent.VK_SPACE) {
                        viewModel.stopGameAndSave();
                        mainWindow.showPanel("StartScreen");
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (isMovementKey(keyCode)) {
                    activeKeys.remove(keyCode);
                    if (activeKeys.isEmpty()) {
                        viewModel.stopWalking();
                    }
                }
            }
        };
        addKeyListener(keyAdapter);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (viewModel.getGameState() == GameViewModel.GameState.PLAYING) {
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

    /**
     * Metode yang dipanggil oleh game loop (Timer).
     * @param e Event dari timer.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (viewModel.getGameState() == GameViewModel.GameState.PLAYING) {
            handleMovement(); // Tangani input gerakan yang sedang aktif
            viewModel.updateGame(getWidth(), getHeight());
        }
        repaint(); // Selalu repaint untuk memperbarui tampilan
    }

    /**
     * Metode utama untuk menggambar semua elemen ke layar.
     * Metode ini berfungsi sebagai "sutradara" yang memanggil metode-metode gambar lainnya.
     * @param g Objek Graphics yang digunakan untuk menggambar.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (viewModel.getPlayer() == null) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Urutan penggambaran / layering
        drawBackground(g);
        drawPlayerAndLasso(g2d); // Menggambar pemain dan laso setelah makanan
        drawFoodItems(g2d);
        drawScoreBasket(g2d);
        drawFloatingScores(g2d);
        drawPauseOverlay(g2d); // Overlay pause digambar paling akhir
    }

    // Metode helper untuk menggambar
    // background
    private void drawBackground(Graphics g) {
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    // makanan
    private void drawFoodItems(Graphics2D g2d) {
        for (Food food : viewModel.getFoodItems()) {
            Image imageToDraw;
            if (food.getState() == Food.FoodState.ANIMATING_TO_BASKET) {
                imageToDraw = emptyPlateImage;
            } else {
                imageToDraw = foodImages.get(food.getName());
            }

            if (imageToDraw != null) {
                int imgSize = 64;
                g2d.drawImage(imageToDraw, food.getPosition().x - imgSize / 2, food.getPosition().y - imgSize / 2, imgSize, imgSize, this);
            }
        }
    }

    // pemain dan lasso
    private void drawPlayerAndLasso(Graphics2D g2d) {
        Player player = viewModel.getPlayer();
        if (player == null) return;

        // Menggambar animasi karakter pemain
        Image playerImageToDraw = getPlayerAnimationFrame(player);
        drawPlayerImageWithFlip(g2d, player, playerImageToDraw);

        // Menggambar lasso (lidah)
        Lasso lasso = viewModel.getLasso();
        if (lasso.getState() != Lasso.LassoState.IDLE && tongueBaseImage != null && tongueMiddleImage != null && tongueTipImage != null) {
            AffineTransform oldTransform = g2d.getTransform();
            Point start = lasso.getStartPoint();
            Point end = lasso.getEndPoint();
            double dx = end.x - start.x;
            double dy = end.y - start.y;
            double angle = Math.atan2(dy, dx);
            double length = Math.sqrt(dx * dx + dy * dy);

            g2d.translate(start.x, start.y);
            g2d.rotate(angle);

            int baseWidth = tongueBaseImage.getWidth(this);
            int tipWidth = tongueTipImage.getWidth(this);
            int tongueHeight = tongueBaseImage.getHeight(this);
            int middleWidth = (int) length - baseWidth - tipWidth;

            g2d.drawImage(tongueBaseImage, 0, -tongueHeight / 2, baseWidth, tongueHeight, this);
            if (middleWidth > 0) {
                g2d.drawImage(tongueMiddleImage, baseWidth, -tongueHeight / 2, middleWidth, tongueHeight, this);
            }
            g2d.drawImage(tongueTipImage, (int) length - tipWidth, -tongueHeight / 2, tipWidth, tongueHeight, this);

            g2d.setTransform(oldTransform);
        }
    }

    // keranjang
    private void drawScoreBasket(Graphics2D g2d) {
        if (basketImage == null) return;

        int basketWidth = 260;
        int basketHeight = 220;
        int basketX = getWidth() - basketWidth;
        int basketY = (getHeight() - basketHeight) / 2;
        g2d.drawImage(basketImage, basketX, basketY, basketWidth, basketHeight, this);

        g2d.setFont(customFont.deriveFont(Font.BOLD, 18f));
        String scoreText = "Score: " + viewModel.getCurrentScore();
        String countText = "Count: " + viewModel.getCurrentCount();
        int textX = basketX + 87;
        int scoreY = basketY + 120;
        int countY = basketY + 145;

        g2d.setColor(Color.BLACK);
        g2d.drawString(scoreText, textX - 1, scoreY - 1);
        g2d.drawString(scoreText, textX + 1, scoreY + 1);
        g2d.drawString(countText, textX - 1, countY - 1);
        g2d.drawString(countText, textX + 1, countY + 1);

        g2d.setColor(Color.WHITE);
        g2d.drawString(scoreText, textX, scoreY);
        g2d.drawString(countText, textX, countY);
    }

    // floating score
    private void drawFloatingScores(Graphics2D g2d) {
        for (FloatingScore fs : viewModel.getFloatingScores()) {
            g2d.setFont(customFont.deriveFont(Font.BOLD, 22f));
            g2d.setColor(fs.getColor());

            float alpha = (float) fs.getLifespan() / fs.getMaxLifespan();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, alpha)));

            Point.Float pos = fs.getPosition();
            g2d.drawString(fs.getText(), (int)pos.x, (int)pos.y);
        }
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    // overlay ketika pause
    private void drawPauseOverlay(Graphics2D g2d) {
        if (viewModel.getGameState() != GameViewModel.GameState.PAUSED) return;

        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setFont(customFont.deriveFont(Font.BOLD, 50f));
        g2d.setColor(Color.WHITE);
        String pauseText = "GAME PAUSED";
        FontMetrics metricsLarge = g2d.getFontMetrics();
        int x = (getWidth() - metricsLarge.stringWidth(pauseText)) / 2;
        int y = getHeight() / 2 - 50;
        g2d.drawString(pauseText, x, y);

        g2d.setFont(customFont.deriveFont(Font.PLAIN, 20f));
        FontMetrics metricsSmall = g2d.getFontMetrics();
        String resumeText = "Press Enter to Resume";
        String menuText = "Press Space to Back to Menu";
        int xResume = (getWidth() - metricsSmall.stringWidth(resumeText)) / 2;
        int xMenu = (getWidth() - metricsSmall.stringWidth(menuText)) / 2;
        g2d.drawString(resumeText, xResume, y + 60);
        g2d.drawString(menuText, xMenu, y + 90);
    }

    // Method helper lainnya
    // cek movement WASD/Arrow Key
    private boolean isMovementKey(int keyCode) {
        return keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN ||
                keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT ||
                keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_A ||
                keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_D;
    }

    // handle gerakan
    private void handleMovement() {
        if (activeKeys.contains(KeyEvent.VK_UP) || activeKeys.contains(KeyEvent.VK_W)) {
            viewModel.movePlayer("UP", getWidth(), getHeight());
        }
        if (activeKeys.contains(KeyEvent.VK_DOWN) || activeKeys.contains(KeyEvent.VK_S)) {
            viewModel.movePlayer("DOWN", getWidth(), getHeight());
        }
        if (activeKeys.contains(KeyEvent.VK_LEFT) || activeKeys.contains(KeyEvent.VK_A)) {
            viewModel.movePlayer("LEFT", getWidth(), getHeight());
        }
        if (activeKeys.contains(KeyEvent.VK_RIGHT) || activeKeys.contains(KeyEvent.VK_D)) {
            viewModel.movePlayer("RIGHT", getWidth(), getHeight());
        }
    }

    // handle asset player sesuai state
    private Image getPlayerAnimationFrame(Player player) {
        switch (player.getState()) {
            case WALKING:
                walkAnimCounter++;
                if (walkAnimCounter > WALK_FRAME_DURATION) {
                    isWalkFrame = !isWalkFrame;
                    walkAnimCounter = 0;
                }
                return isWalkFrame ? playerWalkImage : playerStandImage;
            case EATING:
                return playerEatImage;
            case IDLE:
            default:
                idleAnimCounter++;
                if (idleAnimCounter > IDLE_FRAME_DURATION) {
                    isIdleJumpFrame = !isIdleJumpFrame;
                    idleAnimCounter = 0;
                }
                return isIdleJumpFrame ? playerJumpImage : playerStandImage;
        }
    }

    // handle arah player
    private void drawPlayerImageWithFlip(Graphics g, Player player, Image image) {
        if (image == null) return;

        int playerWidth = Player.WIDTH;
        int playerHeight = Player.HEIGHT;

        int x = player.getPosition().x - playerWidth / 2;
        int y = player.getPosition().y - playerHeight / 2;

        if (player.getFacing() == Player.Direction.RIGHT) {
            g.drawImage(image, x, y, playerWidth, playerHeight, this);
        } else {
            g.drawImage(image, x + playerWidth, y, -playerWidth, playerHeight, this);
        }
    }
}
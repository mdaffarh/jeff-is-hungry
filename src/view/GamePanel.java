package view;

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

public class GamePanel extends JPanel implements ActionListener {
    private final MainWindow mainWindow;
    private final GameViewModel viewModel;
    private final Timer gameLoop;

    // Variabel untuk Aset Gambar
    private Image backgroundImage;
    private Image basketImage;
    private Image emptyPlateImage;
    private Image playerStandImage, playerJumpImage, playerWalkImage, playerEatImage;
    private Map<String, Image> foodImages;
    //-- 1. Variabel baru untuk aset Three-Patch
    private Image tongueBaseImage, tongueMiddleImage, tongueTipImage;

    // Variabel untuk Font
    private Font customFont;

    // Variabel untuk Kontrol Animasi
    private int idleAnimCounter = 0;
    private final int IDLE_FRAME_DURATION = 40;
    private boolean isIdleJumpFrame = false;

    private int walkAnimCounter = 0;
    private final int WALK_FRAME_DURATION = 15;
    private boolean isWalkFrame = false;

    // Variabel untuk Kontrol Input
    private final Set<Integer> activeKeys = new HashSet<>();

    public GamePanel(MainWindow mainWindow, GameViewModel viewModel) {
        this.mainWindow = mainWindow;
        this.viewModel = viewModel;
        this.setFocusable(true);

        loadAssets(); // Memuat semua gambar dan font
        addListeners(); // Menambahkan semua input listener

        gameLoop = new Timer(16, this);
    }

    private void loadAssets() {
        try {
            // Memuat gambar background dan UI
            backgroundImage = ImageIO.read(getClass().getResourceAsStream("/images/background.png"));
            basketImage = ImageIO.read(getClass().getResourceAsStream("/images/basket.png"));
            emptyPlateImage = ImageIO.read(getClass().getResourceAsStream("/images/foods/plate.png"));

            // Memuat semua gambar makanan
            foodImages = new HashMap<>();
            String[] foodNames = {
                    "steak", "hotdog", "hamburger", "cheesecake", "chocolate", "pudding", "sushi",
                    "carrot", "green", "corn", "eggplant", "cucumber"
            };
            for (String name : foodNames) {
                Image img = ImageIO.read(getClass().getResourceAsStream("/images/foods/" + name + ".png"));
                if (img != null) {
                    foodImages.put(name, img);
                } else {
                    System.err.println("Gagal memuat gambar makanan: " + name + ".png");
                }
            }

            // Memuat gambar animasi karakter
            playerStandImage = ImageIO.read(getClass().getResourceAsStream("/images/jeff/stand.png"));
            playerJumpImage = ImageIO.read(getClass().getResourceAsStream("/images/jeff/jump.png"));
            playerWalkImage = ImageIO.read(getClass().getResourceAsStream("/images/jeff/walk.png"));
            playerEatImage = ImageIO.read(getClass().getResourceAsStream("/images/jeff/eat.png"));

            tongueBaseImage = ImageIO.read(getClass().getResourceAsStream("/images/tongue/tongue_base.png"));
            tongueMiddleImage = ImageIO.read(getClass().getResourceAsStream("/images/tongue/tongue_middle.png"));
            tongueTipImage = ImageIO.read(getClass().getResourceAsStream("/images/tongue/tongue_tip.png"));

        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Gagal memuat salah satu aset gambar!");
            e.printStackTrace();
        }

        try (InputStream is = getClass().getResourceAsStream("/fonts/Pixelify.ttf")) {
            if (is == null) throw new IOException("File font tidak ditemukan: Pixelify.ttf");
            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(baseFont);
            customFont = baseFont;
        } catch (IOException | FontFormatException e) {
            System.err.println("Gagal memuat font kustom. Menggunakan font default.");
            customFont = null;
        }
    }

    private void addListeners() {
        KeyAdapter keyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (isMovementKey(keyCode)) {
                    activeKeys.add(keyCode);
                    handleMovement();
                } else if (keyCode == KeyEvent.VK_SPACE) {
                    viewModel.stopGameAndSave();
                    mainWindow.showPanel("StartScreen");
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

    private boolean isMovementKey(int keyCode) {
        return keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN ||
                keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT ||
                keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_A ||
                keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_D;
    }

    private void handleMovement() {
        // Cek tombol yang sedang aktif di Set dan gerakkan pemain
        if (activeKeys.contains(KeyEvent.VK_UP) || activeKeys.contains(KeyEvent.VK_W)) {
            viewModel.movePlayer("UP");
        }
        if (activeKeys.contains(KeyEvent.VK_DOWN) || activeKeys.contains(KeyEvent.VK_S)) {
            viewModel.movePlayer("DOWN");
        }
        if (activeKeys.contains(KeyEvent.VK_LEFT) || activeKeys.contains(KeyEvent.VK_A)) {
            viewModel.movePlayer("LEFT");
        }
        if (activeKeys.contains(KeyEvent.VK_RIGHT) || activeKeys.contains(KeyEvent.VK_D)) {
            viewModel.movePlayer("RIGHT");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (viewModel.getGameState() == GameViewModel.GameState.PLAYING) {
            viewModel.updateGame(getWidth(), getHeight());
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (viewModel.getGameState() != GameViewModel.GameState.PLAYING) return;

        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Menggambar Karakter Pemain
        Player player = viewModel.getPlayer();
        if (player != null) {
            Image imageToDraw;
            switch (player.getState()) {
                case WALKING:
                    walkAnimCounter++;
                    if (walkAnimCounter > WALK_FRAME_DURATION) {
                        isWalkFrame = !isWalkFrame;
                        walkAnimCounter = 0;
                    }
                    imageToDraw = isWalkFrame ? playerWalkImage : playerStandImage;
                    break;
                case EATING:
                    imageToDraw = playerEatImage;
                    break;
                case IDLE:
                default:
                    idleAnimCounter++;
                    if (idleAnimCounter > IDLE_FRAME_DURATION) {
                        isIdleJumpFrame = !isIdleJumpFrame;
                        idleAnimCounter = 0;
                    }
                    imageToDraw = isIdleJumpFrame ? playerJumpImage : playerStandImage;
                    break;
            }
            drawPlayerImageWithFlip(g, player, imageToDraw);
        }

        // Menggambar Makanan
        for (Food food : viewModel.getFoodItems()) {
            Image imageToDraw;
            if (food.getState() == Food.FoodState.ANIMATING_TO_BASKET) {
                imageToDraw = emptyPlateImage;
            } else {
                imageToDraw = foodImages.get(food.getName());
            }

            if (imageToDraw != null) {
                int imgSize = 64;
                g.drawImage(imageToDraw, food.getPosition().x - imgSize / 2, food.getPosition().y - imgSize / 2, imgSize, imgSize, this);
            }
        }

        //-- 3. Menggambar Laso (Lidah) dengan teknik Three-Patch
        if (player != null) {
            Lasso lasso = viewModel.getLasso();
            if (lasso.getState() != Lasso.LassoState.IDLE && tongueBaseImage != null && tongueMiddleImage != null && tongueTipImage != null) {
                AffineTransform oldTransform = g2d.getTransform();

                Point start = lasso.getStartPoint();
                Point end = lasso.getEndPoint();
                double dx = end.x - start.x;
                double dy = end.y - start.y;
                // 1. Hapus koreksi -90 derajat. Sudut dari atan2 sudah benar untuk aset horizontal.
                double angle = Math.atan2(dy, dx);
                double length = Math.sqrt(dx * dx + dy * dy);

                g2d.translate(start.x, start.y);
                g2d.rotate(angle);

                // 2. Ambil ukuran dari aset
                int baseWidth = tongueBaseImage.getWidth(this);
                int tipWidth = tongueTipImage.getWidth(this);
                int tongueHeight = tongueBaseImage.getHeight(this); // Asumsi tinggi semua patch sama

                // 3. Gambar pangkal lidah di awal (x=0)
                g.drawImage(tongueBaseImage, 0, -tongueHeight / 2, baseWidth, tongueHeight, this);

                // 4. Gambar bagian tengah yang diregangkan setelah pangkal
                int middleWidth = (int) length - baseWidth - tipWidth;
                if (middleWidth > 0) {
                    g.drawImage(tongueMiddleImage, baseWidth, -tongueHeight / 2, middleWidth, tongueHeight, this);
                }

                // 5. Gambar ujung lidah di bagian paling akhir
                g.drawImage(tongueTipImage, (int) length - tipWidth, -tongueHeight / 2, tipWidth, tongueHeight, this);

                g2d.setTransform(oldTransform);
            }
        }
        // Menggambar Keranjang Skor
        if (basketImage != null) {
            int basketWidth = 260;
            int basketHeight = 220;
            int basketX = getWidth() - basketWidth;
            int basketY = (getHeight() - basketHeight) / 2;
            g.drawImage(basketImage, basketX, basketY, basketWidth, basketHeight, this);

            // Menggambar Teks Skor dengan Outline
            g2d.setFont(customFont.deriveFont(Font.BOLD, 18f));
            String scoreText = "Score: " + viewModel.getCurrentScore();
            String countText = "Count: " + viewModel.getCurrentCount();
            int textX = basketX + 87;
            int scoreY = basketY + 120;
            int countY = basketY + 145;

            // Gambar outline (hitam)
            g2d.setColor(Color.BLACK);
            g2d.drawString(scoreText, textX - 1, scoreY - 1);
            g2d.drawString(scoreText, textX + 1, scoreY + 1);
            g2d.drawString(countText, textX - 1, countY - 1);
            g2d.drawString(countText, textX + 1, countY + 1);

            // Gambar isian (putih)
            g2d.setColor(Color.WHITE);
            g2d.drawString(scoreText, textX, scoreY);
            g2d.drawString(countText, textX, countY);
        }
    }

    private void drawPlayerImageWithFlip(Graphics g, Player player, Image image) {
        if (image == null) return;

        int playerWidth = 140;
        int playerHeight = 140;
        int x = player.getPosition().x - playerWidth / 2;
        int y = player.getPosition().y - playerHeight / 2;

        if (player.getFacing() == Player.Direction.RIGHT) {
            g.drawImage(image, x, y, playerWidth, playerHeight, this);
        } else {
            g.drawImage(image, x + playerWidth, y, -playerWidth, playerHeight, this);
        }
    }
}
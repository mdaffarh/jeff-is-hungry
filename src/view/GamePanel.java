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

public class GamePanel extends JPanel implements ActionListener {
    private final MainWindow mainWindow;
    private final GameViewModel viewModel;
    private final Timer gameLoop;

    // --- LANGKAH 1: Tambahkan variabel untuk menampung gambar background ---
    private Image backgroundImage;

    public GamePanel(MainWindow mainWindow, GameViewModel viewModel) {
        this.mainWindow = mainWindow;
        this.viewModel = viewModel;
        this.setFocusable(true);
        // Hapus setBackground() karena akan diganti gambar
        // this.setBackground(Color.CYAN.darker());

        // --- LANGKAH 2: Muat gambar dari folder resources di constructor ---
        try {
            // Pastikan nama file "background.png" sesuai dengan nama file Anda
            backgroundImage = ImageIO.read(getClass().getResourceAsStream("/images/background.png"));
        } catch (IOException e) {
            System.err.println("Gagal memuat gambar background!");
            e.printStackTrace();
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

        // ... (sisa kode untuk menggambar food, player, lasso, dan skor tidak berubah) ...
        for (Food food : viewModel.getFoodItems()) {
            g2d.setColor(food.getColor());
            g2d.fillOval(food.getPosition().x - 15, food.getPosition().y - 15, 30, 30);
            g2d.setColor(Color.BLACK);
            g2d.drawString(String.valueOf(food.getValue()), food.getPosition().x - 5, food.getPosition().y + 5);
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

        g2d.setColor(Color.ORANGE);
        g2d.fillRect(getWidth() - 160, 10, 150, 60);
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Score: " + viewModel.getCurrentScore(), getWidth() - 150, 35);
        g2d.drawString("Count: " + viewModel.getCurrentCount(), getWidth() - 150, 55);
    }
}
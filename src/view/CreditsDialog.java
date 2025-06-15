package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

public class CreditsDialog extends JDialog {

    public CreditsDialog(JFrame parent, Font customFont) {
        super(parent, "Credits", true); // 'true' untuk membuatnya modal

        // Panel Utama
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(240, 220, 180)); // Warna krem

        // Judul
        JLabel titleLabel = new JLabel("Credits & Attributions");
        titleLabel.setFont(customFont.deriveFont(Font.BOLD, 24f));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // -- Hyperlinks --
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Spasi

        // Tambahkan setiap kredit sebagai label yang bisa diklik
        mainPanel.add(createCreditLabel("Background:", "https://crusenho.itch.io/beriesadventureseaside", customFont));
        mainPanel.add(createCreditLabel("Food Assets:", "https://ghostpixxells.itch.io/pixelfood", customFont));
        mainPanel.add(createCreditLabel("", "https://alexkovacsart.itch.io/free-pixel-art-foods", customFont));
        mainPanel.add(createCreditLabel("Fonts (Pixelify Sans):", "https://fonts.google.com/specimen/Pixelify+Sans", customFont));

        mainPanel.add(Box.createVerticalGlue()); // Mendorong tombol ke bawah

        // Tombol Close
        JButton closeButton = new JButton("Close");
        closeButton.setFont(customFont.deriveFont(18f));
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeButton.addActionListener(e -> dispose()); // dispose() untuk menutup dialog

        mainPanel.add(closeButton);

        this.add(mainPanel);
        this.pack(); // Mengatur ukuran dialog agar pas dengan isinya
        this.setResizable(false);
        this.setLocationRelativeTo(parent); // Menampilkan dialog di tengah jendela utama
    }

    // Metode helper untuk membuat label hyperlink
    private JPanel createCreditLabel(String title, String url, Font font) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(font.deriveFont(16f));
        panel.add(titleLabel);

        JLabel linkLabel = new JLabel("<html><a href=''>" + url + "</a></html>");
        linkLabel.setFont(font.deriveFont(16f));
        linkLabel.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Ubah cursor jadi tangan
        linkLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        panel.add(linkLabel);
        return panel;
    }
}
package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.IOException;

public class CreditsDialog extends JDialog {

    public CreditsDialog(JFrame parent, Font customFont) {
        super(parent, "Credits", true);

        // panel Utama
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(240, 220, 180));

        // judul
        JLabel titleLabel = new JLabel("Credits & Attributions");
        titleLabel.setFont(customFont.deriveFont(Font.BOLD, 24f));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // credit-credit dari setiap asset
        mainPanel.add(createCreditLabel("Background Image:", "https://crusenho.itch.io/beriesadventureseaside", customFont));
        mainPanel.add(createCreditLabel("Food Assets:", "", customFont));
        mainPanel.add(createCreditLabel("", "https://ghostpixxells.itch.io/pixelfood", customFont));
        mainPanel.add(createCreditLabel("", "https://alexkovacsart.itch.io/free-pixel-art-foods", customFont));
        mainPanel.add(createCreditLabel("Cursor Assets:", "https://aspecsgaming.itch.io/pixel-art-cursors", customFont));
        mainPanel.add(createCreditLabel("Fonts (Pixelify Sans):", "https://fonts.google.com/specimen/Pixelify+Sans", customFont));

//      // batas
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // asset audio
        mainPanel.add(createCreditLabel("Background Music:", "https://tallbeard.itch.io/music-loop-bundle", customFont));
        mainPanel.add(createCreditLabel("SFX:", "", customFont));
        mainPanel.add(createCreditLabel("", "https://stormyman.itch.io/goofy-sounds-for-scary-monsters", customFont));
        mainPanel.add(createCreditLabel("", "https://mayragandra.itch.io/free-footsteps-sound-effects", customFont));

        mainPanel.add(Box.createVerticalGlue());

        // tombol close
        JButton closeButton = new JButton("Close");
        closeButton.setFont(customFont.deriveFont(18f));
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeButton.addActionListener(e -> dispose());
        mainPanel.add(closeButton);

        this.add(mainPanel);
        this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(parent);

        closeButton.requestFocusInWindow();
    }

    // label dari tiap credit
    private JPanel createCreditLabel(String title, String url, Font font) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(font.deriveFont(16f));
        panel.add(titleLabel);

        JLabel linkLabel = new JLabel("<html><a href=''>" + url + "</a></html>");
        linkLabel.setFont(font.deriveFont(16f));
        linkLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        linkLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (IOException | URISyntaxException ex) {
                    ex.printStackTrace();
                }
            }
        });
        panel.add(linkLabel);
        return panel;
    }
}
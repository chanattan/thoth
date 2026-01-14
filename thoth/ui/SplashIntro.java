package thoth.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import javax.swing.*;

public class SplashIntro extends JWindow {

    private float opacity = 1.0f;
    private float scale = 1.0f;
    private Timer animationTimer;

    private final Runnable onFinish;

    public SplashIntro(Runnable onFinish) {
        this.onFinish = onFinish;

        setSize(600, 400);
        setLocationRelativeTo(null);
        setBackground(new Color(0, 0, 0, 0));

        startAnimation();
        setVisible(true);
    }

    private void startAnimation() {
        animationTimer = new Timer(30, new ActionListener() {
            private int frameCount = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                frameCount++;

                // first 60 frames scaling in
                if (frameCount < 60) {
                    scale = 1.0f + 0.05f * (60 - frameCount) / 60f;
                }

                // after 120 frames start fade out
                if (frameCount > 120) {
                    opacity -= 0.03f;
                    if (opacity <= 0) {
                        animationTimer.stop();
                        try {
                            Thread.sleep(2500);
                        } catch (InterruptedException ex) {
                            System.getLogger(SplashIntro.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                        }
                        dispose();
                        SwingUtilities.invokeLater(onFinish);
                        return;
                    }
                }

                repaint();
            }
        });

        animationTimer.start();
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // fade effect
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

        int w = getWidth();
        int h = getHeight();

        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(0, 0, w, h);

        String title = "THOTH";
        Font titleFont = new Font("Monospaced", Font.BOLD, 60);
        g2.setFont(titleFont);
        FontMetrics fm = g2.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        int titleHeight = fm.getAscent();

        int centerX = w / 2;
        int centerY = h / 2;

        g2.setColor(Color.WHITE);
        g2.drawString(title, centerX - titleWidth / 2, centerY - titleHeight / 2);

        String subtitle = "Invest now, safely.";
        Font subFont = new Font("Monospaced", Font.PLAIN, 20);
        g2.setFont(subFont);
        fm = g2.getFontMetrics();
        int subWidth = fm.stringWidth(subtitle);
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString(subtitle, centerX - subWidth / 2, centerY + titleHeight);

        // scale effect on entire splash
        AffineTransform old = g2.getTransform();
        g2.scale(scale, scale);
        g2.setTransform(old);

        g2.dispose();
    }

    public static void showSplash(Runnable onFinish) {
        SwingUtilities.invokeLater(() -> new SplashIntro(onFinish));
    }
}

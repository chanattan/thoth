package thoth.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.Timer;

public class HaloLabel extends JLabel {

    private float pulse = 0f;
    private boolean expanding = true;

    private static final int BASE_PADDING = 16;
    private float MAX_PULSE = 6f;
    private float SPEED = 0.25f;
    private Color haloColor = new Color(255, 255, 255, 18);

    private int pauseFrames = 0;
    private static final int PAUSE_AT_MAX = 5; // around 0.5s
    private static final int PAUSE_AT_MIN = 60;
    private int radius = 1;

    private boolean animation = false;

    public HaloLabel(Icon icon, float SPEED, float MAX_PULSE, Color haloColor, int radius) {
        super(icon);
        setOpaque(false);

        this.SPEED = SPEED;
        this.MAX_PULSE = MAX_PULSE;
        this.haloColor = haloColor;
        this.radius = radius;

        int size = Math.max(icon.getIconWidth(), icon.getIconHeight()) + (BASE_PADDING + radius + (int) MAX_PULSE) * 2;
        setPreferredSize(new Dimension(size, size));

        Timer timer = new Timer(8, e -> animate());
        timer.start();
    }

    private void animate() {
        if (!animation && pulse <= 0f) return;
        if (pauseFrames > 0) {
            pauseFrames--;
            return;
        }

        pulse += expanding ? SPEED : -SPEED;

        if (pulse >= MAX_PULSE) {
            pulse = MAX_PULSE;
            expanding = false;
            pauseFrames = PAUSE_AT_MAX;
        } else if (pulse <= 0) {
            pulse = 0;
            expanding = true;
            pauseFrames = PAUSE_AT_MIN;
        }

        repaint();
    }

    public void toggleAnimation(boolean toggle) {
        animation = toggle;
        // Wait until pulse is zero
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!animation && pulse <= 0f) {
            super.paintComponent(g);
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        int grow = Math.round(pulse);

        int inset = BASE_PADDING - grow;

        // Outer halo
        Color haloColorAlpha = new Color(this.haloColor.getRed(), this.haloColor.getGreen(), this.haloColor.getBlue(), Math.min(255, Math.max(0, (int)(this.haloColor.getAlpha() * (1 - pulse / MAX_PULSE)))));
        g2.setColor(haloColorAlpha);
        g2.fillOval(inset + radius, inset + radius, w - (inset + radius) * 2, h - (inset + radius) * 2);

        // Inner halo
        g2.setColor(haloColor.brighter().brighter());
        int innerRadius = Math.max(1, radius / 2);

        g2.fillOval(inset + innerRadius, inset + innerRadius, w - (inset + innerRadius) * 2, h - (inset + innerRadius) * 2);

        g2.dispose();
        super.paintComponent(g);
    }
}

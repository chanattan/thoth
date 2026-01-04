package thoth.ui;

import java.awt.*;
import javax.swing.*;

public class AnimatedPopup {
    public enum Direction { TOP, BOTTOM, LEFT, RIGHT }

    private final FadePanel fadePanel;
    private final Component invoker;
    private Popup popup;
    private Timer timer;

    private final int duration; // ms
    private final int fps;
    private int startX, startY, targetX, targetY;

    public AnimatedPopup(JComponent content, Component invoker,
                         Direction direction, int durationMs, int fps,
                         Point startLocation) {
        this.invoker = invoker;
        this.duration = durationMs;
        this.fps = fps;

        // Wrap content in fade panel
        this.fadePanel = new FadePanel(content);

        // Determine popup position based on direction and startLocation
        Dimension contentSize = content.getPreferredSize();
        startX = startLocation.x;
        startY = startLocation.y;

        switch (direction) {
            case TOP -> { targetX = startX; targetY = startY - contentSize.height; }
            case BOTTOM -> { targetX = startX; targetY = startY + contentSize.height; }
            case LEFT -> { targetX = startX - contentSize.width; targetY = startY; }
            case RIGHT -> { targetX = startX + contentSize.width; targetY = startY; }
            default -> { targetX = startX; targetY = startY; }
        }
    }

    public void show() {
        if (popup != null) popup.hide();

        // create popup at starting location
        popup = PopupFactory.getSharedInstance().getPopup(invoker, fadePanel, startX, startY);
        popup.show();
        fadePanel.setAlpha(0f);

        long startTime = System.currentTimeMillis();

        timer = new Timer(1000 / fps, null);
        timer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            float progress = Math.min(1f, (float) elapsed / duration);

            // linear interpolation for slide
            int x = startX + (int) ((targetX - startX) * progress);
            int y = startY + (int) ((targetY - startY) * progress);

            // move panel inside popup
            fadePanel.setLocation(x - startX, y - startY); // relative movement
            fadePanel.setAlpha(progress);

            if (progress >= 1f) timer.stop();
        });
        timer.start();
    }

    public void hide() {
        if (timer != null && timer.isRunning()) timer.stop();
        if (popup != null) popup.hide();
    }

    private static class FadePanel extends JPanel {
    private final JComponent inner;
    private float alpha;

    public FadePanel(JComponent inner) {
        this.inner = inner;
        setLayout(new BorderLayout());
        add(inner, BorderLayout.CENTER);
        setOpaque(false); // critical
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        // apply alpha for all painting inside this panel
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        // DO NOT call super.paintComponent(g2), it paints the opaque background!
        // super.paintComponent(g2);

        g2.dispose();
    }
}

}

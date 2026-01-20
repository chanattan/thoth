package thoth.ui;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.Timer;

public class AnimatedPopup {
    public enum Direction { TOP, BOTTOM, LEFT, RIGHT }
    public enum AnimationType { SLIDE, FADE, SLIDE_FADE, SCALE }
    
    private final FadePanel fadePanel;
    private final Component invoker;
    private Popup popup;
    private Timer showTimer;
    private Timer hideTimer;
    private final int duration;
    private final int fps;
    private int startX, startY, targetX, targetY;
    private final AnimationType animationType;
    private boolean isShowing;
    private int autoHideDelay;
    private Timer autoHideTimer;
    
    public AnimatedPopup(JComponent content, Component invoker,
                        Direction direction, int durationMs, int fps,
                        Point startLocation) {
        this(content, invoker, direction, durationMs, fps, startLocation, AnimationType.SLIDE_FADE);
    }
    
    public AnimatedPopup(JComponent content, Component invoker,
                        Direction direction, int durationMs, int fps,
                        Point startLocation, AnimationType animationType) {
        this.invoker = invoker;
        this.duration = durationMs;
        this.fps = fps;
        this.animationType = animationType;
        this.isShowing = false;
        this.autoHideDelay = -1;
        
        this.fadePanel = new FadePanel(content);
        
        Dimension contentSize = content.getPreferredSize();
        startX = startLocation.x;
        startY = startLocation.y;
        
        switch (direction) {
            case TOP -> {
                targetX = startX;
                targetY = startY - contentSize.height;
            }
            case BOTTOM -> {
                targetX = startX;
                targetY = startY + contentSize.height;
            }
            case LEFT -> {
                targetX = startX - contentSize.width;
                targetY = startY;
            }
            case RIGHT -> {
                targetX = startX + contentSize.width;
                targetY = startY;
            }
            default -> {
                targetX = startX;
                targetY = startY;
            }
        }

        content.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (autoHideTimer != null && autoHideTimer.isRunning()) {
                    autoHideTimer.stop();
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (autoHideDelay > 0) {
                    autoHideTimer = new Timer(autoHideDelay, ae -> hide());
                    autoHideTimer.setRepeats(false);
                    autoHideTimer.start();
                }
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (autoHideTimer != null && autoHideTimer.isRunning()) {
                    autoHideTimer.stop();
                    hide();
                }
            }
        });
    }
    
    public void setAutoHide(int delayMs) {
        this.autoHideDelay = delayMs;
    }
    
    public void addCloseButton() {
        JButton closeBtn = new JButton("×");
        closeBtn.setFont(new Font("Arial", Font.BOLD, 16));
        closeBtn.setForeground(Color.GRAY);
        closeBtn.setBackground(Color.WHITE);
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setPreferredSize(new Dimension(25, 25));
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> hide());
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(closeBtn, BorderLayout.EAST);
        
        fadePanel.add(topPanel, BorderLayout.NORTH);
    }
    
    public void show() {
        if (isShowing) return;
        
        if (showTimer != null && showTimer.isRunning()) showTimer.stop();
        if (hideTimer != null && hideTimer.isRunning()) hideTimer.stop();
        if (popup != null) popup.hide();
        
        int initialX = startX;
        int initialY = startY;
        
        if (animationType == AnimationType.FADE) {
            initialX = targetX;
            initialY = targetY;
        }
        
        popup = PopupFactory.getSharedInstance().getPopup(invoker, fadePanel, initialX, initialY);
        popup.show();
        fadePanel.setAlpha(0f);
        fadePanel.setScale(animationType == AnimationType.SCALE ? 0.5f : 1f);
        
        isShowing = true;
        long startTime = System.currentTimeMillis();
        
        showTimer = new Timer(1000 / fps, null);
        showTimer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            float progress = Math.min(1f, (float) elapsed / duration);
            float easedProgress = easeOutCubic(progress);
            
            switch (animationType) {
                case SLIDE -> {
                    int x = startX + (int) ((targetX - startX) * easedProgress);
                    int y = startY + (int) ((targetY - startY) * easedProgress);
                    popup.hide();
                    popup = PopupFactory.getSharedInstance().getPopup(invoker, fadePanel, x, y);
                    popup.show();
                    fadePanel.setAlpha(1f);
                }
                case FADE -> {
                    fadePanel.setAlpha(easedProgress);
                }
                case SLIDE_FADE -> {
                    int x = startX + (int) ((targetX - startX) * easedProgress);
                    int y = startY + (int) ((targetY - startY) * easedProgress);
                    popup.hide();
                    popup = PopupFactory.getSharedInstance().getPopup(invoker, fadePanel, x, y);
                    popup.show();
                    fadePanel.setAlpha(easedProgress);
                }
                case SCALE -> {
                    float scale = 0.5f + 0.5f * easedProgress;
                    fadePanel.setScale(scale);
                    fadePanel.setAlpha(easedProgress);
                }
            }
            
            if (progress >= 1f) {
                showTimer.stop();
                if (autoHideDelay > 0) {
                    autoHideTimer = new Timer(autoHideDelay, ae -> hide());
                    autoHideTimer.setRepeats(false);
                    autoHideTimer.start();
                }
            }
        });
        showTimer.start();
    }
    
    public void hide() {
        if (!isShowing) return;
        
        if (autoHideTimer != null && autoHideTimer.isRunning()) autoHideTimer.stop();
        if (showTimer != null && showTimer.isRunning()) showTimer.stop();
        if (hideTimer != null && hideTimer.isRunning()) hideTimer.stop();
        
        long startTime = System.currentTimeMillis();
        
        hideTimer = new Timer(1000 / fps, null);
        hideTimer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            float progress = Math.min(1f, (float) elapsed / duration);
            float easedProgress = easeInCubic(progress);
            
            switch (animationType) {
                case SLIDE -> {
                    int x = targetX + (int) ((startX - targetX) * easedProgress);
                    int y = targetY + (int) ((startY - targetY) * easedProgress);
                    popup.hide();
                    popup = PopupFactory.getSharedInstance().getPopup(invoker, fadePanel, x, y);
                    popup.show();
                    fadePanel.setAlpha(1f);
                }
                case FADE -> {
                    fadePanel.setAlpha(1f - easedProgress);
                }
                case SLIDE_FADE -> {
                    int x = targetX + (int) ((startX - targetX) * easedProgress);
                    int y = targetY + (int) ((startY - targetY) * easedProgress);
                    popup.hide();
                    popup = PopupFactory.getSharedInstance().getPopup(invoker, fadePanel, x, y);
                    popup.show();
                    fadePanel.setAlpha(1f - easedProgress);
                }
                case SCALE -> {
                    float scale = 1f - 0.5f * easedProgress;
                    fadePanel.setScale(scale);
                    fadePanel.setAlpha(1f - easedProgress);
                }
            }
            
            if (progress >= 1f) {
                hideTimer.stop();
                if (popup != null) {
                    popup.hide();
                    popup = null;
                }
                isShowing = false;
            }
        });
        hideTimer.start();
    }
    
    public boolean isShowing() {
        return isShowing;
    }
    
    private float easeOutCubic(float t) {
        return 1 - (float) Math.pow(1 - t, 3);
    }
    
    private float easeInCubic(float t) {
        return (float) Math.pow(t, 3);
    }
    
    public static AnimatedPopup createHelpPopup(String title, String message, Component invoker, Point location) {
        JPanel content = new JPanel();
        content.setLayout(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 149, 237), 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        content.setBackground(new Color(240, 248, 255));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(new Color(25, 25, 112));
        
        JTextArea messageArea = new JTextArea(message);
        messageArea.setWrapStyleWord(true);
        messageArea.setLineWrap(true);
        messageArea.setEditable(false);
        messageArea.setOpaque(false);
        messageArea.setFont(new Font("Arial", Font.PLAIN, 12));
        messageArea.setForeground(Color.DARK_GRAY);
        messageArea.setPreferredSize(new Dimension(250, 80));
        
        content.add(titleLabel, BorderLayout.NORTH);
        content.add(messageArea, BorderLayout.CENTER);
        
        AnimatedPopup popup = new AnimatedPopup(content, invoker, Direction.BOTTOM, 400, 60, location, AnimationType.SLIDE_FADE);
        popup.addCloseButton();
        popup.setAutoHide(5000);
        
        return popup;
    }
    
    private static class FadePanel extends JPanel {
        private final JComponent inner;
        private float alpha;
        private float scale;
        
        public FadePanel(JComponent inner) {
            this.inner = inner;
            this.alpha = 1f;
            this.scale = 1f;
            setLayout(new BorderLayout());
            add(inner, BorderLayout.CENTER);
            setOpaque(false);
        }
        
        public void setAlpha(float alpha) {
            this.alpha = Math.max(0f, Math.min(1f, alpha));
            repaint();
        }
        
        public void setScale(float scale) {
            this.scale = Math.max(0.1f, Math.min(2f, scale));
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            
            if (scale != 1f) {
                int w = getWidth();
                int h = getHeight();
                int centerX = w / 2;
                int centerY = h / 2;
                g2.translate(centerX, centerY);
                g2.scale(scale, scale);
                g2.translate(-centerX, -centerY);
            }
            
            super.paintComponent(g2);
            g2.dispose();
        }
        
        @Override
        protected void paintChildren(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            
            if (scale != 1f) {
                int w = getWidth();
                int h = getHeight();
                int centerX = w / 2;
                int centerY = h / 2;
                g2.translate(centerX, centerY);
                g2.scale(scale, scale);
                g2.translate(-centerX, -centerY);
            }
            
            super.paintChildren(g2);
            g2.dispose();
        }
    }
}
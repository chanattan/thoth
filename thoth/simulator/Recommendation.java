package thoth.simulator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.Popup;
import javax.swing.PopupFactory;

import thoth.logic.Fund;
import thoth.logic.Prediction;

public class Recommendation {
    private Thoth thoth;
    private Fund fund;
    private Popup popup;
    private static final Color BG_COLOR = new Color(30, 30, 30);
    private static final Color ACCENT_COLOR = new Color(100, 149, 237);
    public Prediction prediction;
    
    public Recommendation(Thoth thoth, Fund fund) {
        this.thoth = thoth;
        this.fund = fund;
        this.prediction = thoth.AI.getRecommendationForFund(fund);
    }
    
    public void show(Component invoker, Point location) {
        if (popup != null) {
            popup.hide();
        }
        
        JPanel mainPanel = createPanel();
        
        JPanel borderPanel = new JPanel(new BorderLayout());
        borderPanel.setBackground(BG_COLOR);
        borderPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR, 2),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        borderPanel.add(mainPanel, BorderLayout.CENTER);
        
        popup = PopupFactory.getSharedInstance().getPopup(invoker, borderPanel, location.x, location.y);
        popup.show();
    }
    
    public void hide() {
        if (popup != null) {
            popup.hide();
            popup = null;
        }
    }
    
    private JPanel createPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(8, 8));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        
        // Header: Thoth AI icon + title
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        headerPanel.setBackground(BG_COLOR);
        
        if (Thoth.getThothIcon() != null) {
            JLabel iconLabel = new JLabel(new ImageIcon(
                Thoth.getThothIcon().getScaledInstance(24, 24, Image.SCALE_SMOOTH)
            ));
            headerPanel.add(iconLabel);
        }
        
        JLabel titleLabel = new JLabel("Thoth AI");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(ACCENT_COLOR);
        headerPanel.add(titleLabel);
        
        // Date
        Object[] dateInfo = prediction != null ? prediction.getDate() : thoth.getDate();
        String month = (String) dateInfo[0];
        int year = (int) dateInfo[1];
        JLabel dateLabel = new JLabel("[" + month + "/" + year + "]");
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        dateLabel.setForeground(Color.GRAY);
        headerPanel.add(dateLabel);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // fund info
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BG_COLOR);
        
        JPanel fundNamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        fundNamePanel.setBackground(BG_COLOR);
        
        JPanel colorBox = new JPanel();
        colorBox.setBackground(fund.getColor());
        colorBox.setPreferredSize(new Dimension(12, 12));
        colorBox.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        fundNamePanel.add(colorBox);
        
        JLabel fundNameLabel = new JLabel(fund.getName());
        fundNameLabel.setFont(new Font("Arial", Font.BOLD, 13));
        fundNameLabel.setForeground(Color.WHITE);
        fundNamePanel.add(fundNameLabel);
        
        contentPanel.add(fundNamePanel);
        
        // Current value
        double currentValue = fund.getCurve().getLastValues(fund.getCurve().getSteps() - 1)[0];
        JLabel valueLabel = new JLabel(String.format("Current value: $%.2f", currentValue));
        valueLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        valueLabel.setForeground(Color.LIGHT_GRAY);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        valueLabel.setBorder(BorderFactory.createEmptyBorder(3, 18, 2, 0));
        contentPanel.add(valueLabel);
        
        // Monthly return
        double expectedReturn = getExpectedReturn();
        Color returnColor = expectedReturn >= 0 ? new Color(76, 175, 80) : new Color(244, 67, 54);
        String returnSign = expectedReturn >= 0 ? "+" : "";
        JLabel returnLabel = new JLabel(returnSign + expectedReturn + "% (3m)");
        returnLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        returnLabel.setForeground(returnColor);
        returnLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        returnLabel.setBorder(BorderFactory.createEmptyBorder(2, 18, 3, 0));
        contentPanel.add(returnLabel);
        
        // Separator
        JSeparator separator = new JSeparator();
        separator.setForeground(Color.DARK_GRAY);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        contentPanel.add(separator);
        
        // Thoth score/recommendation
        JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        scorePanel.setBackground(BG_COLOR);
        
        String confidenceBadge = getConfidenceBadge();
        
        JLabel scoreLabel = new JLabel("AI Score:");
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        scoreLabel.setForeground(Color.GRAY);
        scorePanel.add(scoreLabel);
        
        JLabel badgeLabel = new JLabel("<html>" + confidenceBadge + "</html>");
        badgeLabel.setFont(new Font("Arial", Font.BOLD, 11));
        badgeLabel.setForeground(getConfidenceColor());
        scorePanel.add(badgeLabel);
        
        contentPanel.add(scorePanel);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        panel.setPreferredSize(new Dimension(320, 140));
        
        return panel;
    }
    
    private String getConfidenceBadge() {
        if (prediction != null && prediction.fund == fund) {
            return prediction.getConfidenceBadge();
        }
        return "<span style='color:#666666;background:#222;padding:2px 6px;border-radius:8px'>⚪ No confidence</span>";
    }

    private int getConfidenceLevel() {
        // For such a recommendation, we consider only the AI confidence level.
        if (prediction != null) {
            return prediction.getAIConfidenceLevel();
        }
        return 0;
    }
    
    private double getExpectedReturn() {
        if (prediction != null && prediction.fund == fund) {
            return prediction.getExpectedReturn();
        }
        return 0.0;
    }
    
    private Color getConfidenceColor() {
        return switch (getConfidenceLevel()) {
            case 4, 3 -> new Color(76, 175, 80);
            case 2 -> new Color(255, 165, 0);
            case 1 -> new Color(244, 67, 54);
            default -> new Color(200, 200, 200);
        };
    }
    
    // Static factory method for easy usage
    public static Recommendation create(Thoth thoth, Fund fund) {
        return new Recommendation(thoth, fund);
    }
}
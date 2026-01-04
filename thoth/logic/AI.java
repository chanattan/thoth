package thoth.logic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import thoth.simulator.Thoth;

public class AI {

    public class Prediction {
        public Fund fund;
        public float confidence;
        
        public Prediction(Fund fund, float confidence) {
            this.fund = fund;
            this.confidence = confidence;
        }

        public float getExpectedReturn() {
            return computeExpectedReturn(fund);
        }

        public int getConfidenceLevel() {
            if (confidence > 0.75f) return 3; // High confidence
            else if (confidence > 0.5f) return 2; // Medium confidence
            else if (confidence > 0.25f) return 1; // Low confidence
            else return 0; // No confidence
        }

    }

    private Thoth thoth;
    public AI(Thoth thoth) {
        this.thoth = thoth;
    }
    public Prediction predictNextMove() {
        Fund bestFund = null;
        int bestIncrease = Integer.MIN_VALUE;
        for (Fund f : thoth.funds) {
            Curve c = f.getCurve();
            int[] lastValues = c.getLastValues(Math.max(0, c.getSteps() - 5));
            boolean increasing = true;
            for (int i = 1; i < lastValues.length; i++) {
                if (lastValues[i] - lastValues[i - 1] <= 0) {
                    increasing = false;
                    break;
                }
            }
            if (increasing) {
                int increase = lastValues[lastValues.length - 1] - lastValues[0]; // Get the increase over the last period
                if (increase > bestIncrease) {
                    bestIncrease = increase;
                    bestFund = f;
                }
            }
        }

        if (bestFund != null) {
            float confidence = computeConfidence(bestFund);
            return new Prediction(bestFund, confidence);
        }

        return new Prediction(bestFund, 0.0f); // Best action, may be null
    }

    /*
        * TODO: les curves étant prégénérables, on peut afficher en décalé antérieur les courbes et calculer un score de confiance
        * en modèle boîte blanche sur les futures performances.
        * 
        * Computes a confidence score for the fund's recent performance: number of increases over total observations.
        * Returns a float between 0 and 1.
    */
    public float computeConfidence(Fund fund) {
        Curve c = fund.getCurve();
        int[] lastValues = c.getLastValues(Math.max(0, c.getSteps() - 10));
        int increases = 0;
        for (int i = 1; i < lastValues.length; i++) {
            if (lastValues[i] > lastValues[i - 1]) {
                increases++;
            }
        }
        return (float) increases / (lastValues.length - 1); // Proportion of increases
    }

    /**
     * Computes the expected return for a given fund based on its recent performance: it is the percentage increase for the next time step.
     * Returns a double representing the expected return percentage.
     */
    public float computeExpectedReturn(Fund fund) {
        Curve c = fund.getCurve();
        int[] lastValues = c.getLastValues(Math.max(0, c.getSteps() - 5));
        if (lastValues.length < 2) return 0.0f;
        return (float)(lastValues[lastValues.length - 1] - lastValues[0]) / lastValues[0]; // Percentage increase
    }

    public void update() {
        // Future implementation: update AI state if needed.
        // Including autopop.
    }

    public Popup popInfo(JComponent component, int x, int y) {
        JLabel fundLabel = null;
        JLabel noFundLabel = null;
        Prediction prediction = predictNextMove();
        if (prediction.fund != null) {
            String fundName = prediction.fund.getName();
            int expectedIncrease = (int)(prediction.getExpectedReturn() * 100);
            fundLabel = new JLabel("<html><p style='color:orange'>> Best Fund to Invest: <b align='center'>" + fundName + "</b></p><br>" +
                " | Expected Increase: <b style='color:#11ffff'>" + expectedIncrease + "%</b><br>" +
                " | Confidence Level: <b style='color:" + (prediction.getConfidenceLevel() == 3 ? "green" : prediction.getConfidenceLevel() == 2 ? "orange" : "red") + "'>" + prediction.getConfidenceLevel() + "</b><br>" +
                " <br> " +
                " <h5>Note: This prediction is based on the last five months of the fund's performance.</h5><br>" +
                " <h6>(Click anywhere in this popup to close)</h6></html>");
            fundLabel.setForeground(Color.LIGHT_GRAY);
        } else {
            noFundLabel = new JLabel("<html><p style='color:red'>> No clear best fund to invest.</p><br>" +
                " | Confidence Level: <b style='color:red'>0</b><br>"
                + "<br><h5>Note: Thoth is unable to determine with decent confidence which<br>fund is best to invest in at this time.</h5><br>" +
                "<h6>(Click anywhere in this popup to close)</h6></html>");
            noFundLabel.setForeground(Color.LIGHT_GRAY);
        }

        JLabel closeButton = new JLabel("✕");
        closeButton.setFocusable(false);
        closeButton.setForeground(Color.GRAY);
    
        JPanel panel = new JPanel();
        panel.setBackground(new Color(30, 30, 30));
        panel.setLayout(new java.awt.BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new java.awt.BorderLayout());
        topPanel.setBackground(new Color(30, 30, 30));
        JLabel titleLabel = new JLabel("<html><h3 align='center'>Thoth AI</h3></html>");
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 7, 0, 0));
        titleLabel.setForeground(Color.ORANGE);
        JLabel logo = new JLabel(new javax.swing.ImageIcon(
            getClass().getResource("../../assets/thoth_ico.png")
        ));
        topPanel.add(titleLabel, java.awt.BorderLayout.CENTER);
        topPanel.add(logo, java.awt.BorderLayout.WEST);
        topPanel.add(closeButton, java.awt.BorderLayout.EAST);
        panel.add(topPanel, java.awt.BorderLayout.NORTH);

        JSeparator separator = new JSeparator();
        separator.setForeground(Color.GRAY);
        panel.add(separator, BorderLayout.CENTER);

        if (fundLabel != null) {
            panel.add(fundLabel, java.awt.BorderLayout.SOUTH);
        } else {
            panel.add(noFundLabel, java.awt.BorderLayout.SOUTH);
        }

        JPanel content = new JPanel();
        content.setBackground(new Color(30, 30, 30));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        content.add(panel);

        // Helpful

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING, 4, 0));
        buttonPanel.setBackground(new Color(30, 30, 30));

        JLabel helpfulLabel = new JLabel("<html><h5>Was this helpful?</h5></html>");
        helpfulLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 3));
        helpfulLabel.setForeground(Color.LIGHT_GRAY);
        buttonPanel.add(helpfulLabel);

        JLabel slash = new JLabel(" / ");
        slash.setForeground(Color.LIGHT_GRAY);

        JLabel noButton = new JLabel("No");
        noButton.setFocusable(false);
        noButton.setForeground(Color.LIGHT_GRAY);
        noButton.setFont(noButton.getFont().deriveFont(12f));

        JLabel yesButton = new JLabel("Yes");
        yesButton.setFocusable(false);
        yesButton.setForeground(Color.LIGHT_GRAY);
        // set font to 12pt
        yesButton.setFont(yesButton.getFont().deriveFont(12f));
        yesButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                System.out.println("User found the AI prediction helpful.");
                yesButton.setText("<html><h5>You confirmed this was helpful.</h5></html>");
                noButton.setText("");
                slash.setText("");
                helpfulLabel.setText("");
            }
        });
        buttonPanel.add(yesButton);

        buttonPanel.add(slash);

        noButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                System.out.println("User did not find the AI prediction helpful.");
                noButton.setText("<html><h5>You indicated this was not helpful.</h5></html>");
                yesButton.setText("");
                slash.setText("");
                helpfulLabel.setText("");
            }
        });
        buttonPanel.add(noButton);
        
        content.add(buttonPanel);

        JPanel borderPanel = new JPanel();
        borderPanel.setBackground(new Color(30, 30, 30));
        borderPanel.setBorder(javax.swing.BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        borderPanel.setLayout(new BorderLayout());
        borderPanel.add(content, BorderLayout.CENTER);

        Popup popup = PopupFactory.getSharedInstance().getPopup(component, borderPanel, x, y);

        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                popup.hide();
            }
        });

         borderPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                popup.hide();
            }
        });

        return popup;
    }
}
package thoth.logic;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import thoth.simulator.Simulator;
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
        Color BG_COLOR = new Color(30, 30, 30);
        Prediction prediction = predictNextMove();
        if (prediction.fund != null) {
            String fundName = prediction.fund.getName();
            int expectedIncrease = (int)(prediction.getExpectedReturn() * 100);
            fundLabel = new JLabel("<html><p style='color:orange'>> Best Fund to Invest: <b>" + fundName + "</b></p>" +
                " | Expected Increase: <b style='color:#11ffff'>" + expectedIncrease + "%</b>" +
                " | Confidence Level: <b style='color:" + (prediction.getConfidenceLevel() == 3 ? "green" : prediction.getConfidenceLevel() == 2 ? "orange" : "red") + "'>" + prediction.getConfidenceLevel() + "</b>" +
                "<br>| Note: This prediction is based on the last five months of the fund's performance.<br>" +
                " <span style='color:gray'>(Click anywhere in this popup to close)</span></html>");
            fundLabel.setForeground(Color.LIGHT_GRAY);
        } else {
            noFundLabel = new JLabel("<html><p style='color:red'>> No clear best fund to invest.</p>" +
                " | Confidence Level: <b style='color:red'>0</b>" +
                " | Note: Thoth is unable to determine with decent confidence<br>which fund is best to invest in at this time.<br>" +
                " <span style='color:gray'>(Click anywhere in this popup to close)</span></html>");
            noFundLabel.setForeground(Color.LIGHT_GRAY);
        }

        JLabel closeButton = new JLabel("✕");
        closeButton.setFocusable(false);
        closeButton.setForeground(Color.GRAY);

        // Create horizontal layout panel
        JPanel panel = new JPanel();
        panel.setBackground(BG_COLOR);
        panel.setLayout(new java.awt.BorderLayout());

        // Left side logo and title
        JPanel leftPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 5));
        leftPanel.setBackground(BG_COLOR);
        Object[] dateInfo = thoth.getDate();
        String month = (String) dateInfo[0];
        int year = (int) dateInfo[1];
        JLabel titleLabel = new JLabel("<html><h3>Thoth AI</h3>[" + month + "/" + year + "]</html>");
        titleLabel.setForeground(Color.ORANGE);
        JLabel logo = new JLabel(new javax.swing.ImageIcon(
            getClass().getResource("../../assets/thoth_ico.png")
        ));
        leftPanel.add(logo);
        leftPanel.add(titleLabel);

        // main content in center
        JPanel centerPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));
        centerPanel.setBackground(BG_COLOR);
        if (fundLabel != null) {
            centerPanel.add(fundLabel);
        } else {
            centerPanel.add(noFundLabel);
        }

        // Right side with question helpful feedback and close button
        JPanel rightPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 5));
        rightPanel.setBackground(BG_COLOR);

        JLabel helpfulLabel = new JLabel("<html><h5>Helpful?</h5></html>");
        helpfulLabel.setForeground(Color.LIGHT_GRAY);
        rightPanel.add(helpfulLabel);

        JLabel slash = new JLabel(" / ");
        slash.setForeground(Color.LIGHT_GRAY);

        JLabel yesButton = new JLabel("Yes");
        JLabel somewhatButton = new JLabel("Somewhat");
        JLabel noButton = new JLabel("No");
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

        noButton.setFocusable(false);
        noButton.setForeground(Color.LIGHT_GRAY);
        noButton.setFont(noButton.getFont().deriveFont(12f));
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

        somewhatButton.setFocusable(false);
        somewhatButton.setForeground(Color.LIGHT_GRAY);
        somewhatButton.setFont(somewhatButton.getFont().deriveFont(12f));
        somewhatButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                System.out.println("User found the AI prediction somewhat helpful.");
                somewhatButton.setText("<html><h5>You indicated this was somewhat helpful.</h5></html>");
                yesButton.setText("");
                noButton.setText("");
                slash.setText("");
                helpfulLabel.setText("");
            }
        });

        rightPanel.add(yesButton);
        rightPanel.add(slash);
        rightPanel.add(noButton);
        rightPanel.add(slash);
        rightPanel.add(somewhatButton);
        rightPanel.add(new JLabel("  "));
        rightPanel.add(closeButton);

        panel.add(leftPanel, java.awt.BorderLayout.WEST);
        panel.add(centerPanel, java.awt.BorderLayout.CENTER);
        panel.add(rightPanel, java.awt.BorderLayout.EAST);

        JPanel content = new JPanel();
        content.setBackground(new Color(0, 0, 0, 0));
        content.setLayout(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        content.add(panel, BorderLayout.CENTER);

        JPanel borderPanel = new JPanel();
        borderPanel.setBackground(BG_COLOR);
        borderPanel.setBorder(javax.swing.BorderFactory.createLineBorder(Color.ORANGE, 1));
        borderPanel.setLayout(new BorderLayout());
        borderPanel.add(content, BorderLayout.CENTER);

        // bottom of simulator panel, spanning its width
        int simulatorWidth = thoth.window.getSimulator().getWidth();
        int simulatorHeight = thoth.window.getSimulator().getHeight();
        java.awt.Point simulatorLocation = thoth.window.getSimulator().getLocationOnScreen();
        
        int popupX = simulatorLocation.x + 10;
        int popupY = simulatorLocation.y + simulatorHeight - 125;
        int popupWidth = simulatorWidth - 20;
        
        borderPanel.setPreferredSize(new java.awt.Dimension(popupWidth, 90));

        Popup popup = PopupFactory.getSharedInstance().getPopup(component, borderPanel, popupX, popupY);

        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                ((Simulator) component).popup = null;
                //((Simulator) component).thothButton.toggleAnimation(true);
                popup.hide();
            }
        });

        borderPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                ((Simulator) component).popup = null;
                //((Simulator) component).thothButton.toggleAnimation(true);
                popup.hide();
            }
        });

        return popup;
    }
}
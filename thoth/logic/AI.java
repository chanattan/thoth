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

    private Thoth thoth;
    private Popup whyPopup = null;
    private String lastFundText = "";
    private String lastNoFundText = "";
    private boolean moreInfoShown = false;
    public AI(Thoth thoth) {
        this.thoth = thoth;
    }

    /**
     * Deprecated.
     */
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
            return new Prediction(bestFund, computeExpectedReturn(bestFund), confidence, confidence);
        }

        return new Prediction(bestFund, 0.0f, 0.0f, 0.0f); // Best action, may be null
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
    public static float computeExpectedReturn(Fund fund) {
        Curve c = fund.getCurve();
        int[] lastValues = c.getLastValues(Math.max(0, c.getSteps() - 5));
        if (lastValues.length < 2) return 0.0f;
        return (float)(lastValues[lastValues.length - 1] - lastValues[0]) / lastValues[0]; // Percentage increase
    }

    public void update() {
        // Future implementation: update AI state if needed.
        // Including autopop.
    }

    private JLabel fundLabel;
    private JLabel noFundLabel;
    public Popup popInfo(JComponent component) {
        Prediction prediction = thoth.prediction;

        fundLabel = null;
        noFundLabel = null;
        Color BG_COLOR = new Color(30, 30, 30);

        // recommending conditions (less strict than pop): should we recommend at all?
        if (prediction.fund != null && prediction.forecastResult != null && prediction.getAIConfidenceLevel() >= 1
                && prediction.getExpectedReturn() > 0.5) {
            // showing hint
            Simulator.lastHintTime = System.currentTimeMillis();

            int confidence = prediction.getAIConfidenceLevel();
            //String explanation = "The AI recommends investing in " + prediction.fund.getName() + " because it has shown a consistent upward trend in recent periods.";
            
            // Logger
            thoth.logger.logShowHint(
                confidence >= 3 ? "low" : (confidence == 2 ? "medium" : "high"),
                "factors",
                true, // precondition_ok (capital suffisant ?)
                "Recommendation: " + prediction.fund.getName() // notes
            );
            
            String fundName = prediction.fund.getName();
            double expectedIncrease = prediction.getExpectedReturn();
            
            String arimaFactors = ArimaPredictor.explain(prediction, prediction.forecastResult, 
                                                    prediction.fund.getCurve().getPregeneratedValue());
            
            lastFundText = "<html><p style='color:orange'>> Best Fund to Invest: <b>" + fundName + "</b></p>" +
                "| Expected +<span style='color:#00FFFF'>" + expectedIncrease + "%</span> (3 months) " +
                "| Confidence: <b> " + prediction.getConfidenceBadge() + "</b>" +
                "<br>" + arimaFactors.replace("\n", "<br>") + "</html>";
            fundLabel = new JLabel(lastFundText);
            fundLabel.setForeground(Color.LIGHT_GRAY);
                
        } else {
            lastNoFundText = "<html><p style='color:red'>> No prediction available.</p>" +
                "| Confidence too low<br>" +
                "<span>(Need more data or stable trend)</span></html>";
            noFundLabel = new JLabel(lastNoFundText);
            noFundLabel.setForeground(Color.LIGHT_GRAY);
        }

        JLabel whyLabel = new JLabel("<html><i>[How does Thoth work?]</i></html>");
        whyLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (whyPopup != null) {
                    whyPopup.hide();
                    whyPopup = null;
                    return;
                }
                JPanel whyPanel = new JPanel();
                whyPanel.setBackground(BG_COLOR);
                whyPanel.setLayout(new BorderLayout());
                JLabel content = new JLabel("<html><div style='width:300px;'>" + ArimaPredictor.why().replace("\n", "<br>") + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span style='font-size:small'>(Click anywhere to close)</span></div></html>");
                content.setForeground(Color.LIGHT_GRAY);
                content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
                whyPanel.add(content, BorderLayout.CENTER);

                JPanel borderPanel = new JPanel();
                borderPanel.setBackground(BG_COLOR);
                borderPanel.setBorder(javax.swing.BorderFactory.createLineBorder(Color.ORANGE, 1));
                borderPanel.setLayout(new BorderLayout());
                borderPanel.add(whyPanel, BorderLayout.CENTER);

                int simulatorHeight = thoth.window.getSimulator().getHeight();
                java.awt.Point simulatorLocation = thoth.window.getSimulator().getLocationOnScreen();
                
                int popupX = simulatorLocation.x + 10;
                int popupY = simulatorLocation.y + simulatorHeight - 360;

                whyPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        // Close the popup when clicked
                        javax.swing.SwingUtilities.getWindowAncestor(borderPanel).dispose();
                    }
                });

                whyPopup = PopupFactory.getSharedInstance().getPopup(component, borderPanel, popupX, popupY);
                whyPopup.show();
            }
        });
        whyLabel.setForeground(Color.LIGHT_GRAY);

        JLabel moreInfoLabel = new JLabel("<html><i>[More info]</i></html>");
        moreInfoLabel.setForeground(Color.LIGHT_GRAY);
        moreInfoLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (prediction == null || prediction.fund == null || prediction.forecastResult == null) {
                    return;
                }
                if (AI.this.noFundLabel != null) {
                    if (AI.this.noFundLabel.getText().contains("3 MONTHS")) {
                        AI.this.noFundLabel.setText(lastNoFundText);
                        moreInfoLabel.setText("<html><i>[More info]</i></html>");
                        moreInfoShown = false;
                    } else {
                        AI.this.noFundLabel.setText("<html><p style='color:red'>> No prediction available.</p>" +
                            "| Confidence too low<br>" +
                            "<span>" + ArimaPredictor.moreInfo(prediction.forecastResult, prediction.fund.getCurve().getPregeneratedValue()).replace("\n", "<br>") + "</span></html>");
                        moreInfoLabel.setText("<html><i>[Less info]</i></html>");
                        moreInfoShown = true;
                    }
                } else if (AI.this.fundLabel != null) {
                    if (AI.this.fundLabel.getText().contains("3 MONTHS")) {
                        AI.this.fundLabel.setText(lastFundText);
                        moreInfoLabel.setText("<html><i>[More info]</i></html>");
                        moreInfoShown = false;
                    } else {
                        AI.this.fundLabel.setText("<html><p style='color:orange'>> Best Fund to Invest: <b>" + prediction.fund.getName() + "</b></p>" +
                            "| Expected +<span style='color:#00FFFF'>" + prediction.getExpectedReturn() + "%</span> (3 months) " +
                            "| Confidence: <b> " + prediction.getConfidenceBadge() + "</b>" +
                            "<br>" + ArimaPredictor.moreInfo(prediction.forecastResult, prediction.fund.getCurve().getPregeneratedValue()).replace("\n", "<br>") + "</html>");
                        moreInfoLabel.setText("<html><i>[Less info]</i></html>");
                        moreInfoShown = true;
                    }
                }
            }
        });
        moreInfoLabel.setForeground(Color.LIGHT_GRAY);

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
        JLabel logo = Thoth.getThothIcon() != null ? new JLabel(new javax.swing.ImageIcon(
            Thoth.getThothIcon().getScaledInstance(32, 32, java.awt.Image.SCALE_SMOOTH)
        )) : new JLabel("[Thoth]");
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

        JLabel helpfulLabel = new JLabel("<html><h4>Helpful?</h4></html>");
        helpfulLabel.setForeground(Color.LIGHT_GRAY);
        rightPanel.add(helpfulLabel);

        JLabel slash = new JLabel(" / ");
        slash.setForeground(Color.LIGHT_GRAY);
        JLabel slash2 = new JLabel(" / ");
        slash2.setForeground(Color.LIGHT_GRAY);

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
                yesButton.setText("<html><h4>You confirmed this was helpful.</h4></html>");
                noButton.setText("");
                slash.setText("");
                helpfulLabel.setText("");
                slash2.setText("");
                somewhatButton.setText("");

                thoth.logger.logSubmit(0.9, "Y", "User found helpful");
                thoth.window.sim.foundHelpful +=1;
            }
        });

        noButton.setFocusable(false);
        noButton.setForeground(Color.LIGHT_GRAY);
        noButton.setFont(noButton.getFont().deriveFont(12f));
        noButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                System.out.println("User did not find the AI prediction helpful.");
                noButton.setText("<html><h4>You indicated this was not helpful.</h4></html>");
                yesButton.setText("");
                slash.setText("");
                helpfulLabel.setText("");
                slash2.setText("");
                somewhatButton.setText("");
                thoth.logger.logSubmit(0.2, "Y", "User did not find this helpful");
                thoth.window.sim.notHelpful +=1;
            }
        });

        somewhatButton.setFocusable(false);
        somewhatButton.setForeground(Color.LIGHT_GRAY);
        somewhatButton.setFont(somewhatButton.getFont().deriveFont(12f));
        somewhatButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                System.out.println("User found the AI prediction somewhat helpful.");
                somewhatButton.setText("<html><h4>You indicated this was somewhat helpful.</h4></html>");
                yesButton.setText("");
                noButton.setText("");
                slash.setText("");
                helpfulLabel.setText("");
                slash2.setText("");
                thoth.logger.logSubmit(0.5, "Y", "User found somewhat helpful");
                thoth.window.sim.somewhatHelpful +=1;
            }
        });

        rightPanel.add(yesButton);
        rightPanel.add(slash);
        rightPanel.add(noButton);
        rightPanel.add(slash2);
        rightPanel.add(somewhatButton);
        rightPanel.add(new JLabel("  "));
        rightPanel.add(closeButton);

        panel.add(leftPanel, java.awt.BorderLayout.WEST);
        panel.add(centerPanel, java.awt.BorderLayout.CENTER);
        panel.add(rightPanel, java.awt.BorderLayout.EAST);
        JPanel twocolPanel = new JPanel(new java.awt.BorderLayout());
        twocolPanel.setBackground(BG_COLOR);
        twocolPanel.add(whyLabel, java.awt.BorderLayout.EAST);
        twocolPanel.add(moreInfoLabel, java.awt.BorderLayout.WEST);
        twocolPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(twocolPanel, java.awt.BorderLayout.SOUTH);

        if (moreInfoShown) {
            moreInfoLabel.getMouseListeners()[0].mouseClicked(new java.awt.event.MouseEvent(
                moreInfoLabel, java.awt.event.MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 0, 0, 1, false ));
        }

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
        int popupY = simulatorLocation.y + simulatorHeight - 145;
        int popupWidth = simulatorWidth - 20;
        
        borderPanel.setPreferredSize(new java.awt.Dimension(popupWidth, 110));

        Popup popup = PopupFactory.getSharedInstance().getPopup(component, borderPanel, popupX, popupY);

        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                thoth.window.sim.removeThoth();
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                thoth.window.sim.removeThoth();
            }
        });

        borderPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                thoth.window.sim.removeThoth();
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                thoth.window.sim.removeThoth();
            }
        });

        return popup;
    }
}
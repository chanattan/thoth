package thoth.logic;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import thoth.simulator.Thoth;

public class AI {

    protected class Prediction {
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

    public JPopupMenu popInfo(JComponent component, int x, int y) {
        JPopupMenu popup = new JPopupMenu();

        Prediction prediction = predictNextMove();
        if (prediction.fund != null) {
            String fundName = prediction.fund.getName();
            JLabel fundLabel = new JLabel("<html><h4 align='center'>Thoth</h4><p style='color:orange'>> Best Fund to Invest:</p> <b>" + fundName + "</b><br>" +
                " | Expected Increase: " + prediction.getExpectedReturn() + "<br>" +
                " | Confidence Level: " + prediction.getConfidenceLevel() + "<br>" +
                " <br> " +
                " Note: This prediction is based on the last five months of the fund's performance.<br>" +
                " <i>(Click anywhere to close this popup)</i></html>");
            popup.add(fundLabel);
        } else {
            JLabel noFundLabel = new JLabel("<html><h4 align='center'>Thoth</h4><p style='color:red'>> No clear best fund to invest.</p><br>" +
                " | Confidence Level: 0<br>"
                + "<br>Note: Thoth is unable to determine with decent confidence which<br>fund is best to invest in at this time.<br>" +
                "<i>(Click anywhere to close this popup)</i></html>");
            popup.add(noFundLabel);
        }
        
        // show popup at mouse position
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                popup.show(component, e.getX(), e.getY());
            }
        });
        return popup;
    }
}
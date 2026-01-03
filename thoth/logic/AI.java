package thoth.logic;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import thoth.simulator.Thoth;

public class AI {

    private Thoth thoth;
    public AI(Thoth thoth) {
        this.thoth = thoth;
    }
    public Fund predictNextMove() {
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
        return bestFund; // Best action, may be null
    }

    public JPopupMenu popInfo(JComponent component, int x, int y) {
        JPopupMenu popup = new JPopupMenu();

        Fund bestFund = predictNextMove();
        if (bestFund != null) {
            String fundName = bestFund.getName();
            JLabel fundLabel = new JLabel("Thoth: Best Fund to Invest: " + fundName);
            popup.add(fundLabel);
        } else {
            JLabel noFundLabel = new JLabel("Thoth: No clear best fund to invest.");
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
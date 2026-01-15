	package thoth.simulator;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import thoth.logic.Fund;

public class FundsPanel extends JPanel {
    private Thoth thoth;
    public JSplitPane parentPane;
    private Fund clickedFund = null;

    public FundsPanel(Thoth thoth) {
        this.thoth = thoth;
        this.setLayout(new BorderLayout());

        this.setBackground(Color.BLACK);
        addMouseListener(mouseEvent());
    }
    
    public void updatePanel() {
        repaint();
    }

    private java.awt.event.MouseAdapter mouseEvent() {
        return new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                int clickY = e.getY();
                int maxDisplayedFunds = 8;
                int yOffset = 30;
                int globalYOffset = 70;
                
                for (int i = 0; i < Math.min(thoth.funds.size(), maxDisplayedFunds - 1); i++) {
                    int fundY = globalYOffset + yOffset * i;
                    if (clickY >= fundY - 15 && clickY <= fundY + 15) {
                        clickedFund = thoth.funds.get(i);
                        System.out.println("Clicked on fund: " + clickedFund.getName());
                        break;
                    }
                }

                if (clickedFund != null) {
                    thoth.window.sim.selectedFund = clickedFund;
                    repaint();
                }
            }
        };
    }

    @Override
    protected void paintComponent(Graphics g2) {
		/*
			Draw meta information on the left.
		*/
        super.paintComponent(g2);
        Graphics2D g = (Graphics2D) g2;

		// Horizontal bar
        String header = "FUNDS";
        FontMetrics fm = g.getFontMetrics();
        int x = (this.getWidth() - fm.stringWidth(header)) / 2;
        int y = 30;
        // background for title
        g.setColor(Color.ORANGE);
        int padding = 10;
        g.fillRoundRect(x - padding, y - fm.getAscent() - 7, fm.stringWidth(header) + 3 * padding + 5, fm.getHeight() + 10, 15, 15);

        g.setColor(Color.BLACK);
        g.setFont(Thoth.customFont.deriveFont(Font.PLAIN, 20f));
        g.drawString(header, x, y);

		int maxDisplayedFunds = 8;
		int yOffset = 30;
        int globalYOffset = 70;
		for (int l = 0; l < thoth.funds.size(); l++) {
			if (l >= maxDisplayedFunds - 1)
				break;
			Fund f = thoth.funds.get(l);
			String fundName = f.getName();

            // Background for selected fund
            if (clickedFund == f) {
                g.setColor(new Color(255, 255, 255, 50));
                g.fillRoundRect(10, globalYOffset + yOffset * l - 20, this.getWidth() - 20, 25, 10, 10);
            }

			Color c = f.getColor();
			if (c == null) {
				c = Simulator.colorFromIndex(l + 10);
				f.setColor(c);
			}
			g.setColor(c);
			g.drawString(fundName, 20, globalYOffset + yOffset * l);
            float val = f.getValueChangePercent();
            NewsPanel.drawColoredParenthesesText(g, String.format("(" + (val >= 0 ? "+" : "") + "%.2f%%)", val), 125, globalYOffset + yOffset * l, c);
		}
	}
}
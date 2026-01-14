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

    public FundsPanel(Thoth thoth) {
        this.thoth = thoth;
        this.setLayout(new BorderLayout());

        this.setBackground(Color.BLACK);
    }
    
    public void updatePanel() {
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g2) {
		/*
			Draw meta information on the left.
		*/
        super.paintComponent(g2);
        Graphics2D g = (Graphics2D) g2;

		// Bar
		g.setColor(Color.BLACK);
		g.fillRect(0, 26, 120, this.getHeight());
		g.setFont(new Font("Monospaced", Font.PLAIN, 13)); 

		// Information relative to the n displayed funds
		int maxDisplayedFunds = 8;
		int yOffset = 30;
		for (int l = 0; l < thoth.funds.size(); l++) {
			if (l >= maxDisplayedFunds - 1)
				break;
			Fund f = thoth.funds.get(l);
			String fundName = f.getName();

			Color c = f.getColor();
			if (c == null) {
				c = Simulator.colorFromIndex(l + 10);
				f.setColor(c);
			}
			g.setColor(c);
			g.drawString(fundName, 20, 30 + yOffset * l);
            float val = f.getValueChangePercent();
            NewsPanel.drawColoredParenthesesText(g, String.format("(" + (val >= 0 ? "+" : "") + "%.2f%%)", val), 125, 30 + yOffset * l, c);
		}
	}
}
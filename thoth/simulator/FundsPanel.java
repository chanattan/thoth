	package thoth.simulator;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import thoth.logic.Fund;

public class FundsPanel extends JPanel {
    private Thoth thoth;
    public JSplitPane parentPane;
    private Fund clickedFund = null;
    private Rectangle clearButton = null;
    private Rectangle modeButton = null;
    private int mode = 0;
    private boolean poppedThoth = false;

    private Image thothImage = null;

    public FundsPanel(Thoth thoth) {
        this.thoth = thoth;
        this.setLayout(new BorderLayout());

        thothImage = Thoth.getThothIcon();

        this.setBackground(Color.BLACK);
        addMouseListener(mouseEvent());
    }
    
    public void updatePanel() {
        repaint();
    }

    public void clearFocus() {
        this.clickedFund = null;
    }

    public Fund getClickedFund() {
        return this.clickedFund;
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
                        thoth.window.sim.removeThoth();
                        System.out.println("Clicked on fund: " + clickedFund.getName());
                        break;
                    }
                }

                // Fund selected
                if (clickedFund != null) {
                    thoth.logger.startMeasure("decision_time_ms");
                    thoth.window.sim.selectedFund = clickedFund;
                    repaint();
                }

                // Check if clear button is clicked
                if (clearButton != null && clearButton.contains(e.getPoint())) {
                    clearFocus();
                    repaint();
                }

                // Check mode button
                if (modeButton != null && modeButton.contains(e.getPoint())) {
                    thoth.window.sim.setFundDisplay(mode = (mode + 1) % 3);
                    repaint();
                }

                // Thoth button
                if (clickedFund != null) {
                    Rectangle thothButtonBounds = new Rectangle(FundsPanel.this.getWidth() - 50, globalYOffset + yOffset * thoth.funds.indexOf(clickedFund) - 15, 20, 20);
                    if (thothButtonBounds.contains(e.getPoint())) {
                        if (poppedThoth)
                            thoth.window.sim.popupThoth();
                        else
                            thoth.window.sim.removeThoth();
                        poppedThoth = !poppedThoth;
                    }
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

                // Draw Thoth for help
                g.drawImage(thothImage, this.getWidth() - 50, globalYOffset + yOffset * l - 15, 20, 20, null);
            }

			Color c = f.getColor();
			if (c == null) {
				c = Simulator.colorFromIndex(l + 10);
				f.setColor(c);
			}
			g.setColor(c);
			g.drawString(fundName, 20, globalYOffset + yOffset * l);
            float val = f.getValueChangePercent();
            NewsPanel.drawColoredParenthesesText(g, String.format("(" + (val >= 0 ? "+" : "") + "%.2f%%)", val), 125, globalYOffset + yOffset * l, val >= 0 ? Color.GREEN.darker() : Color.RED.darker());
		}

        // Clear button
        // Background button
        clearButton = new Rectangle(this.getWidth() - 80, this.getHeight() - 40, 70, 20);
        g.setColor(new Color(255, 255, 255, 50));
        g.fillRect(clearButton.x - 2, clearButton.y - 2, clearButton.width + 5, clearButton.height + 5);
        g.setColor(Color.RED);
        g.fill(clearButton);
        g.setColor(Color.WHITE);
        g.setFont(Thoth.customFont.deriveFont(Font.PLAIN, 12f));
        g.drawString("Clear Selection", clearButton.x + 2, clearButton.y + 14);

        // Mode buttons
        modeButton = new Rectangle(12, this.getHeight() - 40, 50, 20);
        g.setColor(new Color(255, 255, 255, 50));
        g.fillRect(modeButton.x - 2, modeButton.y - 2, modeButton.width + 5, modeButton.height + 5);
        g.setColor(Color.WHITE);
        g.fill(modeButton);
        g.setColor(Color.BLACK);
        g.setFont(Thoth.customFont.deriveFont(Font.PLAIN, 13f));
        String txt = mode == 0 ? "Month" : mode == 1 ? "Quarter" : "Year";
        g.drawString(txt, modeButton.x + (mode == 0 ? 8 : mode == 1 ? 6 : 13), modeButton.y + 14);
	}
}
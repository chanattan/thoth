import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class InvestorPanel extends JPanel {
	
	@Override
	public void paint(Graphics g2) {
		super.paint(g2);
		Graphics2D g = (Graphics2D) g2;
		g.drawString("TEST", 500, 500);
	}
	
	
}

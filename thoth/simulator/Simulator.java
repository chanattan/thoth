package thoth.simulator;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JPanel;
import thoth.logic.Curve;
import thoth.logic.Fund;

@SuppressWarnings("serial")
public class Simulator extends JPanel {

	private ArrayList<Fund> data;
	private Thoth thoth;

	public Simulator(Thoth thoth) {
        setBackground(Color.BLACK);
		this.thoth = thoth;
	}
	
	/*
		Fills in the data about curves for a set of given funds names. 
	*/
	public void fillData(ArrayList<Fund> data) {
		this.data = data;
	}

	/*
		Defines the panel's content, i.e., drawing the curves and any UI-related content for the main screen.
	*/
	@Override
	public void paintComponent(Graphics g2) {
		super.paintComponent(g2);
		Graphics2D g = (Graphics2D) g2;

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(2));
		g.setColor(Color.WHITE);
		g.translate(100, 500);
		g.drawString("TEXT", 500, 500);

		// For each fund, display its associated curve in a different color.
        Color[] colors = {Color.BLUE, Color.RED, Color.GREEN, Color.ORANGE, Color.MAGENTA, Color.CYAN}; // Max 6 curves.
        int colorIndex = 0;
		int xoffset = 60;
		int yoffset = 100;

        for (Fund fund : data) {
            String name = fund.getName();
            Curve curve = fund.getCurve();
			float effect = 0; //this.thoth.getEffect(name);

            g.setColor(colors[colorIndex % colors.length]);

			int offset = xoffset;
            for (int i = 0; i < curve.getSteps() - 1; i++) {
				int x1 = (i+1) * offset;
				int x2 = (i+2) * offset;
				int y1 = curve.getValue(i, effect) + yoffset;
				int y2 = curve.getValue(i+1, effect) + yoffset;
                g.drawLine(x1, y1, x2, y2);
				g.fillOval(x1 - 3, y1 - 3, 3, 6);
            }

			int xlast = curve.getSteps() - 1;
			int ylast = curve.getValue(xlast, effect);

			g.fillOval(xlast - 4, ylast - 3, 6, 6);
            // Label the curve near its last point
			g.drawString(name, xlast + 5, ylast - 5);

            colorIndex++;
        }
	}
}

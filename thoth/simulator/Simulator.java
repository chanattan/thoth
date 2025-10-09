package thoth.simulator;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import java.awt.geom.Line2D;

import javax.swing.JPanel;
import javax.swing.Timer;
import thoth.logic.Curve;
import thoth.logic.Fund;

@SuppressWarnings("serial")
public class Simulator extends JPanel {

	private ArrayList<Fund> data;
	private Thoth thoth;

	public Simulator(Thoth thoth) {
        setBackground(Color.BLACK);
		this.thoth = thoth;
		// Timer for global animation that updates every 16ms (~60 FPS)
        new Timer(16, e -> {
            boolean repaint = update();
			if (repaint) {
            	repaint();
			}
        }).start();
	}

	/*
		This method updates the global state of the panel, mostly pertaining to animations which will get repainted in paintComponent().
	*/

	// Drawings
	private int dx = 0;
	private float hue = 0;
	private boolean update() {
		dx += 10;
		if (dx > this.getWidth())
			dx = 0;
        hue += 0.005f;
        if (hue > 1f) hue -= 1f;  // wrap around after full cycle
		return true;
	}

	/*
		Fills in the data about curves for a set of given funds names. 
	*/
	public void fillData(ArrayList<Fund> data) {
		this.data = data;
	}

	/*
		Drawing methods.
	*/
	private void setOptions(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(2));
		g.setFont(new Font("Monospaced", Font.PLAIN, 10)); 
	}

	/*
		Defines the panel's content, i.e., drawing the curves and any UI-related content for the main screen.
	*/
	@Override
	public void paintComponent(Graphics g2) {
		super.paintComponent(g2);
		Graphics2D g = (Graphics2D) g2;
        this.setOptions(g);

        int h = getHeight();
		Paint oldPaint = g.getPaint();
		GradientPaint bg = new GradientPaint(0, 0, new Color(10, 15, 22),
                                             0, h, new Color(18, 24, 32));
											 
		g.setPaint(bg);
		g.fillRect(0, 0, this.getWidth(), h);
		g.setPaint(oldPaint);

		AffineTransform lastTransform = (AffineTransform) g.getTransform().clone();

		// ========== Grid
		Shape oldClip = g.getClip();
		g.setClip(140, 50, this.getWidth() - 180, this.getHeight() - 80);
		this.drawGrid(g);
		g.setClip(oldClip);

		// ========== Main Frame (curves)
		g.translate(100, 500);
		this.drawMainFrame(g);

		g.setTransform(lastTransform);

		this.drawMeta(g);

		// ========== Header
		g.setTransform(lastTransform);
		g.setStroke(new BasicStroke(2));

		this.drawHeader(g);
	}

	private void drawGrid(Graphics2D g2) {
		int w = getWidth();
        int h = getHeight();
		int minorStep = 20;
		int majorStep = 80;
		Color minorColor = new Color(255, 255, 255, 20);
        Color majorColor = new Color(255, 255, 255, 45);
		g2.setStroke(new BasicStroke(1f));
        g2.setColor(minorColor);
        // Alignement à 0.5 pour des lignes nettes 1px
        for (int x = 0; x <= w; x += minorStep) {
            double xx = x + 0.5;
            g2.draw(new Line2D.Double(xx, 0, xx, h));
        }
        for (int y = 0; y <= h; y += minorStep) {
            double yy = y + 0.5;
            g2.draw(new Line2D.Double(0, yy, w, yy));
        }

        // lignes majeures
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(majorColor);
        for (int x = 0; x <= w; x += majorStep) {
            double xx = x + 0.5;
            g2.draw(new Line2D.Double(xx, 0, xx, h));
        }
        for (int y = 0; y <= h; y += majorStep) {
            double yy = y + 0.5;
            g2.draw(new Line2D.Double(0, yy, w, yy));
        }

		Color axisColor = new Color(255, 255, 255, 160);

        // Dessiner axes 
        g2.setColor(axisColor);
        g2.setStroke(new BasicStroke(1.5f));
        // Axe X en bas
        g2.draw(new Line2D.Double(0.5, h - 0.5, w - 0.5, h - 0.5));
        // Axe Y à gauche
        g2.draw(new Line2D.Double(0.5, 0.5, 0.5, h - 0.5));
	}

	private void drawHeader(Graphics2D g) {
		// Bar
		g.setColor(Window.THEME_COLOR);
		g.fillRect(0, 0, this.getWidth(), 26);

		// Animation line
        Color color = Color.getHSBColor(hue, 1.0f, 1.0f);
		g.setColor(color);
		int lineWidth = 100;
		g.drawLine(dx, 26, Math.min(lineWidth + dx, this.getWidth()), 26);
		g.setColor(Color.LIGHT_GRAY);

		// Information
		g.drawString("Thoth AI does not predict the future and can make mistakes. Check here for more info or the [?] button below.", 10, 18);
	}

	public static Color colorFromIndex(int index) {
        index = Math.max(0, index - 1);

        float hue = (index * 0.15f) % 1.0f;
        float saturation = 0.9f;
        float brightness = 1f;

        return Color.getHSBColor(hue, saturation, brightness);
    }

	private void drawMeta(Graphics2D g) {
		/*
			Draw meta information on the left.
		*/

		// Bar
		g.setColor(Color.BLACK);
		g.fillRect(0, 26, 120, this.getHeight());

		// Information relative to the n displayed funds
		int maxDisplayedFunds = 4;
		int yOffset = 20;
		for (int i = 0; i < maxDisplayedFunds; i++) {
			Fund f = this.thoth.funds.get(i);
			String fundName = f.getName();

			Color c = Simulator.colorFromIndex(i + 10);
			g.setColor(c);
			g.drawString(fundName, 20, 20 + yOffset * i);
			yOffset += 20;
		}
	}

	private void drawMainFrame(Graphics2D g) {
		// For each fund, display its associated curve in a different color.
        Color[] colors = {Color.BLUE, Color.RED, Color.GREEN, Color.ORANGE, Color.MAGENTA, Color.CYAN}; // Max 6 curves.
        int colorIndex = 0;
		int xoffset = 60;
		int yoffset = 100;

        for (Fund fund : data) {
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

            colorIndex++;
        }
	}
}

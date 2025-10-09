package thoth.simulator;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import javax.swing.JPanel;
import javax.swing.Timer;
import thoth.logic.Curve;
import thoth.logic.Fund;
import thoth.logic.Action;

@SuppressWarnings("serial")
public class Simulator extends JPanel {

	private ArrayList<Fund> data;
	private Thoth thoth;
    private final java.util.List<Object[]> points = new ArrayList<Object[]>(); // Invest actions
    private Point2D.Double click = null;
	private AffineTransform currTransform = null;
	private Point2D worldPt;

	// Mouse dragging
    private double offsetX = 0;
    private double offsetY = 0;
    private double scale = 1.0;
    private Point lastDragPoint = null;
	
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

		int offset = 60;
		int yoffset = 100;
		float effect = 0; //this.thoth.getEffect(name);
		for (Fund f : this.thoth.funds) {
			Curve curve = f.getCurve();
			for (int i = 0; i < curve.getSteps(); i++) {
				int x1 = (i+1) * offset;
				int y1 = curve.getValue(i, effect) + yoffset;
				Point2D.Double point = new Point2D.Double(x1, y1);
				points.add(new Object[] {point, new Action(i, -curve.getValue(i, effect), f)});
			}
		}

		MouseAdapter ma = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (currTransform == null) {
					return;
				}

				try {
					AffineTransform completeTransform = new AffineTransform();
					completeTransform.translate(offsetX, offsetY);
					completeTransform.scale(scale, scale);
					completeTransform.translate(100, 500);
					
					AffineTransform screenToWorld = completeTransform.createInverse();
					worldPt = screenToWorld.transform(e.getPoint(), null);

					double threshold = 20 / scale; 
					click = null;
					for (Object[] o : points) {
						Point2D.Double p = (Point2D.Double) o[0];
						Action a = (Action) o[1];
						double dist = worldPt.distance(p);
						if (dist < threshold) {
							click = p;
							System.out.println("Investing " + a.getValue() + " in Fund " + a.getFund().getName());
							break;
						}
					}
				} catch (NoninvertibleTransformException ex) {
					ex.printStackTrace();
				}
				repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
				lastDragPoint = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastDragPoint != null) {
                    int dx = e.getX() - lastDragPoint.x;
                    int dy = e.getY() - lastDragPoint.y;
                    offsetX += dx;
                    offsetY += dy;
                    lastDragPoint = e.getPoint();
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                lastDragPoint = null;
            }

			@Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double zoomFactor = 1.1;
                int notches = e.getWheelRotation();
                if (notches < 0)
                    scale *= zoomFactor;
                else
                    scale /= zoomFactor;
                repaint();
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
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

		AffineTransform lastTransform = (AffineTransform) g.getTransform().clone();

        int h = getHeight();
		Paint oldPaint = g.getPaint();
		GradientPaint bg = new GradientPaint(0, 0, new Color(10, 15, 22),
                                             0, h, new Color(18, 24, 32));
											 
		g.setPaint(bg);
		g.fillRect(0, 0, this.getWidth(), h);
		g.setPaint(oldPaint);

		// Apply effects
        g.translate(offsetX, offsetY);
		g.scale(scale, scale);

		// ========== Grid
		// It should be y-invert to be cleaner.
		g.translate(120, -30);
		this.drawGrid(g);
		g.translate(-120, 30);

		// ========== Main Frame (curves)
		g.translate(100, 500);
		this.drawMainFrame(g);
		g.translate(-100, -500);

		g.setTransform(lastTransform);

		this.drawMeta(g);

		// ========== Header
        this.setOptions(g);
		g.setTransform(lastTransform);
		g.setStroke(new BasicStroke(2));
		
		g.translate(offsetX, offsetY);
		g.scale(scale, scale);
		this.currTransform = (AffineTransform) g.getTransform().clone();
		g.setTransform(lastTransform);

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
		g.setFont(new Font("Monospaced", Font.PLAIN, 13)); 

		// Information relative to the n displayed funds
		int maxDisplayedFunds = 8;
		int yOffset = 30;
		for (int i = 0; i < this.thoth.funds.size(); i++) {
			if (i >= maxDisplayedFunds - 1)
				break;
			Fund f = this.thoth.funds.get(i);
			String fundName = f.getName();

			Color c = Simulator.colorFromIndex(i + 10);
			g.setColor(c);
			g.drawString(fundName, 20, 50 + yOffset * i);
		}
	}

	private void drawMainFrame(Graphics2D g) {
		// For each fund, display its associated curve in a different color.
        int colorIndex = 0;
		int xoffset = 60;
		int yoffset = 100;

		// Draw worldPt in the correct coordinate space
		if (click != null) {
			g.setColor(Color.ORANGE);
			g.drawOval((int) click.getX() - 8, (int) click.getY() - 8, 16, 16);
		}

        for (int y = 0; y < this.thoth.funds.size(); y++) {
			Fund fund = this.thoth.funds.get(y);
            Curve curve = fund.getCurve();
			float effect = 0; //this.thoth.getEffect(name);

			Color c = Simulator.colorFromIndex(y + 10);
            g.setColor(c);

			int offset = xoffset;
            for (int i = 0; i < curve.getSteps() - 1; i++) {
				int x1 = (i+1) * offset;
				int x2 = (i+2) * offset;
				int y1 = curve.getValue(i, effect) + yoffset;
				int y2 = curve.getValue(i+1, effect) + yoffset;
                g.drawLine(x1, y1, x2, y2);
				g.fillOval(x1 - 3, y1 - 3, 6, 6);
            }

			int xlast = curve.getSteps() - 1;
			int ylast = curve.getValue(xlast, effect) + yoffset;
			g.fillOval((xlast + 1) * offset - 3, ylast - 3, 6, 6);

            colorIndex++;
        }
	}
}
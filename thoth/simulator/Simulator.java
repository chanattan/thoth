package thoth.simulator;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.Popup;
import javax.swing.Timer;
import javax.swing.border.Border;
import thoth.logic.Curve;
import thoth.logic.Fund;
import thoth.logic.Action;
import thoth.logic.Player;
import thoth.ui.HaloLabel;

@SuppressWarnings("serial")
public class Simulator extends JPanel {

	private ArrayList<Fund> funds;
	private Thoth thoth;
    private final java.util.List<Object[]> points = new ArrayList<Object[]>(); // Invest actions
    private Point2D.Double click = null;
	private AffineTransform currTransform = null;
	private Point2D worldPt;
	private final Timer time;
	private int currentTimeStep = 0; // mois

	// Mouse dragging
    private double offsetX = 0;
    private double offsetY = 0;
    private double scale = 1.0;
	// smooth dragging
	private double targetOffsetX = 0;
	private double targetOffsetY = 0;

	// Lerp factor
	private static final double LERP = 0.25;
	private double zoomAnchorX = 0; // world coordinates under cursor
	private double zoomAnchorY = 0;
	private boolean zooming = false;
	private double targetScale = 1.0; // zoom
	private double velocityScale = 0.0;
	private static final double SMOOTHNESS = 0.25;

	// clamp zoom limits
	private static final double MIN_SCALE = 0.1;
	private static final double MAX_SCALE = 10.0;

    private Point lastDragPoint = null;
	public Popup popup = null; // helper AI

	// Investing
	public Fund selectedFund = null;

	// Display
	public HaloLabel thothButton;
	
	public Simulator(Thoth thoth) {
        setBackground(Color.BLACK);
		this.thoth = thoth;
		// Timer for global animation that updates every 16ms.
        new Timer(16, e -> {
            boolean repaint = update();
			if (repaint) {
            	repaint();
			}
        }).start();

		this.funds = thoth.funds;

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

					double threshold = 10 / scale; 
					click = null;
					for (Object[] o : points) {
						Point2D.Double p = (Point2D.Double) o[0];
						Fund f = (Fund) o[1];
						double dist = worldPt.distance(p);
						if (dist < threshold) {
							click = p;
							System.out.println("Considering Fund " + f.getName());
							selectedFund = f;
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

					targetOffsetX += dx;
					targetOffsetY += dy;

					lastDragPoint = e.getPoint();
				}
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                lastDragPoint = null;
            }

			@Override
            public void mouseWheelMoved(MouseWheelEvent e) {
				double rotation = e.getPreciseWheelRotation();

				Point p = e.getPoint();

				double mouseWorldX = (p.x - offsetX) / scale;
				double mouseWorldY = (p.y - offsetY) / scale;

				zoomAnchorX = mouseWorldX;
				zoomAnchorY = mouseWorldY;
				zooming = true;

				double zoomFactor = 0.015;
				velocityScale += -rotation * zoomFactor;

				targetScale = scale * (1.0 + velocityScale);
				targetScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, targetScale));

				targetOffsetX = p.x - mouseWorldX * targetScale;
				targetOffsetY = p.y - mouseWorldY * targetScale;
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
		addMouseWheelListener(ma);

		setFocusable(true);
		requestFocusInWindow();

		setLayout(new BorderLayout());
		// Thoth button
		createThothButton();

		// Global timer
		this.time = new Timer(3000, updateGlobal());
		this.time.start();
	}

	public final void createThothButton() {
		javax.swing.ImageIcon originalIcon = new javax.swing.ImageIcon(
			getClass().getResource("../../assets/thoth.png")
		);
		java.awt.Image scaledImage = originalIcon.getImage().getScaledInstance(originalIcon.getIconWidth() / 9, 
		originalIcon.getIconHeight() / 9, java.awt.Image.SCALE_SMOOTH);

		thothButton = new HaloLabel(new ImageIcon(scaledImage), 0.2f, 10f, new Color(100, 149, 237, 30), -10);
		thothButton.setToolTipText("Click to get Thoth AI predictions.");

		JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
		bottomBar.setOpaque(false);
		bottomBar.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 20, 0));
		bottomBar.add(thothButton);
		add(bottomBar, BorderLayout.SOUTH);

		thothButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.CROSSHAIR_CURSOR));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				setCursor(java.awt.Cursor.getDefaultCursor());
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// AI popup
				Point mousePos = e.getPoint();
				if (mousePos != null) {
					if (popup != null) {
						popup.hide();
						popup = null;
					} else {
						popup = thoth.AI.popInfo(Simulator.this, mousePos.x, mousePos.y);
						thothButton.toggleAnimation(false);
						popup.show();
					}
				}
			}
		});
	}

	/**
	 * Global timer update method.
	 */
	private ActionListener updateGlobal() {
		return (ActionEvent e) -> {
                    updateSimulator();
                    thoth.window.investorPanel.updateInventoryPanel();
                };
	}
	private void updateSimulator() {
		// Temps global.
		currentTimeStep += 1;

		// Génération des nouvelles valeurs.
		for (Fund f : funds) {
			// Mettre à jour news
			thoth.seekNews(f.getName());
			// Mettre à jour valeurs
			Curve c = f.getCurve();
			float effect = thoth.useEffect(f.getName()); // TODO: the effect is permanent. make it on a period, for now only once.
			int nextVal = (int) c.nextValue(effect);
			c.storeValue(nextVal);

			// Plus-value des actions pour l'utilisateur : màj.
			// TODO.
		}

		// Màj panels
		thoth.window.investorPanel.updatePanel(thoth);
		thoth.window.newsPanel.updatePanel();
		thoth.window.fundsPanel.updatePanel();

		// Mettre à jour les points pour clicks
		int offset = 20;
		int yoffset = -160;
		for (Fund f : funds) {
			Curve curve = f.getCurve();
			int[] values = curve.getLastValues(0);
			for (int l = 0; l < curve.getSteps(); l++) {
				int x1 = (l+1) * offset;
				int val = (int) values[l];
				int y1 = val + yoffset;
				Point2D.Double point = new Point2D.Double(x1, -y1);
				points.add(new Object[] {point, f});
			}
		}
	}

	public int getTime() {
		// Returns the current time step of the simulation based on the timer.
		// Assuming each tick is a month.
		return this.currentTimeStep;
	}

	/*
		This method updates the global state of the panel, mostly pertaining to animations which will get repainted in paintComponent().
	*/

	// Drawings
	private int dx = 0;
	private float hue = 0;
	private boolean update() {
		dx += 10;
		if (dx > this.getWidth()) dx = 0;

		hue += 0.005f;
		if (hue > 1f) hue -= 1f;

		// Smooth lerp for offsets
		offsetX += (targetOffsetX - offsetX) * LERP;
		offsetY += (targetOffsetY - offsetY) * LERP;

		// Smooth scale
		if (zooming && Math.abs(velocityScale) > 0.00001) {
			double newScale = scale * (1.0 + velocityScale);
			newScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, newScale));

			// Adjust target offsets so zoomAnchor stays under cursor
			targetOffsetX = targetOffsetX + (zoomAnchorX * (scale - newScale));
			targetOffsetY = targetOffsetY + (zoomAnchorY * (scale - newScale));

			scale = newScale;

			// Decay velocity for smooth stop
			velocityScale *= (1.0 - SMOOTHNESS);
		} else {
			zooming = false;
		}
		return true;
	}

	/*
		Fills in the data about curves for a set of given funds names. 
	*/
	public void fillData(ArrayList<Fund> data) {
		this.funds = data;
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
		int w = getWidth() * 4;
        int h = getHeight() * 4;
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
		// Animation line
        Color color = Color.getHSBColor(hue, 1.0f, 1.0f);
		g.setColor(color);
		int lineWidth = 100;
		g.drawLine(dx, this.getHeight() - 26, Math.min(lineWidth + dx, this.getWidth()), this.getHeight() - 26);
		g.setColor(Color.LIGHT_GRAY);

		// Thoth bar
		g.setColor(Window.THEME_COLOR);
		g.fillRect(0, this.getHeight() - 26, this.getWidth(), 26);

		// Information
		g.setColor(Color.LIGHT_GRAY);
		g.drawString("Thoth AI does not predict the future and can make mistakes. Click the button [?] for more information.", 10, this.getHeight() - 10);
	}

	public static Color colorFromIndex(int index) {
        index = Math.max(0, index - 1);

        float hue = (index * 0.15f) % 1.0f;
        float saturation = 0.9f;
        float brightness = 1f;

        return Color.getHSBColor(hue, saturation, brightness);
    }

	private void drawMainFrame(Graphics2D g) {
		// For each fund, display its associated curve in a different color.
        int colorIndex = 0;
		int xoffset = 20;
		int yoffset = -160;

		// Draw worldPt in the correct coordinate space
		if (click != null) {
			g.setColor(Color.ORANGE);
			int x = (int) click.getX();
			int y = (int) click.getY();
			g.drawOval(x - 8, y - 8, 16, 16);
			// Draw a vertical line to the time axis
			Stroke oldStroke = g.getStroke();
			Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                                  0, new float[]{9}, 0);
  			g.setStroke(dashed);
			g.drawLine(x, y, x, 160);
			g.setStroke(oldStroke);
		}

		// Draw curves
		for (int y = 0; y < this.funds.size(); y++) {
			Fund fund = this.funds.get(y);
            Curve curve = fund.getCurve();
			float effect = 0; //this.thoth.getEffect(name);

			Color c = Simulator.colorFromIndex(y + 10);
            g.setColor(c);

			int offset = xoffset;
			int[] values = curve.getLastValues(0); // Get all values for now.
            for (int i = 0; i < values.length - 1; i++) {
				int x1 = (i+1) * offset;
				int x2 = (i+2) * offset;
				int y1 = (int) values[i] + yoffset;
				int y2 = (int) values[i+1] + yoffset;
                g.drawLine(x1, -y1, x2, -y2);
				g.fillOval(x1 - 3, -y1 - 3, 6, 6);
            }

			if (values.length > 0) {
				int xlast = curve.getSteps() - 1;
				int ylast = (int) values[values.length - 1] + yoffset;
				g.fillOval((xlast + 1) * offset - 3, -ylast - 3, 6, -6);
			}

            colorIndex++;
        }
	}
}
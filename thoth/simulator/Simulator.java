package thoth.simulator;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JToolTip;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import thoth.logic.AI.Prediction;
import thoth.logic.Curve;
import thoth.logic.Fund;
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
	private int currentTimeStep = 10; // mois
	private int timeStepSpeed = 3000; // 3 seconds 
	private Popup tipPopup = null;

	// Mouse dragging
    private double offsetX = 0;
    private double offsetY = 0;
    private double scale = 1.0;
	// smooth dragging
	private double targetOffsetX = 0;
	private double targetOffsetY = 0;

	// Lerp factor
	private static final double LERP = 0.2;
	private double zoomAnchorX = 0; // world coordinates under cursor
	private double zoomAnchorY = 0;
	private boolean zooming = false;
	private double targetScale = 1.0; // zoom
	private double velocityScale = 0.0;
	private static final double SMOOTHNESS = 0.1;

	// clamp zoom limits
	private static final double MIN_SCALE = 0.1;
	private static final double MAX_SCALE = 10.0;

    private Point lastDragPoint = null;
	public Popup popup = null; // helper AI
	private Popup disclaimerFrame = null;

	// Investing
	public Fund selectedFund = null;

	// Display
	public HaloLabel thothButton;
	private Rectangle disclaimerButton;

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
				// disclaimer button
				Point mousePos = e.getPoint();
				if (disclaimerButton.contains(mousePos)) {
					if (disclaimerOverlay != null) {
						Simulator.this.toggleDisclaimer(false);
					} else {
						Simulator.this.toggleDisclaimer(true);
					}
				}

				// tip popup
				if (tipPopup != null) {
					tipPopup.hide();
					tipPopup = null;
				}
				
				// curve focus
                if (currTransform == null) {
					return;
				}

				try {
					AffineTransform completeTransform = new AffineTransform();
					completeTransform.translate(offsetX, offsetY);
					completeTransform.scale(scale, scale);
					completeTransform.translate(40, getHeight());
					
					AffineTransform screenToWorld = completeTransform.createInverse();
					worldPt = screenToWorld.transform(e.getPoint(), null);

					double threshold = 15 / scale; 
					click = null;
					for (Object[] o : points) {
						Point2D.Double p = (Point2D.Double) o[0];
						Fund f = (Fund) o[1];
						double dist = worldPt.distance(p);
						if (dist < threshold) {
							click = p;
							System.out.println("Considering Fund " + f.getName());
							// Popup for info fund
							// Retrieve time step
							int timeStep = (int) (p.x / 20) - 1; // since x = (i+1)*20
							Object[] dateInfo = Thoth.getDateStatic(timeStep);
							String month = (String) dateInfo[0];
							int year = (int) dateInfo[1];
							String tooltipText = "<html><center><span style='color:black'><b>" + f.getName() + "</b></span><br>" +
								"<span style='color:gray'>[" + month + "/" + year + "]</center></span><br>" +
								"Fund Value: " + f.getCurve().getLastValues(timeStep)[0] + "<br>" +
								"(Click for more details)</html>";

							JToolTip tooltip = createToolTip();
							tooltip.setTipText(tooltipText);
							tooltip.setBackground(new Color(50, 50, 50, 230));
							tooltip.setForeground(Color.WHITE);
							tooltip.setBorder(javax.swing.BorderFactory.createLineBorder(Color.GRAY));

							javax.swing.PopupFactory factory = javax.swing.PopupFactory.getSharedInstance();
							tipPopup = factory.getPopup(Simulator.this, tooltip, e.getXOnScreen() + 10, e.getYOnScreen() - 30);
							tipPopup.show();
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
				if (tipPopup != null) {
					tipPopup.hide();
					tipPopup = null;
				}
				
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

				double zoomFactorPerNotch = 1.15; // >1 for zoom in
				double factor = Math.pow(zoomFactorPerNotch, -rotation);

				targetScale = scale * factor;
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
		this.time = new Timer(timeStepSpeed, updateGlobal());
		this.time.setInitialDelay(0);
		this.time.start();
	}

	public final void createThothButton() {
		javax.swing.ImageIcon originalIcon = new javax.swing.ImageIcon(
			getClass().getResource("../../assets/thoth.png")
		);
		java.awt.Image scaledImage = originalIcon.getImage().getScaledInstance(originalIcon.getIconWidth() / 9, 
		originalIcon.getIconHeight() / 9, java.awt.Image.SCALE_SMOOTH);

		thothButton = new HaloLabel(new ImageIcon(scaledImage), 0.2f, 10f, new Color(255, 165, 0, 30), -10);
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
						removeThoth();
					} else {
						popupThoth();
					}
				}
			}
		});
	}

	/**
	 * Triggers Thoth.
	 */
	public void popupThoth() {
		if (popup != null) return;
		thoth.logger.startMeasure("human_read_thoth");
		popup = thoth.AI.popInfo(Simulator.this);
		//thothButton.toggleAnimation(false);
		popup.show();
	}

	public void removeThoth() {
		if (popup != null) {
			thoth.logger.stopMeasure("human_read_thoth");
			popup.hide();
			popup = null;
		}
	}

	private JPanel disclaimerOverlay;
	private void toggleDisclaimer(boolean show) {
		if (!show) {
			if (disclaimerOverlay != null) {
				remove(disclaimerOverlay);
				disclaimerOverlay = null;
				repaint();
			}
			return;
		}

		String disclaimerText =
			"<html><div style='width:360px;text-align:left;'>"
			+ "<table width='100%' cellpadding='0' cellspacing='0'><tr>"
			+ "<td><b>Disclaimer</b></td>"
			+ "<td align='right'><font color='gray'><i>[Click anywhere to close]</i></font></td>"
			+ "</tr></table><br>"
			+ "Thoth AI is a simulation tool designed to provide predictions "
			+ "based on historical data and trends.<br><br>"
			+ "It does not guarantee future results and may make incorrect predictions.<br><br>"
			+ "Do not rely solely on Thoth AI for investment decisions. "
			+ "Consult a qualified financial advisor when necessary."
			+ "</div></html>";

		JLabel text = new JLabel(disclaimerText);
		text.setForeground(Color.LIGHT_GRAY);
		text.setFont(Thoth.customFont.deriveFont(12f));

		JPanel content = new JPanel(new BorderLayout());
		content.setOpaque(false);
		content.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
		content.add(text, BorderLayout.CENTER);

		disclaimerOverlay = new JPanel(new BorderLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
									RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(new Color(30, 30, 30, 220));
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
				super.paintComponent(g);
			}
		};
		disclaimerOverlay.setOpaque(false);
		disclaimerOverlay.add(content, BorderLayout.CENTER);

		disclaimerOverlay.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				toggleDisclaimer(false);
			}
		});

		// Layout & positioning
		setLayout(null);

		Dimension pref = disclaimerOverlay.getPreferredSize();
		int width = pref.width;
		int height = pref.height;

		int x = (getWidth() - width) - 10;
		int y = getHeight() - height - 40;

		disclaimerOverlay.setBounds(x, y, width, height);
		add(disclaimerOverlay);
		setComponentZOrder(disclaimerOverlay, 0);
		revalidate();
		repaint();
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
		points.clear(); // juste ajouter les derniers...
		int offset = 20;
		int yoffset = 0;
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
		// Thoth notification
		Prediction nextPrediction = thoth.AI.predictNextMove();
		if (nextPrediction.fund == null || nextPrediction.getConfidenceLevel() <= 2) {
			//popup.hide();
			//popup = null;
			thothButton.toggleAnimation(false);
			//System.out.println("no Prediction");
			repaint();
		} else if (nextPrediction.fund != null) {
			//System.out.println("new Prediction");
			thothButton.toggleAnimation(true);
			repaint();
		}
		// Animation line
		dx += 10;
		if (dx > this.getWidth()) dx = 0;

		hue += 0.005f;
		if (hue > 1f) hue -= 1f;

		// Smooth lerp for offsets
		offsetX += (targetOffsetX - offsetX) * LERP;
		offsetY += (targetOffsetY - offsetY) * LERP;
		scale += (targetScale  - scale) * LERP;

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
		g.translate(0, -getHeight()*3);
		this.drawGrid(g);
		g.translate(0, getHeight()*3);

		// ========== Main Frame (curves)
		g.translate(40, getHeight());
		this.drawMainFrame(g);
		g.translate(-40, -getHeight());

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
		int w = getWidth() * 10;
		int h = getHeight() * 4;
		int minorStep = 20;
		int majorStep = 100;
		Color minorColor = new Color(255, 255, 255, 20);
		Color majorColor = new Color(255, 255, 255, 45);
		
		g2.setStroke(new BasicStroke(1f));
		g2.setColor(minorColor);

		int hoffset = 10;
		int axisX = 60;
		int axisY = 98;
		
		// Lignes verticales mineures
		for (int x = minorStep; x <= w + minorStep; x += minorStep) {
			if (x == minorStep) continue;
			g2.draw(new Line2D.Double(-axisX + x, 0, -axisX + x, h + axisY));
		}
		
		// Lignes horizontales mineures
		for (int y = minorStep; y <= h; y += minorStep) {
			g2.draw(new Line2D.Double(-axisX + minorStep, y - hoffset + axisY, (-axisX) + w + minorStep, y - hoffset + axisY));
		}

		// Lignes majeures
		g2.setStroke(new BasicStroke(1f));
		g2.setColor(majorColor);
		
		for (int x = majorStep; x <= w; x += majorStep) {
			g2.draw(new Line2D.Double(-axisX + x + minorStep, 0, -axisX + x + minorStep, h + axisY));
		}
		
		for (int y = majorStep; y <= h; y += majorStep) {
			g2.draw(new Line2D.Double(-axisX + minorStep, y - hoffset + axisY, (-axisX) + w + minorStep, y - hoffset + axisY));
		}

		Color axisColor = new Color(255, 255, 255, 160);
		Color labelColor = new Color(200, 200, 200, 200);

		// main axes
		g2.setColor(axisColor);
		g2.setStroke(new BasicStroke(2f));
		
		// axes X et Y
		float toffset = 0f;
		g2.draw(new Line2D.Double(axisX, h - toffset, axisX + w, h - toffset));
		AffineTransform at = g2.getTransform();
		
		if (at.getTranslateX() < axisX - 50) {
			//g2.scale(scale, scale);
			g2.translate(-offsetX / scale, 0);
		}
		g2.draw(new Line2D.Double(axisX + toffset, 0, axisX + toffset, h));

		// graduations
		g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
		FontMetrics fm = g2.getFontMetrics();
		
		int valueStep = 100;
		int pixelPerValue = 1;
		
		// Graduations y
		for (int value = 0; value <= 10000; value += valueStep) {
			int yPos = h - (value * pixelPerValue);
			
			if (yPos < 0) break;
			
			g2.setColor(axisColor);
			g2.drawLine(axisX - 5, yPos, axisX + 5, yPos);
			
			g2.setColor(labelColor);
			String label = String.valueOf(value);
			int labelWidth = fm.stringWidth(label);
			g2.drawString(label, axisX - labelWidth - 10, yPos + 4);
		}
		g2.setTransform(at);

		int monthPixelStep = 20;  // à xoffset dans drawMainFrame
		
		for (int month = 0; month <= (int) (w / monthPixelStep); month++) {
			int xPos = axisX + (month * monthPixelStep);
			if (xPos > w) break;

			g2.setColor(axisColor);
			g2.drawLine(xPos, h - 5, xPos, h + 5);
			
			// label tous les 3 mois
			if (month % 3 == 0) {
				g2.setColor(labelColor);
				String label = String.valueOf(month % 12 + 1);
				int labelWidth = fm.stringWidth(label);
				g2.drawString(label, xPos - labelWidth / 2, h + 18);
			}
		}
		
		g2.setColor(labelColor);
		g2.setFont(new Font("Monospaced", Font.BOLD, 11));
		g2.drawString("Valeur", 5, 15);
		g2.drawString("Temps (mois)", w - 120, h - 10);
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

		// [?] button as box
		if (disclaimerButton == null) {
			disclaimerButton = new java.awt.Rectangle(this.getWidth() - 23, this.getHeight() - 23, 20, 20);
		}
		g.setColor(Window.THEME_COLOR.darker());
		g.fill(disclaimerButton);
		g.setColor(Color.LIGHT_GRAY);
		g.drawString("[?]", this.getWidth() - 22, this.getHeight() - 10);
	}

	public static Color colorFromIndex(int index) {
        index = Math.max(0, index - 1);

        float hue = (index * 0.15f) % 1.0f;
        float saturation = 0.9f;
        float brightness = 1f;

        return Color.getHSBColor(hue, saturation, brightness);
    }

	private int mode = 0;
	/**
	 * Sets the fund display mode.
	 * 0 : monthly
	 * 1 : quarterly
	 * 2 : yearly
	 */
	public void setFundDisplay(int mode) {
		if (mode < 0 || mode > 2) {
			mode = 0;
		}
		this.mode = mode;
	}

	private void drawMainFrame(Graphics2D g) {
		// Draw worldPt in the correct coordinate space
		if (click != null) {
			g.setColor(Color.ORANGE);
			int x = (int) click.getX();
			int y = (int) click.getY();
			g.drawOval(x - 8, y - 8, 16, 16);
			// Draw a vertical line to the time axis
			Stroke oldStroke = g.getStroke();
			Stroke dashed = new BasicStroke(0.7f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0);
  			g.setStroke(dashed);
			g.drawLine(x, y, x, 0);
			g.setStroke(oldStroke);
		}

		drawCurves(g);
	}

	private void drawCurves(Graphics2D g) {
		// For each fund, display its associated curve in a different color.
		int xoffset = 20;
		int yoffset = 0;

		for (int y = 0; y < this.funds.size(); y++) {
			Fund fund = this.funds.get(y);

			Color c = Simulator.colorFromIndex(y + 10);

			Fund clickedFund = thoth.window.fundsPanel.getClickedFund();
			if (clickedFund != null && clickedFund != fund)
				c = new Color(c.getRed(), c.getGreen(), c.getBlue(), 90).darker().darker(); // dim other funds
				
            g.setColor(c);
				
            Curve curve = fund.getCurve();
			int offset = xoffset;

			switch (mode) {
				case 0 -> 					{
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
							int xlast = values.length * offset;
							int ylast = (int) values[values.length - 1] + yoffset;
							g.fillOval(xlast - 3, -ylast - 3, 6, 6);
						}
					}
				case 1 -> 					{
						int[] values = curve.getLastValues(0);
						// capture values quaterly
						for (int i = 0; i < values.length - 1; i += 3) {
							int x1 = (i+1) * offset;
							int x2 = (i+4) * offset;
							int y1 = (int) values[i] + yoffset;
							int y2 = (int) values[Math.min(i+3, values.length - 1)] + yoffset;
							g.drawLine(x1, -y1, x2, -y2);
							g.fillOval(x1 - 3, -y1 - 3, 6, 6);
						}
						if (values.length > 0) {
							int lastIndex = ((values.length - 1) / 3) * 3;
							int xlast = (lastIndex + 1) * offset;
							int ylast = (int) values[lastIndex] + yoffset;
							g.fillOval(xlast - 3, -ylast - 3, 6, 6);
						}
					}
				case 2 -> 					{
						int[] values = curve.getLastValues(0);
						// capture values yearly
						for (int i = 0; i < values.length - 1; i += 12) {
							int x1 = (i+1) * offset;
							int x2 = (i+13) * offset;
							int y1 = (int) values[i] + yoffset;
							int y2 = (int) values[Math.min(i+12, values.length - 1)] + yoffset;
							g.drawLine(x1, -y1, x2, -y2);
							g.fillOval(x1 - 3, -y1 - 3, 6, 6);
						}
						if (values.length > 0) {
							int lastIndex = ((values.length - 1) / 12) * 12;
							int xlast = (lastIndex + 1) * offset;
							int ylast = (int) values[lastIndex] + yoffset;
							g.fillOval(xlast - 3, -ylast - 3, 6, 6);
						}
					}
				default -> {}
			}
        }
	}
}
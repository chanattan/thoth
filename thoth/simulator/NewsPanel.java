
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

public class NewsPanel extends JPanel {
    private Thoth thoth;
    private JLabel newsLabel;
    private JTextArea newsArea;
    public JSplitPane parentPane;

    public NewsPanel(Thoth thoth) {
        this.thoth = thoth;
        this.setLayout(new BorderLayout());

        this.createComponents();
        this.setBackground(Color.BLACK);
    }

    private void createComponents() {
        // Icon

        javax.swing.ImageIcon originalIcon = new javax.swing.ImageIcon("assets/news_logo2.png");
        java.awt.Image scaledImage = originalIcon.getImage().getScaledInstance(
            originalIcon.getIconWidth() / 7, 
            originalIcon.getIconHeight() / 7, 
            java.awt.Image.SCALE_SMOOTH
        );
        newsLabel = new JLabel(new javax.swing.ImageIcon(scaledImage));
        //newsLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        //newsLabel.setForeground(Color.WHITE);
        newsLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 0, 0, 0));
        newsArea = new JTextArea();
        newsArea.setEditable(false);
        newsLabel.setHorizontalAlignment(JLabel.RIGHT);
        add(newsLabel, BorderLayout.NORTH);

        newsArea.setBackground(Color.BLACK);
        newsArea.setForeground(Color.WHITE);
    }

    public void updateNews(String news) {
        newsArea.setText(news);
    }

    public void updatePanel() {
        repaint();
    }

	@Override
	public void paintComponent(Graphics g2) {
		super.paintComponent(g2);
		Graphics2D g = (Graphics2D) g2;
        this.setOptions(g);

        drawHeader(g);
        drawNews(g);
    }

    private void drawHeader(Graphics2D g) {
        String header = "NEWS";
        FontMetrics fm = g.getFontMetrics();
        int x = (this.getWidth() - fm.stringWidth(header)) / 2;
        int y = 30;
        // background for title
        g.setColor(new Color(50, 50, 50));
        int padding = 10;
        g.fillRoundRect(x - padding, y - fm.getAscent() - 7, fm.stringWidth(header) + 3 * padding + 5, fm.getHeight() + 10, 15, 15);

        g.setColor(Color.WHITE);
        g.setFont(Thoth.customFont.deriveFont(Font.PLAIN, 20f));
        g.drawString(header, x, y);

        Object[] dateInfo = thoth.getDate();
        String month = (String) dateInfo[0];
        int year = (int) dateInfo[1];

        // Background rectangle for date
        String dateStr = "Date: " + month + "/" + year;
        g.setFont(Thoth.customFont.deriveFont(Font.PLAIN, 16f));
        FontMetrics dateFm = g.getFontMetrics();
        int datePadding = 6;
        int dateXPos = 10;
        int dateYPos = 20 - dateFm.getAscent();
        g.setColor(new Color(50, 50, 50));
        g.fillRoundRect(dateXPos - datePadding, dateYPos - datePadding - 4, dateFm.stringWidth(dateStr) + 2 * datePadding, dateFm.getHeight() + 2 * datePadding, 10, 10);
        int dateX = 10;
        int dateY = 20;
        g.setColor(Color.WHITE);
        g.drawString(dateStr, dateX, dateY);
    }

    private void setOptions(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(2));
		g.setFont(new Font("Monospaced", Font.PLAIN, 13)); 
	}

	private void drawNews(Graphics2D g) {
		List<News> pool = this.thoth.news;
		for (int i = 0; i < pool.size(); i++) {
			News n = pool.get(i);
			g.setColor(Color.WHITE);
			int x = 18;
			int y = 50 + (i + 1) * 27; // espacement vertical

			FontMetrics fm = g.getFontMetrics();
			String txt = n.getTitle() + " (" + n.getInitialEffect() + "%)";
			int textHeight = fm.getHeight() + 3;

			g.setColor(Color.WHITE);
            int panelWidth = getWidth() - 40;
            g.fillRect(x, y - fm.getAscent(), Math.max(400, panelWidth), textHeight);
            // Draw background
            g.setColor(Color.WHITE);
            g.fillRect(x, y - fm.getAscent(), Math.max(400, panelWidth), textHeight);

            g.setColor(Color.LIGHT_GRAY);
            g.drawRect(x, y - fm.getAscent(), Math.max(400, panelWidth), textHeight);

            // Draw fund name in fund color, then the rest
            // Find corresponding fund by name
            Fund correspondingFund = null;
            for (Fund fund : thoth.funds) {
                if (n.correspondsTo(fund.getName())) {
                    correspondingFund = fund;
                    break;
                }
            }
            if (correspondingFund != null) {
                String fundName = correspondingFund.getName();
                String newsTitle = n.getTitle();
                String effectText = " (" + n.getInitialEffect() + "%)";
                Color effectColor = (n.getInitialEffect() > 0) ? Color.GREEN : Color.RED;

                g.setColor(correspondingFund.getColor());
                g.drawString(fundName + " ", x + 3, y + (fm.getDescent() / 2));

                int nameWidth = fm.stringWidth(fundName + " ");

                g.setColor(Color.BLACK);
                String titleOnly = newsTitle.replace(fundName, "").trim();
                g.drawString(titleOnly, x + 3 + nameWidth, y + (fm.getDescent() / 2));

                int titleWidth = fm.stringWidth(titleOnly);

                g.setColor(effectColor);
                g.drawString(effectText, x + 3 + nameWidth + titleWidth, y + (fm.getDescent() / 2));
            } else {
                Color c = (n.getInitialEffect() > 0) ? Color.GREEN : Color.RED;
                NewsPanel.drawColoredParenthesesText(g, txt, x + 3, y + (fm.getDescent() / 2), c);
            }
        }
	}

    public static void drawColoredParenthesesText(Graphics2D g2, String text, int x, int y, Color parenColor) {
		FontMetrics fm = g2.getFontMetrics();
		boolean insideParen = false;

		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);

			// switch color based on whether we're inside parentheses
			if (c == '(') {
				insideParen = true;
				g2.setColor(parenColor);
			} else if (c == ')') {
				g2.setColor(parenColor);
				insideParen = false; // will revert on next char
			} else if (!insideParen) {
				g2.setColor(Color.DARK_GRAY);
			}

			// draw the character
			g2.drawString(String.valueOf(c), x, y);

			// advance x
			x += fm.charWidth(c);
		}
	}

}
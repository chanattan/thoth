package thoth.simulator;


import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.Timer;

import thoth.logic.Fund;


public class NewsPanel extends JPanel {
    private Thoth thoth;
    private JLabel newsLabel;
    private JTextArea newsArea;
    public JSplitPane parentPane;
    
    private List<NewsItem> displayedNews;
    private float animationOffset;
    private Timer animationTimer;
    private static final int ANIMATION_DURATION = 500;
    private static final int ANIMATION_FPS = 60;

    public NewsPanel(Thoth thoth) {
        this.thoth = thoth;
        this.setLayout(new BorderLayout());
        this.displayedNews = new ArrayList<>();
        this.animationOffset = 0f;


        this.createComponents();
        this.setBackground(Color.BLACK);
    }

    private void createComponents() {
        newsArea = new JTextArea();
        newsArea.setEditable(false);

        newsArea.setBackground(Color.BLACK);
        newsArea.setForeground(Color.WHITE);
    }

    public void updateNews(String news) {
        newsArea.setText(news);
    }

    public void updatePanel() {
        List<News> currentNews = this.thoth.news;
        
        if (!isSameNewsList(currentNews)) {
            startScrollAnimation(currentNews);
        } else {
            repaint();
        }
    }
    
    private boolean isSameNewsList(List<News> newsList) {
        if (displayedNews.size() != newsList.size()) {
            return false;
        }
        for (int i = 0; i < newsList.size(); i++) {
            if (!displayedNews.get(i).news.equals(newsList.get(i))) {
                return false;
            }
        }
        return true;
    }
    
    private void startScrollAnimation(List<News> newNews) {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        
        List<NewsItem> oldNews = new ArrayList<>(displayedNews);
        displayedNews.clear();
        
        for (News n : newNews) {
            displayedNews.add(new NewsItem(n, false));
        }
        
        for (NewsItem item : oldNews) {
            boolean found = false;
            for (NewsItem newItem : displayedNews) {
                if (newItem.news.equals(item.news)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                displayedNews.add(0, new NewsItem(item.news, true));
            }
        }
        
        animationOffset = 0f;
        
        animationTimer = new Timer(1000 / ANIMATION_FPS, e -> {
            animationOffset += (float) ANIMATION_FPS / ANIMATION_DURATION;
            
            if (animationOffset >= 1.0f) {
                animationOffset = 1.0f;
                animationTimer.stop();
                
                displayedNews.removeIf(item -> item.isOld);
            }
            
            repaint();
        });
        animationTimer.start();
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
        g.setFont(Thoth.customFont.deriveFont(Font.PLAIN, 20f));
        
        String header = "NEWS";
        FontMetrics fm = g.getFontMetrics();
        int x = 115;
        int y = 22;
        // background for title
        g.setColor(new Color(50, 50, 50));
        int padding = 10;
        g.fillRect(x - padding, y - fm.getAscent() - 7, getWidth(), fm.getHeight() + 6);

        g.setColor(Color.WHITE);
        g.drawString(header, x, y);

        Object[] dateInfo = thoth.getDate();
        String month = (String) dateInfo[0];
        int year = (int) dateInfo[1];

        // Background rectangle for date
        String dateStr = "Date: " + month + "/" + year;
        g.setFont(Thoth.customFont.deriveFont(Font.PLAIN, 16f));
        FontMetrics dateFm = g.getFontMetrics();
        int datePadding = 6;
        int dateXPos = 0;
        int dateYPos = 20 - dateFm.getAscent();
        g.setColor(new Color(50, 50, 50));
        g.fillRect(dateXPos - datePadding, dateYPos - datePadding - 4, dateFm.stringWidth(dateStr) + 2 * datePadding + 12, dateFm.getHeight() + 2 * datePadding);
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
        Font originalFont = g.getFont();
        
        FontMetrics fm = g.getFontMetrics();
        int itemHeight = fm.getHeight() + 7;
        
        int visibleCount = 0;
        for (NewsItem item : displayedNews) {
            if (!item.isOld) {
                visibleCount++;
            }
        }
        
        int displayIndex = 0;
        for (int i = 0; i < displayedNews.size(); i++) {
            NewsItem item = displayedNews.get(i);
            News n = item.news;
            
            float yPosition;
            float alpha = 1.0f;
            
            if (item.isOld) {
                yPosition = 35 + (displayIndex - animationOffset) * itemHeight;
                alpha = 1.0f - animationOffset;
            } else {
                int positionInNew = 0;
                for (int j = 0; j < i; j++) {
                    if (!displayedNews.get(j).isOld) {
                        positionInNew++;
                    }
                }
                yPosition = 35 + (positionInNew + (1 - animationOffset) * (displayIndex - positionInNew)) * itemHeight;
            }
            
            if (yPosition > -itemHeight && yPosition < getHeight() && alpha > 0) {
                drawSingleNews(g, n, (int) yPosition, alpha);
            }
            
            displayIndex++;
        }
        
        g.setFont(originalFont);
    }
    
    private void drawSingleNews(Graphics2D g, News n, int yBase, float alpha) {
        g.setColor(Color.WHITE);
        int x = 18;
        int y = yBase + 27;

        FontMetrics fm = g.getFontMetrics();
        String txt = n.getTitle() + " (" + n.getInitialEffect() + "%)";
        int textHeight = fm.getHeight() + 3;

        int panelWidth = getWidth() - 40;
        
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alpha));
        
        g2.setColor(Color.WHITE);
        g2.fillRect(x, y - fm.getAscent(), Math.max(400, panelWidth), textHeight);

        g2.setColor(Color.LIGHT_GRAY);
        g2.drawRect(x, y - fm.getAscent(), Math.max(400, panelWidth), textHeight);

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
            Color effectColor = (n.getInitialEffect() > 0) ? Color.GREEN.darker() : Color.RED.darker();

            g2.setColor(correspondingFund.getColor());
            g2.drawString(fundName + " ", x + 3, y + (fm.getDescent() / 2));

            int nameWidth = fm.stringWidth(fundName + " ");

            g2.setColor(Color.BLACK);
            String titleOnly = newsTitle.replace(fundName, "").trim();
            g2.drawString(titleOnly, x + 3 + nameWidth, y + (fm.getDescent() / 2));

            int titleWidth = fm.stringWidth(titleOnly);

            g2.setColor(effectColor);
            g2.drawString(effectText, x + 3 + nameWidth + titleWidth, y + (fm.getDescent() / 2));
            //int effectWidth = fm.stringWidth(effectText);

            // Draw date next to effect text
            /*Object[] dateInfo = Thoth.getDateStatic(WIDTH)
            int month = Integer.parseInt((String) dateInfo[0]);
            int year = (int) dateInfo[1];
            String dateStr = " [" + String.format("%02d", month) + "/" + year + "]";
            g.setColor(Color.GRAY);
            g.drawString(dateStr, x + 3 + nameWidth + titleWidth + effectWidth, y + (fm.getDescent() / 2));*/
        } else {
            Color c = (n.getInitialEffect() > 0) ? Color.GREEN.darker() : Color.RED.darker();
            NewsPanel.drawColoredParenthesesText(g2, txt, x + 3, y + (fm.getDescent() / 2), c);
        }
        
        g2.dispose();
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
    
    private static class NewsItem {
        News news;
        boolean isOld;
        
        NewsItem(News news, boolean isOld) {
            this.news = news;
            this.isOld = isOld;
        }
    }


}
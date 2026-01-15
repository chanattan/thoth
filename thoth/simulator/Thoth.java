package thoth.simulator;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.*;
import thoth.logic.AI;
import thoth.logic.Fund;
import thoth.logic.Player;
import thoth.ui.SplashIntro;

public class Thoth {

    public ArrayList<News> news;
    public ArrayList<Fund> funds;
    public Player player;
    public Random r;
    public Window window;
    public AI AI;
    public static Font customFont;
    public static final int NB_NEWS_MAX = 5;

    public Thoth() {
        // Initializes simulation.
        this.news = News.generateNews(5);
        this.funds = Fund.generateFunds(5);
        this.player = new Player();
        this.r = new Random();
        this.window = null;
        this.AI = new AI(this);
        customFont = loadFont(14f);
    }

    // Returns the effect for a given news.
    public float useEffect(String fundName) {
        List<News> snews = new ArrayList<News>(this.news).reversed();
        // Get the last effect
        for (News n : snews) {
            if (n.correspondsTo(fundName)) { // TODO: remplacer par fund reference
                return n.getEffect(); // Deplete the given effect.
            }
        }
        return 0f;
    }

    // Updates news
    public void seekNews(String fundName) {
        if (r.nextFloat() < .1) { // 1% chance of having a new given any fund name.
            News n = News.yieldNew(fundName);
            if (n != null) {
                if (this.news.size() >= NB_NEWS_MAX) {
                    this.news.remove(0);
                }
                this.news.add(n);
            }
        }
    }

    private Font loadFont(float size) {
        try (InputStream is = Thoth.class.getResourceAsStream("../../assets/Grenze-SemiBold.ttf")) {

            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
            return baseFont.deriveFont(size);

        } catch (Exception e) {
            e.printStackTrace();
            return new Font("SansSerif", Font.PLAIN, (int) size);
        }
    }

    // Month converted to date starting from year 2054
    public Object[] getDate() {
        int month = this.window.getSimulator().getTime();
        int year = 2054 + (month / 12);
        int displayMonth = (month % 12) + 1;
        return new Object[]{String.format("%02d", displayMonth), year};
    }

    public static Object[] getDateStatic(int month) {
        int year = 2054 + (month / 12);
        int displayMonth = (month % 12) + 1;
        return new Object[]{String.format("%02d", displayMonth), year};
    }

    public static void main(String[] args) {
        boolean splash = false;
        if (!splash) {
            SwingUtilities.invokeLater(() -> {
                Thoth thoth = new Thoth();
                Window w = new Window(thoth);
                thoth.window = w;
                try {
                    w.setIconImage(ImageIO.read(new File("assets/thoth.png")));
                } catch (IOException ex) {
                    System.getLogger(Thoth.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
                w.setLocationRelativeTo(null);
                w.getSimulator().fillData(thoth.funds);
            });
            return;
        }
        SplashIntro.showSplash(() -> {
            // launch main simulator frame after fade out
            SwingUtilities.invokeLater(() -> {
                Thoth thoth = new Thoth();
                Window w = new Window(thoth);
                thoth.window = w;
                try {
                    w.setIconImage(ImageIO.read(new File("assets/thoth.png")));
                } catch (IOException ex) {
                    System.getLogger(Thoth.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
                w.setLocationRelativeTo(null);
                w.getSimulator().fillData(thoth.funds);
            });
        });
    }
}

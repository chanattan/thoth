package thoth.simulator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import thoth.log.Logger;
import thoth.logic.AI;
import thoth.logic.Fund;
import thoth.logic.Player;
import thoth.logic.Prediction;
import thoth.ui.AnimatedPopup;
import thoth.ui.AnimatedPopup.AnimationType;
import thoth.ui.AnimatedPopup.Direction;
import thoth.ui.SplashIntro;

public class Thoth {

    public ArrayList<News> news;
    public ArrayList<Fund> funds;
    public Logger logger;
    public Player player;
    public Random r;
    public Window window;
    public AI AI;
    public static Font customFont;
    public static final int NB_NEWS_MAX = 5;
    public static Thoth instance;

    public Prediction prediction = null;

    public Thoth() {
        // Initializes simulation.
        this.news = News.generateNews(5);
        this.funds = Fund.generateFunds(5);
        this.player = new Player(this);
        this.r = new Random();
        this.window = null;
        this.AI = new AI(this);
        this.logger = new Logger();
        logger.setCondition("H_plus_IA");
        logger.setSliceId("novice");
        logger.setTrigger("on_demand");
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
        
        try (InputStream is = getClass().getResourceAsStream("/assets/Grenze-SemiBold.ttf")) {

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

    public static int dateToTimestep(Object[] date) {
        int month = Integer.parseInt((String) date[0]);
        int year = (Integer) date[1];
        return (year - 2054) * 12 + (month - 1);
    }

    public static Object[] getDateStatic(int month) {
        int year = 2054 + (month / 12);
        int displayMonth = (month % 12) + 1;
        return new Object[]{String.format("%02d", displayMonth), year};
    }

    public static Image getThothIcon() {
        try {
            return ImageIO.read(Thoth.class.getResourceAsStream("/assets/thoth.png"));
        } catch (IOException ex) {
            System.getLogger(Thoth.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            return null;
        }
    }

    public static void showTutorial(Thoth thoth) {
        // Vérifier si c'est le premier lancement
        if (!isFirstLaunch()) {
            new Timer(1500, e -> {
                Thoth.instance.window.sim.animationTime.start();
                Thoth.instance.window.sim.time.start();
            }).start();
            return;
        }
        
        final int POPUP_DURATION = 800;
        final int FPS = 60;
        
        showTutorialStep(thoth, 1, POPUP_DURATION, FPS);
    }

    public static boolean isFirstLaunch() {
        File configFile = new File(System.getProperty("user.home"), ".thoth_config");
        return !configFile.exists();
    }

    private static void setFirstLaunchCompleted() {
        File configFile = new File(System.getProperty("user.home"), ".thoth_config");
        try {
            configFile.createNewFile();
            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write("first_launch_completed=true");
            }
            new Timer(1500, e -> {
                Thoth.instance.window.sim.animationTime.start();
                Thoth.instance.window.sim.time.start();
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void resetFirstLaunch() {
        File configFile = new File(System.getProperty("user.home"), ".thoth_config");
        if (configFile.exists()) {
            configFile.delete();
        }
    }

    private static void showTutorialStep(Thoth thoth, int step, int duration, int fps) {
        switch (step) {
            case 1 ->  {
                JPanel welcomeContent = createTutorialPanel(
                    "Welcome to Thoth!",
                    "<html><div style='width: 350px;'>" +
                    "<b>Thoth</b> is a stock investment simulator where an AI (Thoth AI) helps you choose investments.<br><br>" +
                    "You start with a capital and can invest in various funds. The goal is to <b>make money</b> but also to have the <b>highest number of profitable stocks</b>!<br><br>" +
                    "<i style='color: #666;'>Disclaimer: This is a simulation for educational purposes only.</i><br><br>" +
                    "<div style='text-align: center; color: #999;'><i>Click to continue...</i></div>" +
                    "</div></html>",
                    new Color(100, 149, 237)
                );
                
                Point centerScreen = new Point(
                    thoth.window.getX() + thoth.window.getWidth() / 2 - 200,
                    thoth.window.getY() + thoth.window.getHeight() / 2 - 300
                );
                
                AnimatedPopup popup = new AnimatedPopup(
                    welcomeContent,
                    thoth.window,
                    Direction.BOTTOM,
                    duration,
                    fps,
                    centerScreen,
                    AnimationType.FADE
                );
                
                Timer[] nextTimer = new Timer[1];
                welcomeContent.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (nextTimer[0] != null) nextTimer[0].stop();
                        popup.hide();
                        nextTimer[0] = new Timer(duration + 100, evt -> showTutorialStep(thoth, 2, duration, fps));
                        nextTimer[0].setRepeats(false);
                        nextTimer[0].start();
                    }
                });
                welcomeContent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                
                popup.show();
            }
            
            case 2 ->  {
                JPanel newsContent = createTutorialPanel(
                    "News Panel",
                    "<html><div style='width: 320px;'>" +
                    "News appears here and affects the stock market with a <b>one-month delay</b>.<br><br>" +
                    "• News appears <b>one month in advance</b><br>" +
                    "• Recent news appear at the <b>bottom</b><br>" +
                    "• Older news scroll to the <b>top</b><br><br>" +
                    "The date corresponds to the latest news and aligns with the fund curves displayed on the main window.<br><br>" +
                    "<div style='text-align: center; color: #999;'><i>Click to continue...</i></div>" +
                    "</div></html>",
                    new Color(76, 175, 80)
                );
                
                Point newsLocation = new Point(
                    thoth.window.getWidth() / 2 - 150,
                    -165
                );
                
                AnimatedPopup popup = new AnimatedPopup(
                    newsContent,
                    thoth.window,
                    Direction.BOTTOM,
                    duration,
                    fps,
                    newsLocation,
                    AnimationType.FADE
                );
                
                Timer[] nextTimer = new Timer[1];
                newsContent.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (nextTimer[0] != null) nextTimer[0].stop();
                        popup.hide();
                        nextTimer[0] = new Timer(duration + 100, evt -> showTutorialStep(thoth, 3, duration, fps));
                        nextTimer[0].setRepeats(false);
                        nextTimer[0].start();
                    }
                });
                newsContent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                
                popup.show();
            }
            
            case 3 ->  {
                JPanel fundsContent = createTutorialPanel(
                    "Funds Panel",
                    "<html><div style='width: 320px;'>" +
                    "All simulation funds are displayed here.<br><br>" +
                    "• <b>Click on a fund</b> to select it for investment<br>" +
                    "• A <b>Thoth button</b> appears next to selected funds for quick AI recommendations<br>" +
                    "• <b>Monthly returns</b> (growth rate) are displayed next to each fund<br>" +
                    "• Click <b>\"Month\"</b> to change the time axis display<br>" +
                    "• Click <b>\"Clear selection\"</b> to show all curves again<br>" +
                    "• Hover Thoth logo to see quick Thoth AI prediction.<br>" +
                    "Please use Thoth at the bottom left for the best recommendation to invest in.<br><br>" +
                    "<div style='text-align: center; color: #999;'><i>Click to continue...</i></div>" +
                    "</div></html>",
                    new Color(255, 152, 0)
                );
                
                Point fundsLocation = new Point(
                    thoth.window.getWidth() / 2 - 150,
                    thoth.window.getHeight() / 2 - 450
                );
                
                AnimatedPopup popup = new AnimatedPopup(
                    fundsContent,
                    thoth.window,
                    Direction.BOTTOM,
                    duration,
                    fps,
                    fundsLocation,
                    AnimationType.FADE
                );
                
                Timer[] nextTimer = new Timer[1];
                fundsContent.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (nextTimer[0] != null) nextTimer[0].stop();
                        popup.hide();
                        nextTimer[0] = new Timer(duration + 100, evt -> showTutorialStep(thoth, 4, duration, fps));
                        nextTimer[0].setRepeats(false);
                        nextTimer[0].start();
                    }
                });
                fundsContent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                
                popup.show();
            }
            
            case 4 ->  {
                JPanel investContent = createTutorialPanel(
                    "Investment Panel",
                    "<html><div style='width: 320px;'>" +
                    "This is where you manage your investments.<br><br>" +
                    "• Your <b>current stocks</b> are displayed here<br>" +
                    "• Enter the <b>amount</b> you want to invest in the selected fund<br>" +
                    "• Press <b>Enter</b> or click <b>\"Invest\"</b> to confirm<br><br>" +
                    "Your stocks will show your shares, capital gains, etc.<br><br>" +
                    "<div style='text-align: center; color: #999;'><i>Click to continue...</i></div>" +
                    "</div></html>",
                    new Color(156, 39, 176)
                );
                
                Point investLocation = new Point(
                    thoth.window.getWidth() / 2 - 150,
                    thoth.window.getHeight() / 2
                );
                
                AnimatedPopup popup = new AnimatedPopup(
                    investContent,
                    thoth.window,
                    Direction.BOTTOM,
                    duration,
                    fps,
                    investLocation,
                    AnimationType.FADE
                );
                
                Timer[] nextTimer = new Timer[1];
                investContent.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (nextTimer[0] != null) nextTimer[0].stop();
                        popup.hide();
                        nextTimer[0] = new Timer(duration + 100, evt -> showTutorialStep(thoth, 5, duration, fps));
                        nextTimer[0].setRepeats(false);
                        nextTimer[0].start();
                    }
                });
                investContent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                
                popup.show();
            }
            
            case 5 ->  {
                JPanel chartContent = createTutorialPanel(
                    "Main Chart",
                    "<html><div style='width: 340px;'>" +
                    "Fund curves are displayed with colors matching the funds panel.<br><br>" +
                    "• <b>Click on curve points</b> to get detailed fund information at that time<br>" +
                    "• Your stocks appear as <b>orange points</b><br>" +
                    "• <b>Drag</b> to pan the view<br>" +
                    "• <b>Zoom</b> in and out to explore different timeframes<br><br>" +
                    "<div style='text-align: center; color: #999;'><i>Click to continue...</i></div>" +
                    "</div></html>",
                    new Color(33, 150, 243)
                );
                
                Point chartLocation = new Point(
                    thoth.window.getWidth() / 2 - 200,
                    thoth.window.getHeight() / 2 - 400
                );
                
                AnimatedPopup popup = new AnimatedPopup(
                    chartContent,
                    thoth.window,
                    Direction.BOTTOM,
                    duration,
                    fps,
                    chartLocation,
                    AnimationType.FADE
                );
                
                Timer[] nextTimer = new Timer[1];
                chartContent.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (nextTimer[0] != null) nextTimer[0].stop();
                        popup.hide();
                        nextTimer[0] = new Timer(duration + 100, evt -> showTutorialStep(thoth, 6, duration, fps));
                        nextTimer[0].setRepeats(false);
                        nextTimer[0].start();
                    }
                });
                chartContent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                
                popup.show();
            }
            
            case 6 ->  {
                JPanel finalContent = createTutorialPanel(
                    "Thoth AI Assistant",
                    "<html><div style='width: 340px;'>" +
                    "Use <b>Thoth AI</b> to get investment recommendations!<br><br>" +
                    "• Click the <b>Thoth logo</b> at the bottom left to get AI advice<br>" +
                    "• <b>Rate the recommendations</b> (helpful/not helpful/somewhat helpful) to refine Thoth AI's suggestions<br><br>" +
                    "<div style='text-align: center; margin-top: 15px; font-size: 16px; color: #2196F3;'>" +
                    "<b>Have fun and invest wisely!</b>" +
                    "</div><br>" +
                    "<div style='text-align: center; color: #999;'><i>Click to close</i></div>" +
                    "</div></html>",
                    new Color(0, 150, 136)
                );
                
                Point finalLocation = new Point(
                    thoth.window.getWidth() / 2 - 200,
                    thoth.window.getHeight() / 2 - 300
                );
                
                AnimatedPopup popup = new AnimatedPopup(
                    finalContent,
                    thoth.window,
                    Direction.BOTTOM,
                    duration,
                    fps,
                    finalLocation,
                    AnimationType.FADE
                );
                
                finalContent.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        popup.hide();
                        setFirstLaunchCompleted();
                    }
                });
                finalContent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                
                popup.show();
            }
        }
    }

    private static JPanel createTutorialPanel(String title, String content, Color accentColor) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accentColor, 3),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(accentColor);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JLabel contentLabel = new JLabel(content);
        contentLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        contentLabel.setVerticalAlignment(JLabel.TOP);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(contentLabel, BorderLayout.CENTER);
        
        return panel;
    }

    public static void main(String[] args) {
        boolean splash = true;
        if (!splash) {
            SwingUtilities.invokeLater(() -> {
                Thoth thoth = new Thoth();
                Thoth.instance = thoth;
                Window w = new Window(thoth);
                thoth.window = w;
                Thoth.showTutorial(thoth);
                w.setIconImage(Thoth.getThothIcon());
                w.setLocationRelativeTo(null);
                w.getSimulator().fillData(thoth.funds);
            });
            return;
        }
        SplashIntro.showSplash(() -> {
            // launch main simulator frame after fade out
            SwingUtilities.invokeLater(() -> {
                Thoth thoth = new Thoth();
                Thoth.instance = thoth;
                Window w = new Window(thoth);
                thoth.window = w;
                Thoth.showTutorial(thoth);
                w.setIconImage(Thoth.getThothIcon());
                w.setLocationRelativeTo(null);
                w.getSimulator().fillData(thoth.funds);
            });
        });
    }
}

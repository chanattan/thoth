package thoth.simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import javax.swing.*;
import thoth.logic.Fund;
import thoth.logic.Player;

public class Thoth {

    public ArrayList<News> news;
    public ArrayList<Fund> funds;
    public Player player;
    public Random r;

    public Thoth() {
        // Initializes simulation.
        this.news = News.generateNews(5);
        this.funds = Fund.generateFunds(5);
        this.player = new Player();
        this.r = new Random();
    }

    // Returns the effect for a given news.
    public float useEffect(String fundName) {
        List<News> snews = new ArrayList<News>(this.news).reversed();
        // Get the last effect
        for (News n : snews) {
            if (n.correspondsTo(fundName)) { // TODO: remplacer par fund reference
                return n.useEffect(); // Deplete the given effect.
            }
        }
        return 0f;
    }

    // Updates news
    public void seekNews(String fundName) {
        if (r.nextFloat() < .01) { // 1% chance of having a new given any fund name.
            News n = News.yieldNew(fundName);
            if (n != null) {
                this.news.add(n);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Thoth thoth = new Thoth();
            Window w = new Window(thoth);
            w.getSimulator().fillData(thoth.funds);
        });
    }
}
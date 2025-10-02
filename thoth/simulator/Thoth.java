package thoth.simulator;

import java.util.ArrayList;
import javax.swing.*;
import thoth.logic.Fund;

public class Thoth {

    public ArrayList<News> news;
    public ArrayList<Fund> funds;

    public Thoth() {
        // Initializes simulation.
        this.news = News.generateNews(10);
        this.funds = Fund.generateFunds(5);
    }

    // Returns the effect for a given news.
    public float getEffect(String fundName) {
        for (News n : this.news) {
            if (n.getTitle().toLowerCase().contains(fundName.toLowerCase())) { // TODO: remplacer par fund reference
                return n.getEffect();
            }
        }
        return 0f;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Thoth thoth = new Thoth();
            Window w = new Window(thoth);
            w.getSimulator().fillData(thoth.funds);
        });
    }
}
package thoth.logic;

import java.util.Random;

public class Player {
    private double capital;

    public Player(double capital) {
        this.capital = capital;
    }

    public Player() {
        Random rand = new Random();
        this.capital = 100000 + rand.nextInt(300000);
    }

    public double getCapital() {
        return this.capital;
    }

    public void invest(Action a) {
        
    }
}
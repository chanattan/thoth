package thoth.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Player {
    private double capital;
    private HashMap<Fund, ArrayList<Action>> actions;

    public Player(double capital) {
        this.capital = capital;
        this.actions = new HashMap<Fund, ArrayList<Action>>();
    }

    public Player() {
        Random rand = new Random();
        this(100000 + rand.nextInt(300000));
    }

    public double getCapital() {
        return this.capital;
    }

    /*
        The player decides to invest in a given fund, at a given time (i.e., buys an action).
        If the player has enough capital, the action is bought and stored among the actions the player has.
        The value of an action can change over time.
    */
    public void invest(Action a) throws InsufficientCapital {
        if (a.getValue() > this.capital)
            throw new InsufficientCapital(a);
        this.capital -= a.getValue();
        this.actions.getOrDefault(a.getFund(), new ArrayList<Action>()).add(a);
    }

    public void sellAction(Action a) {
        // TODO.
        // updateCapital();
    }

    public static class InsufficientCapital extends Exception {

        private String fundName;
        private double value;

        public InsufficientCapital(Action a) {
            this.fundName = a.getFund().getName();
            this.value = a.getValue();
        }

        @Override
        public String getMessage() {
            return "Player has insufficient funds for the fund " + this.fundName + ", at the current price of " + this.value + "€.";
        }
        
    }
}
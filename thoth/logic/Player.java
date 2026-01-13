package thoth.logic;

import java.util.HashMap;
import java.util.Random;

public class Player {
    private double capital;
    private HashMap<Fund, Action> actions;

    public Player(double capital) {
        this.capital = capital;
        this.actions = new HashMap<Fund, Action>();
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
    public void invest(int boughtTime, double boughtValue, Fund f) throws InsufficientCapital {
        if (this.capital - boughtValue < 0)
            throw new InsufficientCapital(boughtValue, f);
        this.capital -= boughtValue;
        Action existingAction = this.actions.get(f);
        if (existingAction != null) {
            existingAction.updateShare(boughtValue);
        } else {
            this.actions.put(f, new Action(boughtTime, boughtValue, f));
        }
    }

    public void sellAction(Action a) {
        if (!this.actions.containsValue(a)) {
            return;
        }
        double actionValue = a.getValue();
        this.capital += actionValue;
        this.actions.remove(a.getFund());
    }

    public HashMap<Fund, Action> getActions() {
        return this.actions;
    }

    public static class InsufficientCapital extends Exception {

        private String fundName;
        private double value;

        public InsufficientCapital(double value, Fund f) {
            this.fundName = f.getName();
            this.value = value;
        }

        @Override
        public String getMessage() {
            return "Player has insufficient funds for the fund " + this.fundName + ", at the current price of " + this.value + "€.";
        }
        
    }
}
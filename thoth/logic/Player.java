package thoth.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import thoth.simulator.Thoth;

public class Player {
    private double capital;
    private HashMap<Fund, ArrayList<Action>> actions;
    private Thoth thoth;

    public Player(Thoth thoth, double capital) {
        this.thoth = thoth;
        this.capital = capital;
        this.actions = new HashMap<Fund, ArrayList<Action>>();
    }

    public Player(Thoth thoth) {
        Random rand = new Random();
        this(thoth, 2500 + rand.nextInt(500));
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
        ArrayList<Action> existingActions = this.actions.get(f);
        if (existingActions != null) {
            Action a = getAction(boughtTime, f); // check if action at boughtTime already exists
            if (a == null) {
                Action b = new Action(boughtTime, boughtValue, f);
                b.associatedPrediction = thoth.prediction.clone();
                existingActions.add(b);
            } else
                a.updateShare(boughtValue);
        } else {
            ArrayList<Action> newActions = new ArrayList<>();
            Action a = new Action(boughtTime, boughtValue, f);
            a.associatedPrediction = thoth.prediction.clone();
            newActions.add(a);
            this.actions.put(f, newActions);
        }
    }

    public float getConfidence() {
        int helpful = thoth.window.sim.foundHelpful;
        int somewhat = thoth.window.sim.somewhatHelpful;
        int notHelp = thoth.window.sim.notHelpful;
        int total = helpful + somewhat + notHelp;
        if (total == 0) return 0.5f; // neutre tant qu'on n'a pas de données

        // Score entre -1 et +1
        float rawScore = (helpful + 0.5f * somewhat - notHelp) / total; 
        rawScore = Math.max(-1f, Math.min(1f, rawScore));
        return (rawScore + 1f) / 2f; // entre 0 et 1
    }

    public void sellAction(Action a) {
        ArrayList<Action> fundActions = this.actions.get(a.getFund());
        if (fundActions != null) {
            if (fundActions.contains(a))
                fundActions.remove(a);
            if (fundActions.isEmpty()) {
                this.actions.remove(a.getFund());
            }
        }
        double actionValue = a.getValue();
        this.capital += actionValue;
    }

    public boolean hasInvestedIn(Fund f) {
        return this.actions.containsKey(f);
    }

    public HashMap<Fund, ArrayList<Action>> getActions() {
        return this.actions;
    }

    public Action getAction(int timeStep, Fund f) {
        ArrayList<Action> fundActions = this.actions.get(f);
        if (fundActions != null && timeStep >= 0) {
            for (Action a : fundActions) {
                if (a.getBoughtTime() == timeStep) {
                    return a;
                }
            }
        }
        return null;
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
package thoth.logic;

public class Action {
    private int time;
    private double value;
    private Fund fund;

    public Action(int time, double value, Fund fund) {
        this.time = time;
        this.value = value;
        this.fund = fund;
    }

    public void updateValue(double newValue) {
        this.value = newValue;
    }

    public int getTime() {
        return this.time;
    }

    public double getValue() {
        return this.value;
    }

    public Fund getFund() {
        return this.fund;
    } 

}
package thoth.logic;

public class Action {
    private int time;
    private double share;
    private double boughtValue;
    private Fund fund;

    public Action(int time, double investedValue, Fund fund) {
        this.time = time;
        this.share = investedValue / fund.getCurve().getLastValues(fund.getCurve().getSteps() - 1)[0];
        this.boughtValue = investedValue;
        this.fund = fund;
    }

    public void updateShare(double newInvestedValue) {
        // Last available value of the fund's curve
        Curve c = this.fund.getCurve();
        double lastValue = c.getLastValues(c.getSteps() - 1)[0];
        double newShare = newInvestedValue / lastValue; // share = value / price
        this.share += newShare;
    }

    public double getShare() {
        return this.share;
    }

    public double getPlusValue() {
        double lastValue = this.fund.getCurve().getLastValues(this.fund.getCurve().getSteps() - 1)[0];
        double curveValueAtPurchase = this.fund.getCurve().getLastValues(this.time)[0];
        return (double) Math.round(lastValue / curveValueAtPurchase);
    }

    /**
     * Current value of the action.
     */
    public double getValue() {
        Curve c = this.fund.getCurve();
        double lastValue = c.getLastValues(c.getSteps() - 1)[0];
        return this.share * lastValue;
    }

    public int getBoughtTime() {
        return this.time;
    }

    public double getBoughtValue() {
        return this.boughtValue;
    }

    public Fund getFund() {
        return this.fund;
    } 

}
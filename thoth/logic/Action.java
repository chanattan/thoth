package thoth.logic;

import java.awt.geom.Point2D;

public class Action {
    private int time;
    private double share;
    private double boughtValue;
    private double priceAtPurchase;
    private Fund fund;

    // Drawing
    public Point2D.Double position;

    public Action(int time, double investedValue, Fund fund) {
        this.time = time;
        this.priceAtPurchase = fund.getCurve().getLastValues(time)[0];
        this.share = investedValue / this.priceAtPurchase;
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

    public double getPriceAtPurchase() {
        return this.priceAtPurchase;
    }

    public double getShare() {
        return this.share;
    }

    public double getPlusValue() {
        double lastValue = this.fund.getCurve().getLastValues(this.fund.getCurve().getSteps() - 1)[0];
        return (double) Math.round(lastValue / this.priceAtPurchase * 100 - 100); // in percent
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
package thoth.logic;

import java.awt.geom.Point2D;
import java.text.DecimalFormat;

public class Action {
    private int time;
    private double share;
    private double boughtValue;
    private double priceAtPurchase;
    public Prediction associatedPrediction = null; // for AI_only purposes
    private Fund fund;
    private DecimalFormat df = new DecimalFormat("#.##");

    // Drawing
    public Point2D.Double position;

    public Action(int time, double investedValue, Fund fund) {
        this.time = time;
        this.priceAtPurchase = Math.max(fund.getCurve().getLastValues(time)[0], 0.01);
        this.share = investedValue / this.priceAtPurchase;
        this.boughtValue = investedValue;
        this.fund = fund;
    }

    public void updateShare(double newInvestedValue) {
        // Last available value of the fund's curve
        Curve c = this.fund.getCurve();
        double lastValue = c.getLastValues(c.getSteps() - 1)[0];
        double newShare = newInvestedValue / Math.max(lastValue, 0.01); // share = value / price
        this.share += newShare;
    }

    public double getPriceAtPurchase() {
        return Math.max(this.priceAtPurchase, 0.01); // avoid division by zero
    }

    public double getShare() {
        return Math.max(this.share, 0.001);
    }

    public double getPlusValue() {
        double lastValue = Math.max(this.fund.getCurve().getLastValues(this.fund.getCurve().getSteps() - 1)[0], 0.01);
        return Double.parseDouble(df.format(lastValue / getPriceAtPurchase() * 100 - 100).replaceAll(",", ".")); // in percent
    }

    /**
     * Current value of the action.
     */
    public double getValue() {
        Curve c = this.fund.getCurve();
        double lastValue = Math.max(c.getLastValues(c.getSteps() - 1)[0], 0.01);
        return this.getShare() * lastValue;
    }

    public int getBoughtTime() {
        return this.time;
    }

    public double getBoughtValue() {
        return Math.max(this.boughtValue, 0.01);
    }

    public Fund getFund() {
        return this.fund;
    } 

}
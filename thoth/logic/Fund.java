package thoth.logic;

public class Fund {
    private final String fundName;
    private final Curve curve;

    public Fund(String fundName, Curve curve) {
        this.fundName = fundName;
        this.curve = curve;
    }

    public String getFundName() {
        return this.fundName;
    }

    public Curve getCurve() {
        return this.curve;
    }
}
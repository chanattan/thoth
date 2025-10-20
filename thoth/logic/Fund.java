package thoth.logic;

import java.util.ArrayList;

public class Fund {
    private final String fundName;
    private final Curve curve;

    public Fund(String fundName, Curve curve) {
        this.fundName = fundName;
        this.curve = curve;
    }

    public String getName() {
        return this.fundName;
    }

    public Curve getCurve() {
        return this.curve;
    }

    static final Fund[] funds = {
        new Fund("BNP Paribas", Curve.generateCurve()),
        new Fund("Netflix", Curve.generateCurve()),
        new Fund("Amazon", Curve.generateCurve()),
        new Fund("Apple", Curve.generateCurve()),
        new Fund("Tesla", Curve.generateCurve()),
        new Fund("Google", Curve.generateCurve()),
        new Fund("Microsoft", Curve.generateCurve()),
        new Fund("Facebook", Curve.generateCurve()),
        new Fund("Uber", Curve.generateCurve()),
        new Fund("Airbus", Curve.generateCurve()),
        new Fund("Boeing", Curve.generateCurve()),
        new Fund("Spotify", Curve.generateCurve()),
        new Fund("TotalEnergies", Curve.generateCurve()),
        new Fund("BNP Paribas", Curve.generateCurve())
    };

    public static ArrayList<Fund> generateFunds(int n) {
        ArrayList<Fund> fundsArry = new ArrayList<Fund>();
        // Temporaire
        for (int i = 0; i < n; i++) {
            fundsArry.add(funds[i]);
        }
        return fundsArry; 
    }
}
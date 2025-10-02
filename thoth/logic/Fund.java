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

    static final int INITIAL_STEPS = 20;
    static final Fund[] funds = {
        new Fund("BNP Paribas", Curve.generateCurve(INITIAL_STEPS)),
        new Fund("Netflix", Curve.generateCurve(INITIAL_STEPS)),
        new Fund("Amazon", Curve.generateCurve(INITIAL_STEPS)),
        new Fund("Apple", Curve.generateCurve(INITIAL_STEPS)),
        new Fund("Tesla", Curve.generateCurve(INITIAL_STEPS)),
        new Fund("Google", Curve.generateCurve(INITIAL_STEPS)),
        new Fund("Microsoft", Curve.generateCurve(INITIAL_STEPS)),
        new Fund("Facebook", Curve.generateCurve(INITIAL_STEPS)),
        new Fund("Uber", Curve.generateCurve(INITIAL_STEPS)),
        new Fund("Airbus", Curve.generateCurve(INITIAL_STEPS)),
        new Fund("Boeing", Curve.generateCurve(INITIAL_STEPS)),
        new Fund("Spotify", Curve.generateCurve(INITIAL_STEPS)),
        new Fund("TotalEnergies", Curve.generateCurve(INITIAL_STEPS)),
        new Fund("BNP Paribas", Curve.generateCurve(INITIAL_STEPS))
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
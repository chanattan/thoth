package thoth.logic;

import java.util.Random;
public class Curve {
    private float[] values;
    private final int steps;

    public Curve(int steps, float fbm_min_movement, float fbm_max_movement, float chaos_factor) {
        Random rng = new Random();
        this.values = new float[steps];
        float[] fbm = new float[steps];

        for (int t = 0; t < steps; t += 1) {
            fbm[t] = rng.nextFloat(fbm_min_movement, fbm_max_movement) + (t == 0 ? 0 : fbm[t - 1]);
            float value = (t == 0 ? 0 : this.values[t - 1]);
            this.values[t] = value;
        }

        for (int t = 0; t < steps; t += 1) {
            this.values[t] = lerp(this.values[t], fbm[t], chaos_factor);
        }

        this.steps = steps;
    }

    public int getSteps() {
        return this.steps;
    }

    public int getValue(int t, float event_effect) {
        // TODO: l'event effect dépend de quand elle est apparue, à ajouter dans effect et peut-être définir une classe effect ou màj new.
        return (int) (event_effect + this.values[t]);
    }

    private float lerp(float a, float b, float factor) {
        return a * factor + b * (1f - factor);
    }

    private static final float FBM_MIN = 10;
    private static final float FBM_MAX = 20;
    private static final int CHAOS_FACTOR = 10;
    // Generates a random curve
    public static Curve generateCurve(int steps) {
        Random rand = new Random();
        return new Curve(steps, FBM_MIN, FBM_MAX, rand.nextFloat() * CHAOS_FACTOR);
    }
}

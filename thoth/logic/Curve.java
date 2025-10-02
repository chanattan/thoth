package thoth.logic;

import java.util.Random;
public class Curve {
    private float[] values;
    private final int steps;

    public Curve(int steps, float fbm_min_movement, float fbm_max_movement, float chaos_factor, float[] event_effects) {
        Random rng = new Random();
        this.values = new float[steps];
        float[] fbm = new float[steps];

        for (int t = 0; t < steps; t += 1) {
            fbm[t] = rng.nextFloat(fbm_min_movement, fbm_max_movement) + (t == 0 ? 0 : fbm[t - 1]);
            float value = event_effects[t] + (t == 0 ? 0 : this.values[t - 1]);
            this.values[t] = value;
        }

        for (int t = 0; t < steps; t += 1) {
            this.values[t] = lerp(this.values[t], fbm[t], chaos_factor);
        }

        this.steps = steps;
    }

    public float getSteps() {
        return this.steps;
    }

    public float getValue(int t) {
        return this.values[t];
    }

    private float lerp(float a, float b, float factor) {
        return a * factor + b * (1f - factor);
    }
}

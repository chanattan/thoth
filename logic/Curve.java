import java.util.Random;
public class Curve {
    float[] values;

    public Curve(int steps, float fbm_min_movement, float fbm_max_movement, float chaos_factor, float[] event_effects) {
        Random rng = new Random();
        float[] values = new float[steps];
        float[] fbm = new float[steps];

        for (int t = 0; t < steps; t += 1) {
            fbm[t] = rng.nextFloat(fbm_min_movement, fbm_max_movement) + (t == 0 ? 0 : fbm[t - 1]);
            float value = event_effects[t] + (t == 0 ? 0 : values[t - 1]);
            values[t] = value;
        }

        for (int t = 0; t < steps; t += 1) {
            values[t] = lerp(values[t], fbm[t], chaos_factor);
        }

        this.values = values;
    }

    public float get(int t) {
        return this.values[t];
    }

    float lerp(float a, float b, float factor) {
        return a * factor + b * (1f - factor);
    }
}

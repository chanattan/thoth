import java.util.Random;
public class Curve {
    float[] values;

    public Curve(int steps, int news_freq_interval_min, int news_freq_interval_max, float chaos_factor, News[] news) {
        Random rng = new Random();
        float[] values = new float[steps];
        int next_news_counter = randomIntBounds(news_freq_interval_min, news_freq_interval_max, rng);
        float last_news_effect = 0;

        for (int t = 0; t < steps; t += 1) {
            float value = t == 0 ? 0 : values[t - 1];
            if (next_news_counter == 0) {
                int event_index = randomIntBounds(0, news.length, rng);
                News event = news[event_index];
                last_news_effect = event.getEffect();
                value += event.getEffect();
            } else {
                value += last_news_effect * 0.75;
                next_news_counter -= 1;
            }

            values[t] = value;
        }

        this.values = values;
    }

    int randomIntBounds(int min, int max, Random rng) {
        return rng.nextInt(max - min + 1) + min;
    }
}

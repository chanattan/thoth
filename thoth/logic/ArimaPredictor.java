package thoth.logic;

import com.workday.insights.timeseries.arima.Arima;
import com.workday.insights.timeseries.arima.struct.ArimaParams;
import com.workday.insights.timeseries.arima.struct.ForecastResult;
import thoth.simulator.Thoth;

public class ArimaPredictor {

    private static final ArimaParams PARAMS = new ArimaParams(1, 1, 2, 0, 0, 0, 0);

    /**
     * Predict 3 months horizon
     */
    public static Prediction predict(Thoth thoth, Fund fund) {
        Curve c = fund.getCurve();
        int n = Math.min(80, c.getSteps());
        int[] raw = c.getLastValues(c.getSteps() - n);
        if (raw.length < 10) {
            return new Prediction(fund, 0, -1, -1); // no conf
        }
        
        double[] data = new double[raw.length];
        for (int i = 0; i < raw.length; i++) data[i] = raw[i];
        
        try {
            ForecastResult result = Arima.forecast_arima(data, 3, PARAMS);
            double last = data[data.length - 1];
            
            // Expected return 3m (mois 3)
            double expected_3m = result.getForecast()[2];
            double ret_3m_pct = (expected_3m - last) / last * 100;
            
            // Confidence 3m CI
            double upper_3m = result.getForecastUpperConf()[2];
            double lower_3m = result.getForecastLowerConf()[2];
            double ci_width_3m = (upper_3m - lower_3m) / expected_3m * 100;
            double conf_3m = Math.max(0, 95 - ci_width_3m);
            float fbMult = getFeedbackMultiplier(thoth); // eg [0.7, 1.3]
            double conf_3m_adjusted = conf_3m * fbMult / 100.0;  // Normalisé [0,1]
            conf_3m_adjusted = Math.max(0, Math.min(1, conf_3m_adjusted));

            System.out.println("ARIMA conf: " + conf_3m / 100 + "% → Adjusted: " + conf_3m_adjusted + "% (fbMult=" + fbMult + ")");
            System.out.println("confidence 3m for " + fund.getName() + ": " + conf_3m / 100 + "% (CI width " + ci_width_3m + "%)");
            
            Prediction p = new Prediction(fund, ret_3m_pct, conf_3m / 100, (float) conf_3m_adjusted);
            p.forecastResult = result;
            return p;
        } catch (Exception e) {
            return new Prediction(fund, 0, -1, -1);
        }
    }

    private static float getFeedbackMultiplier(Thoth thoth) {
        int helpful = thoth.window.sim.foundHelpful;
        int somewhat = thoth.window.sim.somewhatHelpful;
        int notHelp = thoth.window.sim.notHelpful;
        int total = helpful + somewhat + notHelp;
        if (total == 0) return 1.0f; // neutre tant qu'on n'a pas de données

        // Score entre -1 et +1
        float rawScore = (helpful + 0.5f * somewhat - notHelp) / total; 
        rawScore = Math.max(-1f, Math.min(1f, rawScore));

        // on transforme en multiplicateur doux
        return 1.0f + 0.5f * rawScore;
    }

    public float computeConfidence(Thoth thoth, Fund fund) {
        // Confiance qui s'adapte au feedback du joueur
        Curve c = fund.getCurve();
        int[] lastValues = c.getLastValues(Math.max(0, c.getSteps() - 10));
        if (lastValues.length < 2) return 0f;

        int increases = 0;
        for (int i = 1; i < lastValues.length; i++) {
            if (lastValues[i] > lastValues[i - 1]) {
                increases++;
            }
        }
        float base = (float) increases / (lastValues.length - 1);

        float multiplier = getFeedbackMultiplier(thoth);
        float adjusted = base * multiplier;

        // clamp dans [0,1]
        return Math.max(0f, Math.min(1f, adjusted));
    }

    /**
     * Best fund (3m score)
     */
    public static Prediction recommend(Thoth thoth) {
        Prediction best = null;
        double bestScore = -999;
        for (Fund f : thoth.funds) {
            Prediction pred = predict(thoth, f);
            double score = pred.expectedReturn * pred.confidence;
            boolean enoughCapital = pred.fund.getCurve().getLastValues(pred.fund.getCurve().getSteps() - 1)[0] <= thoth.player.getCapital();
            if (pred.confidence > 0.5 && score > bestScore && pred.expectedReturn > 0 && enoughCapital) {
                best = pred;
                bestScore = score;
            }
        }
        return best != null ? best : new Prediction(null, 0, 0, 0);
    }

    /**
     * Explication simple (3 lignes)
     */
    public static String explain(Prediction pred, ForecastResult r, double last) {
        double noise = Math.sqrt(r.getRMSE()) / last * 100;
        Curve c = pred.fund.getCurve();
        int n = Math.min(20, c.getSteps());
        int[] rawData = c.getLastValues(c.getSteps() - n);
        double[] data = new double[rawData.length];
        for (int i = 0; i < rawData.length; i++) {
            data[i] = rawData[i];
        }
        double momentum = computeAR1(data) * 100; // AR(1)
        
        return String.format("""
            1. <b>Momentum</b> %.0f%% (AR) | 2. <b>Noise</b> %.0f%% (MA) | 3. <b>Trend</b> %.1f%%
            """, momentum, noise, pred.expectedReturn/3);
    }

    /**
     * AR(1) coefficient des 20 derniers points
     */
    private static double computeAR1(double[] data) {
        if (data.length < 10) return 0.6;
        
        // 20 derniers points
        int n = Math.min(20, data.length);
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        
        for (int i = n-1; i > 0; i--) {
            double x = data[i-1]; // lag1
            double y = data[i]; // actuel
            sumX += x; sumY += y; sumXY += x*y; sumXX += x*x;
        }
        
        double beta = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
        return Math.max(0, Math.min(1, beta));
    }

    public static String why() {
        return """
            <b>Thoth AI</b> uses <i>time series analysis</i> (<span style='color:#00ff88'>ARIMA models</span>) 
            to predict fund performance over a <b>3-month horizon</b>.

            Thoth reads the last <b>80 months</b> (max) of all funds' <i>waves</i>. It identifies <b>3 key components</b>:
            - <span style='color:#11ffff'>Momentum</span>: is the wave still rising? <b>(+65%)</b>
            - <span style='color:#ffaa00'>Noise</span>: are fluctuations stabilizing? <b>(8%)</b> 
            - <span style='color:#00ff88'>Trend</span>: 3-month direction? <b>(+1.4%/month)</b>

            It is a <i>mathematical</i> model which provides <b>confidence value as wave precision</b>. Higher confidence (better) means <i>narrower prediction intervals</i>.""";
    }

    /**
     * Détails mois par mois (more info)
     */
    public static String moreInfo(ForecastResult r, double last) {
        return String.format("""
            <b><span style='color:#FF0000'>3 MONTHS</span> Detailed (±95%% <i>margin</i>)</b>:
            <span style='color:#11ffff'>M1:</span> <b>%.0f</b> <i>± %.0f</i> | <span style='color:#11ffff'>M2:</span> <b>%.0f</b> <i>±%.0f</i> | <span style='color:#11ffff'>M3:</span> <b>%.0f</b> <i>±%.0f</i>
            """, 
            r.getForecast()[0], ciHalfWidth(0, r),
            r.getForecast()[1], ciHalfWidth(1, r),
            r.getForecast()[2], ciHalfWidth(2, r)
        );
    }

    private static double ciHalfWidth(int month, ForecastResult r) {
        double upper = r.getForecastUpperConf()[month];
        double lower = r.getForecastLowerConf()[month];
        return (upper - lower) / 2;  // demi-largeur centrée
    }
}

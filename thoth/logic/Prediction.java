package thoth.logic;
import java.text.DecimalFormat;

import com.workday.insights.timeseries.arima.struct.ForecastResult;
import thoth.simulator.Thoth;

public class Prediction implements Cloneable {
    public Fund fund;
    public double confidence;
    public double adjustedConfidence;
    public double expectedReturn;
    public double score;
    public ForecastResult forecastResult; // for ARIMA
    private DecimalFormat df = new DecimalFormat("#.##");
    private Object[] date;

    public Prediction(Fund fund, double expectedReturn, double confidence, double adjustedConfidence) {
        this.fund = fund;
        this.confidence = confidence;
        this.expectedReturn = expectedReturn;
        this.score = expectedReturn * confidence; // simple scoring
        this.adjustedConfidence = adjustedConfidence;
        this.date = Thoth.instance.getDate();
    }

    public double getScore() {
        return this.score;
    }

    public double getExpectedReturn() {
        return Double.parseDouble(df.format(this.expectedReturn).replaceAll(",", "."));
    }

    public int getAdjustedConfidenceLevel() { // adjusted for the user
        if (adjustedConfidence > 0.75f) return 3; // High confidence
        else if (adjustedConfidence > 0.5f) return 2; // Medium confidence
        else if (adjustedConfidence > 0.25f) return 1; // Low confidence
        else return 0; // No confidence
    }

    public int getAIConfidenceLevel() {
        if (confidence > 0.75f) return 3; // High confidence
        else if (confidence > 0.5f) return 2; // Medium confidence
        else if (confidence > 0.25f) return 1; // Low confidence
        else return 0; // No confidence
    }

    public Object[] getDate() {
        return this.date;
    }

    /**
     * It returns a badge based on AI confidence level.
     */
    public String getConfidenceBadge() {
        int level = getAIConfidenceLevel();
        return switch(level) {
            case 3 -> "<span style='color:#00ff88;background:#004400;padding:2px 6px;border-radius:8px'>🟢 High confidence</span>";
            case 2 -> "<span style='color:#ffaa00;background:#664400;padding:2px 6px;border-radius:8px'>🟡 Medium confidence</span>";
            case 1 -> "<span style='color:#ff6644;background:#440000;padding:2px 6px;border-radius:8px'>🔴 Low confidence</span>";
            default -> "<span style='color:#666666;background:#222;padding:2px 6px;border-radius:8px'>⚪ No confidence</span>";
        };
    }

    public Prediction clone() {
        return new Prediction(this.fund, this.expectedReturn, this.confidence, this.adjustedConfidence);
    }

}
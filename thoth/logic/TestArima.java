// thoth/logic/TestArima.java
package thoth.logic;
import com.workday.insights.timeseries.arima.Arima;
import com.workday.insights.timeseries.arima.struct.ArimaParams;
import com.workday.insights.timeseries.arima.struct.ForecastResult;

public class TestArima {
    public static void main(String[] args) {
        // 100 points simu
        Curve c = Curve.generateCurve();
        double[] data = java.util.Arrays.stream(c.getLastValues(0))
            .mapToDouble(i -> i)
            .toArray();
        // Get a subarray of the data
        int sampleSize = 9;
        double[] subdata = java.util.Arrays.copyOfRange(data, 0, sampleSize + 1);
        
        ArimaParams p = new ArimaParams(1, 1, 2, 0, 0, 0, 0);
        ForecastResult r = Arima.forecast_arima(subdata, 1, p);
        System.out.println("ARIMA OK: " + java.util.Arrays.toString(r.getForecast()));
        System.out.println("Confidence: " + getConfidence(r, c.getPregeneratedValue(), 1));
        // Check if forecasting ressembles the later data
        System.out.println("Expected: " + c.getLastValues(c.getSteps() - 1)[0] + ", " + c.getPregeneratedValue());
        
        // Error ratios
        for (int i = 0; i < r.getForecast().length; i++) {
            double expected = data[sampleSize + i];
            double forecasted = r.getForecast()[i];
            double error = Math.abs(forecasted - expected) / expected;
            System.out.println("Error for point " + (sampleSize + i) + ": " + (error * 100) + "%");
        }
    }
    
    public static double getConfidence(ForecastResult r, int last, int horizon) {
        double pred = r.getForecast()[0];
        double upper = r.getForecastUpperConf()[0];
        double lower = r.getForecastLowerConf()[0];
        System.out.println("Prediction: " + pred + ", Upper confidence: " + upper + " Lower confidence: " + lower);
        
        double width = (upper - lower) / last * 100;
        
        return Math.max(0, 100 - width);  // 95% CI → ~80-90 confiance
    }

}

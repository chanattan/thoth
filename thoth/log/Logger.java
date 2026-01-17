package thoth.log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Logger {

    private HashMap<String, Long> measures;
    private HashMap<String, Object> data;
    private HashMap<String, Integer> counts;
    private String timestamp;
    private final long session_id;

    public Logger() {
        measures = new HashMap<>();
        counts = new HashMap<>();
        data = new HashMap<>();
        Date d = new Date();
        d.setTime(System.currentTimeMillis());
        timestamp = d.toString();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        timestamp = formatter.format(d);
        System.out.println("Logger initialized at " + timestamp);
        session_id = System.getProperty("user.name").hashCode();
        System.out.println("Session ID: " + session_id);
    }

    public void startMeasure(String key) {
        measures.put(key, System.currentTimeMillis());
    }

    public void stopMeasure(String key) {
        if (measures.containsKey(key)) {
            long startTime = measures.get(key);
            long elapsed = System.currentTimeMillis() - startTime;
            logMeasure(key, elapsed);
        } else {
            System.err.println("[WARN] No measure started for key: " + key);
        }
    }

    private void logMeasure(String key, long value) {
        data.put(key, (Long) data.getOrDefault(key, 0L) + value);
        counts.put(key, counts.getOrDefault(key, 0) + 1);
        measures.remove(key);
    }

    /**
     * This functions prepares data by averaging all measures done, then exports all them.
     *  timestamp, session_id, trial_id, condition, event_type, trigger, ai_output, ai_uncertainty, explanation_variant, human_action, user_confidence, correct, decision_time_ms, slice_id, precondition_ok, abstained, fallback_used, notes

     */
    public void saveData() {
        // Prepare data
        for (String key : data.keySet()) {
            data.put(key, (Long) data.get(key) / counts.get(key));
        }

        data.put("session_id", session_id);
        data.put("timestamp", timestamp);
        
        // Placeholder for saving data to file or database
        System.out.println("> Saving log data...");
        for (String key : data.keySet()) {
            System.out.println(key + ": " + data.get(key));
        }
    }
    
}
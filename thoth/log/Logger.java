package thoth.log;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import thoth.simulator.Simulator;
import thoth.simulator.Thoth;

public class Logger {

    // État pour le schéma HAI complet
    private List<HAIEvent> haiEvents = new ArrayList<>();
    private long sessionId;
    private int trialId = 0;
    private int aiTrialId = 0;
    private String currentCondition = "H_plus_IA";
    private String currentTrigger = "on_demand";
    private String currentSliceId = "novice";

    private HashMap<String, Long> measures;
    private HashMap<String, Object> data;
    private String timestamp;

    private String lastShowHintUncertainty = "na";
    private String lastShowHintAiOutput = "none";
    

    // Classe interne pour les événements HAI (18 colonnes exactes)
    public static class HAIEvent {
        public final String timestamp;
        public final long sessionId;
        public final int trialId;
        public final String condition;
        public final String eventType; // show_hint, user_action, submit
        public final String trigger;
        public final String aiOutput; // shown, none
        public final String aiUncertainty; // low, mid, high, na
        public final String explanationVariant; // factors, contrastive, off, na
        public final String humanAction; // accept, override, ignore
        public final Double userConfidence; // 0.0-1.0
        public final String correct; // Y, N
        public final Long decisionTimeMs;
        public final String sliceId;
        public final Boolean preconditionOk;
        public final Boolean abstained;
        public final Boolean fallbackUsed;
        public final String notes;

        public HAIEvent(Builder builder) {
            this.timestamp = builder.timestamp;
            this.sessionId = builder.sessionId;
            this.trialId = builder.trialId;
            this.condition = builder.condition;
            this.eventType = builder.eventType;
            this.trigger = builder.trigger;
            this.aiOutput = builder.aiOutput;
            this.aiUncertainty = builder.aiUncertainty;
            this.explanationVariant = builder.explanationVariant;
            this.humanAction = builder.humanAction;
            this.userConfidence = builder.userConfidence;
            this.correct = builder.correct;
            this.decisionTimeMs = builder.decisionTimeMs;
            this.sliceId = builder.sliceId;
            this.preconditionOk = builder.preconditionOk;
            this.abstained = builder.abstained;
            this.fallbackUsed = builder.fallbackUsed;
            this.notes = builder.notes;
        }

        public String toCsv() {
            return String.format("%s,%d,%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,\"%s\"",
                timestamp, sessionId, trialId, condition, eventType, trigger,
                aiOutput, aiUncertainty, explanationVariant, humanAction,
                userConfidence, correct, decisionTimeMs, sliceId,
                preconditionOk != null ? preconditionOk.toString() : "",
                abstained != null ? abstained.toString() : "",
                fallbackUsed != null ? fallbackUsed.toString() : "",
                notes != null ? notes.replace(",", ";").replace("\"", "\"\"") : "");
        }

        public static class Builder {
            // Tous les champs avec valeurs par défaut null/empty
            public String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date());
            public long sessionId;
            public int trialId;
            public String condition = "H_plus_IA";
            public String eventType;
            public String trigger = "on_demand";
            public String aiOutput;
            public String aiUncertainty;
            public String explanationVariant;
            public String humanAction;
            public Double userConfidence;
            public String correct;
            public Long decisionTimeMs;
            public String sliceId = "novice";
            public Boolean preconditionOk = true;
            public Boolean abstained = false;
            public Boolean fallbackUsed = false;
            public String notes = "";

            public Builder sessionId(long sessionId) { this.sessionId = sessionId; return this; }
            public Builder trialId(int trialId) { this.trialId = trialId; return this; }
            public Builder condition(String condition) { this.condition = condition; return this; }
            public Builder eventType(String eventType) { this.eventType = eventType; return this; }
            public Builder trigger(String trigger) { this.trigger = trigger; return this; }
            public Builder sliceId(String sliceId) { this.sliceId = sliceId; return this; }
            public Builder aiOutput(String aiOutput) { this.aiOutput = aiOutput; return this; }
            public Builder aiUncertainty(String aiUncertainty) { this.aiUncertainty = aiUncertainty; return this; }
            public Builder explanationVariant(String explanationVariant) { this.explanationVariant = explanationVariant; return this; }
            public Builder humanAction(String humanAction) { this.humanAction = humanAction; return this; }
            public Builder userConfidence(Double userConfidence) { this.userConfidence = userConfidence; return this; }
            public Builder correct(String correct) { this.correct = correct; return this; }
            public Builder decisionTimeMs(Long decisionTimeMs) { this.decisionTimeMs = decisionTimeMs; return this; }
            public Builder preconditionOk(Boolean preconditionOk) { this.preconditionOk = preconditionOk; return this; }
            public Builder notes(String notes) { this.notes = notes; return this; }
            public Builder abstained(Boolean abstained) { this.abstained = abstained; return this; }
            public Builder fallbackUsed(Boolean fallbackUsed) { this.fallbackUsed = fallbackUsed; return this; }
            
            public HAIEvent build() { 
                if ("H_only".equals(condition)) {
                    aiOutput = "none";
                    aiUncertainty = "na";
                    explanationVariant = "off";
                } else {
                    explanationVariant = "factors"; // par défaut
                }

                if ("AI_only".equals(condition)) {
                    humanAction = "none";
                    userConfidence = null;
                    decisionTimeMs = null;
                }

                if ("high".equals(aiUncertainty) || !preconditionOk) {
                    abstained = true;
                    fallbackUsed = true;
                    if ("H_plus_IA".equals(condition)) aiOutput = "none";
                }
                return new HAIEvent(this); 
            }
        }
    }

    public Logger() {
        measures = new HashMap<>();
        data = new HashMap<>();
        timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date());
        sessionId =  System.getProperty("user.name").hashCode();
        System.out.println("HAI Logger initialized - Session ID: " + sessionId);
    }

    /** Nouvelle recommandation IA */
    public void logShowHint(String aiUncertainty, String explanationVariant, String aiOutput,
                           boolean preconditionOk, String notes) {
        trialId++;
        this.lastShowHintUncertainty = aiUncertainty;
        this.lastShowHintAiOutput = aiOutput;
        if (!aiOutput.equals("none")) {
            Simulator.lastHintTime = System.currentTimeMillis();
            Simulator.lastHintStep = Thoth.instance.window.getSimulator().getTime();
        }

        HAIEvent.Builder builder = new HAIEvent.Builder()
            .sessionId(sessionId)
            .trialId(trialId)
            .condition("H_plus_IA")
            .eventType("show_hint")
            .aiUncertainty(lastShowHintUncertainty)
            .aiOutput(lastShowHintAiOutput)
            .trigger(currentTrigger)
            .sliceId(currentSliceId)
            .aiUncertainty(aiUncertainty)
            .explanationVariant(explanationVariant)
            .preconditionOk(preconditionOk)
            .notes(notes);

        haiEvents.add(builder.build());
        System.out.println("v show_hint #" + trialId + " (uncertainty=" + aiUncertainty + ")");
    }

    /** Hover IA */
    public void logPassiveShowHint(String aiUncertainty, String explanationVariant, String aiOutput,
                           boolean preconditionOk, String notes) {

        HAIEvent.Builder builder = new HAIEvent.Builder()
            .sessionId(sessionId)
            .trialId(trialId)
            .condition("H_plus_IA")
            .eventType("hover_prediction")
            .aiUncertainty(lastShowHintUncertainty)
            .aiOutput(lastShowHintAiOutput)
            .trigger(currentTrigger)
            .sliceId(currentSliceId)
            .aiUncertainty(aiUncertainty)
            .explanationVariant(explanationVariant)
            .preconditionOk(preconditionOk)
            .notes(notes);

        haiEvents.add(builder.build());
    }

    /** Décision utilisateur */
    private String lastEventType = "";
    public void logUserAction(String humanAction, float userConfidence, String notes) {
        long decisionTimeMs;
        boolean human_p_IA = Thoth.instance.window.sim.popup != null && Thoth.instance.window.getSimulator().getTime() - Simulator.lastHintStep <= 3;
        if (human_p_IA) {
             decisionTimeMs = Simulator.lastHintTime > 0 ? System.currentTimeMillis() - Simulator.lastHintTime : 0L;
        } else {
             decisionTimeMs = this.measures.getOrDefault("decision_time_ms", 0L); // Human only
             // incrémente seulement si c'est un new trial H_only
            if (!"user_action".equals(lastEventType)) {
                this.trialId++;
            }
        }

        HAIEvent.Builder builder = new HAIEvent.Builder()
            .sessionId(sessionId)
            .trialId(trialId)
            .condition(human_p_IA ? "H_plus_IA" : "H_only")
            .eventType("user_action")
            .aiUncertainty(human_p_IA ? lastShowHintUncertainty : "na")
            .aiOutput(human_p_IA ? lastShowHintAiOutput : "none")
            .userConfidence((double) userConfidence)
            .trigger(currentTrigger)
            .sliceId(currentSliceId)
            .humanAction(humanAction)
            .decisionTimeMs(decisionTimeMs)
            .notes(notes);
        
        haiEvents.add(builder.build());

        lastEventType = "user_action";

        System.out.println("> user_action #" + trialId + " (" + humanAction + ", " + decisionTimeMs + "ms)");
    }

    /** Évaluation utilisateur */
    public void logSubmit(double userConfidence, String correct, String notes) {
        HAIEvent.Builder builder = new HAIEvent.Builder()
            .sessionId(sessionId)
            .trialId(trialId)
            .condition("H_plus_IA")
            .eventType("submit")
            .aiUncertainty(lastShowHintUncertainty)
            .aiOutput(lastShowHintAiOutput)
            .trigger(currentTrigger)
            .sliceId(currentSliceId)
            .userConfidence(userConfidence)
            .correct(correct)
            .notes(notes);
        
        haiEvents.add(builder.build());

        System.out.println("> submit #" + trialId + " (confidence=" + userConfidence + ", correct=" + correct + ")");
    }

    /** Évaluation IA */
    public void logAI(double aiConfidence, String correct, String notes) {
        aiTrialId++;
        HAIEvent.Builder builder = new HAIEvent.Builder()
            .sessionId(sessionId)
            .trialId(aiTrialId)
            .condition("AI_only")
            .eventType("submit")
            .aiUncertainty(aiConfidence >= 3 ? "high" : (aiConfidence >= 2 ? "mid" : "low"))
            .aiOutput("shown") // pour IA
            .trigger(currentTrigger)
            .sliceId(currentSliceId)
            .correct(correct)
            .notes(notes);
        
        haiEvents.add(builder.build());

        System.out.println("> AI_only submit #" + trialId + " (confidence=" + aiConfidence + ", correct=" + correct + ")");
    }

    public void setCondition(String condition) { this.currentCondition = condition; }
    public void setTrigger(String trigger) { this.currentTrigger = trigger; }
    public void setSliceId(String sliceId) { this.currentSliceId = sliceId; }

    public void startMeasure(String key) {
        measures.put(key, System.currentTimeMillis());
    }
    
    public void stopMeasure(String key) {
        if (measures.containsKey(key)) {
            long elapsed = System.currentTimeMillis() - measures.get(key);
            measures.put(key, elapsed);
        }
    }

    /** EXPORT CSV HAI */
    public void saveHAIData(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("timestamp,session_id,trial_id,condition,event_type,trigger," +
                          "ai_output,ai_uncertainty,explanation_variant,human_action," +
                          "user_confidence,correct,decision_time_ms,slice_id,precondition_ok," +
                          "abstained,fallback_used,notes");

            for (HAIEvent event : haiEvents) {
                writer.println(event.toCsv());
            }
            writer.println("Player score: " + Thoth.instance.player.getCapital());
            System.out.println("✅ HAI CSV exporté: " + filename + " (" + haiEvents.size() + " lignes)");
        } catch (IOException e) {
            System.err.println("Erreur export CSV: " + e.getMessage());
        }
    }

    public void saveData() {
        saveHAIData("events_sample.csv");
    }

    public int getTrialCount() { return trialId; }
    public List<HAIEvent> getEvents() { return new ArrayList<>(haiEvents); }
}

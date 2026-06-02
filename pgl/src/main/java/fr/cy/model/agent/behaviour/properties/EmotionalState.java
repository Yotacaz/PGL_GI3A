package fr.cy.model.agent.behaviour.properties;

import java.util.Optional;

public enum EmotionalState {
    /** Agent is calm and behaves normally, reducing stress to others around them */
    CALM(0.0f, 0.5f, -0.01),
    /** Agent is selfish and may start pushing others to escape faster */
    SELFISH(0.5f, 1f, 0.1),
    /** Agent is panicking and may cause more stress to others around it */
    PANICKING(1f, Double.MAX_VALUE, 0.5);

    private final double minStressLevel;
    private final double maxStressLevel;
    private final double stressInducedToOthers;

    private EmotionalState(double minStressLevel, double maxStressLevel, double stressInducedToOthers) {
        this.minStressLevel = minStressLevel;
        this.maxStressLevel = maxStressLevel;
        this.stressInducedToOthers = stressInducedToOthers;
    }

    public double getMaxStressLevel() {
        return maxStressLevel;
    }

    public double getMinStressLevel() {
        return minStressLevel;
    }

    public double getStressInducedToOthers() {
        return stressInducedToOthers;
    }

    public static Optional<EmotionalState> fromdouble(double stressLevel, double stressTolerance) {
        for (EmotionalState state : EmotionalState.values()) {
            if (stressLevel >= state.getMinStressLevel() * stressTolerance
                    && stressLevel < state.getMaxStressLevel() * stressTolerance) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }

    public EmotionalState nextState() {
        switch (this) {
            case CALM:
                return SELFISH;
            case SELFISH:
                return PANICKING;
            case PANICKING:
                return PANICKING; // No next state, remains panicking
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }

}

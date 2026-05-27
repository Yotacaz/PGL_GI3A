package fr.cy.model.agent.behaviour.properties;

import java.util.Optional;

public enum EmotionalState {
    /** Agent is calm and behaves normally */
    CALM(0.0f, 0.5f),
    /** Agent is selfish and may start pushing others to escape faster */
    SELFISH(0.5f, 1f),
    /** Agent is panicking and may cause more stress to others around it */
    PANICKING(1f, Double.MAX_VALUE);

    private final double minStressLevel;
    private final double maxStressLevel;

    private EmotionalState(double minStressLevel, double maxStressLevel) {
        this.minStressLevel = minStressLevel;
        this.maxStressLevel = maxStressLevel;
    }

    public double getMaxStressLevel() {
        return maxStressLevel;
    }

    public double getMinStressLevel() {
        return minStressLevel;
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

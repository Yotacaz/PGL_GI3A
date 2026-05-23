package fr.cy.model.agent;

import java.util.Optional;

public enum AgentState {
    /** Agent is calm and behaves normally */
    CALM(0.0f, 0.5f),
    /** Agent is selfish and may start pushing others to escape faster */
    SELFISH(0.5f, 1f),
    /** Agent is panicking and may cause more stress to others around it */
    PANICKING(1f, Double.MAX_VALUE);

    private final double minStressLevel;
    private final double maxStressLevel;

    private AgentState(double minStressLevel, double maxStressLevel) {
        this.minStressLevel = minStressLevel;
        this.maxStressLevel = maxStressLevel;
    }

    public double getMaxStressLevel() {
        return maxStressLevel;
    }

    public double getMinStressLevel() {
        return minStressLevel;
    }

    public static Optional<AgentState> fromdouble(double stressLevel, double stressTolerance) {
        for (AgentState state : AgentState.values()) {
            if (stressLevel >= state.getMinStressLevel() * stressTolerance
                    && stressLevel < state.getMaxStressLevel() * stressTolerance) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }

    public AgentState nextState() {
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

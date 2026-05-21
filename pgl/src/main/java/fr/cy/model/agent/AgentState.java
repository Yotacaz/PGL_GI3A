package fr.cy.model.agent;

import java.util.Optional;

public enum AgentState {
    CALM(0.0f, 0.5f),
    SELFISH(0.5f, 1f),
    PANICKING(1f, Float.MAX_VALUE);

    private final float minStressLevel;
    private final float maxStressLevel;

    private AgentState(float minStressLevel, float maxStressLevel) {
        this.minStressLevel = minStressLevel;
        this.maxStressLevel = maxStressLevel;
    }

    public float getMaxStressLevel() {
        return maxStressLevel;
    }

    public float getMinStressLevel() {
        return minStressLevel;
    }

    public static Optional<AgentState> fromFloat(float stressLevel, float stressTolerance) {
        for (AgentState state : AgentState.values()) {
            if (stressLevel >= state.getMinStressLevel()*stressTolerance && stressLevel < state.getMaxStressLevel()*stressTolerance) {
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

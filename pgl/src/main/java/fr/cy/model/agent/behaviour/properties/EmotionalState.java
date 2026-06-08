package fr.cy.model.agent.behaviour.properties;

import java.util.Optional;

public enum EmotionalState {
    /**
     * Agent is calm and behaves normally, reducing stress to others around them.
     * This is the default state when stress levels are low.
     */
    CALM(0.0f, 0.3f, -0.05),
    /**
     * Agent is selfish and may start pushing others to escape faster.
     * This state occurs at moderate stress levels where the agent becomes
     * more focused on personal survival.
     */
    SELFISH(0.3f, 0.7f, 0.05),
    /**
     * Agent is panicking and may cause more stress to others around it.
     * This is the highest stress state where the agent's behavior becomes
     * erratic and potentially dangerous to others.
     */
    PANICKING(0.7f, 1.01d, 0.2);

    /** The minimum stress level required to enter this emotional state */
    private final double minStressLevel;
    
    /** The maximum stress level before transitioning to the next emotional state */
    private final double maxStressLevel;
    
    /** The amount of stress this emotional state induces to other agents */
    private final double stressInducedToOthers;

    /**
     * Creates an emotional state with the specified stress level boundaries and impact.
     * 
     * @param minStressLevel the minimum stress level for this state
     * @param maxStressLevel the maximum stress level for this state
     * @param stressInducedToOthers the stress impact on other agents
     */
    private EmotionalState(double minStressLevel, double maxStressLevel, double stressInducedToOthers) {
        this.minStressLevel = minStressLevel;
        this.maxStressLevel = maxStressLevel;
        this.stressInducedToOthers = stressInducedToOthers;
    }

    /**
     * Gets the maximum stress level for this emotional state.
     * 
     * @return the maximum stress level before transitioning to the next state
     */
    public double getMaxStressLevel() {
        return maxStressLevel;
    }

    /**
     * Gets the minimum stress level for this emotional state.
     * 
     * @return the minimum stress level required to enter this state
     */
    public double getMinStressLevel() {
        return minStressLevel;
    }

    /**
     * Gets the amount of stress this emotional state induces to other agents.
     * 
     * @return the stress impact on other agents (can be negative for calming states)
     */
    public double getStressInducedToOthers() {
        return stressInducedToOthers;
    }

    /**
     * Converts a stress level value to the corresponding emotional state.
     * 
     * @param stressLevel the stress level to convert (0.0 to 1.0)
     * @return an Optional containing the corresponding emotional state,
     *         or empty if the stress level doesn't match any state
     */
    public static Optional<EmotionalState> fromdouble(double stressLevel) {
        for (EmotionalState state : EmotionalState.values()) {
            if (stressLevel >= state.getMinStressLevel()
                    && stressLevel < state.getMaxStressLevel()) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }

    /**
     * Gets the next emotional state in the progression.
     * 
     * <p>The emotional states progress as follows: CALM → SELFISH → PANICKING.
     * Once an agent reaches PANICKING, it remains in that state.</p>
     * 
     * @return the next emotional state in the progression
     */
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

package fr.cy.model.agent.behaviour.properties;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Objects;
import java.util.Set;

import fr.cy.model.agent.behaviour.personalityTraits.AgentPersonalityTrait;

public class AgentDecisionalProperties implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    /** Current state of the agent, which can be CALM, SELFISH, or PANICKING */
    private EmotionalState state = EmotionalState.CALM;
    /** Stress level of the agent, between 0 and 1 */
    private double stressLevel = 0.0;
    /**
     * Tolerance to stress, between 0 and 1, above which the agent starts panicking
     */
    private double stressTolerance;

    /**
     * Tolerance to crowding, between 0 and 1, above which the agent starts
     * panicking
     */
    private double congestionTolerance;

    /**
     * Factor representing the agent's own decision-making ability, between 0 and
     * 1, where 0 means the agent will always follow the crowd
     */
    private double baseOwnDecisionMakingFactor;

    /** Score multiplier for repeating the last decision */
    private double repeatLastDecisionFactor = 0.0;

    /**
     * Maximum accumulated stress experienced by the agent during its journey, used
     * for statistics
     */
    private double maxAccumulatedStress = 0;

    /** List of personality traits that can influence the agent's behavior */
    private final Set<AgentPersonalityTrait> personalityTraits = new HashSet<>(); // TODO: implement feature

    public AgentDecisionalProperties(int agentId, double stressTolerance, double baseOwnDecisionMakingFactor,
            double repeatLastDecisionTendency, double congestionTolerance) {
        this.id = agentId;
        this.stressTolerance = stressTolerance;
        this.baseOwnDecisionMakingFactor = baseOwnDecisionMakingFactor;
        this.repeatLastDecisionFactor = repeatLastDecisionTendency;
        this.congestionTolerance = congestionTolerance;
    }

    /**
     * Update the state of the agent based on its current stress level and
     * tolerance.
     * 
     * @return the new state of the agent after the update
     */
    public EmotionalState updateEmotionnalState() {
        Optional<EmotionalState> optState = EmotionalState.fromdouble(stressLevel, stressTolerance);
        if (optState.isEmpty()) {
            System.err.println("Warning: Agent " + id + " has an invalid stress level of " + stressLevel
                    + " with a tolerance of " + stressTolerance + ". Defaulting to PANICKING state.");
        }
        state = optState.orElse(EmotionalState.PANICKING); // Default to PANICKING if no state matches
        return state;
    }

    /**
     * @return the current state of the agent
     */
    public EmotionalState getEmotionnalState() {
        return state;
    }

    public double getStressTolerance() {
        return stressTolerance;
    }

    public void setStressLevel(double stressLevel) {
        this.stressLevel = stressLevel;
        if (stressLevel > maxAccumulatedStress) {
            maxAccumulatedStress = stressLevel;
        }
    }

    public double getStressLevel() {
        return stressLevel;
    }

    /**
     * @return the base own decision-making factor (0..1)
     */
    public double getBaseOwnDecisionMakingFactor() {
        return baseOwnDecisionMakingFactor;
    }

    public double getCurrentOwnDecisionMakingFactor() {
        return baseOwnDecisionMakingFactor * stressLevel;
    }

    public double getMaxAccumulatedStress() {
        return maxAccumulatedStress;
    }

    public double getRepeatLastDecisionFactor() {
        return repeatLastDecisionFactor;
    }

    public void setRepeatLastDecisionFactor(double repeatLastDecisionTendency) {
        this.repeatLastDecisionFactor = repeatLastDecisionTendency;
    }

    /** @return the congestion tolerance (0..1) */
    public double getCongestionTolerance() {
        return congestionTolerance;
    }

    public void setCongestionTolerance(double congestionTolerance) {
        this.congestionTolerance = congestionTolerance;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        AgentDecisionalProperties other = (AgentDecisionalProperties) obj;
        return id == other.id && Double.compare(stressLevel, other.stressLevel) == 0
                && Double.compare(stressTolerance, other.stressTolerance) == 0
                && Double.compare(congestionTolerance, other.congestionTolerance) == 0
                && Double.compare(baseOwnDecisionMakingFactor, other.baseOwnDecisionMakingFactor) == 0
                && Double.compare(maxAccumulatedStress, other.maxAccumulatedStress) == 0
                && state == other.state && Objects.equals(personalityTraits, other.personalityTraits);
    }

    @Override
    public String toString() {
        return "AgentDecisionalProperties{" +
                "id=" + id +
                ", state=" + state +
                ", stressLevel=" + String.format("%.3f", stressLevel) +
                ", stressTolerance=" + String.format("%.3f", stressTolerance) +
                ", congestionTolerance=" + String.format("%.3f", congestionTolerance) +
                ", baseOwnDecisionMakingFactor=" + String.format("%.3f", baseOwnDecisionMakingFactor) +
                ", maxAccumulatedStress=" + String.format("%.3f", maxAccumulatedStress) +
                '}';
    }
}

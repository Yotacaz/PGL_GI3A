package fr.cy.model.agent.behaviour.properties;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import fr.cy.model.agent.behaviour.personalityTraits.AgentPersonalityTrait;

public class AgentDecisionalProperties {
    private int id;
    /** Current state of the agent, which can be CALM, SELFISH, or PANICKING */
    private EmotionalState state = EmotionalState.CALM;
    /** Stress level of the agent, between 0 and 1 */
    private double stressLevel = 0.0;
    /** Tolerance to stress, between 0 and 1, above which the agent starts panicking */
    private double stressTolerance;

    /** Factor representing the agent's own decision-making ability, between 0 and
     *  1, where 0 means the agent will always follow the crowd */
    private double baseOwnDecisionMakingFactor;


    /** List of personality traits that can influence the agent's behavior */
    private final Set<AgentPersonalityTrait> personalityTraits = new HashSet<>(); //TODO: implement feature


    public AgentDecisionalProperties(int agentId, double stressTolerance, double baseOwnDecisionMakingFactor) {
        this.id = agentId;
        this.stressTolerance = stressTolerance;
        this.baseOwnDecisionMakingFactor = baseOwnDecisionMakingFactor;
    }

    /**
     * Update the state of the agent based on its current stress level and
     * tolerance.
     * 
     * @return the new state of the agent after the update
     */
    public EmotionalState updateState() {
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
    public EmotionalState getState() {
        return state;
    }

    public double getStressTolerance() {
        return stressTolerance;
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

    
}

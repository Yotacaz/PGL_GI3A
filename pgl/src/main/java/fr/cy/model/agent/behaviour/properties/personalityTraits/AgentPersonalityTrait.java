package fr.cy.model.agent.behaviour.properties.personalityTraits;

import java.util.Map;
import java.util.Objects;

import fr.cy.model.agent.behaviour.decisions.AgentPossibleDecision;

public class AgentPersonalityTrait {
    public AgentPersonalityTrait() {
    }

    /** Method to evaluate if a possible decision is acceptable based on the trait's influence on the agent's behavior. 
     * By default, it returns true, meaning the trait does not restrict any decisions. 
     * Specific traits can override this method to implement their own logic for evaluating decisions.
     * @param decisionScores A map of possible decisions and their associated scores, which can be used to evaluate the decision based on the trait's influence.
     * @return true if the decision is acceptable for the agent with this trait, false otherwise
     */
    public boolean evaluatePossibleDecision(Map<AgentPossibleDecision, Double> decisionScores) {
        // Default implementation: always return true
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getClass());
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj != null && this.getClass() == obj.getClass());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}

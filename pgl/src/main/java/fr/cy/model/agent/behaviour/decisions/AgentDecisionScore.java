package fr.cy.model.agent.behaviour.decisions;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Holds the aggregated score for a possible decision and its weighted
 * neighboring graph-element choices.
 */
public abstract class AgentDecisionScore<T> implements Serializable {
    /** Serial version UID for serialization compatibility */
    private static final long serialVersionUID = 1L;

    /** The aggregated score for the decision, representing its overall desirability */
    private final double decisionScore;
    /** A map of neighboring graph elements to their associated scores, representing the agent's preferences for those elements */
    private final Map<T, Double> preferredNeighboringElements;
    /** The total score for the preferred neighboring graph elements, used for weighted selection */
    private final double totalScoreForPreferredNeighboringElements;

    /**
     * Creates a new AgentDecisionScore with the specified decision score, preferred neighboring elements, and total score for those elements.
     *
     * @param decisionScore the aggregated score for the decision
     * @param preferredNeighboringElements a map of neighboring graph elements to their associated scores, representing the agent's preferences
     * @param totalScoreForPreferredNeighboringElements the sum of the scores for the preferred neighboring elements, used for weighted selection
     */
    AgentDecisionScore(double decisionScore, Map<T, Double> preferredNeighboringElements,
            double totalScoreForPreferredNeighboringElements) {
        this.decisionScore = Math.max(0.0, decisionScore);
        this.preferredNeighboringElements = Objects.requireNonNull(preferredNeighboringElements,
                "preferredNeighboringElements");
        this.totalScoreForPreferredNeighboringElements = Math.max(0.0, totalScoreForPreferredNeighboringElements);
    }

    /**
     * Gets the score for this decision.
     * @return the decision score
     */
    public double getScore() {
        return decisionScore;
    }

    /**
     * Gets the map of preferred neighboring graph elements and their associated scores.
     * @return an unmodifiable map of preferred neighboring elements to their scores
     */
    public Map<T, Double> getPreferredNeighboringElements() {
        return Collections.unmodifiableMap(preferredNeighboringElements);
    }

    /**
     * Gets the total score for the preferred neighboring graph elements.
     * @return the total score for preferred neighboring elements
     */
    public double getTotalScoreForPreferredNeighboringElements() {
        return totalScoreForPreferredNeighboringElements;
    }

    @Override
    public int hashCode() {
        return Objects.hash(decisionScore, preferredNeighboringElements, totalScoreForPreferredNeighboringElements);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AgentDecisionScore<?> other = (AgentDecisionScore<?>) obj;
        return Double.compare(decisionScore, other.decisionScore) == 0
                && Objects.equals(preferredNeighboringElements, other.preferredNeighboringElements)
                && Double.compare(totalScoreForPreferredNeighboringElements,
                        other.totalScoreForPreferredNeighboringElements) == 0;
    }

    @Override
    public String toString() {
        return "AgentDecisionScore{" +
                "score=" + decisionScore +
                ", preferredElements=" + preferredNeighboringElements.keySet() +
                ", totalPreferredScore=" + totalScoreForPreferredNeighboringElements +
                '}';
    }
}

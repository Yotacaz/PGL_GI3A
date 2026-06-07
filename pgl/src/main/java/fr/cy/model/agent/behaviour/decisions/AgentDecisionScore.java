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
    private static final long serialVersionUID = 1L;

    private final double decisionScore;
    private final Map<T, Double> preferredNeighboringElements;
    private final double totalScoreForPreferredNeighboringElements;

    AgentDecisionScore(double decisionScore, Map<T, Double> preferredNeighboringElements,
            double totalScoreForPreferredNeighboringElements) {
        this.decisionScore = Math.max(0.0, decisionScore);
        this.preferredNeighboringElements = Objects.requireNonNull(preferredNeighboringElements,
                "preferredNeighboringElements");
        this.totalScoreForPreferredNeighboringElements = Math.max(0.0, totalScoreForPreferredNeighboringElements);
    }

    public double getScore() {
        return decisionScore;
    }

    public Map<T, Double> getPreferredNeighboringElements() {
        return Collections.unmodifiableMap(preferredNeighboringElements);
    }

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

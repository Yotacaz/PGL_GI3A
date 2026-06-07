package fr.cy.model.agent.behaviour.decisions;

import java.util.Map;

import fr.cy.model.graph.element.Edge;

/**
 * Holds the aggregated score for a possible decision and whether the
 * decision is considered valid in the current context.
 */
public class AgentNodeDecisionScore extends AgentDecisionScore<Edge> {
    private static final long serialVersionUID = 1L;

    AgentNodeDecisionScore(double totalScore, Map<Edge, Double> preferredNeighboringEdges,
            double totalScoreForPreferredNeighboringEdges) {
        super(totalScore, preferredNeighboringEdges, totalScoreForPreferredNeighboringEdges);
    }

    /** Get the map of preferred neighboring edges and their associated scores. */
    public Map<Edge, Double> getPreferredNeighboringEdges() {
        return getPreferredNeighboringElements();
    }

    public double getTotalScoreForPreferredNeighboringEdges() {
        return getTotalScoreForPreferredNeighboringElements();
    }
}

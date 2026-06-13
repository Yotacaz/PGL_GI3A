package fr.cy.model.agent.behaviour.decisions;

import java.util.Map;

import fr.cy.model.graph.element.Edge;

/**
 * Represents the score and associated data for a node-based decision that an agent can make.
 * 
 * <p>This class extends {@link AgentPossibleDecisionScore} to provide specific functionality
 * for decisions made at nodes, including information about preferred neighboring edges
 * that the agent might consider when moving from the current node.</p>
 */
public class AgentPossibleNodeDecisionScore extends AgentPossibleDecisionScore<Edge> {
    private static final long serialVersionUID = 1L;

    AgentPossibleNodeDecisionScore(double totalScore, Map<Edge, Double> preferredNeighboringEdges,
            double totalScoreForPreferredNeighboringEdges) {
        super(totalScore, preferredNeighboringEdges, totalScoreForPreferredNeighboringEdges);
    }

    /**
     * Gets the map of preferred neighboring edges and their associated scores.
     * 
     * <p>This provides information about which edges the agent is most likely to
     * consider when making a decision at the current node.</p>
     * 
     * @return a map of edges to their preference scores
     */
    public Map<Edge, Double> getPreferredNeighboringEdges() {
        return getPreferredNeighboringElements();
    }

    /**
     * Gets the total score for all preferred neighboring edges.
     * 
     * @return the sum of scores for all preferred neighboring edges
     */
    public double getTotalScoreForPreferredNeighboringEdges() {
        return getTotalScoreForPreferredNeighboringElements();
    }
}

package fr.cy.model.agent.behaviour.decisions;

import java.util.Map;

import fr.cy.model.graph.element.Edge;

/**
 * Holds the aggregated score for a possible decision and whether the
 * decision is considered valid in the current context.
 */
public class AgentDecisionScore {
    private double decisionScore = 0.0;
    private boolean isValid = true;
    /** A map of preferred neighboring edges and their associated scores */
    private Map<Edge, Double> preferredNeighboringEdges;
    private double totalScoreForPreferredNeighboringEdges;
    public AgentDecisionScore(double totalScore, Map<Edge, Double> preferredNeighboringEdges, double totalScoreForPreferredNeighboringEdges) {
        this.decisionScore = totalScore;
        this.preferredNeighboringEdges = preferredNeighboringEdges;
        this.totalScoreForPreferredNeighboringEdges = totalScoreForPreferredNeighboringEdges;
    }

    // public AgentDecisionScore(double totalScore, Map<Edge, Double> preferredNeighboringEdges) {
    //     this(totalScore, true, preferredNeighboringEdges);
    // }

    /** Add a partial score to the total. */
    public void addScore(double score) {
        this.decisionScore += score;
    }

    /** Get the aggregated score. */
    public double getScore() {
        return decisionScore;
    }

    /** True if the decision is currently valid for selection. */
    public boolean isValid() {
        return isValid;
    }

    /** Get the map of preferred neighboring edges and their associated scores. */
    public Map<Edge, Double> getPreferredNeighboringEdges() {
        return preferredNeighboringEdges;
    }

    public double getTotalScoreForPreferredNeighboringEdges() {
        return totalScoreForPreferredNeighboringEdges;
    }
}

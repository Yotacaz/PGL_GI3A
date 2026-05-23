package fr.cy.model.agent.decisions;

/**
 * Holds the aggregated score for a possible decision and whether the
 * decision is considered valid in the current context.
 */
public class AgentDecisionScore {
    private double totalScore = 0.0;
    private boolean isValid;

    public AgentDecisionScore(double totalScore, boolean isValid) {
        this.totalScore = totalScore;
        this.isValid = isValid;
    }

    /** Add a partial score to the total. */
    public void addScore(double score) {
        this.totalScore += score;
    }

    /** Get the aggregated score. */
    public double getScore() {
        return totalScore;
    }

    /** True if the decision is currently valid for selection. */
    public boolean isValid() {
        return isValid;
    }
}

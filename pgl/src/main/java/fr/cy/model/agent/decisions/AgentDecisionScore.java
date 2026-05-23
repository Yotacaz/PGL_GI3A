package fr.cy.model.agent.decisions;

public class AgentDecisionScore {
    private double totalScore = 0.0;
    private boolean isValid;
    public AgentDecisionScore(double totalScore, boolean isValid) {
        this.totalScore = totalScore;
        this.isValid = isValid;
    }

    public void addScore(double score) {
        this.totalScore += score;
    }

    public double getScore() {
        return totalScore;
    }

    public boolean isValid() {
        return isValid;
    }
}

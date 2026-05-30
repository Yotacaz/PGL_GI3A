package fr.cy.model.agent.behaviour.decisions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.behaviour.agentActions.AgentAction;
import fr.cy.model.agent.behaviour.agentActions.FollowSingleEdgeAction;
import fr.cy.model.agent.behaviour.properties.AgentDecisionalProperties;
import fr.cy.model.graph.CongestionStats;
import fr.cy.model.graph.element.Edge;

public enum AgentPossibleDecision {
    

    FOLLOW_CROWD {
        @Override
        public AgentDecisionScore computeScore(DecisionNodeContext context, AgentDecisionalProperties agentState,
                double decisionMakingFactor, AgentPossibleDecision lastDecision, AgentAction lastAction) {
            //get 
            CongestionStats<Edge> congestionStats = context.getCongestionStatsForOutgoingEdges();
            Map<Edge, Double> preferredNeighboringEdges = new HashMap<>();
            double totalScoreForPreferredNeighboringEdges = 0.0;
            for (Edge edge : context.getOutgoingEdges()) {
                //edgeScore is 0 if no one on edge
                double edgeScore = edge.getCongestion()
                        / (1.0 + edge.getTotalStressInducedIncludingNeighbors() * 0.125); //prefer more crowded edges
                preferredNeighboringEdges.put(edge, edgeScore);
                totalScoreForPreferredNeighboringEdges += edgeScore;
            }
            double decisionScore = (congestionStats.getAverageCongestionLevel()* agentState.getCongestionTolerance()
                    - agentState.getCurrentOwnDecisionMakingFactor()) * decisionMakingFactor; //can be 0 if no crowd
            if (lastDecision == this) {
                decisionScore *= agentState.getRepeatLastDecisionFactor(); //prefer to repeat last decision if it was the same
            }
            return new AgentDecisionScore(decisionScore, preferredNeighboringEdges,
                    totalScoreForPreferredNeighboringEdges);
        }

        @Override
        public AgentAction toAgentAction(DecisionNodeContext context, Agent agent, AgentDecisionScore decisionScore) {
            Map<Edge, Double> preferredEdges = decisionScore.getPreferredNeighboringEdges();
            double totalScore = decisionScore.getTotalScoreForPreferredNeighboringEdges();
            Edge chosenEdge = selectEdgeBasedOnScores(preferredEdges, totalScore);
            return new FollowSingleEdgeAction(agent, chosenEdge);

        }
    },
    FOLLOW_LESS_CROWDED_PATH {
        @Override
        public AgentDecisionScore computeScore(DecisionNodeContext context, AgentDecisionalProperties agentState,
                double decisionMakingFactor, AgentPossibleDecision lastDecision, AgentAction lastAction) {
            CongestionStats<Edge> congestionStats = context.getCongestionStatsForOutgoingEdges();
            Map<Edge, Double> preferredNeighboringEdges = new HashMap<>();
            double totalScoreForPreferredNeighboringEdges = 0.0;
            for (Edge edge : context.getOutgoingEdges()) {
                double edgeScore = (1.0 - edge.getCongestion())
                        / (1.0 + edge.getTotalStressInducedIncludingNeighbors() * 0.125); //prefer less crowded edges
                preferredNeighboringEdges.put(edge, edgeScore);
                totalScoreForPreferredNeighboringEdges += edgeScore;
            }
            double decisionScore = (-congestionStats.getAverageCongestionLevel()
                    - agentState.getCurrentOwnDecisionMakingFactor()) * decisionMakingFactor;
            if (lastDecision == this) {
                decisionScore *= agentState.getRepeatLastDecisionFactor();
            }
            return new AgentDecisionScore(decisionScore, preferredNeighboringEdges,
                    totalScoreForPreferredNeighboringEdges);
        }

        @Override
        public AgentAction toAgentAction(DecisionNodeContext context, Agent agent, AgentDecisionScore decisionScore) {
            Map<Edge, Double> preferredEdges = decisionScore.getPreferredNeighboringEdges();
            double totalScore = decisionScore.getTotalScoreForPreferredNeighboringEdges();
            Edge chosenEdge = selectEdgeBasedOnScores(preferredEdges, totalScore);
            return new FollowSingleEdgeAction(agent, chosenEdge);
        }
    },
    FOLLOW_RECOMMENDED_PATH {
        @Override
        public AgentDecisionScore computeScore(DecisionNodeContext context, AgentDecisionalProperties agentState,
                double decisionMakingFactor, AgentPossibleDecision lastDecision, AgentAction lastAction) {
            Map<Edge, Double> preferredNeighboringEdges = new HashMap<>();
            double totalScoreForPreferredNeighboringEdges = 0.0;
            if (context.getRecommendedPath() != null) {
                List<Edge> recommendedPathEdges = context.getRecommendedPath().getEdges();
                for (Edge edge : context.getOutgoingEdges()) {
                    double edgeScore = recommendedPathEdges.contains(edge) ? 1.0 : 0.0; //prefer edges in the recommended path
                    preferredNeighboringEdges.put(edge, edgeScore);
                    totalScoreForPreferredNeighboringEdges += edgeScore;
                }
            }
            double decisionScore = ((context.getRecommendedPath() != null ? 1.0 : 0.0)
                    - agentState.getCurrentOwnDecisionMakingFactor()) * decisionMakingFactor;
            if (lastDecision == this) {
                decisionScore *= agentState.getRepeatLastDecisionFactor();
            }
            return new AgentDecisionScore(decisionScore, preferredNeighboringEdges,
                    totalScoreForPreferredNeighboringEdges);
        }

        @Override
        public AgentAction toAgentAction(DecisionNodeContext context, Agent agent, AgentDecisionScore decisionScore) {
            Map<Edge, Double> preferredEdges = decisionScore.getPreferredNeighboringEdges();
            double totalScore = decisionScore.getTotalScoreForPreferredNeighboringEdges();
            Edge chosenEdge = selectEdgeBasedOnScores(preferredEdges, totalScore);
            return new FollowSingleEdgeAction(agent, chosenEdge);
        }

    },
    RANDOM {
        @Override
        public AgentDecisionScore computeScore(DecisionNodeContext context, AgentDecisionalProperties agentState,
                double decisionMakingFactor, AgentPossibleDecision lastDecision, AgentAction lastAction) {
            Map<Edge, Double> preferredNeighboringEdges = new HashMap<>();
            double totalScoreForPreferredNeighboringEdges = 0.0;
            for (Edge edge : context.getOutgoingEdges()) {
                double edgeScore = Math.random(); //random score for each edge
                preferredNeighboringEdges.put(edge, edgeScore);
                totalScoreForPreferredNeighboringEdges += edgeScore;
            }
            double decisionScore = (Math.random() + agentState.getStressLevel()) * decisionMakingFactor;
            if (lastDecision == this) {
                decisionScore *= agentState.getRepeatLastDecisionFactor();
            }
            return new AgentDecisionScore(decisionScore, preferredNeighboringEdges,
                    totalScoreForPreferredNeighboringEdges);
        }

        @Override
        public AgentAction toAgentAction(DecisionNodeContext context, Agent agent, AgentDecisionScore decisionScore) {
            Map<Edge, Double> preferredEdges = decisionScore.getPreferredNeighboringEdges();
            double totalScore = decisionScore.getTotalScoreForPreferredNeighboringEdges();
            Edge chosenEdge = selectEdgeBasedOnScores(preferredEdges, totalScore);
            return new FollowSingleEdgeAction(agent, chosenEdge);
        }
    },
    NICEST_PATH {
        @Override
        public AgentDecisionScore computeScore(DecisionNodeContext context, AgentDecisionalProperties agentState,
                double decisionMakingFactor, AgentPossibleDecision lastDecision, AgentAction lastAction) {
            Map<Edge, Double> preferredNeighboringEdges = new HashMap<>();
            double totalScoreForPreferredNeighboringEdges = 0.0;
            for (Edge edge : context.getOutgoingEdges()) {
                double edgeScore = 1.0 / (edge.getStressInducingFactor() + 1.0) - edge.getCongestion(); //prefer less congested edges
                preferredNeighboringEdges.put(edge, edgeScore);
                totalScoreForPreferredNeighboringEdges += edgeScore;
            }
            double decisionScore = (-context.getCongestionStatsForOutgoingEdges().getAverageCongestionLevel()
                    + agentState.getCurrentOwnDecisionMakingFactor()) * decisionMakingFactor;
            if (lastDecision == this) {
                decisionScore *= agentState.getRepeatLastDecisionFactor();
            }
            return new AgentDecisionScore(decisionScore, preferredNeighboringEdges,
                    totalScoreForPreferredNeighboringEdges);
        }

        @Override
        public AgentAction toAgentAction(DecisionNodeContext context, Agent agent, AgentDecisionScore decisionScore) {
            Map<Edge, Double> preferredEdges = decisionScore.getPreferredNeighboringEdges();
            double totalScore = decisionScore.getTotalScoreForPreferredNeighboringEdges();
            Edge chosenEdge = selectEdgeBasedOnScores(preferredEdges, totalScore);
            return new FollowSingleEdgeAction(agent, chosenEdge);
        }
    },
    FOLLOW_SHORTEST_PATH {
        @Override
        public AgentDecisionScore computeScore(DecisionNodeContext context, AgentDecisionalProperties agentState,
                double decisionMakingFactor, AgentPossibleDecision lastDecision, AgentAction lastAction) {
            Map<Edge, Double> preferredNeighboringEdges = new HashMap<>();
            double totalScoreForPreferredNeighboringEdges = 0.0;
            if (context.getRecommendedPath() != null) {
                List<Edge> recommendedPathEdges = context.getRecommendedPath().getEdges();
                for (Edge edge : context.getOutgoingEdges()) {
                    double edgeScore = recommendedPathEdges.contains(edge) ? 1.0 : 0.0; //prefer edges in the recommended path
                    preferredNeighboringEdges.put(edge, edgeScore);
                    totalScoreForPreferredNeighboringEdges += edgeScore;
                }
            }
            double decisionScore = ((context.getRecommendedPath() != null ? 1.0 : 0.0)
                    + agentState.getCurrentOwnDecisionMakingFactor()) * decisionMakingFactor;
            if (lastDecision == this) {
                decisionScore *= agentState.getRepeatLastDecisionFactor();
            }
            return new AgentDecisionScore(decisionScore, preferredNeighboringEdges,
                    totalScoreForPreferredNeighboringEdges);
        }

        @Override
        public AgentAction toAgentAction(DecisionNodeContext context, Agent agent, AgentDecisionScore decisionScore) {
            Map<Edge, Double> preferredEdges = decisionScore.getPreferredNeighboringEdges();
            double totalScore = decisionScore.getTotalScoreForPreferredNeighboringEdges();
            Edge chosenEdge = selectEdgeBasedOnScores(preferredEdges, totalScore);
            return new FollowSingleEdgeAction(agent, chosenEdge);
        }
    },
    CONTINUE_LAST_ACTION {
        @Override
        public AgentDecisionScore computeScore(DecisionNodeContext context, AgentDecisionalProperties agentState,
                double decisionMakingFactor, AgentPossibleDecision lastDecision, AgentAction lastAction) {
            double decisionScore = 0;
            if (lastAction != null && lastAction.isCompleted() == false) {
                decisionScore = 1.0 * decisionMakingFactor; //prefer to continue last action if there is one
            }
            if (lastDecision == this) {
                decisionScore *= agentState.getRepeatLastDecisionFactor();
            }
            return new AgentDecisionScore(decisionScore, null, 0.0);
        }

        @Override
        public AgentAction toAgentAction(DecisionNodeContext context, Agent agent, AgentDecisionScore decisionScore) {
            AgentAction lastAction = Objects.requireNonNull(agent.getCurrentAction(),
                    "Last action cannot be null when choosing to continue last action");
            return lastAction;
        }
    };

    // /** Functional interface for scoring edges */
    // @FunctionalInterface
    // private static interface interfaceEdgeScorer{
    //     double computeEdgeScore(Edge edge);
    // }
    //TODO: fix code duplication with helpers methods

    public abstract AgentDecisionScore computeScore(DecisionNodeContext context, AgentDecisionalProperties agentState,
            double decisionMakingFactor, AgentPossibleDecision lastDecision, AgentAction lastAction);

    public abstract AgentAction toAgentAction(DecisionNodeContext context, Agent agent,
            AgentDecisionScore decisionScore);

    private static Edge selectEdgeBasedOnScores(Map<Edge, Double> edgeScores, double totalScore) {
        double randomValue = Math.random() * totalScore;
        for (Map.Entry<Edge, Double> entry : edgeScores.entrySet()) {
            randomValue -= entry.getValue();
            if (randomValue <= 0) {
                return entry.getKey();
            }
        }
        return null; // Should not happen if totalScore > 0
    }
}

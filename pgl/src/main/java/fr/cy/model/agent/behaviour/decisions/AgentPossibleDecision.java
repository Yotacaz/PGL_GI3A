package fr.cy.model.agent.behaviour.decisions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.cy.model.agent.behaviour.properties.AgentDecisionalProperties;
import fr.cy.model.graph.CongestionStats;
import fr.cy.model.graph.element.Edge;

public enum AgentPossibleDecision {
    FOLLOW_CROWD {
        @Override
        public AgentDecisionScore computeScore(DecisionNodeContext context, AgentDecisionalProperties agentState) {
            //get 
            CongestionStats<Edge> congestionStats = context.getCongestionStatsForOutgoingEdges();
            Map<Edge, Double> preferredNeighboringEdges = new HashMap<>();
            double totalScoreForPreferredNeighboringEdges = 0.0;
            for (Edge edge : context.getOutgoingEdges()) {
                double edgeScore = edge.getCongestion()
                        / (1.0 + edge.getTotalStressInducedIncludingNeighbors() * 0.125); //prefer more crowded edges
                preferredNeighboringEdges.put(edge, edgeScore);
                totalScoreForPreferredNeighboringEdges += edgeScore;
            }
            double decisionScore = congestionStats.getAverageCongestionLevel()
                    - agentState.getCurrentOwnDecisionMakingFactor();
            return new AgentDecisionScore(decisionScore, preferredNeighboringEdges,
                    totalScoreForPreferredNeighboringEdges);
        }
    },
    FOLLOW_LESS_CROWDED_PATH {
        @Override
        public AgentDecisionScore computeScore(DecisionNodeContext context, AgentDecisionalProperties agentState) {
            CongestionStats<Edge> congestionStats = context.getCongestionStatsForOutgoingEdges();
            Map<Edge, Double> preferredNeighboringEdges = new HashMap<>();
            double totalScoreForPreferredNeighboringEdges = 0.0;
            for (Edge edge : context.getOutgoingEdges()) {
                double edgeScore = (1.0 - edge.getCongestion())
                        / (1.0 + edge.getTotalStressInducedIncludingNeighbors() * 0.125); //prefer less crowded edges
                preferredNeighboringEdges.put(edge, edgeScore);
                totalScoreForPreferredNeighboringEdges += edgeScore;
            }
            double decisionScore = -congestionStats.getAverageCongestionLevel()
                    - agentState.getCurrentOwnDecisionMakingFactor();
            return new AgentDecisionScore(decisionScore, preferredNeighboringEdges,
                    totalScoreForPreferredNeighboringEdges);
        }
    },
    FOLLOW_RECOMMENDED_PATH {
        @Override
        public AgentDecisionScore computeScore(DecisionNodeContext context, AgentDecisionalProperties agentState) {
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
            double decisionScore = (context.getRecommendedPath() != null ? 1.0 : 0.0)
                    - agentState.getCurrentOwnDecisionMakingFactor();
            return new AgentDecisionScore(decisionScore, preferredNeighboringEdges,
                    totalScoreForPreferredNeighboringEdges);
        }
    },
    RANDOM {
        @Override
        public AgentDecisionScore computeScore(DecisionNodeContext context, AgentDecisionalProperties agentState) {
            Map<Edge, Double> preferredNeighboringEdges = new HashMap<>();
            double totalScoreForPreferredNeighboringEdges = 0.0;
            for (Edge edge : context.getOutgoingEdges()) {
                double edgeScore = Math.random(); //random score for each edge
                preferredNeighboringEdges.put(edge, edgeScore);
                totalScoreForPreferredNeighboringEdges += edgeScore;
            }
            double decisionScore = Math.random() + agentState.getStressLevel();
            return new AgentDecisionScore(decisionScore, preferredNeighboringEdges,
                    totalScoreForPreferredNeighboringEdges);
        }
    },
    NICEST_PATH{
        @Override
        public AgentDecisionScore computeScore(DecisionNodeContext context, AgentDecisionalProperties agentState) {
            Map<Edge, Double> preferredNeighboringEdges = new HashMap<>();
            double totalScoreForPreferredNeighboringEdges = 0.0;
            for (Edge edge : context.getOutgoingEdges()) {
                double edgeScore = 1.0/(edge.getStressInducingFactor() + 1.0) - edge.getCongestion(); //prefer less congested edges
                preferredNeighboringEdges.put(edge, edgeScore);
                totalScoreForPreferredNeighboringEdges += edgeScore;
            }
            double decisionScore = -context.getCongestionStatsForOutgoingEdges().getAverageCongestionLevel()
                    + agentState.getCurrentOwnDecisionMakingFactor();
            return new AgentDecisionScore(decisionScore, preferredNeighboringEdges,
                    totalScoreForPreferredNeighboringEdges);
        }
    },
    FOLLOW_SHORTEST_PATH{
        @Override
        public AgentDecisionScore computeScore(DecisionNodeContext context, AgentDecisionalProperties agentState) {
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
            double decisionScore = (context.getRecommendedPath() != null ? 1.0 : 0.0)
                    + agentState.getCurrentOwnDecisionMakingFactor();
            return new AgentDecisionScore(decisionScore, preferredNeighboringEdges,
                    totalScoreForPreferredNeighboringEdges);
        }
    };

    public abstract AgentDecisionScore computeScore(DecisionNodeContext context, AgentDecisionalProperties agentState);
}

package fr.cy.model.agent.behaviour.decisions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.behaviour.AgentBehaviourException;
import fr.cy.model.agent.behaviour.agentActions.AgentAction;
import fr.cy.model.agent.behaviour.agentActions.FollowSingleEdgeAction;
import fr.cy.model.agent.behaviour.agentActions.WaitAction;
import fr.cy.model.agent.behaviour.properties.AgentDecisionalProperties;
import fr.cy.model.graph.CongestionStats;
import fr.cy.model.graph.element.Edge;

/**
 * Enum representing the different possible decisions an agent can make at a decision point.
 * Each decision has its own logic for computing a score based on the current context and
 * for converting that decision into an actionable {@link AgentAction}.
 */
public enum AgentPossibleNodeDecision {

    FOLLOW_CROWD {
        @Override
        public AgentDecisionScore computeScore(DecisionNodeContext context, AgentDecisionalProperties agentState,
                double decisionMakingFactor, AgentPossibleNodeDecision lastDecision, AgentAction lastAction,
                List<Double> edgeScoreMultipliers) {
            //get 
            CongestionStats<Edge> congestionStats = context.getCongestionStatsForOutgoingEdges();
            Map<Edge, Double> preferredNeighboringEdges = new HashMap<>();
            InterfaceEdgeScorer edgeScorer = (Edge edge) -> edge.getCongestion()
                    / (1.0 + edge.getCachedTotalStressInducedIncludingNeighbors() * 0.125); //prefer more crowded edges
            double totalScoreForPreferredNeighboringEdges = computeEdgesScore(context.getOutgoingEdges(), edgeScorer,
                    preferredNeighboringEdges, edgeScoreMultipliers);
            double decisionScore = (congestionStats.getAverageCongestionLevel() * agentState.getCongestionTolerance()
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
                double decisionMakingFactor, AgentPossibleNodeDecision lastDecision, AgentAction lastAction,
                List<Double> edgeScoreMultipliers) {
            CongestionStats<Edge> congestionStats = context.getCongestionStatsForOutgoingEdges();
            Map<Edge, Double> preferredNeighboringEdges = new HashMap<>();

            InterfaceEdgeScorer edgeScorer = (Edge edge) -> (1.0 - edge.getCongestion())
                    / (1.0 + edge.getCachedTotalStressInducedIncludingNeighbors() * 0.125); //prefer less crowded edges
            double totalScoreForPreferredNeighboringEdges = computeEdgesScore(context.getOutgoingEdges(), edgeScorer,
                    preferredNeighboringEdges, edgeScoreMultipliers);

            double decisionScore = (1.0
                    + congestionStats.getAverageCongestionLevel() / (1.0 + agentState.getCongestionTolerance())
                    + agentState.getCurrentOwnDecisionMakingFactor()) * (1.0 - agentState.getCongestionTolerance())
                    * decisionMakingFactor * 10;
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
                double decisionMakingFactor, AgentPossibleNodeDecision lastDecision, AgentAction lastAction,
                List<Double> edgeScoreMultipliers) {
            Map<Edge, Double> preferredNeighboringEdges = new HashMap<>();
            double totalScoreForPreferredNeighboringEdges = 0.0;
            if (context.getRecommendedPath() != null) {
                InterfaceEdgeScorer edgeScorer = (Edge edge) -> context.getRecommendedPath().getEdges().contains(edge)
                        ? 1.0
                        : 0.0; //prefer edges in the recommended path
                totalScoreForPreferredNeighboringEdges = computeEdgesScore(context.getOutgoingEdges(), edgeScorer,
                        preferredNeighboringEdges, edgeScoreMultipliers);
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
                double decisionMakingFactor, AgentPossibleNodeDecision lastDecision, AgentAction lastAction,
                List<Double> edgeScoreMultipliers) {
            Map<Edge, Double> preferredNeighboringEdges = new HashMap<>();

            InterfaceEdgeScorer edgeScorer = (Edge edge) -> RNG.nextDouble(); //random score for each edge
            double totalScoreForPreferredNeighboringEdges = computeEdgesScore(context.getOutgoingEdges(), edgeScorer,
                    preferredNeighboringEdges, edgeScoreMultipliers);
            double decisionScore = (RNG.nextDouble() + agentState.getStressLevel()) * decisionMakingFactor;
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
                double decisionMakingFactor, AgentPossibleNodeDecision lastDecision, AgentAction lastAction,
                List<Double> edgeScoreMultipliers) {
            Map<Edge, Double> preferredNeighboringEdges = new HashMap<>();
            InterfaceEdgeScorer edgeScorer = (Edge edge) -> Math.max(1.0 / (edge.getStressInducingImpact() + 1.0)
                    - edge.getCongestion(), 0.0); //prefer less congested and less stress-inducing edges
            double totalScoreForPreferredNeighboringEdges = computeEdgesScore(context.getOutgoingEdges(), edgeScorer,
                    preferredNeighboringEdges, edgeScoreMultipliers);

            double decisionScore = (1.0 - context.getCongestionStatsForOutgoingEdges().getAverageCongestionLevel()
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
                double decisionMakingFactor, AgentPossibleNodeDecision lastDecision, AgentAction lastAction,
                List<Double> edgeScoreMultipliers) {
            Map<Edge, Double> preferredNeighboringEdges = new HashMap<>();
            double totalScoreForPreferredNeighboringEdges = 0.0;
            if (context.getRecommendedPath() != null) {
                List<Edge> recommendedPathEdges = context.getRecommendedPath().getEdges();
                InterfaceEdgeScorer edgeScorer = (Edge edge) -> recommendedPathEdges.contains(edge) ? 1.0 : 0.0; //prefer edges in the recommended path
                totalScoreForPreferredNeighboringEdges = computeEdgesScore(context.getOutgoingEdges(), edgeScorer,
                        preferredNeighboringEdges, edgeScoreMultipliers);
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
            Edge chosenEdge = selectEdgeBasedOnScores(preferredEdges, totalScore); //FIXME: temporary solution, should directly follow the recommended path without random selection
            return new FollowSingleEdgeAction(agent, chosenEdge);
        }
    },
    CONTINUE_LAST_ACTION {
        @Override
        public AgentDecisionScore computeScore(DecisionNodeContext context, AgentDecisionalProperties agentState,
                double decisionMakingFactor, AgentPossibleNodeDecision lastDecision, AgentAction lastAction,
                List<Double> edgeScoreMultipliers) {
            Map<Edge, Double> preferredNeighboringEdges = new HashMap<>();
            double totalScoreForPreferredNeighboringEdges = 0.0;
            double decisionScore = 0;
            if (lastAction != null && lastAction.isCompleted() == false) {
                //prefer to continue on the same edge if there is an ongoing action
                InterfaceEdgeScorer edgeScorer = (
                        Edge edge) -> edge.equals(lastAction.getClosestTargetEdge()) ? 1.0 : 0.0;
                totalScoreForPreferredNeighboringEdges = computeEdgesScore(context.getOutgoingEdges(), edgeScorer,
                        preferredNeighboringEdges, edgeScoreMultipliers);
                decisionScore = 1.0 * decisionMakingFactor; //prefer to continue last action if there is one
            }
            if (lastDecision == this) {
                decisionScore *= agentState.getRepeatLastDecisionFactor();
            }
            return new AgentDecisionScore(decisionScore, preferredNeighboringEdges,
                    totalScoreForPreferredNeighboringEdges);
        }

        @Override
        public AgentAction toAgentAction(DecisionNodeContext context, Agent agent, AgentDecisionScore decisionScore) {
            AgentAction lastAction = Objects.requireNonNull(agent.getCurrentAction(),
                    "Last action cannot be null when choosing to continue last action");
            return lastAction;
        }
    },
    WAIT {
        @Override
        public AgentDecisionScore computeScore(DecisionNodeContext context, AgentDecisionalProperties agentState,
                double decisionMakingFactor, AgentPossibleNodeDecision lastDecision, AgentAction lastAction,
                List<Double> edgeScoreMultipliers) {
            Map<Edge, Double> preferredNeighboringEdges = new HashMap<>();
            double totalScoreForPreferredNeighboringEdges = 0.0;
            double minEdgeScoreMultiplier = edgeScoreMultipliers.stream().min(Double::compare).orElse(1.0);
            double decisionScore = decisionMakingFactor / (1 + minEdgeScoreMultiplier); //if there is no good edge to take, prefer to wait, especially if stress level is high
            if (lastDecision == this) {
                decisionScore *= agentState.getRepeatLastDecisionFactor();
            }
            return new AgentDecisionScore(decisionScore, preferredNeighboringEdges,
                    totalScoreForPreferredNeighboringEdges);
        }

        @Override
        public AgentAction toAgentAction(DecisionNodeContext context, Agent agent, AgentDecisionScore decisionScore) {
            double timeToWait = RNG.nextDouble() * 0.5 + 0.5; //wait between 0.5 and 1 s
            return new WaitAction(agent, timeToWait);
        }
    };

    private static final Random RNG = new Random();

    public abstract AgentDecisionScore computeScore(DecisionNodeContext context, AgentDecisionalProperties agentState,
            double decisionMakingFactor, AgentPossibleNodeDecision lastDecision, AgentAction lastAction,
            List<Double> edgeScoreMultipliers);

    public abstract AgentAction toAgentAction(DecisionNodeContext context, Agent agent,
            AgentDecisionScore decisionScore);

    private static Edge selectEdgeBasedOnScores(Map<Edge, Double> edgeScores, double totalScore) {
        double randomValue = RNG.nextDouble() * totalScore;
        for (Map.Entry<Edge, Double> entry : edgeScores.entrySet()) {
            randomValue -= entry.getValue();
            if (randomValue <= 0) {
                return entry.getKey();
            }
        }
        throw new AgentBehaviourException(
                "No edge selected. Should never happen if totalScore is the sum of edgeScores");
    }

    /** Functional interface for scoring edges */
    @FunctionalInterface
    private static interface InterfaceEdgeScorer {
        public double computeEdgeScore(Edge edge);
    }

    /** Helper method to compute scores for a list of edges based on a given edge scorer
     *  and update the preferred edges map. Uses precomputed edge score multipliers to avoid recomputation. */
    private static double computeEdgesScore(List<Edge> edges, InterfaceEdgeScorer edgeScorer,
            Map<Edge, Double> preferredNeighboringEdges, List<Double> edgeScoreMultipliers) {
        double totalScore = 0.0;
        if (edges.size() != edgeScoreMultipliers.size()) {
            throw new IllegalArgumentException("Edge score multipliers list must have the same size as the edges list");
        }
        for (int i = 0; i < edges.size(); i++) {
            Edge edge = edges.get(i);
            double score = Math.max(0.0, edgeScorer.computeEdgeScore(edge));
            score *= edgeScoreMultipliers.get(i); //adjust score using precomputed multiplier
            preferredNeighboringEdges.put(edge, score);
            totalScore += score;
        }
        return totalScore;
    }

}

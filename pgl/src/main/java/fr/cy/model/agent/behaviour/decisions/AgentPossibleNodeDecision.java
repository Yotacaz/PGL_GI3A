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
import fr.cy.model.agent.behaviour.properties.AgentDecisionalProperties;
import fr.cy.model.fire.Fire;
import fr.cy.model.graph.CongestionStats;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;

/**
 * Enum representing the different possible decisions an agent can make at a decision point.
 * Each decision has its own logic for computing a score based on the current context and
 * for converting that decision into an actionable {@link AgentAction}.
 */
public enum AgentPossibleNodeDecision {

    FOLLOW_CROWD {
        @Override
        public AgentDecisionScore computeScore(DecisionNodeContext context, AgentDecisionalProperties agentState,
                double decisionMakingFactor, AgentPossibleNodeDecision lastDecision, AgentAction lastAction) {
            //get 
            CongestionStats<Edge> congestionStats = context.getCongestionStatsForOutgoingEdges();
            Map<Edge, Double> preferredNeighboringEdges = new HashMap<>();
            InterfaceEdgeScorer edgeScorer = (Edge edge) -> edge.getCongestion()
                    / (1.0 + edge.getCachedTotalStressInducedIncludingNeighbors() * 0.125); //prefer more crowded edges
            double totalScoreForPreferredNeighboringEdges = computeEdgesScore(context.getOutgoingEdges(), edgeScorer,
                    preferredNeighboringEdges, context, agentState);
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
                double decisionMakingFactor, AgentPossibleNodeDecision lastDecision, AgentAction lastAction) {
            CongestionStats<Edge> congestionStats = context.getCongestionStatsForOutgoingEdges();
            Map<Edge, Double> preferredNeighboringEdges = new HashMap<>();

            InterfaceEdgeScorer edgeScorer = (Edge edge) -> (1.0 - edge.getCongestion())
                    / (1.0 + edge.getCachedTotalStressInducedIncludingNeighbors() * 0.125); //prefer less crowded edges
            double totalScoreForPreferredNeighboringEdges = computeEdgesScore(context.getOutgoingEdges(), edgeScorer,
                    preferredNeighboringEdges, context, agentState);

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
                double decisionMakingFactor, AgentPossibleNodeDecision lastDecision, AgentAction lastAction) {
            Map<Edge, Double> preferredNeighboringEdges = new HashMap<>();
            double totalScoreForPreferredNeighboringEdges = 0.0;
            if (context.getRecommendedPath() != null) {
                InterfaceEdgeScorer edgeScorer = (Edge edge) -> context.getRecommendedPath().getEdges().contains(edge) ? 1.0
                        : 0.0; //prefer edges in the recommended path
                totalScoreForPreferredNeighboringEdges = computeEdgesScore(context.getOutgoingEdges(), edgeScorer,
                        preferredNeighboringEdges, context, agentState);
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
                double decisionMakingFactor, AgentPossibleNodeDecision lastDecision, AgentAction lastAction) {
            Map<Edge, Double> preferredNeighboringEdges = new HashMap<>();

            InterfaceEdgeScorer edgeScorer = (Edge edge) -> RNG.nextDouble(); //random score for each edge
            double totalScoreForPreferredNeighboringEdges = computeEdgesScore(context.getOutgoingEdges(), edgeScorer,
                    preferredNeighboringEdges, context, agentState);
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
                double decisionMakingFactor, AgentPossibleNodeDecision lastDecision, AgentAction lastAction) {
            Map<Edge, Double> preferredNeighboringEdges = new HashMap<>();
            InterfaceEdgeScorer edgeScorer = (Edge edge) -> Math.max(1.0 / (edge.getStressInducingImpact() + 1.0)
                    - edge.getCongestion(), 0.0); //prefer less congested and less stress-inducing edges
            double totalScoreForPreferredNeighboringEdges = computeEdgesScore(context.getOutgoingEdges(), edgeScorer,
                    preferredNeighboringEdges, context, agentState);

            double decisionScore = (1.0-context.getCongestionStatsForOutgoingEdges().getAverageCongestionLevel()
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
                double decisionMakingFactor, AgentPossibleNodeDecision lastDecision, AgentAction lastAction) {
            Map<Edge, Double> preferredNeighboringEdges = new HashMap<>();
            double totalScoreForPreferredNeighboringEdges = 0.0;
            if (context.getRecommendedPath() != null) {
                List<Edge> recommendedPathEdges = context.getRecommendedPath().getEdges();
                InterfaceEdgeScorer edgeScorer = (Edge edge) -> recommendedPathEdges.contains(edge) ? 1.0 : 0.0; //prefer edges in the recommended path
                totalScoreForPreferredNeighboringEdges = computeEdgesScore(context.getOutgoingEdges(), edgeScorer,
                        preferredNeighboringEdges, context, agentState);
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
                double decisionMakingFactor, AgentPossibleNodeDecision lastDecision, AgentAction lastAction) {
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

    private static final Random RNG = new Random();

    public abstract AgentDecisionScore computeScore(DecisionNodeContext context, AgentDecisionalProperties agentState,
            double decisionMakingFactor, AgentPossibleNodeDecision lastDecision, AgentAction lastAction);

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
        throw new AgentBehaviourException("No edge selected. Should never happen if totalScore is the sum of edgeScores");
    }

    /** Functional interface for scoring edges */
    @FunctionalInterface
    private static interface InterfaceEdgeScorer {
        public double computeEdgeScore(Edge edge);
    }

    /** Helper method to compute scores for a list of edges based on a given edge scorer
     *  and update the preferred edges map. */
    private static double computeEdgesScore(List<Edge> edges, InterfaceEdgeScorer edgeScorer,
            Map<Edge, Double> preferredNeighboringEdges, DecisionNodeContext context,
            AgentDecisionalProperties agentState) {
        double totalScore = 0.0;
        Node sourceNode = context.getSourceNode();
        for (Edge edge : edges) {
            Node oppositeNode = edge.getOppositeNode(sourceNode);
            double score = Math.max(0.0, edgeScorer.computeEdgeScore(edge));
            //penalize edges on fire
            if (edge.isOnFire()) {
                Fire fire = edge.getFire();
                assert fire != null;
                score *= 0.1 / (1.0 + fire.getIntensity() + fire.getSmokeLevel() + fire.getSpreadRate()); //avoid
            }
            //penalize very congested edges
            double congestion = edge.getCongestion();
            if (congestion > 0.96) {
                score *= 0.4 * (1.0 + congestion - agentState.getCongestionTolerance());
            }
            //penalize edges leading to very stressful nodes
            double stressInducedByOppositeNode = oppositeNode.getStressInducingImpact();
            if (stressInducedByOppositeNode > 0.96) {
                score *= 0.4 * (1.0 + stressInducedByOppositeNode - agentState.getStressTolerance());
            }
            preferredNeighboringEdges.put(edge, score);
            totalScore += score;
        }
        return totalScore;
    }
}

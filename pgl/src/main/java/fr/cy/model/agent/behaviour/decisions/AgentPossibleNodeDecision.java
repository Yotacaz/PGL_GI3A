package fr.cy.model.agent.behaviour.decisions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.ToDoubleFunction;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.behaviour.agentActions.AgentAction;
import fr.cy.model.agent.behaviour.agentActions.FollowSingleEdgeAction;
import fr.cy.model.agent.behaviour.agentActions.WaitAction;
import fr.cy.model.agent.properties.AgentDecisionalProperties;
import fr.cy.model.agent.context.NodeContext;
import fr.cy.model.graph.CongestionStats;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;

/**
 * Enum representing the different possible decisions an agent can make at a decision point.
 * Each decision has its own logic for computing a score based on the current context and
 * for converting that decision into an actionable {@link AgentAction}.
 */
public enum AgentPossibleNodeDecision implements AgentDecision {

    FOLLOW_CROWD {
        @Override
        public AgentPossibleNodeDecisionScore computeScore(NodeContext context, Agent agent,
                double decisionMakingFactor, AgentPossibleNodeDecision lastDecision, AgentAction lastAction,
                List<Double> edgeScoreMultipliers) {
            AgentDecisionalProperties agentState = agent.getBehavioralState();
            //get 
            CongestionStats<Edge> congestionStats = context.getCongestionStatsForOutgoingEdges();
            double congestionLevel = congestionStats == null ? 0.0 : congestionStats.getAverageCongestionLevel();
            Map<Edge, Double> preferredNeighboringEdges = new HashMap<>();
            Node sourceNode = context.getSourceNode();
            double totalScoreForPreferredNeighboringEdges = computeEdgesScore(sourceNode.getOutgoingEdges(),
                    edge -> edge.getCongestion()
                            / (1.0 + edge.getCachedTotalStressInducedIncludingNeighbors() * 0.125),
                    preferredNeighboringEdges, edgeScoreMultipliers);
            double decisionScore = (congestionLevel * agentState.getCongestionTolerance()
                    - agentState.getCurrentOwnDecisionMakingFactor()) * decisionMakingFactor; //can be 0 if no crowd
            if (lastDecision == this) {
                decisionScore *= agentState.getRepeatLastDecisionFactor(); //prefer to repeat last decision if it was the same
            }
            return new AgentPossibleNodeDecisionScore(decisionScore, preferredNeighboringEdges,
                    totalScoreForPreferredNeighboringEdges);
        }

        @Override
        public AgentAction toAgentAction(NodeContext context, Agent agent, AgentPossibleNodeDecisionScore decisionScore) {
            Map<Edge, Double> preferredEdges = decisionScore.getPreferredNeighboringEdges();
            double totalScore = decisionScore.getTotalScoreForPreferredNeighboringEdges();
            Edge chosenEdge = selectEdgeBasedOnScores(preferredEdges, totalScore);
            Node targetNode = Objects.requireNonNull(chosenEdge.getOppositeNode(agent.getCurrentNode()));
            return new FollowSingleEdgeAction(agent, chosenEdge, targetNode);
        }
    },
    FOLLOW_LESS_CROWDED_PATH {
        @Override
        public AgentPossibleNodeDecisionScore computeScore(NodeContext context, Agent agent,
                double decisionMakingFactor, AgentPossibleNodeDecision lastDecision, AgentAction lastAction,
                List<Double> edgeScoreMultipliers) {
            CongestionStats<Edge> congestionStats = context.getCongestionStatsForOutgoingEdges();
            Map<Edge, Double> preferredNeighboringEdges = new HashMap<>();
            AgentDecisionalProperties agentState = agent.getBehavioralState();
            double congestionLevel = congestionStats == null ? 0.0 : congestionStats.getAverageCongestionLevel();
            Node sourceNode = context.getSourceNode();
            double totalScoreForPreferredNeighboringEdges = computeEdgesScore(sourceNode.getOutgoingEdges(),
                    edge -> (1.0 - edge.getCongestion())
                            / (1.0 + edge.getCachedTotalStressInducedIncludingNeighbors() * 0.125),
                    preferredNeighboringEdges, edgeScoreMultipliers);

            double decisionScore = (1.0
                    + congestionLevel / (1.0 + agentState.getCongestionTolerance())
                    + agentState.getCurrentOwnDecisionMakingFactor()) * (1.0 - agentState.getCongestionTolerance())
                    * decisionMakingFactor * 10;
            if (lastDecision == this) {
                decisionScore *= agentState.getRepeatLastDecisionFactor();
            }
            return new AgentPossibleNodeDecisionScore(decisionScore, preferredNeighboringEdges,
                    totalScoreForPreferredNeighboringEdges);
        }

        @Override
        public AgentAction toAgentAction(NodeContext context, Agent agent, AgentPossibleNodeDecisionScore decisionScore) {
            Map<Edge, Double> preferredEdges = decisionScore.getPreferredNeighboringEdges();
            double totalScore = decisionScore.getTotalScoreForPreferredNeighboringEdges();
            Edge chosenEdge = selectEdgeBasedOnScores(preferredEdges, totalScore);
            Node targetNode = Objects.requireNonNull(chosenEdge.getOppositeNode(agent.getCurrentNode()));
            return new FollowSingleEdgeAction(agent, chosenEdge, targetNode);
        }
    },
    FOLLOW_RECOMMENDED_PATH {
        @Override
        public AgentPossibleNodeDecisionScore computeScore(NodeContext context, Agent agent,
                double decisionMakingFactor, AgentPossibleNodeDecision lastDecision, AgentAction lastAction,
                List<Double> edgeScoreMultipliers) {
            Map<Edge, Double> preferredNeighboringEdges = new HashMap<>();
            double totalScoreForPreferredNeighboringEdges = 0.0;
            AgentDecisionalProperties agentState = agent.getBehavioralState();
            Node sourceNode = context.getSourceNode();
            if (context.getRecommendedPath() != null) {
                totalScoreForPreferredNeighboringEdges = computeEdgesScore(sourceNode.getOutgoingEdges(),
                        edge -> context.getRecommendedPath().getEdges().contains(edge) ? 1.0 : 0.0,
                        preferredNeighboringEdges, edgeScoreMultipliers);
            }
            double decisionScore = ((context.getRecommendedPath() != null ? 1.0 : 0.0)
                    - agentState.getCurrentOwnDecisionMakingFactor()) * decisionMakingFactor;
            if (lastDecision == this) {
                decisionScore *= agentState.getRepeatLastDecisionFactor();
            }
            return new AgentPossibleNodeDecisionScore(decisionScore, preferredNeighboringEdges,
                    totalScoreForPreferredNeighboringEdges);
        }

        @Override
        public AgentAction toAgentAction(NodeContext context, Agent agent, AgentPossibleNodeDecisionScore decisionScore) {
            Map<Edge, Double> preferredEdges = decisionScore.getPreferredNeighboringEdges();
            double totalScore = decisionScore.getTotalScoreForPreferredNeighboringEdges();
            Edge chosenEdge = selectEdgeBasedOnScores(preferredEdges, totalScore);
            Node targetNode = Objects.requireNonNull(chosenEdge.getOppositeNode(agent.getCurrentNode()));

            return new FollowSingleEdgeAction(agent, chosenEdge, targetNode);
        }

    },
    RANDOM {
        @Override
        public AgentPossibleNodeDecisionScore computeScore(NodeContext context, Agent agent,
                double decisionMakingFactor, AgentPossibleNodeDecision lastDecision, AgentAction lastAction,
                List<Double> edgeScoreMultipliers) {
            Map<Edge, Double> preferredNeighboringEdges = new HashMap<>();
            AgentDecisionalProperties agentState = agent.getBehavioralState();
            Node sourceNode = context.getSourceNode();

            double totalScoreForPreferredNeighboringEdges = computeEdgesScore(sourceNode.getOutgoingEdges(),
                    edge -> RNG.nextDouble(),
                    preferredNeighboringEdges, edgeScoreMultipliers);
            double decisionScore = (RNG.nextDouble() + agentState.getStressLevel()) * decisionMakingFactor;
            if (lastDecision == this) {
                decisionScore *= agentState.getRepeatLastDecisionFactor();
            }
            return new AgentPossibleNodeDecisionScore(decisionScore, preferredNeighboringEdges,
                    totalScoreForPreferredNeighboringEdges);
        }

        @Override
        public AgentAction toAgentAction(NodeContext context, Agent agent, AgentPossibleNodeDecisionScore decisionScore) {
            Map<Edge, Double> preferredEdges = decisionScore.getPreferredNeighboringEdges();
            double totalScore = decisionScore.getTotalScoreForPreferredNeighboringEdges();
            Edge chosenEdge = selectEdgeBasedOnScores(preferredEdges, totalScore);
            Node targetNode = Objects.requireNonNull(chosenEdge.getOppositeNode(agent.getCurrentNode()));

            return new FollowSingleEdgeAction(agent, chosenEdge, targetNode);
        }
    },
    NICEST_PATH {
        @Override
        public AgentPossibleNodeDecisionScore computeScore(NodeContext context, Agent agent,
                double decisionMakingFactor, AgentPossibleNodeDecision lastDecision, AgentAction lastAction,
                List<Double> edgeScoreMultipliers) {
            AgentDecisionalProperties agentState = agent.getBehavioralState();
            Node sourceNode = context.getSourceNode();
            Map<Edge, Double> preferredNeighboringEdges = new HashMap<>();
            double totalScoreForPreferredNeighboringEdges = computeEdgesScore(sourceNode.getOutgoingEdges(),
                    edge -> Math.max(1.0 / (edge.getStressInducingImpact() + 1.0)
                            - edge.getCongestion(), 0.0),
                    preferredNeighboringEdges, edgeScoreMultipliers);
            double averageCongestionLevel = context.getCongestionStatsForOutgoingEdges() != null
                    ? context.getCongestionStatsForOutgoingEdges().getAverageCongestionLevel()
                    : 0.0;
            double decisionScore = (1.0 - averageCongestionLevel
                    + agentState.getCurrentOwnDecisionMakingFactor()) * decisionMakingFactor;
            if (lastDecision == this) {
                decisionScore *= agentState.getRepeatLastDecisionFactor();
            }
            return new AgentPossibleNodeDecisionScore(decisionScore, preferredNeighboringEdges,
                    totalScoreForPreferredNeighboringEdges);
        }

        @Override
        public AgentAction toAgentAction(NodeContext context, Agent agent, AgentPossibleNodeDecisionScore decisionScore) {
            Map<Edge, Double> preferredEdges = decisionScore.getPreferredNeighboringEdges();
            double totalScore = decisionScore.getTotalScoreForPreferredNeighboringEdges();
            Edge chosenEdge = selectEdgeBasedOnScores(preferredEdges, totalScore);
            Node targetNode = Objects.requireNonNull(chosenEdge.getOppositeNode(agent.getCurrentNode()));

            return new FollowSingleEdgeAction(agent, chosenEdge, targetNode);
        }
    },
    FOLLOW_SHORTEST_PATH {
        @Override
        public AgentPossibleNodeDecisionScore computeScore(NodeContext context, Agent agent,
                double decisionMakingFactor, AgentPossibleNodeDecision lastDecision, AgentAction lastAction,
                List<Double> edgeScoreMultipliers) {
            AgentDecisionalProperties agentState = agent.getBehavioralState();
            Node sourceNode = context.getSourceNode();
            Map<Edge, Double> preferredNeighboringEdges = new HashMap<>();
            double totalScoreForPreferredNeighboringEdges = 0.0;
            boolean hasRecommendedPath = context.getRecommendedPath() != null;
            if (hasRecommendedPath) {
                List<Edge> recommendedPathEdges = context.getRecommendedPath().getEdges();
                totalScoreForPreferredNeighboringEdges = computeEdgesScore(sourceNode.getOutgoingEdges(),
                        edge -> recommendedPathEdges.contains(edge) ? 1.0 : 0.0,
                        preferredNeighboringEdges, edgeScoreMultipliers);
            }

            double decisionScore = hasRecommendedPath
                    ? (1.0 + agentState.getCurrentOwnDecisionMakingFactor()) * decisionMakingFactor
                    : 0.0;
            if (lastDecision == this) {
                decisionScore *= agentState.getRepeatLastDecisionFactor();
            }
            return new AgentPossibleNodeDecisionScore(decisionScore, preferredNeighboringEdges,
                    totalScoreForPreferredNeighboringEdges);
        }

        @Override
        public AgentAction toAgentAction(NodeContext context, Agent agent, AgentPossibleNodeDecisionScore decisionScore) {
            Map<Edge, Double> preferredEdges = decisionScore.getPreferredNeighboringEdges();
            double totalScore = decisionScore.getTotalScoreForPreferredNeighboringEdges();
            Edge chosenEdge = selectEdgeBasedOnScores(preferredEdges, totalScore); //FIXME: temporary solution, should directly follow the recommended path without random selection
            Node targetNode = Objects.requireNonNull(chosenEdge.getOppositeNode(agent.getCurrentNode()));

            return new FollowSingleEdgeAction(agent, chosenEdge, targetNode);
        }
    },
    CONTINUE_LAST_ACTION {
        @Override
        public AgentPossibleNodeDecisionScore computeScore(NodeContext context, Agent agent,
                double decisionMakingFactor, AgentPossibleNodeDecision lastDecision, AgentAction lastAction,
                List<Double> edgeScoreMultipliers) {
            AgentDecisionalProperties agentState = agent.getBehavioralState();
            Map<Edge, Double> preferredNeighboringEdges = new HashMap<>();
            double totalScoreForPreferredNeighboringEdges = 0.0;
            double decisionScore = 0;
            Node sourceNode = context.getSourceNode();
            if (lastAction != null && lastAction.isCompleted() == false) {
                //prefer to continue on the next edge of the action if there is an ongoing action
                totalScoreForPreferredNeighboringEdges = computeEdgesScore(sourceNode.getOutgoingEdges(),
                        edge -> edge.equals(lastAction.getClosestTargetEdge()) ? 1.0 : 0.0,
                        preferredNeighboringEdges, edgeScoreMultipliers);
                decisionScore = 1.0 * decisionMakingFactor; //prefer to continue last action if there is one
            }
            if (lastDecision == this) {
                decisionScore *= agentState.getRepeatLastDecisionFactor();
            }

            return new AgentPossibleNodeDecisionScore(decisionScore, preferredNeighboringEdges,
                    totalScoreForPreferredNeighboringEdges);
        }

        @Override
        public AgentAction toAgentAction(NodeContext context, Agent agent, AgentPossibleNodeDecisionScore decisionScore) {
            AgentAction lastAction = Objects.requireNonNull(agent.getCurrentAction(),
                    "Last action cannot be null when choosing to continue last action");
            return lastAction;
        }
    },
    WAIT {
        @Override
        public AgentPossibleNodeDecisionScore computeScore(NodeContext context, Agent agent,
                double decisionMakingFactor, AgentPossibleNodeDecision lastDecision, AgentAction lastAction,
                List<Double> edgeScoreMultipliers) {
            AgentDecisionalProperties agentState = agent.getBehavioralState();
            Map<Edge, Double> preferredNeighboringEdges = new HashMap<>();
            double totalScoreForPreferredNeighboringEdges = 0.0;
            double minEdgeScoreMultiplier = edgeScoreMultipliers.stream().min(Double::compare).orElse(1.0);
            double decisionScore = decisionMakingFactor / (1 + minEdgeScoreMultiplier); //if there is no good edge to take, prefer to wait, especially if stress level is high
            double damageByNode = context.getSourceNode().getDamageForAgent(agent, 1.0);
            if (damageByNode > 0) {
                decisionScore *= 0.5 / (1 + damageByNode);
            }
            if (lastDecision == this) {
                decisionScore *= agentState.getRepeatLastDecisionFactor();
            }
            return new AgentPossibleNodeDecisionScore(decisionScore, preferredNeighboringEdges,
                    totalScoreForPreferredNeighboringEdges);
        }

        @Override
        public AgentAction toAgentAction(NodeContext context, Agent agent, AgentPossibleNodeDecisionScore decisionScore) {
            double timeToWait = RNG.nextDouble() * 0.5 + 1; //wait between 1 and 1.5 s
            return new WaitAction(agent, timeToWait);
        }
    };

    private static final Random RNG = new Random();

    public abstract AgentPossibleNodeDecisionScore computeScore(NodeContext context, Agent agent,
            double decisionMakingFactor, AgentPossibleNodeDecision lastDecision, AgentAction lastAction,
            List<Double> edgeScoreMultipliers);

    public abstract AgentAction toAgentAction(NodeContext context, Agent agent,
            AgentPossibleNodeDecisionScore decisionScore);

    private static Edge selectEdgeBasedOnScores(Map<Edge, Double> edgeScores, double totalScore) {
        return DecisionScoreUtils.selectElementBasedOnScores(edgeScores, totalScore);
    }

    private static double computeEdgesScore(List<Edge> edges, ToDoubleFunction<Edge> edgeScorer,
            Map<Edge, Double> preferredNeighboringEdges, List<Double> edgeScoreMultipliers) {
        return DecisionScoreUtils.computeElementScores(edges, edgeScorer, preferredNeighboringEdges,
                edgeScoreMultipliers);
    }

}

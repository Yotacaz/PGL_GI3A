package fr.cy.model.agent.behaviour.decisions;

import java.util.Map;
import java.util.Objects;
import java.util.Random;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.behaviour.agentActions.AgentAction;
import fr.cy.model.agent.behaviour.agentActions.FollowSingleEdgeAction;
import fr.cy.model.agent.behaviour.agentActions.WaitBeforeOtherAction;
import fr.cy.model.agent.behaviour.properties.AgentDecisionalProperties;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;

public enum AgentPossibleEdgeDecision {
    /** Agent decides to go back to the node it came from. */
    BACKTRACK {
        @Override
        public double computeScore(Node currentTargetNode, AgentDecisionalProperties agentState,
                AgentPossibleEdgeDecision lastDecision, AgentAction lastAction,
                Map<Node, Double> nodeScoreMultipliers) {
            Edge currentEdge = Objects.requireNonNull(lastAction.getClosestTargetEdge());
            Node backtrackNode = Objects.requireNonNull(currentEdge.getOppositeNode(currentTargetNode));
            double targetNodeScore = nodeScoreMultipliers.get(backtrackNode);

            double backtrackNodeScore = nodeScoreMultipliers.get(backtrackNode);
            return 100 * targetNodeScore < backtrackNodeScore ? backtrackNodeScore : 0.0;
        }

        @Override
        public AgentAction toAgentAction(Agent agent) {
            double currentProgress = 1 - agent.getCurrentEdgeProgress();
            Edge currentEdge = Objects.requireNonNull(agent.getCurrentEdge());
            Node targetNode = Objects
                    .requireNonNull(currentEdge.getOppositeNode(agent.getCurrentNodeOrNextNodeIfOnEdge()));

            return new FollowSingleEdgeAction(agent, currentEdge, targetNode, Math.max(0.0, currentProgress));
        }
    },
    /** Agent decides to continue toward the node it was already going to. */
    CONTINUE {
        @Override
        public double computeScore(Node currentTargetNode, AgentDecisionalProperties agentState,
                AgentPossibleEdgeDecision lastDecision, AgentAction lastAction,
                Map<Node, Double> nodeScoreMultipliers) {
            Objects.requireNonNull(currentTargetNode);
            double continueScore = nodeScoreMultipliers.get(currentTargetNode);
            return continueScore;
        }

        @Override
        public AgentAction toAgentAction(Agent agent) {
            return Objects.requireNonNull(agent.getCurrentAction());
        }
    },
    /** Agent decides to wait on the current edge. */
    WAIT {
        @Override
        public double computeScore(Node currentTargetNode, AgentDecisionalProperties agentState,
                AgentPossibleEdgeDecision lastDecision, AgentAction lastAction,
                Map<Node, Double> nodeScoreMultipliers) {
            Objects.requireNonNull(currentTargetNode);
            double continueScore = nodeScoreMultipliers.get(currentTargetNode);
            Edge currentEdge = Objects.requireNonNull(lastAction.getClosestTargetEdge());
            Node backtrackNode = Objects.requireNonNull(currentEdge.getOppositeNode(currentTargetNode));

            double backtrackNodeScore = nodeScoreMultipliers.get(backtrackNode);
            double waitScore = 1.0 / (0.5 + continueScore + backtrackNodeScore);
            return 50 * continueScore < backtrackNodeScore ? waitScore : 0.0;
        }

        @Override
        public AgentAction toAgentAction(Agent agent) {
            double timeToWait = RNG.nextDouble() * 0.5 + 1;
            AgentAction previousAction = Objects.requireNonNull(agent.getCurrentAction());

            return new WaitBeforeOtherAction(agent, timeToWait, previousAction);
        }
    };

    private static final Random RNG = new Random();

    public abstract double computeScore(Node currentTargetNode,
            AgentDecisionalProperties agentState,
            AgentPossibleEdgeDecision lastDecision, AgentAction lastAction,
            Map<Node, Double> nodeScoreMultipliers);

    public abstract AgentAction toAgentAction(Agent agent);

}

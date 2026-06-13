package fr.cy.model.agent.behaviour.decisions;

import java.util.Map;
import java.util.Objects;
import java.util.Random;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.behaviour.agentActions.AgentAction;
import fr.cy.model.agent.behaviour.agentActions.FollowSingleEdgeAction;
import fr.cy.model.agent.behaviour.agentActions.WaitBeforeOtherAction;
import fr.cy.model.agent.properties.AgentDecisionalProperties;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;

public enum AgentPossibleEdgeDecision implements AgentDecision {
    /** Agent decides to go back to the node it came from. */
    BACKTRACK {
        @Override
        public double computeScore(Node currentTargetNode, AgentDecisionalProperties agentState,
                AgentPossibleEdgeDecision lastDecision, AgentAction lastAction,
                Map<Node, Double> nodeScoreMultipliers) {
            Edge currentEdge = Objects.requireNonNull(lastAction.getClosestTargetEdge());
            Node backtrackNode = Objects.requireNonNull(currentEdge.getOppositeNode(currentTargetNode));
            double targetNodeScore = nodeScoreMultipliers.get(currentTargetNode);

            double backtrackNodeScore = nodeScoreMultipliers.get(backtrackNode);
            return 2 * targetNodeScore < backtrackNodeScore ? 2*backtrackNodeScore*backtrackNodeScore : 0.0;
        }

        @Override
        public AgentAction toAgentAction(Agent agent) {
            System.out.println("old progress: " + agent.getCurrentEdgeProgress());
            double currentProgress = 1 - agent.getCurrentEdgeProgress();
            Edge currentEdge = Objects.requireNonNull(agent.getCurrentEdge());
            Node targetNode = Objects
                    .requireNonNull(currentEdge.getOppositeNode(agent.getCurrentNodeOrNextNodeIfOnEdge()));
            // System.out.println("Agent " + agent.getId() + " is backtracking to node " + targetNode.getId() + " with progress " + currentProgress);
            agent.setCurrentEdgeProgress(currentProgress); // update edge progress to reflect backtracking
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
    WAIT_BEFORE_ACTION {
        @Override
        public double computeScore(Node currentTargetNode, AgentDecisionalProperties agentState,
                AgentPossibleEdgeDecision lastDecision, AgentAction lastAction,
                Map<Node, Double> nodeScoreMultipliers) {
            Objects.requireNonNull(currentTargetNode);
            double continueScore = nodeScoreMultipliers.get(currentTargetNode);
            Edge currentEdge = Objects.requireNonNull(lastAction.getClosestTargetEdge());
            Node backtrackNode = Objects.requireNonNull(currentEdge.getOppositeNode(currentTargetNode));

            double backtrackNodeScore = nodeScoreMultipliers.get(backtrackNode);
            double waitScore = 1.0 / (1 + continueScore + backtrackNodeScore);
            if (lastDecision == WAIT_BEFORE_ACTION) {
                waitScore = 0; // discourage waiting multiple times in a row to prevent infinite loops of waiting
            }
            return 2 * continueScore < backtrackNodeScore ? waitScore : 0.0;
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

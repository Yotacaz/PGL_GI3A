package fr.cy.model.agent.behaviour.decisions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.agentActions.AgentAction;
import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Node;
import fr.cy.model.pathfinding.PathFinder;

public class DecisionContextProvider {
    private final Graph graph;
    private final PathFinder pathFinder;
    
    // private final 
    public DecisionContextProvider(Graph graph, PathFinder pathFinder) {
        this.graph = graph;
        this.pathFinder = pathFinder;
    }

    public DecisionContext constructContext(Agent agent) {
        // // Placeholder implementation - in a real implementation, these would be calculated based on the agent's state and environment
        // List<Node> recommendedPath = pathFinder.findPath(agent.getCurrentNode(), agent.getDestinationNode());
        // List<Agent> nearbyAgents = graph.getAgentsNearNode(agent.getCurrentNode());
        // double localStressLevel = 0.5; // Placeholder value
        // double localCrowdingLevel = 0.5; // Placeholder value

        // return new DecisionContext(recommendedPath, nearbyAgents, localStressLevel, localCrowdingLevel);
        return null;
    }
        
}

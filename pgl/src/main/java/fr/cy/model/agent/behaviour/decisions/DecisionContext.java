package fr.cy.model.agent.behaviour.decisions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.behaviour.agentActions.AgentAction;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;

public class DecisionContext {
    private List<Node> recommendedPath;
    private List<Agent> nearbyAgents;
    /**Read only map of agent actions for this tick*/
    private double localStressLevel;
    private double localCrowdingLevel;
    /* Map of edges to the number of agents currently taking or planning to next take that edge */
    private Map<Edge, Integer> numberOfAgentsTakingEdgeInSameDirection = new HashMap<>(); 

    public DecisionContext(List<Node> recommendedPath, List<Agent> nearbyAgents,
            double localStressLevel, double localCrowdingLevel) {
        this.recommendedPath = recommendedPath;
        this.nearbyAgents = nearbyAgents;
        this.localStressLevel = localStressLevel;
        this.localCrowdingLevel = localCrowdingLevel;
    }

    private void computeNumberOfAgentsTakingEdgeInSameDirection() {
        // for 
    }

    public double getLocalCrowdingLevel() {
        return localCrowdingLevel;
    }

    public double getLocalStressLevel() {
        return localStressLevel;
    }

    public List<Agent> getNearbyAgents() {
        return nearbyAgents;
    }

    public List<Node> getRecommendedPath() {
        return recommendedPath;
    }

}

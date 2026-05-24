package fr.cy.model.agent.behaviour.decisions;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.agentActions.AgentAction;
import fr.cy.model.graph.element.Node;

public class DecisionContext {
    private List<Node> recommendedPath;
    private List<Agent> nearbyAgents;
    /**Read only map of agent actions for this tick*/
    private Map<Agent, AgentAction> agentActionsThisTick;
    private Map<Agent, AgentAction> agentActionsPreviousTick;
    private double localStressLevel;
    private double localCrowdingLevel;

    public DecisionContext(List<Node> recommendedPath, List<Agent> nearbyAgents,
            Map<Agent, AgentAction> agentActionsThisTick, Map<Agent, AgentAction> agentActionsPreviousTick,
            double localStressLevel, double localCrowdingLevel) {
        this.recommendedPath = recommendedPath;
        this.nearbyAgents = nearbyAgents;
        this.agentActionsThisTick = Collections.unmodifiableMap(agentActionsThisTick);
        this.agentActionsPreviousTick = Collections.unmodifiableMap(agentActionsPreviousTick);
        this.localStressLevel = localStressLevel;
        this.localCrowdingLevel = localCrowdingLevel;
    }

    public Map<Agent, AgentAction> getAgentActionsThisTick() {
        return agentActionsThisTick;
    }

    public Map<Agent, AgentAction> getAgentActionsPreviousTick() {
        return agentActionsPreviousTick;
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

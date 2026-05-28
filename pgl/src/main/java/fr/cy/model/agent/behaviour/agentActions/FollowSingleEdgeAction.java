package fr.cy.model.agent.behaviour.agentActions;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.AgentSettings;
import fr.cy.model.graph.element.Edge;

public class FollowSingleEdgeAction extends AbstractMoveAction {
    private final Edge edgeToFollow;
    
    public FollowSingleEdgeAction(Agent agent, Edge edgeToFollow) {
        super(agent);
        this.edgeToFollow = edgeToFollow;
    }

    @Override
    public Edge getCurrentEdgeOrNextEdgeIfOnNode() {
        return edgeToFollow;
    }

    @Override
    public double perform(AgentSettings agentSettings) {
        double consumedTime = travelAlongEdge(agentSettings, edgeToFollow);
        return consumedTime;
    }

    public Edge getEdgeToFollow() {
        return edgeToFollow;
    }
}

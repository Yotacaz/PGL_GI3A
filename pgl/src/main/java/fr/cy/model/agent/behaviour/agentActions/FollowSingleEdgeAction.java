package fr.cy.model.agent.behaviour.agentActions;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.AgentSettings;
import fr.cy.model.agent.exceptions.AgentStateException;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;
import java.util.Objects;

public class FollowSingleEdgeAction extends AbstractMoveAction {
    private static final long serialVersionUID = 1L;
    private final Edge edgeToFollow;
    private final Node destinationNode;

    public FollowSingleEdgeAction(Agent agent, Edge edgeToFollow, Node destinationNode) {
        super(agent);
        this.destinationNode = Objects.requireNonNull(destinationNode);
        this.edgeToFollow = Objects.requireNonNull(edgeToFollow);
    }

    public FollowSingleEdgeAction(Agent agent, Edge edgeToFollow, Node destinationNode, double initialProgress) {
        this(agent, edgeToFollow, destinationNode);
        setProgress(initialProgress);
    }

    // @Override
    // public void setProgress(double newProgress) {
    //     super.setProgress(newProgress);
    //     setEdgeProgress(newProgress); // keep edge progress in sync with overall action progress
    // }

    @Override
    public void setEdgeProgress(double newProgress) {
        super.setEdgeProgress(newProgress);
        setProgress(newProgress); // keep edge progress in sync with overall action progress
    }


    @Override
    public Edge getClosestTargetEdge() {
        return edgeToFollow;
    }

    @Override
    public Node getClosestTargetNode() {
        return agent.isOnNode() ? agent.getCurrentNode() : destinationNode;
    }

    @Override
    public double perform(AgentSettings agentSettings, double availableTime) {
        if (getProgress() >= 1.0)
            throw new AgentStateException("Action should not be performed if already completed");
        double consumedTime = travelAlongEdge(agentSettings, edgeToFollow, availableTime);
        // setProgress(getEdgeProgress()); // single edge progress is the overall action progress
        
        return consumedTime;
    }

    public Edge getEdgeToFollow() {
        return edgeToFollow;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), edgeToFollow == null ? null : edgeToFollow.getId());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        FollowSingleEdgeAction other = (FollowSingleEdgeAction) obj;
        Integer e1 = edgeToFollow == null ? null : edgeToFollow.getId();
        Integer e2 = other.edgeToFollow == null ? null : other.edgeToFollow.getId();
        return Objects.equals(e1, e2);
    }

    @Override
    public String toString() {
        return super.toString().replace("}", "") + ", edgeToFollow="
                + (edgeToFollow == null ? "null" : edgeToFollow.toString()) + '}';
    }
}

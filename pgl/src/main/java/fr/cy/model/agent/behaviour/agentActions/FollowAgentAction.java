package fr.cy.model.agent.behaviour.agentActions;

import java.util.Objects;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.AgentSettings;
import fr.cy.model.graph.element.Edge;

/**
 * Action representing behavior where an agent follows another agent.
 */
public class FollowAgentAction extends AgentAction {
	private static final long serialVersionUID = 1L;

	private Agent targetAgent;

	public FollowAgentAction(Agent agent, Agent targetAgent) {
		super(agent);
		this.targetAgent = targetAgent;
	}

	@Override
	public double perform(AgentSettings agentSettings, double availableTime) {
		// Placeholder: implement follow-agent behavior
		return 0.0;
	}

	@Override
	public boolean isCompleted() {
		// Placeholder: determine if the follow action is completed
		return false;
	}

	@Override
	public Edge getClosestTargetGraphElement() {
		// TODO
		return targetAgent.getCurrentEdgeOrNextEdgeIfOnNode();
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), targetAgent == null ? null : targetAgent.getId());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		FollowAgentAction other = (FollowAgentAction) obj;
		Integer t1 = targetAgent == null ? null : targetAgent.getId();
		Integer t2 = other.targetAgent == null ? null : other.targetAgent.getId();
		return Objects.equals(t1, t2);
	}

	@Override
	public String toString() {
		return super.toString().replace("}", "") + ", targetAgentId="
				+ (targetAgent == null ? "null" : targetAgent.getId()) + '}';
	}
}

package fr.cy.model.agent.behaviour.properties.personalityTraits;

/**
 * Marker trait for agents with reduced mobility or special needs. Simulation
 * logic should check for this trait and adapt movement/assistance rules.
 */
public class DisabledAgent extends AgentPersonalityTrait {
	@Override
	public String toString() {
		return "DisabledAgent";
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj || (obj != null && this.getClass() == obj.getClass());
	}

	@Override
	public int hashCode() {
		return this.getClass().hashCode();
	}

}

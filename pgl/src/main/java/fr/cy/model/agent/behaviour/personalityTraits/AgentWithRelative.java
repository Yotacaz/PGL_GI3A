package fr.cy.model.agent.behaviour.personalityTraits;

import java.util.List;
import java.util.Objects;

import fr.cy.model.agent.Agent;

/**
 * Personality trait for agents that care about family members. The trait may
 * influence routing/decisions so that the agent stays close to its relatives.
 */
public class AgentWithRelative extends AgentPersonalityTrait {

    /** Level of attachment to family members, between 0 and 1 */
    private double attachmentLevel;

    private List<Agent> familyMembers;

    public AgentWithRelative(List<Agent> familyMembers) {
        this.familyMembers = familyMembers;
    }

    public boolean addRelative(Agent agent) {
        return familyMembers.add(agent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attachmentLevel, familyMembers);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AgentWithRelative other = (AgentWithRelative) obj;
        return Double.compare(attachmentLevel, other.attachmentLevel) == 0
                && Objects.equals(familyMembers, other.familyMembers);
    }

    @Override
    public String toString() {
        return "AgentWithRelative{" +
                "attachmentLevel=" + attachmentLevel +
                ", familySize=" + (familyMembers == null ? 0 : familyMembers.size()) +
                '}';
    }
}

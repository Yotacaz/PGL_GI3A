package fr.cy.model.agent.behaviour.properties.personalityTraits;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import fr.cy.model.agent.Agent;

/**
 * Personality trait for agents that care about family members. The trait may
 * influence routing/decisions so that the agent stays close to its relatives.
 */
public class AgentWithGroup extends AgentPersonalityTrait {

    /** Level of attachment to family members, between 0 and 1 */
    private double attachmentLevel;

    private List<Agent> familyMembers;

    public AgentWithGroup(List<Agent> familyMembers) {
        this.familyMembers = familyMembers;
    }

    public boolean addRelative(Agent agent) {
        return familyMembers.add(agent);
    }

    public double getAttachmentLevel() {
        return attachmentLevel;
    }

    public Map<Agent, Double> getDistanceFromRelatives(Agent agent) {
        if (familyMembers == null || familyMembers.isEmpty()) {
            return Collections.singletonMap(agent, Double.MAX_VALUE); // No relatives, so consider distance as infinite
        }
        
        return null;
        // return familyMembers.stream()
        //         .filter(Objects::nonNull)
        //         .collect(Collectors.toMap(
        //                 relative -> relative,
        //                 relative -> relative.get().distanceTo(agent.getCurrentGraphElement())
        //         ));
    }

    @Override
    public int hashCode() {
        return Objects.hash(attachmentLevel, familyMembers);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AgentWithGroup other = (AgentWithGroup) obj;
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

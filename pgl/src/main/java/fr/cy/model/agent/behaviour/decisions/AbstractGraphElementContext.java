package fr.cy.model.agent.behaviour.decisions;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import fr.cy.model.agent.Agent;
import fr.cy.model.graph.CongestionStats;
import fr.cy.model.graph.element.GraphElement;

abstract class AbstractGraphElementContext<T extends GraphElement> implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<T> accessibleElements;
    private final CongestionStats<T> congestionStats;

    AbstractGraphElementContext(List<T> accessibleElements) {
        this.accessibleElements = Objects.requireNonNull(accessibleElements, "accessibleElements");
        this.congestionStats = CongestionStats.computeCongestionStats(accessibleElements);
    }

    /**
     * @return the list of accessible graph elements (edges or nodes) that the agent can choose from, in an unmodifiable view
     */
    public List<T> getAccessibleElements() {
        return Collections.unmodifiableList(accessibleElements);
    }

    /** @return the congestion statistics for the accessible graph elements, used to inform decision-making can be null if there are no accessible elements 
    */
    public CongestionStats<T> getCongestionStatsForAccessibleElements() {
        return congestionStats;
    }

    /** @return the list of accessible graph elements sorted by congestion (least congested first) */
    public List<T> getSortedAccessibleElementsByCongestion() {
        return congestionStats == null ? Collections.emptyList() : Collections.unmodifiableList(congestionStats.getSortedByCongestion());
    }

    /**
     * Registers an agent's intent to use an outgoing {@code GraphElement}.
     * 
     * <p>This method checks if the graph element has sufficient capacity for the agent
     * and updates the space occupation data accordingly. If the graph element is already
     * at capacity, the registration fails.</p>
     * 
     * @param element the graph element the agent intends to use
     * @param agent the agent attempting to register its intent
     * @return true if the registration was successful, false if the graph element is at capacity
     */
    abstract boolean registerOutgoingIntent(T element, Agent agent);
}

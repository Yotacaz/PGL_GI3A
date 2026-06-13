package fr.cy.model.agent.context;

import java.io.Serializable;

import fr.cy.model.agent.Agent;
import fr.cy.model.graph.CongestionStats;
import fr.cy.model.graph.element.GraphElement;

abstract class AbstractGraphElementContext<T extends GraphElement> implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * The congestion statistics for the accessible graph elements, used to inform
     * decision-making
     */
    private final CongestionStats<T> congestionStatsForElements;

    /**
     * Constructs a new {@code AbstractGraphElementContext} with the specified
     * congestion statistics.
     * 
     * @param congestionStatsForElements the congestion statistics for the
     *                                   accessible graph elements
     */
    protected AbstractGraphElementContext(CongestionStats<T> congestionStatsForElements) {
        this.congestionStatsForElements = congestionStatsForElements;
    }

    /**
     * @return the congestion statistics for the accessible graph elements, used to
     *         inform decision-making can be null if there are no accessible
     *         elements
     */
    public CongestionStats<T> getCongestionStatsForAccessibleElements() {
        return congestionStatsForElements;
    }

    /**
     * Registers an agent's intent to use an outgoing {@code GraphElement}.
     * 
     * <p>
     * This method checks if the graph element has sufficient capacity for the agent
     * and updates the space occupation data accordingly. If the graph element is
     * already
     * at capacity, the registration fails.
     * </p>
     * 
     * @param element the graph element the agent intends to use
     * @param agent   the agent attempting to register its intent
     * @return true if the registration was successful, false if the graph element
     *         is at capacity
     */
    abstract boolean registerOutgoingIntent(T element, Agent agent);
}

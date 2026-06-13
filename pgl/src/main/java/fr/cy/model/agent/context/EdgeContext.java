package fr.cy.model.agent.context;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Objects;

import fr.cy.model.agent.Agent;
import fr.cy.model.graph.CongestionStats;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;

/** Context for decisions made while traversing an {@link Edge}. */
public class EdgeContext extends AbstractGraphElementContext<Node> {
    private static final long serialVersionUID = 1L;

    /** The edge from which the decision is made. */
    private final Edge sourceEdge;

    /**
     * @param sourceEdge      the edge from which the decision is made
     * @param accessibleNodes the list of nodes that can be accessed from the edge
     */
    EdgeContext(Edge sourceEdge) {
        super(sourceEdge.getNeighbors());
        this.sourceEdge = Objects.requireNonNull(sourceEdge, "sourceEdge");
    }


    /**
     * Returns the congestion statistics for the accessible outgoing nodes.
     *
     * @return the congestion statistics for the accessible outgoing nodes, can be
     *         null if there are no accessible nodes
     */
    public CongestionStats<Node> getCongestionStatsForOutgoingNodes() {
        return getCongestionStatsForAccessibleElements();
    }

    /**
     * Returns the list of accessible nodes sorted by congestion level.
     *
     * @return the list of accessible nodes sorted by congestion (least congested
     *         first)
     */
    public List<Node> getSortedAccessiblesNodesByCongestion() {
        return getSortedAccessibleElementsByCongestion();
    }

    /**
     * Registers that {@code agent} intends to enter {@code node}.
     */
    @Override
    boolean registerOutgoingIntent(Node node, Agent agent) {
        Objects.requireNonNull(node);
        Objects.requireNonNull(agent);

        if (node.equals(sourceEdge.getStart())) {
            // TODO check flux
        } else if (node.equals(sourceEdge.getEnd())) {
        } else {
            throw new InvalidParameterException("node" + node + "is not connected to the edge of this context");
        }

        return true;
    }

    /**
     * Returns the edge from which the decision is made.
     *
     * @return the source edge
     */
    public Edge getSourceEdge() {
        return sourceEdge;
    }

    @Override
    public int hashCode() {
        return sourceEdge.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        EdgeContext other = (EdgeContext) obj;
        return sourceEdge.equals(other.sourceEdge);
    }

    @Override
    public String toString() {
        return "DecisionEdgeContext{" +
                "sourceEdge=" + sourceEdge +
                '}';
    }
}

package fr.cy.model.pathfinding;

import fr.cy.model.graph.element.Node;

/**
 * Couple of a node and its associated distance.
 *
 * @param node the node
 * @param distance the distance from/to the node
 */
public record NodeDistance(Node node, double distance) implements Comparable<NodeDistance> {
    @Override
    public int compareTo(NodeDistance other) {
        return Double.compare(distance, other.distance);
    }
}

package fr.cy.model.pathfinding;

import fr.cy.model.graph.element.Node;

/**
 * Couple(Node, distance)
 */
public record NodeDistance(Node node, double distance) implements Comparable<NodeDistance> {
    @Override
    public int compareTo(NodeDistance other) {
        return Double.compare(distance, other.distance);
    }
}

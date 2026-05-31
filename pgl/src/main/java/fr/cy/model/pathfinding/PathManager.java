package fr.cy.model.pathfinding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;

public abstract class PathManager implements Serializable {
    private static final long serialVersionUID = 1L;

    public static List<Edge> computeEdgesFromNodes(List<Node> nodes) {
        List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < nodes.size() - 1; i++) {
            Node from = nodes.get(i);
            Node to = nodes.get(i + 1);
            Edge edge = from.getEdgeTo(to);
            if (edge == null) {
                throw new IllegalArgumentException("No edge between " + from + " and " + to);
            }
            edges.add(edge);
        }
        return edges;
    }
}

package fr.cy.model.pathfinding;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;

public class GraphPath implements Serializable {
    private static final long serialVersionUID = 1L;
    // TODO: this is an example of a path class, adapt it
    private List<Node> nodes;
    private List<Edge> edges;

    public GraphPath(List<Node> nodes) {
        this.nodes = nodes;
        this.edges = PathManager.computeEdgesFromNodes(nodes);
    }

    public Edge getEdgeAt(int index) {
        if (index < 0 || index >= edges.size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + edges.size());
        }
        return edges.get(index);
    }

    public Node getNodeAt(int index) {
        if (index < 0 || index >= nodes.size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + nodes.size());
        }
        return nodes.get(index);
    }

    public List<Node> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

}

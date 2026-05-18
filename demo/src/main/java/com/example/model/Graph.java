package com.example.model;

import java.util.ArrayList;
import java.util.List;

/*UTILISER HASHMAP */
public class Graph {

    private final List<Node> nodes;
    private final List<Edge> edges;

    public Graph() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    public Graph(List<Edge> edges, List<Node> nodes) {
        this.edges = edges;
        this.nodes = nodes;
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    public void removeNode(Node node) {
        edges.removeIf(edge -> edge.getStart().equals(node)
                || edge.getEnd().equals(node));

        nodes.remove(node);
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    public void removeEdge(Edge edge) {
        edges.remove(edge);
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public Node getNodeById(int id) {
        for (Node node : nodes) {
            if (node.getId() == id) {
                return node;
            }
        }

        return null;
    }

    public List<Node> getNeighbors(Node node) {
        List<Node> neighbors = new ArrayList<>();

        for (Edge edge : edges) {
            if (edge.getStart().equals(node)) {
                neighbors.add(edge.getEnd());
            }
            if (!edge.isDirected()
                    &&
                    edge.getEnd().equals(node)) {
                neighbors.add(edge.getStart());
            }
        }
        return neighbors;
    }

}

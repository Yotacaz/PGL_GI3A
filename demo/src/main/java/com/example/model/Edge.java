package com.example.model;

public class Edge {

    private final int id;

    private final Node start;
    private final Node end;

    private boolean directed;

    private int capacity;

    public Edge(int id, Node start, Node end, boolean directed, int capacity) {

        if (start == null || end == null) {
            throw new IllegalArgumentException(
                    "An edge must connect two nodes.");
        }

        this.id = id;
        this.start = start;
        this.end = end;

        this.directed = directed;

        this.capacity = capacity;
    }

    public int getId() {
        return id;
    }

    public Node getStart() {
        return start;
    }

    public Node getEnd() {
        return end;
    }

    public boolean isDirected() {
        return directed;
    }

    public void setDirected(boolean directed) {
        this.directed = directed;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {

        if (capacity < 1) {
            throw new IllegalArgumentException(
                    "Capacity must be positive.");
        }

        this.capacity = capacity;
    }

    public double getLength() {
        return start.distanceTo(end);
    }

    @Override
    public String toString() {

        String arrow = directed ? " -> " : " <-> ";

        return start + arrow + end;
    }
}

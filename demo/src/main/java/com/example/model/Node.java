package com.example.model;

public class Node {
    private int id;

    private int x;
    private int y;

    public Node(int id, int x, int y) {
        this.x = x;
        this.y = y;
        this.id = id;
    }

    @Override
    public String toString() {
        return "Node " + id + ":(" + x + ", + " + y + ")";
    }

    public int getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double distanceTo(Node node) {
        int dx = Math.abs(node.getX() - x);
        int dy = Math.abs(node.getY() - y);

        return Math.sqrt(dx * dx + dy * dy);
    }
}

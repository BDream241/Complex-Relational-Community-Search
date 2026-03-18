package model;


public class Edge {
    private final Vertex from;
    private final Vertex to;
    private final String label;

    public Edge(Vertex from, Vertex to, String label) {
        this.from = from;
        this.to = to;
        this.label = label;
    }

    public Vertex getFrom() {
        return from;
    }

    public Vertex getTo() {
        return to;
    }

    public String getLabel() {
        return label;
    }
}


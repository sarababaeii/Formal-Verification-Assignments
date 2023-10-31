package cmpt.sat;

import java.util.ArrayList;

public class Graph {
    private int numberOfVertices;
    private int numberOfColors;
    private ArrayList<ArrayList<Integer>> edges;

    public Graph(int numberOfVertices, int numberOfColors) {
        this.numberOfVertices = numberOfVertices;
        this.numberOfColors = numberOfColors;
        initializeEdges();
    }

    public Graph(int numberOfVertices, int numberOfColors, ArrayList<ArrayList<Integer>> edges) {
        this.numberOfVertices = numberOfVertices;
        this.numberOfColors = numberOfColors;
        this.edges = edges;
    }

    public int getNumberOfVertices() {
        return numberOfVertices;
    }

    public void setNumberOfVertices(int numberOfVertices) {
        this.numberOfVertices = numberOfVertices;
    }

    public int getNumberOfColors() {
        return numberOfColors;
    }

    public void setNumberOfColors(int numberOfColors) {
        this.numberOfColors = numberOfColors;
    }

    public ArrayList<ArrayList<Integer>> getEdges() {
        return edges;
    }

    public void setEdges(ArrayList<ArrayList<Integer>> edges) {
        this.edges = edges;
    }

    public void initializeEdges() {
        edges = new ArrayList<>(numberOfVertices);
        for (int i = 0; i < numberOfVertices; i++) {
            edges.add(new ArrayList<>());
        }
    }

    public void insertEdge(int v, int w) {
        edges.get(v).add(w);
        edges.get(w).add(v);
    }
}

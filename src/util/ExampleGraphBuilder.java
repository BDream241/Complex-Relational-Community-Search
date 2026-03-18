package util;

import model.HINGraph;
import model.Vertex;
import model.VertexType;

public class ExampleGraphBuilder {


    public static HINGraph buildExampleGraph() {
        HINGraph graph = new HINGraph();


        Vertex a1 = graph.addVertex(1, VertexType.AUTHOR, "A1");
        Vertex a2 = graph.addVertex(2, VertexType.AUTHOR, "A2");
        Vertex a3 = graph.addVertex(3, VertexType.AUTHOR, "A3");
        Vertex a4 = graph.addVertex(4, VertexType.AUTHOR, "A4");
        Vertex a5 = graph.addVertex(5, VertexType.AUTHOR, "A5");


        Vertex p2 = graph.addVertex(12, VertexType.PAPER, "P2");
        Vertex p3 = graph.addVertex(13, VertexType.PAPER, "P3");
        Vertex p4 = graph.addVertex(14, VertexType.PAPER, "P4");


        Vertex c1 = graph.addVertex(21, VertexType.CONFERENCE, "C1");
        Vertex c2 = graph.addVertex(22, VertexType.CONFERENCE, "C2");


        graph.addUndirectedEdge(a5.getId(), c2.getId(), "");
        graph.addUndirectedEdge(a5.getId(), c1.getId(), "");
        graph.addUndirectedEdge(a2.getId(), c1.getId(), "");
        graph.addUndirectedEdge(c2.getId(), a1.getId(), "");
        graph.addUndirectedEdge(c1.getId(), a1.getId(), "");


        graph.addUndirectedEdge(a1.getId(), p2.getId(), "");
        graph.addUndirectedEdge(a1.getId(), p3.getId(), "");
        graph.addUndirectedEdge(a1.getId(), p4.getId(), "");

        graph.addUndirectedEdge(p2.getId(), a3.getId(), "");
        graph.addUndirectedEdge(p2.getId(), a4.getId(), "");

        graph.addUndirectedEdge(p3.getId(), a3.getId(), "");
        graph.addUndirectedEdge(p3.getId(), a4.getId(), "");

        graph.addUndirectedEdge(p4.getId(), a4.getId(), "");

        return graph;
    }
}

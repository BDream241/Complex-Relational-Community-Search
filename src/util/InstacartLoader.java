package util;

import model.HINGraph;
import model.VertexType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InstacartLoader {

    private static final String DATASET_PATH = "src/Dataset/Instacart/";

    public static HINGraph loadGraph() {
        HINGraph graph = new HINGraph();
        long start = System.currentTimeMillis();

        try {
            // System.out.println("Loading Instacart Graph from " + DATASET_PATH + " ...");


            List<Integer> userIds = loadEntities(graph, "entity_user.txt", VertexType.USER);
            List<Integer> productIds = loadEntities(graph, "entity_product.txt", VertexType.PRODUCT);
            List<Integer> aisleIds = loadEntities(graph, "entity_aisle.txt", VertexType.AISLE);
            List<Integer> deptIds = loadEntities(graph, "entity_department.txt", VertexType.DEPARTMENT);

            // System.out.println("Entities loaded.");


            loadRelations(graph, "relation_user_product.txt", userIds, productIds, "user_product");
            loadRelations(graph, "relation_product_aisle.txt", productIds, aisleIds, "product_aisle");
            loadRelations(graph, "relation_product_department.txt", productIds, deptIds, "product_dept");

            // System.out.println("Graph loaded successfully in " + (System.currentTimeMillis() - start) + " ms");
            // System.out.println("Total Vertices: " + graph.getVertices().size());

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return graph;
    }

    private static List<Integer> loadEntities(HINGraph graph, String filename, VertexType type) throws IOException {
        List<Integer> globalIds = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(DATASET_PATH + filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                int localId = Integer.parseInt(parts[0]);
                String name = (parts.length > 2) ? parts[2] : "Unknown";

                int globalId = graph.getVertices().size();
                graph.addVertex(globalId, type, name);

                while (globalIds.size() <= localId) {
                    globalIds.add(-1);
                }
                globalIds.set(localId, globalId);
            }
        }
        return globalIds;
    }

    private static void loadRelations(HINGraph graph, String filename, List<Integer> srcMapping, List<Integer> dstMapping,
                                      String label) throws IOException {
        int count = 0;
        Set<String> existingEdges = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(DATASET_PATH + filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length < 2) continue;

                int srcLocalId = Integer.parseInt(parts[0]);
                int dstLocalId = Integer.parseInt(parts[1]);

                if (srcLocalId < srcMapping.size() && dstLocalId < dstMapping.size()) {
                    int srcGlobalId = srcMapping.get(srcLocalId);
                    int dstGlobalId = dstMapping.get(dstLocalId);

                    if (srcGlobalId != -1 && dstGlobalId != -1) {
                        int min = Math.min(srcGlobalId, dstGlobalId);
                        int max = Math.max(srcGlobalId, dstGlobalId);
                        String key = min + "-" + max;

                        if (!existingEdges.contains(key)) {
                            graph.addUndirectedEdge(srcGlobalId, dstGlobalId, label);
                            existingEdges.add(key);
                            count++;
                        }
                    }
                }
            }
        }
        // System.out.println("  Loaded " + count + " edges from " + filename);
    }
}

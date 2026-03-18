package util;

import model.VertexType;
import model.HINGraph;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DBLPLoader {


    private static final String DATASET_PATH = "src/Dataset/DBLP/";

    public static HINGraph loadGraph() {
        HINGraph graph = new HINGraph();
        long start = System.currentTimeMillis();

        try {
            // System.out.println("Loading DBLP Graph from " + DATASET_PATH + " ...");


            List<Integer> paperGlobalIds = loadEntities(graph, DATASET_PATH + "entity_paper.txt", "PAPER");
            List<Integer> authorGlobalIds = loadEntities(graph, DATASET_PATH + "entity_author.txt", "AUTHOR");
            List<Integer> confGlobalIds = loadEntities(graph, DATASET_PATH + "entity_conf.txt", "CONFERENCE");
            List<Integer> focusGlobalIds = loadEntities(graph, DATASET_PATH + "entity_focus.txt", "FOCUS");

            // System.out.println("Entities loaded:");
            // System.out.println("  Papers: " + paperGlobalIds.size());
            // System.out.println("  Authors: " + authorGlobalIds.size());
            // System.out.println("  Conferences: " + confGlobalIds.size());
            // System.out.println("  Focus Areas: " + focusGlobalIds.size());


            loadRelations(graph, DATASET_PATH + "relation_paper_author.txt", paperGlobalIds, authorGlobalIds, "paper_author");
            loadRelations(graph, DATASET_PATH + "relation_paper_conf.txt", paperGlobalIds, confGlobalIds, "paper_conf");
            loadRelations(graph, DATASET_PATH + "relation_paper_focus.txt", paperGlobalIds, focusGlobalIds, "paper_focus");

            // System.out.println("Graph loaded successfully in " + (System.currentTimeMillis() - start) + " ms");
            // System.out.println("Total Vertices: " + graph.getVertices().size());


        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return graph;
    }

    private static List<Integer> loadEntities(HINGraph graph, String filePath, String type) throws IOException {
        List<Integer> globalIds = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");


                int localId = Integer.parseInt(parts[0]);
                String name = (parts.length > 2) ? parts[2] : "Unknown";


                VertexType vType = VertexType.valueOf(type);
                int globalId = graph.getVertices().size();
                graph.addVertex(globalId, vType, name);


                while (globalIds.size() <= localId) {
                    globalIds.add(-1);
                }
                globalIds.set(localId, globalId);
            }
        }
        return globalIds;
    }

    private static void loadRelations(HINGraph graph, String filePath, List<Integer> srcMapping, List<Integer> dstMapping,
                                      String label) throws IOException {
        int count = 0;
        int duplicateCount = 0;
        Set<String> existingEdges = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
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
                        } else {
                            duplicateCount++;
                        }
                    }
                }
            }
            // System.out.println("  Total " + count + " edges loaded for " + label + " (Skipped " + duplicateCount + " duplicates)");
        }
    }
}

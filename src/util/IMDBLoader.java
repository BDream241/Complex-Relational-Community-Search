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

public class IMDBLoader {

    private static final String DATASET_PATH = "src/Dataset/IMDB/";

    public static HINGraph loadGraph() {
        HINGraph graph = new HINGraph();
        long start = System.currentTimeMillis();

        try {
            // System.out.println("Loading IMDB Graph from " + DATASET_PATH + " ...");


            List<Integer> movieIds = loadEntities(graph, "entity_movie.txt", VertexType.MOVIE);
            List<Integer> actorIds = loadEntities(graph, "entity_actor.txt", VertexType.ACTOR);
            List<Integer> directorIds = loadEntities(graph, "entity_director.txt", VertexType.DIRECTOR);
            List<Integer> writerIds = loadEntities(graph, "entity_writer.txt", VertexType.WRITER);
            List<Integer> producerIds = loadEntities(graph, "entity_producer.txt", VertexType.PRODUCER);
            List<Integer> composerIds = loadEntities(graph, "entity_composer.txt", VertexType.COMPOSER);
            List<Integer> cinematographerIds = loadEntities(graph, "entity_cinematographer.txt", VertexType.CINEMATOGRAPHER);
            List<Integer> editorIds = loadEntities(graph, "entity_editor.txt", VertexType.EDITOR);
            List<Integer> designerIds = loadEntities(graph, "entity_designer.txt", VertexType.DESIGNER);
            List<Integer> costumeIds = loadEntities(graph, "entity_costume.txt", VertexType.COSTUME);

            // System.out.println("Entities loaded.");


            loadRelations(graph, "relation_movie_actor.txt", movieIds, actorIds, "movie_actor");
            loadRelations(graph, "relation_movie_director.txt", movieIds, directorIds, "movie_director");
            loadRelations(graph, "relation_movie_writer.txt", movieIds, writerIds, "movie_writer");
            loadRelations(graph, "relation_movie_producer.txt", movieIds, producerIds, "movie_producer");
            loadRelations(graph, "relation_movie_composer.txt", movieIds, composerIds, "movie_composer");
            loadRelations(graph, "relation_movie_cinematographer.txt", movieIds, cinematographerIds, "movie_cinematographer");
            loadRelations(graph, "relation_movie_editor.txt", movieIds, editorIds, "movie_editor");
            loadRelations(graph, "relation_movie_designer.txt", movieIds, designerIds, "movie_designer");
            loadRelations(graph, "relation_movie_costume.txt", movieIds, costumeIds, "movie_costume");

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

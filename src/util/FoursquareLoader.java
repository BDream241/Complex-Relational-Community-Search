package util;

import model.HINGraph;
import model.Vertex;
import model.VertexType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class FoursquareLoader {


  private static List<Integer> userGlobalIds;
  private static List<Integer> venueGlobalIds;
  private static List<Integer> categoryGlobalIds;
  private static List<Integer> cityGlobalIds;
  private static List<Integer> dateGlobalIds;


  private static int globalIdCounter = 0;

  public static HINGraph loadGraph(String datasetPath) {
    HINGraph graph = new HINGraph();


    userGlobalIds = new ArrayList<>();
    venueGlobalIds = new ArrayList<>();
    categoryGlobalIds = new ArrayList<>();
    cityGlobalIds = new ArrayList<>();
    dateGlobalIds = new ArrayList<>();
    globalIdCounter = 0;

    long startTime = System.currentTimeMillis();
    // System.out.println("Loading Foursquare graph from: " + datasetPath);

    try {

      loadEntities(graph, datasetPath + "entity_users.txt", VertexType.USER, userGlobalIds);
      loadEntities(graph, datasetPath + "entity_venues.txt", VertexType.VENUE, venueGlobalIds);
      loadEntities(graph, datasetPath + "entity_categories.txt", VertexType.CATEGORY, categoryGlobalIds);
      loadEntities(graph, datasetPath + "entity_cities.txt", VertexType.CITY, cityGlobalIds);
      loadEntities(graph, datasetPath + "entity_dates.txt", VertexType.DATE, dateGlobalIds);

      // System.out.println("Entities loaded. Total vertices: " + globalIdCounter);


      loadRelations(graph, datasetPath + "relation_user_venue.txt", userGlobalIds, venueGlobalIds, "visit");
      loadRelations(graph, datasetPath + "relation_venue_category.txt", venueGlobalIds, categoryGlobalIds,
          "belongs_to");
      loadRelations(graph, datasetPath + "relation_venue_city.txt", venueGlobalIds, cityGlobalIds, "located_in");
      loadRelations(graph, datasetPath + "relation_user_date.txt", userGlobalIds, dateGlobalIds, "at_time");

    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }

    long endTime = System.currentTimeMillis();
    // System.out.println("Graph loaded in " + (endTime - startTime) + " ms.");
    // System.out.println("Total vertices in graph: " + graph.getVertices().size());


    userGlobalIds = null;
    venueGlobalIds = null;
    categoryGlobalIds = null;
    cityGlobalIds = null;
    dateGlobalIds = null;

    return graph;
  }

  private static void loadEntities(HINGraph graph, String filePath, VertexType type, List<Integer> mapping)
      throws IOException {
    // System.out.println("Loading entities from " + filePath + "...");
    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] parts = line.split("\t");
        if (parts.length >= 2) {
          int localId = Integer.parseInt(parts[0]);
          String name = parts[1];

          while (mapping.size() <= localId) {
            mapping.add(-1);
          }

          int globalId = globalIdCounter++;
          mapping.set(localId, globalId);

          graph.addVertex(globalId, type, name);
        }
      }
    }
  }

  private static void loadRelations(HINGraph graph, String filePath, List<Integer> srcMapping, List<Integer> dstMapping,
      String label) throws IOException {
    // System.out.println("Loading relations from " + filePath + "...");


    Set<String> existingEdges = new HashSet<>();

    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
      String line;
      int count = 0;
      int duplicateCount = 0;

      while ((line = br.readLine()) != null) {
        String[] parts = line.split("\t");
        if (parts.length >= 2) {
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
        if (count % 1000000 == 0 && count > 0) {
          // System.out.println("  Loaded " + count + " edges...");
        }
      }
      System.out
          .println("  Total " + count + " edges loaded for " + label + " (Skipped " + duplicateCount + " duplicates)");
    }
  }
}

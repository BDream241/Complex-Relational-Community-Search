import algorithm.GlobalCRCommunitySearcher;
import algorithm.GreedyCRCommunitySearcher;
import algorithm.LocalSearchCRCommunitySearcher;
import algorithm.LocalSearchExactCRCommunitySearcher;
import model.*;
import util.ExampleGraphBuilder;
import util.FoursquareLoader;
import util.DBLPLoader;
import util.IMDBLoader;
import util.InstacartLoader;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Set;


public class Main {

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);

    while (true) {
      System.out.println("\n==========================================");
      System.out.println("Please select the experiment to run (enter a number):");
      System.out.println("1. Simple Example from the Paper (A-P-A)");
      System.out.println("2. Foursquare Dataset");
      System.out.println("3. DBLP Dataset");
      System.out.println("4. IMDB Dataset");
      System.out.println("5. Instacart Dataset");
      System.out.println("0. Exit");
      System.out.println("==========================================");
      System.out.print("Please enter your choice: ");

      String input = scanner.nextLine();

      switch (input) {
        case "1":
          runPaperExample(scanner);
          break;
        case "2":
          runFoursquareExperiment(scanner);
          break;
        case "3":
          runDBLPExperiment(scanner);
          break;
        case "4":
          runIMDBExperiment(scanner);
          break;
        case "5":
          runInstacartExperiment(scanner);
          break;
        case "0":
          System.out.println("Exiting the program.");
          scanner.close();
          return;
        default:
          System.out.println("Invalid option, please try again.");
      }
    }
  }

  private static void runPaperExample(Scanner scanner) {
    System.out.println("==== Simple Example Experiment from the Paper ====");

    String algorithm = promptForAlgorithmSelection(scanner);
    if (algorithm == null)
      return;

    HINGraph graph = ExampleGraphBuilder.buildExampleGraph();

    System.out.println("Total number of vertices: " + graph.getVertices().size());

    MetaPath apa = new MetaPath(Arrays.asList(
            VertexType.AUTHOR,
            VertexType.PAPER,
            VertexType.AUTHOR));

    int k = 2;
    ComplexRelationalConstraint constraint = new ComplexRelationalConstraint(
            VertexType.AUTHOR,
            VertexType.AUTHOR,
            apa,
            k);
    List<ComplexRelationalConstraint> constraints = java.util.Collections.singletonList(constraint);

    Vertex query = graph.getVertex(1);
    int maxHops = 4;

    runAlgorithms(graph, constraints, query, maxHops, algorithm);
  }

  private static void runFoursquareExperiment(Scanner scanner) {
    System.out.println("==== Foursquare Dataset Experiment ====");

    String algorithm = promptForAlgorithmSelection(scanner);
    if (algorithm == null)
      return;

    // Hardcoded MetaPath: USER - VENUE - USER (4 types)
    MetaPath mp = MetaPathConstants.FOURSQUARE_METAPATHS.get(3);

    int k = promptForKValue(scanner);

    String datasetPath = "src/Dataset/FS/";
    HINGraph graph = FoursquareLoader.loadGraph(datasetPath);

    if (graph == null || graph.getVertices().isEmpty()) {
      System.err.println("Graph loading failed or the graph is empty. Please check whether there are data files under the path " + datasetPath + ".");
      return;
    }

    ComplexRelationalConstraint constraint = new ComplexRelationalConstraint(
            mp.get(0),
            mp.get(mp.length() - 1),
            mp,
            k);
    List<ComplexRelationalConstraint> constraints = java.util.Collections.singletonList(constraint);

    int queryId = 2460;
    Vertex query = graph.getVertex(queryId);

    if (query == null) {
      System.err.println("Vertex with ID " + queryId + " not found!");
      return;
    }

    System.out.println("Query vertex: name=" + query.getName() + ", id=" + query.getId() + ", type=" + query.getType());
    System.out.println("Constraint: MetaPath=" + mp + ", k=" + k);

    runAlgorithms(graph, constraints, query, 2, algorithm);
  }

  private static void runDBLPExperiment(Scanner scanner) {
    System.out.println("==== DBLP Dataset Experiment ====");
    String algorithm = promptForAlgorithmSelection(scanner);
    if (algorithm == null)
      return;

    // Hardcoded MetaPath: AUTHOR - PAPER - AUTHOR (3 types)
    MetaPath mp = MetaPathConstants.DBLP_METAPATHS.get(2);

    int k = promptForKValue(scanner);

    HINGraph graph = DBLPLoader.loadGraph();

    if (graph == null || graph.getVertices().isEmpty()) {
      System.err.println("Graph loading failed or the graph is empty.");
      return;
    }

    ComplexRelationalConstraint constraint = new ComplexRelationalConstraint(
            mp.get(0),
            mp.get(mp.length() - 1),
            mp,
            k);
    List<ComplexRelationalConstraint> constraints = java.util.Collections.singletonList(constraint);

    int queryId = 32747;
    Vertex query = graph.getVertex(queryId);

    if (query == null) {
      System.err.println("Vertex with ID " + queryId + " not found!");
      return;
    }

    System.out.println("Query vertex: name=" + query.getName() + ", id=" + query.getId() + ", type=" + query.getType());
    System.out.println("Constraint: MetaPath=" + mp + ", k=" + k);

    runAlgorithms(graph, constraints, query, 2, algorithm);
  }

  private static void runIMDBExperiment(Scanner scanner) {
    System.out.println("==== IMDB Dataset Experiment ====");
    String algorithm = promptForAlgorithmSelection(scanner);
    if (algorithm == null)
      return;

    // Hardcoded MetaPath: ACTOR - MOVIE - ACTOR (4 types)
    MetaPath mp = MetaPathConstants.IMDB_METAPATHS.get(3);

    int k = promptForKValue(scanner);

    HINGraph graph = IMDBLoader.loadGraph();

    if (graph == null || graph.getVertices().isEmpty()) {
      System.err.println("Graph loading failed or the graph is empty.");
      return;
    }

    ComplexRelationalConstraint constraint = new ComplexRelationalConstraint(
            mp.get(0),
            mp.get(mp.length() - 1),
            mp,
            k);
    List<ComplexRelationalConstraint> constraints = java.util.Collections.singletonList(constraint);

    int queryId = 797869;
    Vertex query = graph.getVertex(queryId);

    if (query == null) {
      System.err.println("Vertex with ID " + queryId + " not found!");
      return;
    }

    System.out.println("Query vertex: name=" + query.getName() + ", id=" + query.getId() + ", type=" + query.getType());
    System.out.println("Constraint: MetaPath=" + mp + ", k=" + k);

    runAlgorithms(graph, constraints, query, 2, algorithm);
  }

  private static void runInstacartExperiment(Scanner scanner) {
    System.out.println("==== Instacart Dataset Experiment ====");
    String algorithm = promptForAlgorithmSelection(scanner);
    if (algorithm == null)
      return;

    // Hardcoded MetaPath: USER - PRODUCT - USER (3 types)
    MetaPath mp = MetaPathConstants.INSTACART_METAPATHS.get(2);

    int k = promptForKValue(scanner);

    HINGraph graph = InstacartLoader.loadGraph();

    if (graph == null || graph.getVertices().isEmpty()) {
      System.err.println("Graph loading failed or the graph is empty.");
      return;
    }

    ComplexRelationalConstraint constraint = new ComplexRelationalConstraint(
            mp.get(0),
            mp.get(mp.length() - 1),
            mp,
            k);
    List<ComplexRelationalConstraint> constraints = java.util.Collections.singletonList(constraint);

    int queryId = 286;
    Vertex query = graph.getVertex(queryId);

    if (query == null) {
      System.err.println("Vertex with ID " + queryId + " not found!");
      return;
    }

    System.out.println("Query vertex: name=" + query.getName() + ", id=" + query.getId() + ", type=" + query.getType());
    System.out.println("Constraint: MetaPath=" + mp + ", k=" + k);

    runAlgorithms(graph, constraints, query, 2, algorithm);
  }

  private static int promptForKValue(Scanner scanner) {
    while (true) {
      System.out.print("Please enter the value of k (e.g., 2): ");
      String input = scanner.nextLine();
      try {
        int k = Integer.parseInt(input);
        if (k > 0)
          return k;
      } catch (NumberFormatException ignored) {
      }
      System.out.println("Invalid value of k, please enter a positive integer.");
    }
  }



  private static String promptForAlgorithmSelection(Scanner scanner) {

    while (true) {
      System.out.println("\n------------------------------------------");
      System.out.println("Please select the algorithm to run:");
      System.out.println("1. Global (Global Search - Exact)");
      System.out.println("2. Greedy (Greedy Search - Approximate)");
      System.out.println("3. Local Search Exact (Local Search - Exact)");
      System.out.println("4. Local Search Approx (Local Search - Approximate)");
      System.out.println("5. ALL (Run All)");
      System.out.println("0. Return to the previous menu");
      System.out.println("------------------------------------------");
      System.out.print("Please enter your choice: ");

      String input = scanner.nextLine();

      switch (input) {
        case "1":
          return "Global";
        case "2":
          return "Greedy";
        case "3":
          return "LocalSearchExact";
        case "4":
          return "LocalSearchApprox";
        case "5":
          return "ALL";
        case "0":
          return null;
        default:
          System.out.println("Invalid option, please try again.");
      }
    }
  }

  private static void runAlgorithms(HINGraph graph, List<ComplexRelationalConstraint> constraints, Vertex query,
                                    int maxHops, String algorithm) {
    System.out.println("Running algorithm: " + algorithm);
    long start = System.currentTimeMillis();

    if (algorithm.equals("Global") || algorithm.equals("ALL")) {
      runGlobal(graph, constraints, query, maxHops);
    }
    if (algorithm.equals("Greedy") || algorithm.equals("ALL")) {
      runGreedy(graph, constraints, query, maxHops);
    }
    if (algorithm.equals("LocalSearchExact") || algorithm.equals("ALL")) {
      runLocalSearchExact(graph, constraints, query, maxHops);
    }
    if (algorithm.equals("LocalSearchApprox") || algorithm.equals("ALL")) {
      runLocalSearchApprox(graph, constraints, query);
    }

    long end = System.currentTimeMillis();
    System.out.println("Total time: " + (end - start) + " ms");
  }

  private static void runGlobal(HINGraph graph, List<ComplexRelationalConstraint> constraints, Vertex query,
                                int maxHops) {
    System.out.println("\n--- Starting Global Search ---");
    long start = System.currentTimeMillis();
    try {
      GlobalCRCommunitySearcher globalSearcher = new GlobalCRCommunitySearcher(graph, constraints);
      Set<Vertex> globalCom = globalSearcher.search(query, maxHops);
      printCommunity("Algorithm 1 - Global", globalCom);
    } catch (Throwable e) {
      System.out.println("  (No community satisfying the constraints was found - Exception occurred: " + e.getMessage() + ")");

    }
    System.out.println("Global Search took: " + (System.currentTimeMillis() - start) + " ms");
  }

  private static void runGreedy(HINGraph graph, List<ComplexRelationalConstraint> constraints, Vertex query,
                                int maxHops) {
    System.out.println("\n--- Starting Greedy Search ---");
    long start = System.currentTimeMillis();
    try {
      GreedyCRCommunitySearcher greedySearcher = new GreedyCRCommunitySearcher(graph, constraints);
      Set<Vertex> greedyCom = greedySearcher.search(query, maxHops);
      printCommunity("Algorithm 2 - Greedy", greedyCom);
    } catch (Throwable e) {
      System.out.println("  (No community satisfying the constraints was found - Exception occurred: " + e.getMessage() + ")");

    }
    System.out.println("Greedy Search took: " + (System.currentTimeMillis() - start) + " ms");
  }

  private static void runLocalSearchExact(HINGraph graph, List<ComplexRelationalConstraint> constraints, Vertex query,
                                          int maxHops) {
    System.out.println("\n--- Starting Local Search Exact ---");
    long start = System.currentTimeMillis();
    try {
      LocalSearchExactCRCommunitySearcher exactLocalSearcher = new LocalSearchExactCRCommunitySearcher(graph,
              constraints);
      Set<Vertex> exactLocalCom = exactLocalSearcher.search(query, maxHops);
      printCommunity("Algorithm 3 - Local Search Exact", exactLocalCom);
    } catch (Throwable e) {
      System.out.println("  (No community satisfying the constraints was found - Exception occurred: " + e.getMessage() + ")");

    }
    System.out.println("Local Search Exact took: " + (System.currentTimeMillis() - start) + " ms");
  }

  private static void runLocalSearchApprox(HINGraph graph, List<ComplexRelationalConstraint> constraints,
                                           Vertex query) {
    System.out.println("\n--- Starting Local Search Approx ---");
    long start = System.currentTimeMillis();
    try {
      LocalSearchCRCommunitySearcher approxLocalSearcher = new LocalSearchCRCommunitySearcher(graph, constraints);
      Set<Vertex> approxLocalCom = approxLocalSearcher.search(query);
      printCommunity("Algorithm 4 - Local Search Approx", approxLocalCom);
    } catch (Throwable e) {
      System.out.println("  (No community satisfying the constraints was found - Exception occurred: " + e.getMessage() + ")");

    }
    System.out.println("Local Search Approx took: " + (System.currentTimeMillis() - start) + " ms");
  }

  private static void printCommunity(String title, Set<Vertex> community) {
    System.out.println(title + " Result:");
    if (community == null || community.isEmpty()) {
      System.out.println("  (No community satisfying the constraints was found)");
      return;
    }
    System.out.println("  Community Size: " + community.size());

    if (community.size() > 20) {
      System.out.println("  (Showing first 20 vertices)");
      community.stream()
              .limit(20)
              .forEach(v -> System.out.println("  name=" + v.getName() + ", id=" + v.getId() + " - " + v.getType()));
    } else {
      community.stream()
              .sorted((v1, v2) -> Integer.compare(v1.getId(), v2.getId()))
              .forEach(v -> System.out.println("  name=" + v.getName() + ", id=" + v.getId() + " - " + v.getType()));
    }
  }
}


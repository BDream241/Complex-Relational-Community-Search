package algorithm;

import model.*;

import java.util.*;
public class GlobalCRCommunitySearcher extends AbstractCRCommunitySearcher {

  private static class PathInstance {
    Vertex start;
    Vertex end;
    MetaPath metaPath;
    List<Vertex> vertices;

    PathInstance(Vertex start, Vertex end, MetaPath metaPath, List<Vertex> vertices) {
      this.start = start;
      this.end = end;
      this.metaPath = metaPath;
      this.vertices = vertices;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      for (int i = 0; i < vertices.size(); i++) {
        sb.append(vertices.get(i).getId());
        if (i < vertices.size() - 1) {
          sb.append(" -> ");
        }
      }
      sb.append("]");
      return sb.toString();
    }
  }

  private Map<Vertex, Map<MetaPath, List<PathInstance>>> metaPathIndex;
  private Map<Vertex, Map<MetaPath, Map<Vertex, Integer>>> pairCounts;
  private Map<Vertex, List<PathInstance>> invertedIndex;
  private Set<PathInstance> invalidPaths;

  public GlobalCRCommunitySearcher(HINGraph graph,
      List<ComplexRelationalConstraint> constraints) {
    super(graph, constraints);
    this.metaPathIndex = new HashMap<>();
    this.pairCounts = new HashMap<>();
    this.invertedIndex = new HashMap<>();
    this.invalidPaths = new HashSet<>();
  }

  public Set<Vertex> search(Vertex query, int maxHops) {
    Set<Vertex> workingSet = new HashSet<>(graph.getVerticesWithinHops(query, maxHops));

    buildIndices(workingSet);

    pruneUnqualifiedVerticesOptimized(workingSet);

    if (!workingSet.contains(query)) {
      return Collections.emptySet();
    }

    Set<Vertex> H = connectedComponentContaining(query, workingSet);

    if (!satisfiesAllConstraints(H)) {
      return Collections.emptySet();
    }

    Set<Vertex> hMin = searchMinCom(H, query);

    return hMin != null ? hMin : Collections.emptySet();
  }


  private void buildIndices(Set<Vertex> vertices) {

    // 清理原有索引
    metaPathIndex.clear();
    pairCounts.clear();
    invertedIndex.clear();
    invalidPaths.clear();

    Runtime runtime = Runtime.getRuntime();

    System.gc();
    try { Thread.sleep(100); } catch (InterruptedException e) {}

    long startTime = System.currentTimeMillis();
    long peakMemory = runtime.totalMemory() - runtime.freeMemory();

    int totalPaths = 0;
    int totalPathLength = 0;

    for (Vertex v : vertices) {

      Map<MetaPath, List<PathInstance>> vIndex = new HashMap<>();

      for (ComplexRelationalConstraint c : constraints) {
        MetaPath mp = c.getMetaPath();

        if (v.getType() == mp.get(0)) {

          List<List<Vertex>> rawPaths = new ArrayList<>();
          List<Vertex> currentPath = new ArrayList<>();
          currentPath.add(v);

          dfsCollectPathInstances(v, mp, 0, vertices, rawPaths, currentPath);

          List<PathInstance> instances = new ArrayList<>();

          for (List<Vertex> rawPath : rawPaths) {

            Vertex end = rawPath.get(rawPath.size() - 1);
            PathInstance pi = new PathInstance(v, end, mp, rawPath);

            instances.add(pi);

            totalPaths++;
            totalPathLength += rawPath.size();

            incrementPairCount(v, end, mp);

            for (Vertex nodeOnPath : rawPath) {
              invertedIndex
                      .computeIfAbsent(nodeOnPath, k -> new ArrayList<>())
                      .add(pi);
            }

            long currentMemory = runtime.totalMemory() - runtime.freeMemory();
            if (currentMemory > peakMemory) peakMemory = currentMemory;
          }

          vIndex.put(mp, instances);
        }
      }

      metaPathIndex.put(v, vIndex);
    }

    long endTime = System.currentTimeMillis();

    long memoryPathInstances = totalPaths * 48L + totalPaths * 32L + totalPathLength * 8L;

    long memoryInvertedIndex = totalPathLength * 8L;

    long memoryPairCounts = totalPaths * 16L;

    long totalApproxMemory = memoryPathInstances + memoryInvertedIndex + memoryPairCounts;

    System.out.println("Build time: " + (endTime - startTime) + " ms");
    System.out.println("Total path instances: " + totalPaths);
    System.out.println("Total path length sum: " + totalPathLength);
    System.out.println("Peak memory during build: " + (peakMemory / 1024) + " KB");
    System.out.println("Approx total index memory: " + (totalApproxMemory / 1024) + " KB");
  }




  private void incrementPairCount(Vertex start, Vertex end, MetaPath mp) {
    pairCounts.computeIfAbsent(start, k -> new HashMap<>())
        .computeIfAbsent(mp, k -> new HashMap<>())
        .merge(end, 1, Integer::sum);
  }

  private void decrementPairCount(Vertex start, Vertex end, MetaPath mp) {
    if (pairCounts.containsKey(start) && pairCounts.get(start).containsKey(mp)) {
      Map<Vertex, Integer> targetMap = pairCounts.get(start).get(mp);
      if (targetMap.containsKey(end)) {
        int newCount = targetMap.get(end) - 1;
        if (newCount <= 0) {
          targetMap.remove(end);
        } else {
          targetMap.put(end, newCount);
        }
      }
    }
  }

  private int getPairCount(Vertex start, Vertex end, MetaPath mp) {
    if (!pairCounts.containsKey(start))
      return 0;
    if (!pairCounts.get(start).containsKey(mp))
      return 0;
    return pairCounts.get(start).get(mp).getOrDefault(end, 0);
  }

  private void dfsCollectPathInstances(Vertex current, MetaPath mp, int idx, Set<Vertex> scope,
      List<List<Vertex>> result, List<Vertex> currentPath) {
    if (idx == mp.length() - 1) {
      if (!current.equals(currentPath.get(0))) {
        result.add(new ArrayList<>(currentPath));
      }
      return;
    }
    VertexType nextType = mp.get(idx + 1);
    for (Edge e : current.getOutgoing()) {
      Vertex next = e.getTo();
      if (scope.contains(next) && next.getType() == nextType) {
        if (currentPath.contains(next)) {
          continue;
        }
        currentPath.add(next);
        dfsCollectPathInstances(next, mp, idx + 1, scope, result, currentPath);
        currentPath.remove(currentPath.size() - 1);
      }
    }
  }

  private void pruneUnqualifiedVerticesOptimized(Set<Vertex> workingSet) {
    Queue<Vertex> queue = new LinkedList<>();

    for (Vertex v : workingSet) {
      if (!isVertexQualifiedUsingCounts(v)) {
        queue.add(v);
      }
    }

    while (!queue.isEmpty()) {
      Vertex vToRemove = queue.poll();
      if (!workingSet.contains(vToRemove))
        continue;

      workingSet.remove(vToRemove);

      List<PathInstance> affectedPaths = invertedIndex.get(vToRemove);
      if (affectedPaths != null) {
        for (PathInstance path : affectedPaths) {
          if (invalidPaths.contains(path))
            continue;

          invalidPaths.add(path);

          decrementPairCount(path.start, path.end, path.metaPath);

          if (workingSet.contains(path.start)) {
            if (!isVertexQualifiedUsingCounts(path.start)) {
              queue.add(path.start);
            }
          }
          if (workingSet.contains(path.end)) {
            if (!isVertexQualifiedUsingCounts(path.end)) {
              queue.add(path.end);
            }
          }
        }
      }
    }
  }

  private boolean isVertexQualifiedUsingCounts(Vertex v) {
    for (ComplexRelationalConstraint c : constraints) {
      boolean isSource = (v.getType() == c.getSourceType());
      boolean isTarget = (v.getType() == c.getTargetType());
      if (!isSource && !isTarget)
        continue;

      boolean constraintSatisfied = false;

      if (isSource) {
        Map<Vertex, Integer> targets = pairCounts.getOrDefault(v, Collections.emptyMap())
            .getOrDefault(c.getMetaPath(), Collections.emptyMap());

        for (int count : targets.values()) {
          if (count >= c.getMinCount()) {
            constraintSatisfied = true;
            break;
          }
        }
      }

      if (!constraintSatisfied && isTarget) {
        for (Vertex u : pairCounts.keySet()) {
          int cnt = getPairCount(u, v, c.getMetaPath());
          if (cnt >= c.getMinCount()) {
            constraintSatisfied = true;
            break;
          }
        }
      }

      if (!constraintSatisfied)
        return false;
    }
    return true;
  }

  private Set<Vertex> searchMinCom(Set<Vertex> H, Vertex query) {
    if (!H.contains(query))
      return null;

    if (!isConnected(H) || !satisfiesAllConstraints(H)) {
      return null;
    }

    Set<Vertex> bestMinH = H;
    List<Vertex> candidates = new ArrayList<>(H);
    candidates.remove(query);

    candidates.sort(Comparator.comparingInt(v -> graph.getNeighbors(v).size()));

    for (Vertex v : candidates) {
      Set<Vertex> nextH = new HashSet<>(H);
      nextH.remove(v);

      Set<Vertex> componentQ = connectedComponentContaining(query, nextH);
      if (componentQ.isEmpty())
        continue;

      if (componentQ.size() >= bestMinH.size()) {
        continue;
      }

      Set<Vertex> result = searchMinCom(componentQ, query);
      if (result != null) {
        if (result.size() < bestMinH.size()) {
          bestMinH = result;
        }
      }
    }
    return bestMinH;
  }
}

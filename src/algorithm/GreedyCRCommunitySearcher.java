package algorithm;

import model.*;
import java.util.*;


public class GreedyCRCommunitySearcher extends AbstractCRCommunitySearcher {


  private Map<Vertex, Map<MetaPath, Map<Vertex, Integer>>> pairCounts;

  private Map<Vertex, List<PathInstance>> invertedIndex;

  private Set<PathInstance> invalidPaths;

  private static class PathInstance {
    Vertex start, end;
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
      return vertices.toString();
    }
  }

  public GreedyCRCommunitySearcher(HINGraph graph,
      List<ComplexRelationalConstraint> constraints) {
    super(graph, constraints);
    this.pairCounts = new HashMap<>();
    this.invertedIndex = new HashMap<>();
    this.invalidPaths = new HashSet<>();
  }

  public Set<Vertex> search(Vertex query, int maxHops) {

    Set<Vertex> workingSet = new HashSet<>(graph.getVerticesWithinHops(query, maxHops));
    if (!workingSet.contains(query))
      return Collections.emptySet();


    Set<VertexType> involvedTypes = new HashSet<>();
    for (ComplexRelationalConstraint c : constraints) {
      MetaPath mp = c.getMetaPath();
      for (int i = 0; i < mp.length(); i++) {
        involvedTypes.add(mp.get(i));
      }
    }
    workingSet.removeIf(v -> !involvedTypes.contains(v.getType()));

    if (!workingSet.contains(query))
      return Collections.emptySet();


    buildIndices(workingSet);


    pruneUnqualifiedVerticesOptimized(workingSet);
    if (!workingSet.contains(query))
      return Collections.emptySet();


    Set<Vertex> HCurrent = connectedComponentContaining(query, workingSet);
    if (HCurrent.isEmpty())
      return Collections.emptySet();


    Map<Vertex, Double> priority = new HashMap<>();
    for (Vertex v : HCurrent) {
      priority.put(v, computeGreedyPriority(v, query));
    }


    Deque<Vertex> Q = new ArrayDeque<>();


    while (true) {

      Vertex w = findLowestPriorityVertex(HCurrent, priority, query);
      if (w == null)
        break;


      Set<Vertex> trialH = new HashSet<>(HCurrent);
      Deque<Vertex> trialQ = new ArrayDeque<>();
      trialQ.push(w);


      boolean qRemoved = false;
      Set<Vertex> removedInThisRound = new HashSet<>();

      while (!trialQ.isEmpty()) {
        Vertex u = trialQ.pop();
        if (!trialH.contains(u))
          continue;

        trialH.remove(u);
        removedInThisRound.add(u);

        if (u.equals(query)) {
          qRemoved = true;
          break;
        }


        for (Edge e : u.getOutgoing()) {
          Vertex v = e.getTo();
          if (trialH.contains(v) && !isVertexQualified(v, trialH)) {
            trialQ.push(v);
          }
        }
      }

      if (qRemoved) {


        break;
      }


      Set<Vertex> comp = connectedComponentContaining(query, trialH);
      if (comp.size() < trialH.size()) {

        trialH = comp;
      }

      if (satisfiesAllConstraints(trialH)) {

        HCurrent = trialH;

      } else {


        break;
      }
    }

    return HCurrent;
  }


  private double computeGreedyPriority(Vertex u, Vertex q) {
    Set<Vertex> nu = new HashSet<>(graph.getNeighbors(u));
    nu.add(u);
    Set<Vertex> nq = new HashSet<>(graph.getNeighbors(q));
    nq.add(q);
    nu.retainAll(nq);
    int interSize = nu.size();

    int dist = graph.shortestDistance(u, q);
    if (dist <= 0 || dist == Integer.MAX_VALUE) {
      dist = Integer.MAX_VALUE / 2;
    }
    return (double) interSize / (double) dist;
  }

  private Vertex findLowestPriorityVertex(Set<Vertex> community,
      Map<Vertex, Double> priority,
      Vertex query) {
    Vertex best = null;
    double bestVal = Double.POSITIVE_INFINITY;
    for (Vertex v : community) {
      if (v.equals(query))
        continue;

      Double p = priority.get(v);
      if (p == null)
        continue;

      if (p < bestVal) {
        bestVal = p;
        best = v;
      }
    }
    return best;
  }


  private void buildIndices(Set<Vertex> vertices) {
    pairCounts.clear();
    invertedIndex.clear();
    invalidPaths.clear();

    for (Vertex v : vertices) {
      for (ComplexRelationalConstraint c : constraints) {
        MetaPath mp = c.getMetaPath();
        if (v.getType() == mp.get(0)) {
          List<List<Vertex>> rawPaths = new ArrayList<>();
          List<Vertex> currentPath = new ArrayList<>();
          currentPath.add(v);
          dfsCollectPathInstances(v, mp, 0, vertices, rawPaths, currentPath);

          for (List<Vertex> rawPath : rawPaths) {
            Vertex end = rawPath.get(rawPath.size() - 1);
            PathInstance pi = new PathInstance(v, end, mp, rawPath);
            incrementPairCount(v, end, mp);
            for (Vertex nodeOnPath : rawPath) {
              invertedIndex.computeIfAbsent(nodeOnPath, k -> new ArrayList<>()).add(pi);
            }
          }
        }
      }
    }
  }


  private void dfsCollectPathInstances(Vertex current, MetaPath mp, int idx, Set<Vertex> scope,
      List<List<Vertex>> result, List<Vertex> currentPath) {
    if (idx == mp.length() - 1) {
      if (!current.equals(currentPath.get(0)))
        result.add(new ArrayList<>(currentPath));
      return;
    }
    VertexType nextType = mp.get(idx + 1);
    for (Edge e : current.getOutgoing()) {
      Vertex next = e.getTo();
      if (scope.contains(next) && next.getType() == nextType) {
        currentPath.add(next);
        dfsCollectPathInstances(next, mp, idx + 1, scope, result, currentPath);
        currentPath.remove(currentPath.size() - 1);
      }
    }
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
        if (newCount <= 0)
          targetMap.remove(end);
        else
          targetMap.put(end, newCount);
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

  private void pruneUnqualifiedVerticesOptimized(Set<Vertex> workingSet) {
    Queue<Vertex> queue = new LinkedList<>();
    for (Vertex v : workingSet) {
      if (!isVertexQualifiedUsingCounts(v))
        queue.add(v);
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
          if (workingSet.contains(path.start) && !isVertexQualifiedUsingCounts(path.start))
            queue.add(path.start);
          if (workingSet.contains(path.end) && !isVertexQualifiedUsingCounts(path.end))
            queue.add(path.end);
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
      boolean satisfied = false;
      if (isSource) {
        Map<Vertex, Integer> targets = pairCounts.getOrDefault(v, Collections.emptyMap()).getOrDefault(c.getMetaPath(),
            Collections.emptyMap());
        for (int count : targets.values())
          if (count >= c.getMinCount()) {
            satisfied = true;
            break;
          }
      }
      if (!satisfied && isTarget) {
        for (Vertex u : pairCounts.keySet()) {
          if (getPairCount(u, v, c.getMetaPath()) >= c.getMinCount()) {
            satisfied = true;
            break;
          }
        }
      }
      if (!satisfied)
        return false;
    }
    return true;
  }
}

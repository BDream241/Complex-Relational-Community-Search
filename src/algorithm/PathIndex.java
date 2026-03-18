package algorithm;

import model.*;

import java.util.*;


public class PathIndex {


  private final Map<Vertex, Map<MetaPath, Map<Vertex, List<PathInstance>>>> forwardIndex;


  private final Map<Vertex, List<PathInstance>> invertedIndex;


  private final Map<Vertex, Map<MetaPath, Map<Vertex, Integer>>> pairCounts;

  public PathIndex() {
    this.forwardIndex = new HashMap<>();
    this.invertedIndex = new HashMap<>();
    this.pairCounts = new HashMap<>();
  }


  public void build(Set<Vertex> vertices, List<ComplexRelationalConstraint> constraints) {
    clear();

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

            if (v.equals(end))
              continue;


            PathInstance pi = new PathInstance(v, end, mp, rawPath);

            addPathInstance(pi);
          }
        }
      }
    }


  }


  private void addPathInstance(PathInstance pi) {

    forwardIndex.computeIfAbsent(pi.start, k -> new HashMap<>())
        .computeIfAbsent(pi.metaPath, k -> new HashMap<>())
        .computeIfAbsent(pi.end, k -> new ArrayList<>())
        .add(pi);


    for (Vertex nodeOnPath : pi.vertices) {
      invertedIndex.computeIfAbsent(nodeOnPath, k -> new ArrayList<>()).add(pi);
    }


    pairCounts.computeIfAbsent(pi.start, k -> new HashMap<>())
        .computeIfAbsent(pi.metaPath, k -> new HashMap<>())
        .merge(pi.end, 1, Integer::sum);

  }

  public void clear() {
    forwardIndex.clear();
    invertedIndex.clear();
    pairCounts.clear();
  }


  public List<PathInstance> getPaths(Vertex start, Vertex end, MetaPath mp) {
    if (!forwardIndex.containsKey(start))
      return Collections.emptyList();
    if (!forwardIndex.get(start).containsKey(mp))
      return Collections.emptyList();
    return forwardIndex.get(start).get(mp).getOrDefault(end, Collections.emptyList());
  }

  public Map<Vertex, List<PathInstance>> getNeighbors(Vertex start, MetaPath mp) {
    if (!forwardIndex.containsKey(start))
      return Collections.emptyMap();
    return forwardIndex.get(start).getOrDefault(mp, Collections.emptyMap());
  }

  public List<PathInstance> getPathsInvolving(Vertex v) {
    return invertedIndex.getOrDefault(v, Collections.emptyList());
  }

  public int getCount(Vertex start, Vertex end, MetaPath mp) {
    if (!pairCounts.containsKey(start))
      return 0;
    if (!pairCounts.get(start).containsKey(mp))
      return 0;
    return pairCounts.get(start).get(mp).getOrDefault(end, 0);
  }


  public void decrementCount(Vertex start, Vertex end, MetaPath mp) {
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


  private void dfsCollectPathInstances(Vertex current, MetaPath mp, int idx, Set<Vertex> scope,
      List<List<Vertex>> result, List<Vertex> currentPath) {

    if (idx == mp.length() - 1) {
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

  public static class PathInstance {
    public Vertex start, end;
    public MetaPath metaPath;
    public List<Vertex> vertices;

    public PathInstance(Vertex start, Vertex end, MetaPath metaPath, List<Vertex> vertices) {
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
}

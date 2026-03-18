package algorithm;

import model.*;

import java.util.*;

public class LocalSearchExactCRCommunitySearcher extends AbstractCRCommunitySearcher {

  private PathIndex pathIndex;

  public LocalSearchExactCRCommunitySearcher(HINGraph graph,
      List<ComplexRelationalConstraint> constraints) {
    super(graph, constraints);
    this.pathIndex = new PathIndex();
  }

  public Set<Vertex> search(Vertex query, int maxHops) {

    Set<Vertex> working = new HashSet<>(graph.getVerticesWithinHops(query, maxHops));
    if (!working.contains(query)) {
      return Collections.emptySet();
    }

    pathIndex.build(working, constraints);

    List<Set<Vertex>> res = new ArrayList<>();
    Set<Vertex> rec = new HashSet<>();
    rec.add(query);

    int idx = 0;

    Map<Integer, List<Set<Vertex>>> map = new HashMap<>();

    for (ComplexRelationalConstraint theta : constraints) {
      idx++;

      List<Set<Vertex>> tmpList = new ArrayList<>();

      VertexType t1 = theta.getSourceType();
      VertexType t2 = theta.getTargetType();

      int k = theta.getMinCount();

      MetaPath mp = theta.getMetaPath();

      Set<Vertex> ST1 = new HashSet<>();
      for (Vertex v : rec) {
        if (v.getType() == t1) {
          ST1.add(v);
        }
      }

      for (Vertex v : ST1) {

        Map<Vertex, List<PathIndex.PathInstance>> neighbors = pathIndex.getNeighbors(v, mp);
        if (!neighbors.isEmpty()) {

          for (Map.Entry<Vertex, List<PathIndex.PathInstance>> entry : neighbors.entrySet()) {

            Vertex u = entry.getKey();

            List<PathIndex.PathInstance> paths = entry.getValue();

            if (u.equals(v) || u.getType() != t2) {
              continue;
            }

            if (paths.size() >= k) {

              Set<Vertex> scom = new HashSet<>();

              // Only take k paths to minimize community size
              for (int i = 0; i < k; i++) {
                scom.addAll(paths.get(i).vertices);
              }

              scom.add(v);
              scom.add(u);

              rec.addAll(scom);

              tmpList.add(scom);
            }
          }
        }
      }

      if (tmpList.isEmpty()) {

        tmpList.add(new HashSet<>());
      }
      map.put(idx - 1, tmpList);
    }

    int constraintCount = idx;

    List<Set<Vertex>> cur = new ArrayList<>(Collections.nCopies(constraintCount, null));

    generateCombinations(map, cur, 0, constraintCount, res, query);

    Set<Vertex> hMin = new HashSet<>(working);

    boolean found = false;

    for (Set<Vertex> R : res) {

      if (!R.contains(query)) {
        continue;
      }

      if (!isConnected(R) || !satisfiesAllConstraints(R)) {
        continue;
      }

      if (!found || R.size() < hMin.size()) {
        hMin = R;
        found = true;
      }
    }

    return found ? hMin : Collections.emptySet();
  }

  private void generateCombinations(Map<Integer, List<Set<Vertex>>> map,
      List<Set<Vertex>> cur,
      int index,
      int constraintCount,
      List<Set<Vertex>> res,
      Vertex query) {

    if (index == constraintCount) {

      Set<Vertex> union = new HashSet<>();
      union.add(query);

      for (Set<Vertex> s : cur) {

        if (s != null) {
          union.addAll(s);
        }
      }

      res.add(union);
      return;
    }

    List<Set<Vertex>> options = map.get(index);
    if (options == null || options.isEmpty()) {

      cur.set(index, new HashSet<>());
      generateCombinations(map, cur, index + 1, constraintCount, res, query);
      return;
    }

    for (Set<Vertex> choice : options) {
      cur.set(index, choice);
      generateCombinations(map, cur, index + 1, constraintCount, res, query);
    }
  }
}

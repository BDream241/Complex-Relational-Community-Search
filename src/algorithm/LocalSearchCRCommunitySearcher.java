package algorithm;

import model.*;

import java.util.*;


public class LocalSearchCRCommunitySearcher extends AbstractCRCommunitySearcher {

  public LocalSearchCRCommunitySearcher(HINGraph graph,
      List<ComplexRelationalConstraint> constraints) {
    super(graph, constraints);
  }


  public Set<Vertex> search(Vertex query) {
    Set<Vertex> H = new HashSet<>();
    H.add(query);

    for (ComplexRelationalConstraint theta : constraints) {
      MetaPath mp = theta.getMetaPath();

      VertexType T1 = mp.get(0);

      if (mp.length() < 2)
        continue;
      VertexType T2 = mp.get(1);

      int k = theta.getMinCount();


      Set<Vertex> ST1 = new HashSet<>();
      for (Vertex v : H) {
        if (v.getType() == T1) {
          ST1.add(v);
        }
      }


      Set<Vertex> scope = new HashSet<>(graph.getVertices());

      for (Vertex u : ST1) {


        Map<Vertex, Set<Vertex>> validNeighborsAndPaths = new HashMap<>();


        for (Edge e : u.getOutgoing()) {
          Vertex v = e.getTo();
          if (!scope.contains(v) || v.getType() != T2) {
            continue;
          }


          Set<Vertex> remainingPathVertices = new HashSet<>();


          int pathCount = collectRemainingPathVertices(v, mp, 1, scope, k, remainingPathVertices);

          if (pathCount > 0) {
            validNeighborsAndPaths.put(v, remainingPathVertices);
          }
        }

        if (validNeighborsAndPaths.isEmpty()) {
          continue;
        }

        Set<Vertex> ST2 = validNeighborsAndPaths.keySet();


        List<Vertex> sorted = new ArrayList<>(ST2);
        sorted.sort((v1, v2) -> {
          double p1 = computeLocalPriority(v1, u, T1);
          double p2 = computeLocalPriority(v2, u, T1);

          return -Double.compare(p1, p2);
        });


        int kEff = Math.min(k, sorted.size());
        List<Vertex> topK = sorted.subList(0, kEff);
        List<Vertex> others = sorted.subList(kEff, sorted.size());

        LinkedHashSet<Vertex> Pre = new LinkedHashSet<>();
        Pre.addAll(topK);
        Pre.addAll(others);


        LinkedHashSet<Vertex> prefix = new LinkedHashSet<>();

        for (Vertex cand : Pre) {
          prefix.add(cand);
          Set<Vertex> trial = new HashSet<>(H);


          for (Vertex vCand : prefix) {
            trial.add(vCand);
            Set<Vertex> pathNodes = validNeighborsAndPaths.get(vCand);
            if (pathNodes != null) {
              trial.addAll(pathNodes);
            }
          }


          Set<Vertex> refinedTrial = new HashSet<>(trial);
          boolean changed = true;
          while (changed && !refinedTrial.isEmpty()) {
              Set<Vertex> unqualified = collectUnqualifiedVertices(refinedTrial);
              if (unqualified.isEmpty()) {
                  changed = false;
              } else {
                  refinedTrial.removeAll(unqualified);
                  changed = true;
              }
          }

          if (refinedTrial.contains(query) && isConnected(refinedTrial)) {
            H = refinedTrial;
            break;
          } else {


          }
        }
      }
    }

    return H;
  }


  private int collectRemainingPathVertices(Vertex current,
      MetaPath metaPath,
      int index,
      Set<Vertex> scope,
      int limit,
      Set<Vertex> outVertices) {
    if (index == metaPath.length() - 1) {

      outVertices.add(current);
      return 1;
    }

    VertexType nextType = metaPath.get(index + 1);
    int totalPaths = 0;


    for (Edge e : current.getOutgoing()) {
      Vertex next = e.getTo();
      if (scope.contains(next) && next.getType() == nextType) {
        int found = collectRemainingPathVertices(next, metaPath, index + 1, scope, limit - totalPaths, outVertices);
        if (found > 0) {
          totalPaths += found;
          outVertices.add(next);
          if (totalPaths >= limit)
            break;
        }
      }
    }

    if (totalPaths > 0) {
      outVertices.add(current);
    }

    return totalPaths;
  }

  private double computeLocalPriority(Vertex v, Vertex u, VertexType t1) {

    int t1Neighbors = 0;
    for (Vertex nb : graph.getNeighbors(v)) {
      if (nb.getType() == t1) {
        t1Neighbors++;
      }
    }
    int dist = graph.shortestDistance(v, u);
    if (dist <= 0 || dist == Integer.MAX_VALUE) {
      dist = Integer.MAX_VALUE / 2;
    }
    return (double) t1Neighbors / (double) dist;
  }
}

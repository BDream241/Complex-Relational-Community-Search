package model;

import java.util.*;


public class HINGraph {
  private final Map<Integer, Vertex> vertices = new HashMap<>();

  public Vertex addVertex(int id, VertexType type) {
    if (vertices.containsKey(id)) {
      throw new IllegalArgumentException("Vertex with id " + id + " already exists.");
    }
    Vertex v = new Vertex(id, type);
    vertices.put(id, v);
    return v;
  }

  public Vertex addVertex(int id, VertexType type, String name) {
    if (vertices.containsKey(id)) {
      throw new IllegalArgumentException("Vertex with id " + id + " already exists.");
    }
    Vertex v = new Vertex(id, type, name);
    vertices.put(id, v);
    return v;
  }

  public Vertex getVertex(int id) {
    return vertices.get(id);
  }

  public Collection<Vertex> getVertices() {
    return vertices.values();
  }


  public void addUndirectedEdge(int fromId, int toId, String label) {
    Vertex from = vertices.get(fromId);
    Vertex to = vertices.get(toId);
    if (from == null || to == null) {
      throw new IllegalArgumentException("Both vertices must exist before adding an edge.");
    }
    Edge e1 = new Edge(from, to, label);
    Edge e2 = new Edge(to, from, label);
    from.addOutgoingEdge(e1);
    to.addOutgoingEdge(e2);
  }

  public Set<Vertex> getNeighbors(Vertex v) {
    Set<Vertex> res = new HashSet<>();
    for (Edge e : v.getOutgoing()) {
      res.add(e.getTo());
    }
    return res;
  }


  public Set<Vertex> getVerticesWithinHops(Vertex source, int maxHops) {
    Set<Vertex> visited = new HashSet<>();
    Queue<Vertex> queue = new ArrayDeque<>();
    Map<Vertex, Integer> depth = new HashMap<>();

    visited.add(source);
    queue.add(source);
    depth.put(source, 0);

    while (!queue.isEmpty()) {
      Vertex cur = queue.poll();
      int d = depth.get(cur);
      if (d == maxHops) {
        continue;
      }
      for (Edge e : cur.getOutgoing()) {
        Vertex w = e.getTo();
        if (!visited.contains(w)) {
          visited.add(w);
          queue.add(w);
          depth.put(w, d + 1);
        }
      }
    }
    return visited;
  }


  public int shortestDistance(Vertex src, Vertex dst) {
    return shortestDistance(src, dst, null);
  }


  public int shortestDistance(Vertex src, Vertex dst, Set<Vertex> allowed) {
    if (src.equals(dst)) {
      return 0;
    }
    Set<Vertex> visited = new HashSet<>();
    Queue<Vertex> queue = new ArrayDeque<>();
    Map<Vertex, Integer> depth = new HashMap<>();

    if (allowed != null && (!allowed.contains(src) || !allowed.contains(dst))) {
      return Integer.MAX_VALUE;
    }

    visited.add(src);
    queue.add(src);
    depth.put(src, 0);

    while (!queue.isEmpty()) {
      Vertex cur = queue.poll();
      int d = depth.get(cur);
      for (Edge e : cur.getOutgoing()) {
        Vertex w = e.getTo();
        if (allowed != null && !allowed.contains(w)) {
          continue;
        }
        if (!visited.contains(w)) {
          if (w.equals(dst)) {
            return d + 1;
          }
          visited.add(w);
          queue.add(w);
          depth.put(w, d + 1);
        }
      }
    }
    return Integer.MAX_VALUE;
  }
}

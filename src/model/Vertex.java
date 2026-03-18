package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Vertex {
  private final int id;
  private final VertexType type;
  private String name;
  private final Map<String, Object> attributes = new HashMap<>();
  private final List<Edge> outgoing = new ArrayList<>();

  public Vertex(int id, VertexType type) {
    this.id = id;
    this.type = type;
    this.name = String.valueOf(id);
  }

  public Vertex(int id, VertexType type, String name) {
    this.id = id;
    this.type = type;
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public VertexType getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<String, Object> getAttributes() {
    return attributes;
  }

  public void setAttribute(String key, Object value) {
    attributes.put(key, value);
  }

  public Object getAttribute(String key) {
    return attributes.get(key);
  }

  public List<Edge> getOutgoing() {
    return outgoing;
  }

  public void addOutgoingEdge(Edge edge) {
    outgoing.add(edge);
  }

  @Override
  public String toString() {

    return "Vertex{" +
        "name='" + name + '\'' +
        ", id=" + id +
        ", type=" + type +
        '}';
  }
}

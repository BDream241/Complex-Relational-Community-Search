package algorithm;
import model.*;
import java.util.*;


public abstract class AbstractCRCommunitySearcher {
    protected final HINGraph graph;
    protected final List<ComplexRelationalConstraint> constraints;

    protected AbstractCRCommunitySearcher(HINGraph graph,
                                          List<ComplexRelationalConstraint> constraints) {
        this.graph = graph;
        this.constraints = constraints;
    }


    protected boolean isVertexQualified(Vertex v, Set<Vertex> community) {
        if (!community.contains(v)) {
            return false;
        }
        for (ComplexRelationalConstraint c : constraints) {
            boolean involvesType =
                    v.getType() == c.getSourceType() || v.getType() == c.getTargetType();
            if (!involvesType) {
                continue;
            }

            boolean okForThisConstraint = false;
            for (Vertex u : community) {
                if (u.equals(v)) {
                    continue;
                }
                boolean typeMatchForward =
                        v.getType() == c.getSourceType() && u.getType() == c.getTargetType();
                boolean typeMatchBackward =
                        v.getType() == c.getTargetType() && u.getType() == c.getSourceType();
                if (!typeMatchForward && !typeMatchBackward) {
                    continue;
                }

                Vertex src = typeMatchForward ? v : u;
                Vertex dst = typeMatchForward ? u : v;
                int cnt = countMetaPathInstances(
                        src,
                        dst,
                        c.getMetaPath(),
                        community,
                        c.getMinCount()
                );
                if (cnt >= c.getMinCount()) {
                    okForThisConstraint = true;
                    break;
                }
            }

            if (!okForThisConstraint) {
                return false;
            }
        }
        return true;
    }


    protected Set<Vertex> collectUnqualifiedVertices(Set<Vertex> community) {
        Set<Vertex> res = new HashSet<>();
        for (Vertex v : community) {
            if (!isVertexQualified(v, community)) {
                res.add(v);
            }
        }
        return res;
    }


    protected boolean satisfiesAllConstraints(Set<Vertex> community) {
        if (community.isEmpty()) {
            return false;
        }


        for (Vertex v : community) {
            if (!isVertexQualified(v, community)) {
                return false;
            }
        }
        return true;
    }


    protected boolean satisfiesConstraint(Set<Vertex> community,
                                          ComplexRelationalConstraint constraint) {

        return true;
    }


    protected int countMetaPathInstances(Vertex u,
                                         Vertex v,
                                         MetaPath metaPath,
                                         Set<Vertex> community,
                                         int limit) {
        if (metaPath.length() < 2) {
            return 0;
        }

        if (u.getType() != metaPath.get(0) || v.getType() != metaPath.get(metaPath.length() - 1)) {
            return 0;
        }

        int[] count = new int[]{0};
        Set<Vertex> visited = new HashSet<>();
        visited.add(u);
        dfsMetaPath(u, v, metaPath, 0, community, visited, limit, count);
        return count[0];
    }

    private void dfsMetaPath(Vertex current,
                             Vertex target,
                             MetaPath metaPath,
                             int index,
                             Set<Vertex> community,
                             Set<Vertex> visited,
                             int limit,
                             int[] count) {
        if (count[0] >= limit) {
            return;
        }
        if (index == metaPath.length() - 1) {
            if (current.equals(target)) {
                count[0]++;
            }
            return;
        }

        VertexType nextType = metaPath.get(index + 1);
        for (Edge e : current.getOutgoing()) {
            Vertex next = e.getTo();
            if (!community.contains(next)) {
                continue;
            }
            if (next.getType() != nextType) {
                continue;
            }
            if (visited.contains(next)) {
                continue;
            }

            visited.add(next);
            dfsMetaPath(next, target, metaPath, index + 1, community, visited, limit, count);
            visited.remove(next);

            if (count[0] >= limit) {
                return;
            }
        }
    }


    protected boolean isConnected(Set<Vertex> community) {
        if (community.isEmpty()) {
            return false;
        }
        Iterator<Vertex> it = community.iterator();
        Vertex start = it.next();

        Set<Vertex> visited = new HashSet<>();
        Queue<Vertex> queue = new ArrayDeque<>();
        visited.add(start);
        queue.add(start);

        while (!queue.isEmpty()) {
            Vertex cur = queue.poll();
            for (Edge e : cur.getOutgoing()) {
                Vertex nxt = e.getTo();
                if (!community.contains(nxt)) {
                    continue;
                }
                if (visited.add(nxt)) {
                    queue.add(nxt);
                }
            }
        }

        return visited.size() == community.size();
    }


    protected Set<Vertex> connectedComponentContaining(Vertex start, Set<Vertex> community) {
        Set<Vertex> result = new HashSet<>();
        if (start == null || !community.contains(start)) {
            return result;
        }
        Queue<Vertex> queue = new ArrayDeque<>();
        result.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            Vertex cur = queue.poll();
            for (Edge e : cur.getOutgoing()) {
                Vertex nxt = e.getTo();
                if (!community.contains(nxt) || result.contains(nxt)) {
                    continue;
                }
                result.add(nxt);
                queue.add(nxt);
            }
        }
        return result;
    }
}


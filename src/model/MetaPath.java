package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MetaPath {
    private final List<VertexType> types;

    public MetaPath(List<VertexType> types) {
        if (types == null || types.size() < 2) {
            throw new IllegalArgumentException("MetaPath must have at least 2 vertex types.");
        }
        this.types = new ArrayList<>(types);
    }

    public List<VertexType> getTypes() {
        return Collections.unmodifiableList(types);
    }

    public int length() {
        return types.size();
    }

    public VertexType get(int index) {
        return types.get(index);
    }

    @Override
    public String toString() {
        return "MetaPath" + types;
    }
}


package model;


public class ComplexRelationalConstraint {
    private final VertexType sourceType;
    private final VertexType targetType;
    private final MetaPath metaPath;
    private final int minCount;


    public ComplexRelationalConstraint(VertexType sourceType,
                                       VertexType targetType,
                                       MetaPath metaPath,
                                       int minCount) {
        this.sourceType = sourceType;
        this.targetType = targetType;
        this.metaPath = metaPath;
        if (minCount <= 0) {
            throw new IllegalArgumentException("minCount must be positive.");
        }
        this.minCount = minCount;
    }

    public VertexType getSourceType() {
        return sourceType;
    }

    public VertexType getTargetType() {
        return targetType;
    }

    public MetaPath getMetaPath() {
        return metaPath;
    }

    public int getMinCount() {
        return minCount;
    }
}


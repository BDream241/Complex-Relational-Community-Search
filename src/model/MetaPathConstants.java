package model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class MetaPathConstants {


    public static final List<MetaPath> DBLP_METAPATHS = Collections.unmodifiableList(Arrays.asList(
        new MetaPath(Arrays.asList(VertexType.AUTHOR, VertexType.PAPER, VertexType.AUTHOR)),
        new MetaPath(Arrays.asList(VertexType.AUTHOR, VertexType.PAPER, VertexType.CONFERENCE, VertexType.PAPER, VertexType.AUTHOR)),
        new MetaPath(Arrays.asList(VertexType.AUTHOR, VertexType.PAPER, VertexType.FOCUS, VertexType.PAPER, VertexType.AUTHOR))
    ));


    public static final List<MetaPath> FOURSQUARE_METAPATHS = Collections.unmodifiableList(Arrays.asList(
        new MetaPath(Arrays.asList(VertexType.USER, VertexType.VENUE, VertexType.USER)),
        new MetaPath(Arrays.asList(VertexType.USER, VertexType.DATE, VertexType.USER)),
        new MetaPath(Arrays.asList(VertexType.USER, VertexType.VENUE, VertexType.CATEGORY, VertexType.VENUE, VertexType.USER)),
        new MetaPath(Arrays.asList(VertexType.USER, VertexType.VENUE, VertexType.CITY, VertexType.VENUE, VertexType.USER))
    ));


    public static final List<MetaPath> IMDB_METAPATHS = Collections.unmodifiableList(Arrays.asList(
        new MetaPath(Arrays.asList(VertexType.ACTOR, VertexType.MOVIE, VertexType.ACTOR)),
        new MetaPath(Arrays.asList(VertexType.ACTOR, VertexType.MOVIE, VertexType.DIRECTOR, VertexType.MOVIE, VertexType.ACTOR)),
        new MetaPath(Arrays.asList(VertexType.ACTOR, VertexType.MOVIE, VertexType.WRITER, VertexType.MOVIE, VertexType.ACTOR)),
        new MetaPath(Arrays.asList(VertexType.ACTOR, VertexType.MOVIE, VertexType.PRODUCER, VertexType.MOVIE, VertexType.ACTOR))
    ));


    public static final List<MetaPath> INSTACART_METAPATHS = Collections.unmodifiableList(Arrays.asList(
        new MetaPath(Arrays.asList(VertexType.USER, VertexType.PRODUCT, VertexType.USER)),
        new MetaPath(Arrays.asList(VertexType.USER, VertexType.PRODUCT, VertexType.AISLE, VertexType.PRODUCT, VertexType.USER)),
        new MetaPath(Arrays.asList(VertexType.USER, VertexType.PRODUCT, VertexType.DEPARTMENT, VertexType.PRODUCT, VertexType.USER))
    ));

    public static List<MetaPath> getMetaPathsForDataset(int choice) {
        switch (choice) {
            case 2: return FOURSQUARE_METAPATHS;
            case 3: return DBLP_METAPATHS;
            case 4: return IMDB_METAPATHS;
            case 5: return INSTACART_METAPATHS;
            default: return Collections.emptyList();
        }
    }
}

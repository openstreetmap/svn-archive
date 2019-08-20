package reverter;

import org.openstreetmap.josm.data.osm.PrimitiveId;

public interface ParseCallback {
    void primitiveParsed(PrimitiveId id);
}

package iodb;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.User;

/**
 *
 * @author zverik
 */
public class CalibrationObject extends ImageryOffsetBase {
    private OsmPrimitive object;
    private long lastUserId;

    public CalibrationObject(OsmPrimitive object, long lastUserId) {
        this.object = object;
        this.lastUserId = lastUserId;
    }

    public CalibrationObject(OsmPrimitive object) {
        this(object, -1);
    }

    public long getLastUserId() {
        return lastUserId;
    }

    public OsmPrimitive getObject() {
        return object;
    }
}

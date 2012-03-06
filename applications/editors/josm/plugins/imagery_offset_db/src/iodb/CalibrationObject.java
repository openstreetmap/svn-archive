package iodb;

import java.util.Map;
import org.openstreetmap.josm.data.osm.*;

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
        this(object, getLastUserId(object));
    }

    public long getLastUserId() {
        return lastUserId;
    }

    public OsmPrimitive getObject() {
        return object;
    }
    
    private static long getLastUserId( OsmPrimitive object ) {
        return object.getUser() == null ? -1 : object.getUser().getId(); // todo?
    }

    @Override
    public void putServerParams( Map<String, String> map ) {
        super.putServerParams(map);
        map.put("object", object instanceof Node ? "node" : "way");
        map.put("id", String.valueOf(object.getId()));
        map.put("lastuser", String.valueOf(lastUserId));
    }
    
}

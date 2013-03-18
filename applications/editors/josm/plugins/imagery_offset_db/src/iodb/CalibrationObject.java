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
        this(object, 0);
    }

    public long getLastUserId() {
        return lastUserId;
    }

    public OsmPrimitive getObject() {
        return object;
    }
    
    @Override
    public void putServerParams( Map<String, String> map ) {
        super.putServerParams(map);
        map.put("object", object instanceof Node ? "node" : "way");
        map.put("id", String.valueOf(object.getId()));
    }

    @Override
    public String toString() {
        return "CalibrationObject{" + "object=" + object + ", lastUserId=" + lastUserId + "position=" + position + ", date=" + date + ", author=" + author + ", description=" + description + ", abandonDate=" + abandonDate + '}';
    }
}

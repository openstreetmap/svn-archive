package iodb;

import java.util.Map;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;

/**
 *
 * @author zverik
 */
public class CalibrationObject extends ImageryOffsetBase {
    private LatLon[] geometry;

    public CalibrationObject(LatLon[] geometry) {
        this.geometry = geometry;
    }

    public CalibrationObject( OsmPrimitive p ) {
        if( p instanceof Node )
            geometry = new LatLon[] { ((Node)p).getCoor() };
        else if( p instanceof Way ) {
            geometry = new LatLon[((Way)p).getNodesCount()];
            for( int i = 0; i < geometry.length; i++ )
                geometry[i] = ((Way)p).getNode(i).getCoor();
        } else
            throw new IllegalArgumentException("Calibration Object can be created either from node or a way");
    }

    public LatLon[] getGeometry() {
        return geometry;
    }

    @Override
    public void putServerParams( Map<String, String> map ) {
        super.putServerParams(map);
        StringBuilder sb = new StringBuilder();
        for( int i = 0; i < geometry.length; i++ ) {
            if( i > 0 )
                sb.append(',');
            sb.append(geometry[i].lon()).append(' ').append(geometry[i].lat());
        }
        map.put("geometry", sb.toString());
    }

    @Override
    public String toString() {
        return "CalibrationObject{" + geometry.length + "nodes; position=" + position + ", date=" + date + ", author=" + author + ", description=" + description + ", abandonDate=" + abandonDate + '}';
    }
}

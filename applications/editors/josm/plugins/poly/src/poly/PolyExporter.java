package poly;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.io.OsmExporter;

/**
 * Writes poly files.
 *
 * @author zverik
 */
public class PolyExporter extends OsmExporter {

    public PolyExporter() {
        super(PolyType.FILE_FILTER);
    }

    @Override
    public void exportData( File file, Layer layer ) throws IOException {
        if( layer instanceof OsmDataLayer ) {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"))) {
                DataSet ds = ((OsmDataLayer)layer).data;
                Map<Way, Boolean> ways = new TreeMap<>();
                String polygonName = file.getName();
                if( polygonName.indexOf('.') > 0 )
                    polygonName = polygonName.substring(0, polygonName.indexOf('.'));
                for( Way w : ds.getWays() ) {
                    if( w.isClosed() ) {
                        boolean outer = isOuter(w);
                        ways.put(w, outer);
                        if( w.hasKey("name") )
                            polygonName = w.get("name").replace("\n", " ");
                    }
                }
                ways = sortOuterInner(ways);

                int counter = 1;
                writer.write(polygonName);
                writer.newLine();
                for( Way w : ways.keySet() ) {
                    if( !ways.get(w) )
                        writer.write('!');
                    writer.write(String.valueOf(counter++));
                    writer.newLine();
                    for( Node n : w.getNodes() ) {
                        writer.write(String.format(Locale.ENGLISH, "   %f   %f", n.getCoor().lon(), n.getCoor().lat()));
                        writer.newLine();
                    }
                    writer.write("END");
                    writer.newLine();
                }
                writer.write("END");
                writer.newLine();
            }
        }
    }

    private boolean isOuter( Way w ) {
        for( OsmPrimitive p : w.getReferrers() ) {
            if( p instanceof Relation && ((Relation)p).isMultipolygon() ) {
                for( RelationMember m : ((Relation)p).getMembers() ) {
                    if( m.refersTo(w) && "inner".equals(m.getRole()) ) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private Map<Way, Boolean> sortOuterInner( Map<Way, Boolean> ways ) {
        LinkedHashMap<Way, Boolean> result = new LinkedHashMap<>(ways.size());
        List<Way> inner = new ArrayList<>();
        for( Way w : ways.keySet() ) {
            Boolean outer = ways.get(w);
            if( outer )
                result.put(w, outer);
            else
                inner.add(w);
        }
        for( Way w : inner )
            result.put(w, Boolean.FALSE);
        return result;
    }
}

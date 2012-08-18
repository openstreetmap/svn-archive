package poly;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
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
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"));
            try {
                DataSet ds = ((OsmDataLayer)layer).data;
                Map<Way, Boolean> ways = new TreeMap<Way, Boolean>(new AreaComparator());
                String polygonName = layer.getName();
                for( Way w : ds.getWays() ) {
                    if( w.isClosed() ) {
                        boolean outer = true;
                        for( OsmPrimitive p : w.getReferrers() ) {
                            if( p instanceof Relation && ((Relation)p).isMultipolygon() ) {
                                for( RelationMember m : ((Relation)p).getMembers() ) {
                                    if( m.refersTo(w) && "inner".equals(m.getRole()) ) {
                                        outer = false;
                                        break;
                                    }
                                }
                            }
                            if( !outer ) break;
                        }
                        ways.put(w, outer);
                        if( w.hasKey("name") )
                            polygonName = w.get("name");
                    }
                }

                int counter = 1;
                writer.write(polygonName);
                writer.newLine();
                for( Way w : ways.keySet() ) {
                    if( !ways.get(w) )
                        writer.write('!');
                    writer.write(w.hasKey("ref") ? w.get("ref") : String.valueOf(counter++));
                    writer.newLine();
                    for( Node n : w.getNodes() ) {
                        writer.write(String.format(Locale.ENGLISH, "   %E   %E", n.getCoor().lon(), n.getCoor().lat()));
                        writer.newLine();
                    }
                    writer.write("END");
                    writer.newLine();
                }
                writer.write("END");
                writer.newLine();
            } finally {
                writer.close();
            }
        }
    }

    private class AreaComparator implements Comparator<Way> {
        @Override
        public int compare( Way w1, Way w2 ) {
            if( w1.hasKey("ref") && !w2.hasKey("ref") )
                return -1;
            else if( !w1.hasKey("ref") && w2.hasKey("ref") )
                return 1;
            else if( w1.hasKey("ref") && w2.hasKey("ref") && !w1.get("ref").equals(w2.get("ref")) )
                return w1.get("ref").compareTo(w2.get("ref"));
            else
                return w1.compareTo(w2);
        }
    }
}

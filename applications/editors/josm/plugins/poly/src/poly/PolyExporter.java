// License: GPL. For details, see LICENSE file.
package poly;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.io.importexport.OsmExporter;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

/**
 * Writes poly files.
 *
 * @author zverik
 * @author Gerd Petermann
 */
public class PolyExporter extends OsmExporter {

    /**
     * Create exporter.
     */
    public PolyExporter() {
        super(PolyType.FILE_FILTER);
    }

    @Override
    public void exportData(File file, Layer layer) throws IOException {
        if (layer instanceof OsmDataLayer) {
            if (((OsmDataLayer) layer).getDataSet().getWays().stream().anyMatch(w -> !w.isClosed())) {
                throw new IOException(tr("Data contains unclosed ways."));
            }
            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8))) {
                DataSet ds = ((OsmDataLayer) layer).getDataSet();
                HashSet<Way> written = new HashSet<>();
                boolean firstFile = true;
                String fileName = file.getName();
                if (fileName.lastIndexOf('.') > 0) {
                    // remove extension
                    fileName = fileName.substring(0, fileName.lastIndexOf('.'));
                }

                for (Relation rel : ds.getRelations()) {
                    if (rel.isMultipolygon()) {
                        if (!firstFile) {
                            writer.println();
                        }
                        writeRelation(writer, fileName, rel, written);
                        firstFile = false;
                    }
                }
                
                if (firstFile) {
                    writer.println(fileName);
                }
                int counter = 1;
                for (Way w : ds.getWays()) {
                    if (!written.contains(w)) {
                        writeWay(writer, w, counter);
                    }
                }
                writer.println("END");
            }
        }
    }

    private static void writeRelation(PrintWriter writer, String fileName, Relation rel, Set<Way> written) {
        String polygonName = fileName;
        if (rel.getName() != null)
            polygonName = rel.getName();
        writer.println(polygonName);
        int counter = 1;
        for (RelationMember rm: rel.getMembers()) {
            if (rm.isWay()) {
                if ("inner".equals(rm.getRole()))
                    writer.write('!');
                Way w = rm.getWay();
                counter = writeWay(writer, w, counter);
                written.add(w);
            }
        }
    }

    private static int writeWay(PrintWriter writer, Way w, int counter) {
        String name = w.getName();
        if (name == null) {
            name = String.valueOf(counter++);
        } 
        writer.println(name);

        for (Node n : w.getNodes()) {
            writer.println(String.format(Locale.ENGLISH, "   %f   %f", n.getCoor().lon(), n.getCoor().lat()));
        }
        writer.println("END");
        return counter;
    }
}

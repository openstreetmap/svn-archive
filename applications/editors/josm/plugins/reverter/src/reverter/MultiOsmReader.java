// License: GPL. For details, see LICENSE file.
package reverter;

import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.stream.XMLStreamException;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.OsmReader;
import org.openstreetmap.josm.io.UTFInputStreamReader;
import org.openstreetmap.josm.tools.XmlUtils;

/**
 * Subclass of {@link org.openstreetmap.josm.io.OsmReader} that can handle multiple XML streams.
 *
 */
public class MultiOsmReader extends OsmReader {

    /**
     * Parse data in source and add to existing dataset.
     * @param source the input stream with OSM data
     * @throws IllegalDataException in case of any exception
     */
    public void addData(InputStream source) throws IllegalDataException {
        try (InputStreamReader ir = UTFInputStreamReader.create(source)) {
            setParser(XmlUtils.newSafeXMLInputFactory().createXMLStreamReader(ir));
            parse();
        } catch (Exception e) {
            throw new IllegalDataException(e);
        }
    }

    public void processData() throws IllegalDataException {
        prepareDataSet();
    }

    public ParseCallback callback;

    @Override
    protected Node parseNode() throws XMLStreamException  {
        Node node = super.parseNode();
        if (callback != null && node != null) {
            callback.primitiveParsed(node);
        }
        return node;
    }
    @Override
    protected Way parseWay() throws XMLStreamException  {
        Way way = super.parseWay();
        if (callback != null && way != null) {
            callback.primitiveParsed(way);
        }
        return way;
    }
    @Override
    protected Relation parseRelation() throws XMLStreamException  {
        Relation relation = super.parseRelation();
        if (callback != null && relation != null) {
            callback.primitiveParsed(relation);
        }
        return relation;
    }
}

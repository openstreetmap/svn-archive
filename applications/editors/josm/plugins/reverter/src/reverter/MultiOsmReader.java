// License: GPL. For details, see LICENSE file.
package reverter;

import java.io.InputStream;
import java.io.InputStreamReader;

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
}

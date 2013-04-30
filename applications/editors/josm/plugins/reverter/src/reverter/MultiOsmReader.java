package reverter;

import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.OsmReader;
import org.openstreetmap.josm.io.UTFInputStreamReader;

/**
 * Subclass of {@see org.openstreetmap.josm.io.OsmReader} that can handle multiple XML streams.
 *
 */
public class MultiOsmReader extends OsmReader {

    public void addData(InputStream source) throws IllegalDataException {
        try {
            InputStreamReader ir = UTFInputStreamReader.create(source, "UTF-8");
            XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(ir);
            setParser(parser);
            parse();
        } catch(XMLStreamException e) {
           throw new IllegalDataException(e);
        } catch(Exception e) {
            throw new IllegalDataException(e);
        }
    }
    
    public void processData() throws IllegalDataException {
        prepareDataSet();
    }
}

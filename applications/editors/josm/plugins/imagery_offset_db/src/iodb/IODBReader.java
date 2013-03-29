package iodb;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.io.UTFInputStreamReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parses the server response. It expects XML in UTF-8 with several &lt;offset&gt;
 * and &lt;calibration&gt; elements.
 * 
 * @author Zverik
 * @license WTFPL
 */
public class IODBReader {
    private List<ImageryOffsetBase> offsets;
    private InputSource source;
    
    /**
     * Initializes the parser. This constructor creates an input source on the input
     * stream, so it may throw an exception (though it's highly improbable).
     * @param source An input stream with XML.
     * @throws IOException Thrown when something's wrong with the stream.
     */
    public IODBReader( InputStream source ) throws IOException {
        this.source = new InputSource(UTFInputStreamReader.create(source, "UTF-8"));
        this.offsets = new ArrayList<ImageryOffsetBase>();
    }

    /**
     * Parses the XML input stream. Creates {@link Parser} to do it.
     * @return The list of offsets.
     * @throws SAXException Thrown when the XML is malformed.
     * @throws IOException Thrown when the input stream fails.
     */
    public List<ImageryOffsetBase> parse() throws SAXException, IOException {
        Parser parser = new Parser();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.newSAXParser().parse(source, parser);
            return offsets;
        } catch( ParserConfigurationException e ) {
            throw new SAXException(e);
        }
    }

    /**
     * The SAX handler for XML from the imagery offset server.
     * Calls {@link IOFields#constructObject()} for every complete object
     * and appends the result to offsets array.
     */
    private class Parser extends DefaultHandler {
        private StringBuffer accumulator = new StringBuffer();
        private IOFields fields;
        private boolean parsingOffset;
        private boolean parsingDeprecate;
        private SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd");

        /**
         * Initialize all fields.
         */
        @Override
        public void startDocument() throws SAXException {
            fields = new IOFields();
            offsets.clear();
            parsingOffset = false;
        }

        /**
         * Parses latitude and longitude from tag attributes.
         * It expects to find them in "lat" and "lon" attributes
         * as decimal degrees. Note that it does not check whether
         * the resulting object is valid: it may not be, especially
         * for locations near the Poles and 180th meridian.
         */
        private LatLon parseLatLon(Attributes atts) {
            return new LatLon(
                    Double.parseDouble(atts.getValue("lat")),
                    Double.parseDouble(atts.getValue("lon")));
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if( !parsingOffset ) {
                if( qName.equals("offset") || qName.equals("calibration") ) {
                    parsingOffset = true;
                    parsingDeprecate = false;
                    fields.clear();
                    fields.position = parseLatLon(attributes);
                    fields.id = Integer.parseInt(attributes.getValue("id"));
                    if( attributes.getValue("flagged") != null && attributes.getValue("flagged").equals("yes") )
                        fields.flagged = true;
                }
            } else {
                if( qName.equals("node") ) {
                    fields.geometry.add(parseLatLon(attributes));
                } else if( qName.equals("imagery-position") ) {
                    fields.imageryPos = parseLatLon(attributes);
                } else if( qName.equals("imagery") ) {
                    String minZoom = attributes.getValue("minzoom");
                    String maxZoom = attributes.getValue("maxzoom");
                    if( minZoom != null )
                        fields.minZoom = Integer.parseInt(minZoom);
                    if( maxZoom != null )
                        fields.maxZoom = Integer.parseInt(maxZoom);
                } else if( qName.equals("deprecated") ) {
                    parsingDeprecate = true;
                }
            }
            accumulator.setLength(0);
        }
        
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if( parsingOffset )
                accumulator.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if( parsingOffset ) {
                if( qName.equals("author") ) {
                    if( !parsingDeprecate )
                        fields.author = accumulator.toString();
                    else
                        fields.abandonAuthor = accumulator.toString();
                } else if( qName.equals("description") ) {
                    fields.description = accumulator.toString();
                } else if( qName.equals("reason") && parsingDeprecate ) {
                    fields.abandonReason = accumulator.toString();
                } else if( qName.equals("date") ) {
                    try {
                        if( !parsingDeprecate )
                            fields.date = dateParser.parse(accumulator.toString());
                        else
                            fields.abandonDate = dateParser.parse(accumulator.toString());
                    } catch (ParseException ex) {
                        throw new SAXException(ex);
                    }
                } else if( qName.equals("deprecated") ) {
                    parsingDeprecate = false;
                } else if( qName.equals("imagery") ) {
                    fields.imagery = accumulator.toString();
                } else if( qName.equals("offset") || qName.equals("calibration") ) {
                    // store offset
                    try {
                        offsets.add(fields.constructObject());
                    } catch( IllegalArgumentException ex ) {
                        // On one hand, we don't care, but this situation is one
                        // of those "it can never happen" cases.
                        System.err.println(ex.getMessage());
                    }
                    parsingOffset = false;
                }
            }
        }
    }
    
    /**
     * An accumulator for parsed fields. When there's enough data, it can construct
     * an offset object. All fields are public to deliver us from tons of getters
     * and setters.
     */
    private class IOFields {
        public int id;
        public LatLon position;
        public Date date;
        public String author;
        public String description;
        public Date abandonDate;
        public String abandonAuthor;
        public String abandonReason;
        public LatLon imageryPos;
        public String imagery;
        public int minZoom, maxZoom;
        public boolean flagged;
        public List<LatLon> geometry;

        /**
         * A constructor just calls {@link #clear()}.
         */
        public IOFields() {
            clear();
        }
        
        /**
         * Clear all fields to <tt>null</tt> and <tt>-1</tt>.
         */
        public void clear() {
            id = -1;
            position = null;
            date = null;
            author = null;
            description = null;
            abandonDate = null;
            abandonAuthor = null;
            abandonReason = null;
            imageryPos = null;
            imagery = null;
            minZoom = -1;
            maxZoom = -1;
            flagged = false;
            geometry = new ArrayList<LatLon>();
        }

        /**
         * Creates an offset object from the fields. Also validates them, but not vigorously.
         * @return A new offset object.
         */
        public ImageryOffsetBase constructObject() {
            if( author == null || description == null || position == null || date == null )
                throw new IllegalArgumentException("Not enought arguments to build an object");
            ImageryOffsetBase result;
            if( geometry.isEmpty() ) {
                if( imagery == null || imageryPos == null )
                    throw new IllegalArgumentException("Both imagery and imageryPos should be specified for the offset");
                result = new ImageryOffset(imagery, imageryPos);
                if( minZoom >= 0 )
                    ((ImageryOffset)result).setMinZoom(minZoom);
                if( maxZoom >= 0 )
                    ((ImageryOffset)result).setMaxZoom(maxZoom);
            } else {
                result = new CalibrationObject(geometry.toArray(new LatLon[0]));
            }
            if( id >= 0 )
                result.setId(id);
            result.setBasicInfo(position, author, description, date);
            result.setDeprecated(abandonDate, abandonAuthor, abandonReason);
            if( flagged )
                result.setFlagged(flagged);
            return result;
        }
    }
}

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
 * Parses the message from server. It expects XML in UTF-8 with several &lt;offset&gt; elements.
 * 
 * @author zverik
 */
public class IODBReader {
    private List<ImageryOffsetBase> offsets;
    private InputSource source;
    
    private class Parser extends DefaultHandler {
        private StringBuffer accumulator = new StringBuffer();
        private IOFields fields;
        private boolean parsingOffset;
        private boolean parsingDeprecate;
        private SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd");

        @Override
        public void startDocument() throws SAXException {
            fields = new IOFields();
            offsets.clear();
            parsingOffset = false;
        }

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
                        System.err.println(ex.getMessage());
                    }
                    parsingOffset = false;
                }
            }
        }
    }
    

    public IODBReader( InputStream source ) throws IOException {
        this.source = new InputSource(UTFInputStreamReader.create(source, "UTF-8"));
        this.offsets = new ArrayList<ImageryOffsetBase>();
    }
    
    public List<ImageryOffsetBase> parse() throws SAXException, IOException {
        Parser parser = new Parser();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.newSAXParser().parse(source, parser);
            return offsets;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            throw new SAXException(e);
        }
    }
    
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
        public List<LatLon> geometry;

        public IOFields() {
            clear();
        }
        
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
            geometry = new ArrayList<LatLon>();
        }

        public ImageryOffsetBase constructObject() {
            if( author == null || description == null || position == null || date == null )
                throw new IllegalArgumentException("Not enought arguments to build an object");
            ImageryOffsetBase result;
            if( geometry.isEmpty() ) {
                if( imagery == null || imageryPos == null )
                    throw new IllegalArgumentException("Both imagery and imageryPos should be sepcified for the offset");
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
            return result;
        }
    }
}

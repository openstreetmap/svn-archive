package org.openstreetmap.josm.plugins.tag2link.io;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.openstreetmap.josm.io.UTFInputStreamReader;
import org.openstreetmap.josm.plugins.tag2link.Tag2LinkConstants;
import org.openstreetmap.josm.plugins.tag2link.data.Condition;
import org.openstreetmap.josm.plugins.tag2link.data.Link;
import org.openstreetmap.josm.plugins.tag2link.data.Rule;
import org.openstreetmap.josm.plugins.tag2link.data.Source;

public class SourcesReader implements Tag2LinkConstants {
    
    XMLStreamReader parser;
    
    public SourcesReader(XMLStreamReader parser) {
        this.parser = parser;
    }

    public static Collection<Source> readSources() {
        List<Source> result = new ArrayList<Source>();

        try {
            InputStream is = SourcesReader.class.getResourceAsStream(XML_LOCATION);
            InputStreamReader ir = UTFInputStreamReader.create(is, UTF8_ENCODING);
            XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(ir);
            result.addAll(new SourcesReader(parser).parseDoc());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * When cursor is at the start of an element, moves it to the end tag of that element.
     * Nested content is skipped.
     *
     * This is basically the same code as parseUnkown(), except for the warnings, which
     * are displayed for inner elements and not at top level.
     */
    private void jumpToEnd(boolean printWarning) throws XMLStreamException {
        while (true) {
            int event = parser.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                parseUnknown(printWarning);
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                return;
            }
        }
    }

    private void jumpToEnd() throws XMLStreamException {
        jumpToEnd(true);
    }
    
    protected void parseUnknown() throws XMLStreamException {
        parseUnknown(true);
    }
    
    protected void parseUnknown(boolean printWarning) throws XMLStreamException {
        if (printWarning) {
            System.out.println(tr("Undefined element ''{0}'' found in input stream. Skipping.", parser.getLocalName()));
        }
        while (parser.hasNext()) {
            int event = parser.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                parseUnknown(false); /* no more warning for inner elements */
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                return;
            }
        }
    }
    
    private Collection<Source> parseDoc() throws XMLStreamException {
        List<Source> result = new ArrayList<Source>();
        while (parser.hasNext()) {
            int event = parser.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if (parser.getLocalName().equals("tag2link")) {
                    result.addAll(parseRoot());
                } else {
                    parseUnknown();
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                break;
            }
        }
        return result;
    }
    
    private Collection<Source> parseRoot() throws XMLStreamException {
        List<Source> result = new ArrayList<Source>();
        while (parser.hasNext()) {
            int event = parser.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if (parser.getLocalName().equals("source")) {
                    result.add(parseSource());
                } else {
                    parseUnknown();
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                break;
            }
        }
        return result;
    }

    private Source parseSource() throws XMLStreamException {
        Source source = new Source(parser.getAttributeValue(null, "name"));
        while (parser.hasNext()) {
            int event = parser.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if (parser.getLocalName().equals("rule")) {
                    source.rules.add(parseRule());
                } else {
                    parseUnknown();
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                break;
            }
        }
        return source;
    }

    private Rule parseRule() throws XMLStreamException {
        Rule rule = new Rule();
        while (parser.hasNext()) {
            int event = parser.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if (parser.getLocalName().equals("condition")) {
                    rule.conditions.add(parseCondition());
                } else if (parser.getLocalName().equals("link")) {
                        rule.links.add(parseLink());
                } else {
                    parseUnknown();
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                break;
            }
        }
        return rule;
    }

    private Condition parseCondition() throws XMLStreamException {
        Condition c = new Condition();
        c.keyPattern = Pattern.compile(parser.getAttributeValue(null, "k"));
        String v = parser.getAttributeValue(null, "v");
        if (v != null) {
        	c.valPattern = Pattern.compile(v);
        }
        c.id = parser.getAttributeValue(null, "id");
        jumpToEnd();
        return c;
    }

    private Link parseLink() throws XMLStreamException {
        Link link = new Link(
	        parser.getAttributeValue(null, "name"),
	        parser.getAttributeValue(null, "href")
        );
        jumpToEnd();
        return link;
    }
}

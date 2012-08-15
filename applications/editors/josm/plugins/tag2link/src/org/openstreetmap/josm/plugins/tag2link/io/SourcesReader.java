//    JOSM tag2link plugin.
//    Copyright (C) 2011-2012 Don-vip & FrViPofm
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
package org.openstreetmap.josm.plugins.tag2link.io;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.openstreetmap.josm.plugins.tag2link.data.LinkPost;
import org.openstreetmap.josm.plugins.tag2link.data.Rule;
import org.openstreetmap.josm.plugins.tag2link.data.Source;

/**
 * Class allowing to read the sources file.
 * @author Don-vip
 *
 */
public class SourcesReader implements Tag2LinkConstants {
    
    XMLStreamReader parser;
    
    /**
     * Constructs a new {@code SourcesReader}.
     * @param parser The XML parser
     */
    public SourcesReader(XMLStreamReader parser) {
        this.parser = parser;
    }

    /**
     * Reads the sources and replies them as a {@code Collection}. 
     * @return The colleciton of sources.
     */
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
                if (parser.getLocalName().equals("src")) {
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
        Link link = null;
        String name = parser.getAttributeValue(null, "name");
        String href = parser.getAttributeValue(null, "href");
        
        if ("POST".equals(parser.getAttributeValue(null, "method"))) {
            Map<String, String> headers = null;
            Map<String, String> params = null;
            while (parser.hasNext()) {
                int event = parser.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (parser.getLocalName().equals("header")) {
                        if (headers == null) {
                            headers = new HashMap<String, String>();
                        }
                        headers.put(parser.getAttributeValue(null, "name"), parser.getAttributeValue(null, "value"));
                    } else if (parser.getLocalName().equals("param")) {
                        if (params == null) {
                            params = new HashMap<String, String>();
                        }
                        params.put(parser.getAttributeValue(null, "name"), parser.getAttributeValue(null, "value"));
                    } else {
                        parseUnknown();
                    }
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    if (parser.getLocalName().equals("link")) {
                        break;
                    }
                }
            }
            link = new LinkPost(name, href, headers, params);
        } else {
            link = new Link(name, href);
            jumpToEnd();
        }
        return link;
    }
}

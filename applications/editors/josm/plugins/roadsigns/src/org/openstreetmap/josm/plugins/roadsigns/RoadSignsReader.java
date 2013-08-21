// License: GPL (v2 or later)
package org.openstreetmap.josm.plugins.roadsigns;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.io.OsmDataParsingException;
import org.openstreetmap.josm.plugins.roadsigns.Sign.SignParameter;
import org.openstreetmap.josm.plugins.roadsigns.Sign.Tag;
import org.openstreetmap.josm.plugins.roadsigns.javacc.ParseException;
import org.openstreetmap.josm.plugins.roadsigns.javacc.TokenMgrError;
import org.openstreetmap.josm.tools.LanguageInfo;

/**
 * Parses a road sign preset file.
 *
 */
public class RoadSignsReader {
    public static final String lang = LanguageInfo.getLanguageCodeXML();

    private InputSource inputSource;
    private final boolean debug = false;
    /* counts the nesting level when we step into unknown elements */
    private int unknownLevel;

    public RoadSignsReader(InputStream source) throws IOException {
        this.inputSource = new InputSource(new InputStreamReader(source, "UTF-8"));
    }

    private class SignParser extends DefaultHandler {
        public List<Sign> allSigns;
        public Map<Sign, List<String>> supplementIds;

        String characters = "";
        //List<Sign> folders = new ArrayList<Sign>();
        Sign curSign;
        Tag curTag;

        private Locator locator;

        @Override
        public void setDocumentLocator(Locator locator) {
            this.locator = locator;
        }


        @Override public void startElement(String ns, String lname, String qname, Attributes atts) throws SAXException {
            if (debug) System.err.println("<"+qname+">: ");

            if (unknownLevel > 0) {
                unknownLevel++;
            } else if (qname.equals("roadsignpreset")) {

            } else if (qname.equals("sign")) {
                if (curSign != null)
                    throwException("found sign inside sign");
                curSign = new Sign();
                supplementIds.put(curSign, new ArrayList<String>());
                try {
                    curSign.ref = ParametrizedString.create(atts.getValue("ref"));
                    curSign.traffic_sign_tag = ParametrizedString.create(atts.getValue("traffic_sign_tag"));
                } catch (ParseException ex) {
                    throw new SAXException(ex);
                } catch (TokenMgrError ex) {
                    throwException(ex.toString());
                }
                curSign.id = atts.getValue("id");

                if (curSign.id == null) {
                    if (curSign.ref == null)
                        throwException("Both id and ref attribute missing.");
                    curSign.id = curSign.ref.toString();
                }

                curSign.name = getLocalized(atts, "name");
                if (curSign.name == null)
                    throwException("missing attribute: name");

                curSign.long_name = getLocalized(atts, "long_name");

                String iconURL = atts.getValue("icon");
                if (iconURL == null) {
                    iconURL = curSign.id;
                    iconURL = iconURL.replace(':', '_');
                    iconURL = iconURL.replace('.', '_');
                }
                curSign.iconURL = iconURL;

                if ("yes".equals(atts.getValue("supplementary"))) {
                    curSign.isSupplementing = true;
                }

                curSign.wiki = atts.getValue("wiki");
                curSign.loc_wiki = getLocalized(atts, "wiki");

                curSign.help = getLocalized(atts, "help");

                String useful = atts.getValue("useful");
                if (useful != null) {
                    curSign.useful = Boolean.parseBoolean(useful);
                }

            } else if (curSign != null && qname.equals("tag")) {
                if (curSign == null) {
                    throwException("found tag outside sign");
                }
                if (curTag != null) {
                    throwException("found tag inside tag");
                }
                curTag = new Tag();
                curTag.ident = atts.getValue("ident");
                curTag.tag_ref = atts.getValue("tag_ref");
                try {
                    curTag.key = ParametrizedString.create(atts.getValue("key"));
                    curTag.value = ParametrizedString.create(atts.getValue("value"));
                    curTag.append_value = ParametrizedString.create(atts.getValue("append_value"));
                    curTag.condition = ParametrizedString.create(atts.getValue("condition"));
                } catch (ParseException ex) {
                    throw new SAXException(ex);
                } catch (TokenMgrError ex) {
                    throw new SAXException(ex.toString());
                }

            } else if (curSign != null && qname.equals("supplementary")) {
                supplementIds.get(curSign).add(getMandatoryAttribute(atts, "id"));

            } else if (curSign != null && qname.equals("parameter")) {
                String inputType = getMandatoryAttribute(atts, "input");
                SignParameter p = new SignParameter(inputType);
                p.ident = getMandatoryAttribute(atts, "ident");
                p.deflt = getMandatoryAttribute(atts, "default");
                p.prefix = atts.getValue("prefix");
                p.suffix = atts.getValue("suffix");
                String width = atts.getValue("field_width");
                if (width != null) {
                    p.fieldWidth = Integer.parseInt(width);
                }
                curSign.params.add(p);
            } else {
                warning("unknown element: "+qname);
                unknownLevel++;
            }

            if (debug) {
                for (int i=0; i<atts.getLength(); ++i) {
                    System.err.println("  "+atts.getQName(i)+": "+atts.getValue(i));
                }
            }
        }
        @Override public void endElement(String ns, String lname, String qname) throws SAXException {
            if (debug) System.err.print("</"+qname+"> |");
            if (unknownLevel > 0) {
                unknownLevel--;
            } else if (qname.equals("sign")) {
                allSigns.add(curSign);
                curSign = null;

            } else if (qname.equals("tag")) {
                curSign.tags.add(curTag);
                curTag = null;
            }

            characters = characters.trim();
            if (!characters.equals("")) {
                if (debug) System.err.print(characters);
                characters = "";
            }
            if (debug) System.err.println("|");
        }
        @Override public void characters(char[] ch, int start, int length) {
            String s = new String(ch, start, length);
            characters += s;
        }

        private String getLocalized(Attributes atts, String ident) {
            String result = atts.getValue(lang+ident);
            if (result == null) {
                result = atts.getValue(ident);
                if (result == null)
                    return null;
                result = tr(result);
            }
            return result;
        }

        private String getMandatoryAttribute(Attributes atts, String ident) throws OsmDataParsingException {
            String result = atts.getValue(ident);
            if (result == null) {
                throwException("missing attribute: "+ident);
            }
            return result;
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            throwException(e);
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            throwException(e);
        }

        protected void throwException(Exception e) throws ExtendedParsingException {
            throw new ExtendedParsingException(e).rememberLocation(locator);
        }
        protected void throwException(String msg) throws OsmDataParsingException {
            throw new OsmDataParsingException(msg).rememberLocation(locator);
        }

        private void warning(String s) {
            try {
                throwException(s);
            } catch (OsmDataParsingException ex) {
                System.err.println("Warning: "+ex.getMessage());
            }
        }
        public void wireSupplements() throws OsmDataParsingException {
            Map<String, Sign> map = new HashMap<String, Sign>();
            for (Sign sign : allSigns) {
                if (map.get(sign.id) != null)
                    throwException("Found 2 signs with same ref "+sign.ref);
                map.put(sign.id, sign);
            }
            for (Sign sign : allSigns) {
                for (String id : supplementIds.get(sign)) {
                    Sign supp = map.get(id);
                    if (supp == null)
                        throwException("Missing supplement definition "+id+" for sign "+sign.id+".");
                    sign.supplements.add(supp);
                }
            }
        }
    }


    public static class ExtendedParsingException extends SAXException {
        private int columnNumber;
        private int lineNumber;

        public ExtendedParsingException() {
            super();
        }

        public ExtendedParsingException(Exception e) {
            super(e);
        }

        public ExtendedParsingException(String message, Exception e) {
            super(message, e);
        }

        public ExtendedParsingException(String message) {
            super(message);
        }

        public ExtendedParsingException rememberLocation(Locator locator) {
            if (locator == null) return this;
            this.columnNumber = locator.getColumnNumber();
            this.lineNumber = locator.getLineNumber();
            return this;
        }

        @Override
        public String getMessage() {
            String msg = super.getMessage();
            if (lineNumber == 0 && columnNumber == 0)
                return msg;
            System.err.println(".");
            if (msg == null) {
                msg = getClass().getName();
            }
            msg = msg + " " + tr("(at line {0}, column {1})", lineNumber, columnNumber);
            return msg;
        }

        public int getColumnNumber() {
            return columnNumber;
        }

        public int getLineNumber() {
            return lineNumber;
        }
    }

    /**
     *
     * @return True if file was properly parsed, false if there was error during parsing but some data were parsed anyway
     * @throws SAXException
     * @throws IOException
     */
    public List<Sign> parse() throws SAXException, IOException {
        SignParser parser = new SignParser();
        parser.allSigns = new ArrayList<Sign>();
        parser.supplementIds = new HashMap<Sign, List<String>>();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.newSAXParser().parse(inputSource, parser);
            parser.wireSupplements();
            String filterPref = Main.pref.get("plugin.roadsigns.preset.filter");
            if (filterPref.equals("useful")) {
                List<Sign> filtered = new ArrayList<Sign>();
                for (Sign s : parser.allSigns) {
                    if (s.isUseful()) {
                        filtered.add(s);
                    }
                }
                return filtered;
            } else {
                return parser.allSigns;
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace(); // broken SAXException chaining
            throw new SAXException(e);
        }
    }
}

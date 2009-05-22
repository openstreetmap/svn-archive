package org.openstreetmap.josm.plugins.czechaddress.parser;

import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.czechaddress.DatabaseLoadException;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Parser for handling database from 'www.mvcr.cz/adresy'.
 *
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 */
public class MvcrParser extends XMLParser {

    Region curRegion = null;
    ViToCi curViToCi = null;
    Suburb curSuburb = null;
    Street curStreet = null;

//==============================================================================
//  IMPLEMENTING THE ContentHandler
//==============================================================================

    public void startElement(String uri, String localName, String name,
                                Attributes attributes) throws SAXException {

        // ========== PARSING REGION ========== //
        if (name.equals("oblast")) {

            // If the region filter is on, apply it!
            if (filRegion != null && !attributes.getValue("nazev").equals(filRegion))
                return;

            /*curRegion = target.findRegion(
                attributes.getValue("nazev"),
                attributes.getValue("kraj"),
                attributes.getValue("okres"));

            if (curRegion == null) {*/
                curRegion = new Region(
                    attributes.getValue("nazev"),
                    attributes.getValue("kraj"),
                    attributes.getValue("okres"));

                target.regions.add(curRegion);
            //}
        }

        // Everything must belong to some region
        if (curRegion == null)
            return;

        // ========== PARSING ViToCI ========== //
        if (name.equals("obec")) {

            // If the viToCi filter is on, apply it!
            if (filViToCi != null && !attributes.getValue("nazev").equals(filViToCi))
                return;

            //curViToCi = curRegion.findViToCi(attributes.getValue("nazev"));
            //if (curViToCi == null) {
                curViToCi = new ViToCi(attributes.getValue("nazev"));
            //    System.out.println("Parser: " + curViToCi);
                curRegion.addViToCi(curViToCi);
            //}
        }

        // ========== PARSING SUBURB ========== //
        if (name.equals("cast")) {
            if (curViToCi == null)
                return;

            // If the suburb filter is on, apply it!
            if (filSuburb != null && !attributes.getValue("nazev").equals(filSuburb))
                return;

            //curSuburb = curViToCi.findSuburb(attributes.getValue("nazev"));
            //if (curSuburb == null) {
                curSuburb = new Suburb(attributes.getValue("nazev"));
            //    System.out.println("Parser: " + curSuburb);
                curViToCi.addSuburb(curSuburb);
            //}
        }

        // ========== PARSING STREET ========== //
        if (name.equals("ulice")) {

            // If the street filter is on, apply it!
            if (filStreet != null && !attributes.getValue("nazev").equals(filStreet))
                return;

            ElementWithStreets   topElem = curSuburb;
            if (topElem == null) topElem = curViToCi;
            if (topElem == null) topElem = curRegion;

            //curStreet = topElem.findStreet(attributes.getValue("nazev"));
            //if (curStreet == null) {
                curStreet = new Street(attributes.getValue("nazev"));
            //    System.out.println("Parser: " + curStreet);
                topElem.addStreet(curStreet);
            //}

        }

        // ========== PARSING HOUSE ========== //
        if (name.equals("a")) {

            if (   (attributes.getValue("p") == null)
                && (attributes.getValue("o") == null))
                return;

            ElementWithHouses    topElem = curStreet;
            if (topElem == null) topElem = curSuburb;
            if (topElem == null) topElem = curViToCi;
            if (topElem == null) topElem = curRegion;

            topElem.addHouse(new House(attributes.getValue("p"),
                                       attributes.getValue("o")));
        }
    }

    @Override
    public void endElement(String uri, String localName, String name)
                                                   throws SAXException {
        if (name.equals("cast")) {
            curSuburb = null;

        } else if (name.equals("obec")) {
            curSuburb = null;
            curViToCi = null;

        } else if (name.equals("oblast")) {
            curSuburb = null;
            curViToCi = null;
            curRegion = null;

        } else if (name.equals("ulice")) {
            curStreet = null;
        }
    }

    public void setDocumentLocator(Locator locator) {}
    public void startDocument() throws SAXException {}
    public void endDocument() throws SAXException {}
    public void startPrefixMapping(String prefix, String uri) throws SAXException {}
    public void endPrefixMapping(String prefix) throws SAXException {}
    public void characters(char[] ch, int start, int length) throws SAXException {}
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}
    public void processingInstruction(String target, String data) throws SAXException {}
    public void skippedEntity(String name) throws SAXException {}

//==============================================================================
//  FILTERING
//==============================================================================

    String filRegion = null;
    String filViToCi = null;
    String filSuburb = null;
    String filStreet = null;

    public void setFilter(String filterRegion, String filterViToCi,
                          String filterSuburb, String filterStreet) {

        if (filterRegion != null) filRegion = filterRegion.toUpperCase();
        if (filterViToCi != null) filViToCi = filterViToCi.toUpperCase();
        if (filterSuburb != null) filSuburb = filterSuburb.toUpperCase();
        if (filterStreet != null) filStreet = filterStreet.toUpperCase();
    }

//==============================================================================
//  IMPLEMENTING THE DatabaseParser
//==============================================================================

    static final String URL_DEFAULT = "http://web.mvcr.cz/adresa/adresy.zip";
    static final String URL_PREFERENCES_KEY = "czechaddress.databaseurl";
    
    @Override
    protected String getDatabaseUrl() {

        if (!Main.pref.hasKey(URL_PREFERENCES_KEY))
            Main.pref.put(URL_PREFERENCES_KEY, URL_DEFAULT);

        return Main.pref.get(URL_PREFERENCES_KEY);
    }

    @Override
    protected String getDatabasePath() {
        return storageDir + "-adresy.zip";
    }

    protected InputStream getDatabaseStream() throws DatabaseLoadException {
        ZipInputStream zis;
        ZipEntry zipEntry = null;
        try {
	    zis = new ZipInputStream(new FileInputStream(getDatabasePath()));

	    while ((zipEntry = zis.getNextEntry()) != null)
	    	if (zipEntry.getName().equals("adresy.xml"))
	    		break;

        } catch (IOException ioexp) {
            throw new DatabaseLoadException("Chyba při čtení archivu s databází.");
        }

        if (zipEntry == null)
	    throw new DatabaseLoadException(
                    "ZIP archiv s databází neobsahuje soubor 'adresy.xml'.");

        return zis;
    }
}

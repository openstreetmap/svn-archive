package org.openstreetmap.josm.plugins.czechaddress.parser;

import static org.openstreetmap.josm.plugins.czechaddress.StringUtils.capitalize;
import static org.openstreetmap.josm.plugins.czechaddress.StringUtils.tryTrim;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.czechaddress.DatabaseLoadException;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.ElementWithHouses;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.ElementWithStreets;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.House;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.Region;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.Street;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.Suburb;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.ViToCi;
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

        // ========== PARSING HOUSE ========== //
        if (name.equals("a")) {

            String cp = tryTrim(attributes.getValue("p"));
            String co = tryTrim(attributes.getValue("o"));
            if ((cp == null) && (co == null))
                return;

            ElementWithHouses    topElem = curStreet;
            if (topElem == null) topElem = curSuburb;
            if (topElem == null) topElem = curViToCi;
            if (topElem == null) topElem = curRegion;

            topElem.addHouse(new House(cp, co));
            return;
        }

        // ========== PARSING STREET ========== //
        if (name.equals("ulice")) {
            String nazev = tryTrim(attributes.getValue("nazev"));

            // If the street filter is on, apply it!
            if (filStreet != null && !nazev.equals(filStreet)) {
                curStreet = null;
                return;
            }

            ElementWithStreets   topElem = curSuburb;
            if (topElem == null) topElem = curViToCi;
            if (topElem == null) topElem = curRegion;

            //curStreet = topElem.findStreet(attributes.getValue("nazev"));
            //if (curStreet == null) {
                curStreet = new Street(capitalize(nazev));
            //    System.out.println("Parser: " + curStreet);
                topElem.addStreet(curStreet);
            //}
            return;
        }

        // ========== PARSING SUBURB ========== //
        if (name.equals("cast")) {
            if (curViToCi == null)
                return;

            String nazev = tryTrim(attributes.getValue("nazev"));

            // If the suburb filter is on, apply it!
            if (filSuburb != null && !nazev.equals(filSuburb)) {
                curSuburb = null;
                curStreet = null;
                return;
            }

            //curSuburb = curViToCi.findSuburb(attributes.getValue("nazev"));
            //if (curSuburb == null) {
                curSuburb = new Suburb(capitalize(nazev));
            //    System.out.println("Parser: " + curSuburb);
                curViToCi.addSuburb(curSuburb);
            //}
            return;
        }

        // ========== PARSING ViToCi ========== //
        if (name.equals("obec")) {

            String nazev = tryTrim(attributes.getValue("nazev"));

            // If the viToCi filter is on, apply it!
            if (filViToCi != null && !nazev.equals(filViToCi)) {
                curViToCi = null;
                curSuburb = null;
                curStreet = null;
                return;
            }

            //curViToCi = curRegion.findViToCi(attributes.getValue("nazev"));
            //if (curViToCi == null) {
                curViToCi = new ViToCi(capitalize(nazev));
            //    System.out.println("Parser: " + curViToCi);
                curRegion.addViToCi(curViToCi);
            //}
            return;
        }

        // ========== PARSING REGION ========== //
        if (name.equals("oblast")) {

            // If the region filter is on, apply it!
            if (filRegion != null &&
                    !attributes.getValue("nazev").trim().equals(filRegion)) {
                curRegion = null;
                curViToCi = null;
                curSuburb = null;
                curStreet = null;
                return;
            }

            /*curRegion = target.findRegion(
                attributes.getValue("nazev"),
                attributes.getValue("kraj"),
                attributes.getValue("okres"));

            if (curRegion == null) {*/
                curRegion = new Region(
                    capitalize(tryTrim(attributes.getValue("nazev"))),
                    capitalize(tryTrim(attributes.getValue("kraj"))),
                    capitalize(tryTrim(attributes.getValue("okres"))));

                target.regions.add(curRegion);
            //}
            return;
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

    static final String URL_DEFAULT = "http://aplikace.mvcr.cz/adresy/Download.aspx";
    static final String URL_PREFERENCES_KEY = "czechaddress.databaseurl";

    static final String[] OLD_URLS = {
        "http://web.mvcr.cz/adresa/adresy.zip", // Remove around 10.02.2011
	"http://aplikace.mvcr.cz/adresa/adresy.zip" // Removed around 12.01.2013
    };

    @Override
    protected String getDatabaseUrl() {

        // No longer add the default URL into preferences.
        //if (!Main.pref.hasKey(URL_PREFERENCES_KEY))
        //    Main.pref.put(URL_PREFERENCES_KEY, URL_DEFAULT);

        /* If an outdated URL is found in the preferences, replace it by the
         * new one. However the urls from the list should be removed after
         * a reasonable amount of time (one year?), because this is a
         * non-systematic solution.
         */
        for (String oldUrl : OLD_URLS)
            if (Main.pref.get(URL_PREFERENCES_KEY, URL_DEFAULT).equals(oldUrl)) {
                Main.pref.put(URL_PREFERENCES_KEY, URL_DEFAULT);
                break;
            }

        return Main.pref.get(URL_PREFERENCES_KEY, URL_DEFAULT);
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

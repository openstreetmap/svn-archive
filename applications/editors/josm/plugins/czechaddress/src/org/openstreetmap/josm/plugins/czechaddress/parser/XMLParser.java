package org.openstreetmap.josm.plugins.czechaddress.parser;

import java.io.IOException;
import org.openstreetmap.josm.plugins.czechaddress.DatabaseLoadException;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Any XML parser capable of filling the database.
 *
 * <p>This class has is capable of initialising the parser and handling errors,
 * but parsing the actual document must be done by subclasses.</p>
 *
 * @see Database
 *
 * @author Radomír Černoch, raodmir.cernoch@gmail.com
 */
public abstract class XMLParser extends DatabaseParser
                                implements ContentHandler {

    protected void parseDatabase() throws DatabaseLoadException {

        try {
            XMLReader xr;
            xr = XMLReaderFactory.createXMLReader();
            xr.setContentHandler(this);
            xr.parse(new InputSource(getDatabaseStream()));

        } catch (IOException ioexp) {
            throw new DatabaseLoadException("Chyba při čtení archivu s databází.");
        } catch (SAXException saxexp) {
            throw new DatabaseLoadException("Selhaho parsování XML souboru s databází adres.");
        }
    }
}

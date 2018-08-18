// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.JunctionChecker.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.openstreetmap.josm.tools.Logging;

public abstract class XMLReader {

    protected String filename;
    protected XMLInputFactory factory = XMLInputFactory.newInstance();
    protected XMLStreamReader parser;

    public XMLReader(String filename) {
        try {
            parser = factory
                .createXMLStreamReader(this.getClass().getResourceAsStream(filename));
        } catch (XMLStreamException e) {
            Logging.error(e);
        }
    }

    public XMLReader(File file) {
        try {
            parser = factory
                    .createXMLStreamReader(new FileInputStream(file));
        } catch (FileNotFoundException | XMLStreamException e) {
            Logging.error(e);
        }
    }

    public abstract void parseXML();
}

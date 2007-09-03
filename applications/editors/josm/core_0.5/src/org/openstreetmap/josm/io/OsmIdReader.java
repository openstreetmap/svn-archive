// License: GPL. Copyright 2007 by Immanuel Scholz and others
package org.openstreetmap.josm.io;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import uk.co.wilson.xml.MinML2;

/**
 * Read only the ids and classes of an stream.
 * 
 * @author Imi
 */
public class OsmIdReader extends MinML2 {

	private boolean cancel;
	Map<Long, String> entries = new HashMap<Long, String>();
	private Reader in;

	@Override public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		if (qName.equals("node") || qName.equals("segment") || qName.equals("way")) {
			try {
				entries.put(Long.valueOf(atts.getValue("id")), qName);
			} catch (Exception e) {
				e.printStackTrace();
				throw new SAXException(tr("Error during parse."));
			}
		}
    }

	public Map<Long, String> parseIds(InputStream in) throws IOException, SAXException {
        this.in = new InputStreamReader(in, "UTF-8");
		try {
	        parse(this.in);
        } catch (SAXException e) {
        	if (!cancel)
        		throw e;
        }        
		return entries;
	}
	
	public void cancel() {
		cancel = true;
		if (in != null)
            try {in.close();} catch (IOException e) {}
	}
}

//    JOSM opendata plugin.
//    Copyright (C) 2011-2012 Don-vip
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
package org.openstreetmap.josm.plugins.opendata.core.io.tabular;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jopendocument.io.SaxContentUnmarshaller;
import org.jopendocument.model.OpenDocument;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class OdsDocument extends OpenDocument implements OdConstants {

	public OdsDocument(InputStream in) {
		loadFrom(in);
	}
	
	private InputSource getEntryInputSource(ZipInputStream zis) throws IOException {
        int n = -1;
		final byte[] buffer = new byte[4096];
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((n = zis.read(buffer, 0, buffer.length)) != -1) {
        	baos.write(buffer, 0, n);
        }
		return new InputSource(new ByteArrayInputStream(baos.toByteArray()));
	}

    public void loadFrom(final InputStream in) {
        final SaxContentUnmarshaller contentHandler = new SaxContentUnmarshaller();

        try {
            final ZipInputStream zis = new ZipInputStream(in);
            final XMLReader rdr = XMLReaderFactory.createXMLReader();
            
            ZipEntry entry = null;
            boolean contentParsed = false;
            
            while (!contentParsed && (entry = zis.getNextEntry()) != null) {
            	if (entry.getName().equals("content.xml")) {
                    rdr.setContentHandler(contentHandler);
                    System.out.println("Parsing content.xml");
                    rdr.parse(getEntryInputSource(zis));
            		contentParsed = true;
            	}
            }
            
        } catch (final Exception e) {
            e.printStackTrace();
        }
        
        init(contentHandler.getBody());
    }
}

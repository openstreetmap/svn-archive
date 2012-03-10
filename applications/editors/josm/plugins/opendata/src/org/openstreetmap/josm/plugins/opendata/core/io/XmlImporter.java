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
package org.openstreetmap.josm.plugins.opendata.core.io;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import javax.xml.bind.JAXBException;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;

public class XmlImporter extends AbstractImporter {

	public XmlImporter() {
		super(XML_FILE_FILTER);
	}
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.io.FileImporter#acceptFile(java.io.File)
	 */
	@Override
	public boolean acceptFile(File pathname) {
		if (super.acceptFile(pathname)) {
			for (URL schemaURL : NeptuneReader.getSchemas()) {
				if (NeptuneReader.acceptsXmlNeptuneFile(pathname, schemaURL)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected DataSet parseDataSet(InputStream in, ProgressMonitor instance)
			throws IllegalDataException {
		try {
			return NeptuneReader.parseDataSet(in, handler, instance);
		} catch (JAXBException e) {
			throw new IllegalDataException(e);
		}
	}
}

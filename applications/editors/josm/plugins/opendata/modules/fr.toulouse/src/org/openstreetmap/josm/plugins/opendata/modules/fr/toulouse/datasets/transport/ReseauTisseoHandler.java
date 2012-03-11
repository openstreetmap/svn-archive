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
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.opendata.core.io.NeptuneReader;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class ReseauTisseoHandler extends ToulouseDataSetHandler {

	private static final URL neptuneSchemaUrl = ReseauTisseoHandler.class.getResource(TOULOUSE_NEPTUNE_XSD);
	
	public ReseauTisseoHandler() {
		super(14022, "network=fr_tisseo");
		NeptuneReader.registerSchema(neptuneSchemaUrl);
		setName("Réseau Tisséo (Métro, Bus, Tram)");
		setCategory(CAT_TRANSPORT);
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsZipFilename(filename, "14022-reseau-tisseo-metro-bus-tram-") || filename.toLowerCase().endsWith(XML_EXT);
	}
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#acceptsFile(java.io.File)
	 */
	@Override
	public boolean acceptsFile(File file) {
		return acceptsFilename(file.getName()) && (file.getName().toLowerCase().endsWith(ZIP_EXT) || NeptuneReader.acceptsXmlNeptuneFile(file, neptuneSchemaUrl));
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler#getSource()
	 */
	@Override
	public String getSource() {
		return SOURCE_TISSEO;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler#getWikiURL()
	 */
	@Override
	public URL getWikiURL() {
		try {
			return new URL("http://wiki.openstreetmap.org/wiki/Toulouse/Transports_en_commun#Réseau_Tisséo");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void updateDataSet(DataSet ds) {
		for (OsmPrimitive p : ds.allPrimitives()) {
			p.put("operator", "Tisséo");
			p.put("network", "fr_tisseo");
		}
	}
}

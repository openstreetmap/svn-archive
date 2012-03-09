package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport;

import java.io.File;
import java.net.URL;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class TisseoHandler extends ToulouseDataSetHandler {

	public TisseoHandler() {
		super(14022);
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return filename.toLowerCase().endsWith(XML_EXT);
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#acceptsFile(java.io.File)
	 */
	@Override
	public boolean acceptsFile(File file) {
		return acceptsFilename(file.getName()) && acceptsXmlNeptuneFile(file);
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.fr.FrenchDataSetHandler#getNeptuneSchema()
	 */
	@Override
	protected URL getNeptuneSchema() {
		return TisseoHandler.class.getResource(TOULOUSE_NEPTUNE_XSD);
	}

	@Override
	public void updateDataSet(DataSet ds) {
		// TODO Auto-generated method stub
		
	}
}

// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pbf.io;

import java.io.IOException;
import java.io.InputStream;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.CachedFile;
import org.openstreetmap.josm.io.OsmImporter;
import org.openstreetmap.josm.plugins.pbf.PbfConstants;
import org.xml.sax.SAXException;

/**
 * @author Don-vip
 *
 */
public class PbfImporter extends OsmImporter {

    public PbfImporter() {
        super(PbfConstants.FILE_FILTER);
    }

	@Override
	protected DataSet parseDataSet(InputStream in, ProgressMonitor progressMonitor) throws IllegalDataException {
		return PbfReader.parseDataSet(in, progressMonitor);
	}

	protected DataSet parseDataSet(final String source) throws IOException, SAXException, IllegalDataException {
        return parseDataSet(new CachedFile(source).getInputStream(), NullProgressMonitor.INSTANCE);
	}
}

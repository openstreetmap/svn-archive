//    JOSM PBF plugin.
//    Copyright (C) 2011 Don-vip
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
package org.openstreetmap.josm.plugins.pbf.io;

import java.io.IOException;
import java.io.InputStream;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.MirroredInputStream;
import org.openstreetmap.josm.io.OsmImporter;
import org.openstreetmap.josm.plugins.pbf.PbfConstants;
import org.xml.sax.SAXException;

/**
 * @author Don-vip
 *
 */
public class PbfImporter extends OsmImporter implements PbfConstants {
    
    public PbfImporter() {
        super(FILE_FILTER);
    }
    
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.io.OsmImporter#parseDataSet(java.io.InputStream, org.openstreetmap.josm.gui.progress.ProgressMonitor)
	 */
	@Override
	protected DataSet parseDataSet(InputStream in, ProgressMonitor progressMonitor) throws IllegalDataException {
		return PbfReader.parseDataSet(in, progressMonitor);
	}

	protected DataSet parseDataSet(final String source) throws IOException, SAXException, IllegalDataException {
        return parseDataSet(new MirroredInputStream(source), NullProgressMonitor.INSTANCE);
	}
}

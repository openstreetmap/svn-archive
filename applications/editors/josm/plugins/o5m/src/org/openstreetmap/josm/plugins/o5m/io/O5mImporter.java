//    JOSM o5m plugin.
//    Copyright (C) 2013 Gerd Petermann
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
package org.openstreetmap.josm.plugins.o5m.io;

import java.io.IOException;
import java.io.InputStream;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.CachedFile;
import org.openstreetmap.josm.io.OsmImporter;
import org.openstreetmap.josm.plugins.o5m.O5mConstants;

/**
 * @author GerdP
 *
 */
public class O5mImporter extends OsmImporter {

    public O5mImporter() {
        super(O5mConstants.FILE_FILTER);
    }

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.io.OsmImporter#parseDataSet(java.io.InputStream, org.openstreetmap.josm.gui.progress.ProgressMonitor)
	 */
	@Override
	protected DataSet parseDataSet(InputStream in, ProgressMonitor progressMonitor) throws IllegalDataException {
		return O5mReader.parseDataSet(in, progressMonitor);
	}

	protected DataSet parseDataSet(final String source) throws IOException, IllegalDataException {
		try(CachedFile cf = new CachedFile(source)){
			return parseDataSet(cf.getInputStream(), NullProgressMonitor.INSTANCE);
		}
	}
}

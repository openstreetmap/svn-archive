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
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

import java.io.InputStream;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.plugins.opendata.core.io.AbstractImporter;

public class KmlKmzImporter extends AbstractImporter {
	
    public KmlKmzImporter() {
        super(KML_KMZ_FILE_FILTER);
    }

	@Override
	protected DataSet parseDataSet(InputStream in, ProgressMonitor instance)
			throws IllegalDataException {
		try {
			if (file.getName().toLowerCase().endsWith(KML_EXT)) {
				return KmlReader.parseDataSet(in, instance);
			} else {
				return KmzReader.parseDataSet(in, instance);
			}
		} catch (Exception e) {
			throw new IllegalDataException(e);
		}
	}
}

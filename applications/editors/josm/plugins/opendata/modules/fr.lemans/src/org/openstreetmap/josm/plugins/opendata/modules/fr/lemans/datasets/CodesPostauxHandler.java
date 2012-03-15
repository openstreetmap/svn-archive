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
package org.openstreetmap.josm.plugins.opendata.modules.fr.lemans.datasets;

import org.openstreetmap.josm.data.osm.DataSet;

public class CodesPostauxHandler extends LeMansDataSetHandler {

	public CodesPostauxHandler() {
		super("F758F369-550EA533-37695DD8-84EB87F5");
		setName("Codes postaux");
		setKmzShpUuid("6449CF5B-550EA533-7E7BB44A-2DB0261B", "644A524A-550EA533-7E7BB44A-9B34A76A");
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsKmzShpFilename(filename, "CODES_POSTAUX") || acceptsZipFilename(filename, "Les codes postaux .*");
	}

	@Override
	public void updateDataSet(DataSet ds) {
		// TODO
	}
}

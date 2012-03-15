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
import org.openstreetmap.josm.data.osm.Node;

public class CantonsSartheHandler extends LeMansDataSetHandler {

	public CantonsSartheHandler() {
		super("F7D936DF-550EA533-37695DD8-29CFF55B");
		setName("Cantons de la Sarthe");
		setCsvKmzShpUuid("62DF4EEF-550EA533-7E7BB44A-45C66201", "62DFCA8F-550EA533-7E7BB44A-7D1AA2D4", "62E017CA-550EA533-7E7BB44A-23772121");
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsCsvKmzShpFilename(filename, "CANTONS_72") || acceptsZipFilename(filename, "Les cantons de la Sarthe .*");
	}

	@Override
	public void updateDataSet(DataSet ds) {
		for (Node n : ds.getNodes()) {
			replace(n, "NOM", "name");
		}
	}
}

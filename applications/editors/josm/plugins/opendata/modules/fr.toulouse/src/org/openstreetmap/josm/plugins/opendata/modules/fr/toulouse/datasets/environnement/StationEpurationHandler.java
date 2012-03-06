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
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.environnement;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class StationEpurationHandler extends ToulouseDataSetHandler {

	public StationEpurationHandler() {
		super(12500, "man_made=wastewater_plant");
		setWikiPage("Stations d'épuration");
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsCsvKmzTabFilename(filename, "Stations_epurations");
	}

	@Override
	public void updateDataSet(DataSet ds) {
		for (Node n : ds.getNodes()) {
			replace(n, "NOM_STATION", "name");
			n.put("man_made", "wastewater_plant");
		}
	}
}

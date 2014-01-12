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
package org.openstreetmap.josm.plugins.opendata.modules.fr.paris.datasets.environnement;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.paris.datasets.ParisDataSetHandler;

public class ArbresRemarquablesHandler extends ParisDataSetHandler {

	public ArbresRemarquablesHandler() {
		super(107);
		setName("Arbres remarquables");
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsShpFilename(filename, "arbres_remarquables") || acceptsZipFilename(filename, "arbres_remarquables_20..");
	}
	
	@Override
	public void updateDataSet(DataSet ds) {
		for (Node n : ds.getNodes()) {
			n.put("natural", "tree");
			replace(n, "ANNEE__PLA", "start_date");
		}
	}

	@Override
	protected String getDirectLink() {
		return PORTAL+"hn/arbres_remarquables_2011.zip";
	}
}

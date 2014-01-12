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
package org.openstreetmap.josm.plugins.opendata.modules.fr.cg41.datasets.environnement;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.plugins.opendata.modules.fr.cg41.datasets.Cg41DataSetHandler;

public class ZonesInondablesBrayeHandler extends Cg41DataSetHandler {
	public ZonesInondablesBrayeHandler() {
		super(14624, "Zone-inondable-de-la-Braye-au-1-25000-(partie-Loir-et-Cher)-30383255");
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsMifFilename(filename, "aleas_braye");
	}

	@Override
	public void updateDataSet(DataSet ds) {
		// TODO Auto-generated method stub
	}
}

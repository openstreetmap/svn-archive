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
package org.openstreetmap.josm.plugins.opendata.modules.fr.sncf.datasets;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;

public class EquipementsHandler extends SncfDataSetHandler {

	private class LambertIICsvHandler extends InternalCsvHandler {
		@Override
		public LatLon getCoor(EastNorth en, String[] fields) {
			// Lambert II coordinates offset by 2000000 (see http://fr.wikipedia.org/wiki/Projection_conique_conforme_de_Lambert#Projections_officielles_en_France_métropolitaine)
			return super.getCoor(new EastNorth(en.getX(), en.getY()-2000000), fields);
		}
	}
	
	public EquipementsHandler() {
		super("equipementsgares");
		setCsvHandler(new LambertIICsvHandler());
		setSingleProjection(lambert4Zones[1]); // Lambert II
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsCsvXlsFilename(filename, "gare_20......");
	}

	@Override
	public void updateDataSet(DataSet ds) {
		for (Node n : ds.getNodes()) {
			replace(n, "nom gare", "name");
			n.put("railway", "station");
		}
	}
}

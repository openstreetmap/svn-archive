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
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public abstract class ChantiersHandler extends ToulouseDataSetHandler {

	public ChantiersHandler(int portalId, String name) {
		super(portalId, "highway=construction");
		setWikiPage("Chantiers en cours");
		setName(name);
		setCategory(CAT_TRANSPORT);
	}
	
	@Override
	public void updateDataSet(DataSet ds) {
		for (OsmPrimitive p : ds.allPrimitives()) {
			if (p.hasKeys()) {
				p.put("highway", "construction");
				p.remove("name");
				p.remove("color");
				replace(p, "Lien", "website");
			}
		}
	}
}

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
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.urbanisme.VoirieHandler;

public class Zone30Handler extends VoirieHandler {
	
	public Zone30Handler() {
		super(12548, "Street", "maxspeed=30");
		setWikiPage("Zones 30");
		setCategory(CAT_TRANSPORT);
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsKmzTabFilename(filename, "Zone30");
	}
	
	@Override
	public void updateDataSet(DataSet ds) {
		super.updateDataSet(ds);
		
		for (Way w : ds.getWays()) {
			w.put("zone:maxspeed", "FR:30");
			w.put("maxspeed", "30");
			replace(w, "SensUnique", "oneway", new String[]{"oui", "non"}, new String[]{"yes", "no"});
			replace(w, "Annee", "start_date");
			replace(w, "Longueur", "length");
			w.remove("code_insee");
			w.remove("commune");
			w.remove("MotDir");
		}
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.portals.fr.toulouse.datasets.urbanisme.VoirieHandler#getStreetId(org.openstreetmap.josm.data.osm.Way)
	 */
	@Override
	protected String getStreetId(Way w) {
		return w.get("code_insee")+"/"+w.get("name");
	}
}

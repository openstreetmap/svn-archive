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
package org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.hydrologie;

import java.net.MalformedURLException;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.DataGouvDataSetHandler;

public class ROEHandler extends DataGouvDataSetHandler {

	public ROEHandler() {
		super("référentiel-des-obstacles-à-l'écoulement-30381987");
		setName("Référentiel des Obstacles à l’Écoulement");
		try {
			setDataURL("http://www.eaufrance.fr/docs/ROE/donnee_obstacles_ecoulement_v3.zip");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsMifTabFilename(filename, "roe_version._20......_wlatin1") 
			|| acceptsShpFilename(filename, "roe_version._20......_system_L93")
			|| acceptsXlsFilename(filename, "roe_version._20......_wlatin1");
	}

	@Override
	public void updateDataSet(DataSet ds) {
		for (Node n : ds.getNodes()) {
			replace(n, "Id_ROE", "ref:FR:ROE");
			replace(n, "Nom", "name");
			n.remove("XL2e"); n.remove("YL2e");
			n.remove("XL93"); n.remove("YL93");
			replace(n, "typeCd", "waterway", new String[]{"1.1", "1.2", "1.6"}, new String[]{"dam", "weir", "lock_gate"});
			replace(n, "typeCd", "man_made", new String[]{"1.3", "1.5"}, new String[]{"dyke", "groyne"});
			replace(n, "typeCd", "bridge", new String[]{"1.4"}, new String[]{"yes"});
			n.remove("typeCd");
		}
	}
}

// License: GPL. For details, see LICENSE file.
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

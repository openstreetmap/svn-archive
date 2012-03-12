package org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.ecologie;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.plugins.opendata.core.datasets.fr.FrenchAdministrativeUnit;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.DataGouvDataSetHandler;
import org.openstreetmap.josm.tools.Pair;

public class ForetsPubliquesHandler extends DataGouvDataSetHandler {

	public ForetsPubliquesHandler() {
		setName("Forêts publiques");
	}
	
	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsZipFilename(filename, "for_publ_v20.._reg..") || acceptsShpFilename(filename, "for_publ_v20.._reg..");
	}

	@Override
	public void updateDataSet(DataSet ds) {
		for (Relation r : ds.getRelations()) {
			r.put("landuse", "forest");
			replace(r, "LLIB_FRT", "name");
		}
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getDataURLs()
	 */
	@Override
	public List<Pair<String, URL>> getDataURLs() {
		List<Pair<String, URL>> result = new ArrayList<Pair<String,URL>>();
		try {
			for (FrenchAdministrativeUnit region : FrenchAdministrativeUnit.allRegions) {
				result.add(getForetURL(region.getCode(), region.getName()));
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return result;
	}

	private Pair<String, URL> getForetURL(String code, String name) throws MalformedURLException {
		return new Pair<String, URL>(name, new URL(FRENCH_PORTAL+"var/download/"+"for_publ_v2011_reg"+code+".zip"));
	}
}

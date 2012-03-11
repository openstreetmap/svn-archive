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

import static org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler.OaQueryType.NODE;
import static org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler.OaQueryType.WAY;
import static org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler.OaRecurseType.NODE_RELATION;
import static org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler.OaRecurseType.RELATION_WAY;
import static org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler.OaRecurseType.WAY_NODE;
import static org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler.OaRecurseType.WAY_RELATION;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.IPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class PistesCyclablesHandler extends ToulouseDataSetHandler {

	protected final Map<String, Collection<String>> map = new HashMap<String, Collection<String>>();
	
	private String streetField;
	
	public PistesCyclablesHandler() {
		this("Nom_voie");
		setCategory(CAT_TRANSPORT);
	}
	
	public PistesCyclablesHandler(String streetField) {
		super(12544, true, "cycleway", "cycleway:right", "cycleway:left", "cycleway:both", "highway=cycleway", "ramp:bicycle=yes", "bicycle=yes", "barrier=cycle_barrier");
		setWikiPage("Réseau cyclable et vert");
		addForbiddenTag("cycleway=no");
		this.streetField = streetField;
		map.put("secondary", Arrays.asList(new String[] {"AVENUE", "Av ", "av ", "Avenue ", "avenue ", "BOULEVARD ", "bd ", "ALLEE", "Allee", "allee", 
				"PONT ", "Pont ", "PORT ", "ROUTE ", "Rte ", "BOULINGRIN", "boulingrin"}));
		map.put("residential", Arrays.asList(new String[] {"CHEMIN ", "Chemin ", "chemin ", "IMPASSE ", "imp ", "PLACE ", "Place ", "place ", 
				"RUE ", "Rue ", "rue ", "QUAI", "VOIE ", "grand rue"}));
	}
	
	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsKmzTabFilename(filename, "Pistes_Cyclables");
	}
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fr.opendata.datasets.SimpleDataSetHandler#getOverpassApiUnion(java.lang.String, java.lang.String[])
	 */
	@Override
	protected String getOverpassApiQueries(String bbox, String... conditions) {
		return oaQuery(bbox, NODE, conditions) + "\n" + 
				oaRecurse(NODE_RELATION, RELATION_WAY, WAY_NODE) + "\n" +
				oaQuery(bbox, WAY, conditions) + "\n" +
				oaRecurse(WAY_NODE, "nodes") + "\n" +
				oaRecurse(WAY_RELATION);
	}

	private String applyHighwayTag(String name, IPrimitive p) {
		if (name != null && p != null) {
			for (String key : map.keySet()) {
				for (String value : map.get(key)) {
					if (name.startsWith(value)) {
						p.put("highway", key);
						return key;
					}
				}
			}
		}
		return null;
	}
	
	@Override
	public void updateDataSet(DataSet ds) {
		for (Way w : ds.getWays()) {

			w.remove("name");
			
			String obs_type = w.get("obs_type");
			if (obs_type.equals("bande")) {
				w.put("cycleway", "lane");
			} else if (obs_type.equals("bande a contresens") || obs_type.equals("contre allee")) {
				w.put("cycleway", "opposite_lane");
			} else if (obs_type.equals("couloir bus")) {
				w.put("cycleway", "share_busway");
			} else if (obs_type.equals("trottoir")) {
				w.put("cycleway", "track");
			} else if (obs_type.equalsIgnoreCase("cheminement mixte") || obs_type.equals("reseau vert")) {
				w.put("highway", "cycleway");
				w.put("foot", "yes");
			} else if (obs_type.equals("piste") || obs_type.equals("voie verte")) {
				w.put("highway", "cycleway");
			} else if (obs_type.equals("zone 30")) {
				w.put("zone:maxspeed", "FR:30");
			} else {
				System.out.println(obs_type);
			}
				
			String name = w.get(streetField);
			if (name != null) {
				w.remove(streetField);
					
				if (w.get("highway") == null && applyHighwayTag(name, w) == null) {
					w.put("highway", "road");
				}

				w.put("name", name);
			}
		}
	}
}

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
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.urbanisme;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.IPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.opendata.core.util.NamesFrUtils;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class VoirieHandler extends ToulouseDataSetHandler {

	protected final Map<String, Collection<String>> map = new HashMap<String, Collection<String>>();
	
	private String streetField;
	
	public VoirieHandler() {
		this(12693, "lib_off", "highway");
		setName("Filaire de voirie");
		setCategory(CAT_URBANISME);
	}
	
	protected VoirieHandler(int portalId, String streetField, String relevantKey) {
		super(portalId, relevantKey);
		this.streetField = streetField;
		map.put("motorway", Arrays.asList(new String[] {"A6", "AUTOROUTE "}));
		map.put("trunk", Arrays.asList(new String[] {"ROCADE "}));
		map.put("secondary", Arrays.asList(new String[] {"AV ", "BD ", "ALL ", "PONT ", "RTE ", "PORT ", "BOULINGRIN"}));
		map.put("residential", Arrays.asList(new String[] {"RUE ", "GRANDE-RUE ", "PROM ", "CHE", "CAMINOT ", "IMP ", "COURS ",
				"LOT ", "ANC", "VIEUX ", "PL ", "CLOS ", "CITE ", "RESIDENCE ", "SENTIER ", "QU ", "SQ ", "VOIE ", "ESP "}));
		map.put("unclassified", Arrays.asList(new String[] {"ZONE "}));
		map.put("road", Arrays.asList(new String[] {"VA "}));
	}
	
	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsKmzTabFilename(filename, "Voies");
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
	
	protected String getStreetId(Way w) {
		return w.get("sti");
	}
	
	@Override
	public void updateDataSet(DataSet ds) {
		Map<String, Relation> associatedStreets = new HashMap<String, Relation>();
		
		for (Way w : ds.getWays()) {
			String name = w.get(streetField);
			if (name != null) {
				w.remove(streetField);
				w.remove("mot_directeur");
				w.remove("color");
				w.remove("rivoli");
				w.remove("nrivoli");
				
				if (applyHighwayTag(name, w) == null) {
					w.put("highway", "road");
				}
				
				if (name.startsWith("RPT ") || name.startsWith("GIRATOIRE ")) {
					// TODO: find correct highway
					w.put("junction", "roundabout");
				} else if (name.matches("RTE D[0-9]+")) {
					w.put("ref", name.split(" ")[1]);
				}
				
				w.put("name", name);
				
				if (name.matches("D[0-9]+.*")) {
					w.put("highway", "secondary");
					replace(w, "name", "ref");
				} else if (!name.startsWith("VA ")) { // Unknown labels
					name = NamesFrUtils.checkStreetName(w, "name");
				}
				
				if (!name.startsWith("VA ")) { // Unknown labels
					Relation street = associatedStreets.get(getStreetId(w));
					if (street == null) {
						associatedStreets.put(getStreetId(w), street = new Relation());
						street.put("type", "associatedStreet");
						street.put("name", name);
						ds.addPrimitive(street);
					}
					street.addMember(new RelationMember("street", w));
				}
			}
		}
	}
}

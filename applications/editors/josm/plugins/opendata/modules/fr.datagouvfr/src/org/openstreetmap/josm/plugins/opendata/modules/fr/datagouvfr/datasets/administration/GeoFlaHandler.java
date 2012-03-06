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
package org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.administration;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.text.WordUtils;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.DataGouvDataSetHandler;

public class GeoFlaHandler extends DataGouvDataSetHandler {
	
	public GeoFlaHandler() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.portals.fr.datagouvfr.datasets.DataGouvDataSetHandler#getLocalPortalURL()
	 */
	@Override
	public URL getLocalPortalURL() {
		try {
			return new URL("http://professionnels.ign.fr/ficheProduitCMS.do?idDoc=6185461");
		} catch (MalformedURLException e) {
			System.err.println(e.getMessage());
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.fr.FrenchDataSetHandler#getLocalPortalIconName()
	 */
	@Override
	public String getLocalPortalIconName() {
		return ICON_IGN_24;
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return isDepartementFile(filename) || isCommuneFile(filename) || isCantonFile(filename) || isArrondissementFile(filename);
	}
		
	protected boolean isDepartementFile(String filename) {
		return acceptsShpMifFilename(filename, "DEPARTEMENT") || acceptsShpMifFilename(filename, "LIMITE_DEPARTEMENT");
	}

	protected boolean isCommuneFile(String filename) {
		return acceptsShpFilename(filename, "COMMUNE") || acceptsShpFilename(filename, "LIMITE_COMMUNE");
	}

	protected boolean isCantonFile(String filename) {
		return acceptsShpFilename(filename, "CANTON") || acceptsShpFilename(filename, "LIMITE_CANTON");
	}

	protected boolean isArrondissementFile(String filename) {
		return acceptsShpFilename(filename, "ARRONDISSEMENT") || acceptsShpFilename(filename, "LIMITE_ARRONDISSEMENT");
	}

	@Override
	public boolean preferMultipolygonToSimpleWay() {
		return true;
	}

	@Override
	public void updateDataSet(DataSet ds) {
		final String filename = getAssociatedFile().getName();
		if (isDepartementFile(filename)) {
			setNationalPortalPath("GEOFLA®-Départements-30383060");
		} else if (isCommuneFile(filename)) {
			setNationalPortalPath("GEOFLA®-Communes-30383083");
		}
		for (OsmPrimitive p : ds.allPrimitives()) {
			if (hasKeyIgnoreCase(p, "Id_geofla", "Id_GéoFLA")) {
				String deptName = WordUtils.capitalizeFully(getAndRemoveIgnoreCase(p, "Nom_dept", "Nom_Département"));
				if ("Reunion".equals(deptName)) {
					deptName = "La Réunion";
				}
				if (isDepartementFile(filename)) {
					p.put("name", deptName);
				} else if (isCommuneFile(filename)) {
					p.put("name", WordUtils.capitalizeFully(getAndRemoveIgnoreCase(p, "NOM_COMM")));
					replace(p, "INSEE_COM", "ref:INSEE");
				}
				p.put("boundary", "administrative");
				String nature = getIgnoreCase(p, "Nature");
				if ("Frontière internationale".equalsIgnoreCase(nature) || "Limite côtière".equalsIgnoreCase(nature)) {
					p.put("admin_level", "2");
				} else if ("Limite de région".equalsIgnoreCase(nature)) {
					p.put("admin_level", "4");
				} else if (isDepartementFile(filename) || "Limite de département".equalsIgnoreCase(nature)) {
					p.put("admin_level", "6");
				} else if(isArrondissementFile(filename) || "Limite d'arrondissement".equalsIgnoreCase(nature)) {
					p.put("admin_level", "7");
				} else if(isCommuneFile(filename)) {
					p.put("admin_level", "8");
				}
				if (p instanceof Relation) {
					p.put("type", "boundary");
				}
				LatLon llCentroid = getLatLon(p, deptName, "centroid", "Centroïde");
				if (llCentroid != null) {
					Node centroid = new Node(llCentroid);
					ds.addPrimitive(centroid);
					//centroid.put("name", p.get("name"));
					if (p instanceof Relation) {
						((Relation) p).addMember(new RelationMember("centroid", centroid));
					}
				}
				LatLon llChefLieu = getLatLon(p, deptName, "chf_lieu", "Chef_Lieu");
				if (llChefLieu != null) {
					Node chefLieu = new Node(llChefLieu);
					ds.addPrimitive(chefLieu);
					//chefLieu.put("Code_chf", getAndRemoveIgnoreCase(p, "Code_chf", "Code_Chef_Lieu"));
					String name = WordUtils.capitalizeFully(getAndRemoveIgnoreCase(p, "Nom_chf", "Nom_Chef_lieu"));
					if (isArrondissementFile(filename)) {
						p.put("name", name);
					}
					chefLieu.put("name", name);
					if (p instanceof Relation) {
						((Relation) p).addMember(new RelationMember("admin_centre", chefLieu));
					}
				}
			}
		}
	}
	
	protected static boolean hasKeyIgnoreCase(OsmPrimitive p, String ... strings) {
		return getIgnoreCase(p, strings) != null;
	}

	protected static String getIgnoreCase(OsmPrimitive p, String ... strings) {
		String result = null;
		for (String s : strings) {
			if (result == null) result = p.get(s);
			if (result == null) result = p.get(s.toUpperCase());
			if (result == null) result = p.get(s.toLowerCase());
		}
		return result;
	}

	protected static void removeIgnoreCase(OsmPrimitive p, String ... strings) {
		for (String s : strings) {
			p.remove(s);
			p.remove(s.toUpperCase());
			p.remove(s.toLowerCase());
		}
	}
	
	protected static String getAndRemoveIgnoreCase(OsmPrimitive p, String ... strings) {
		String result = getIgnoreCase(p, strings);
		removeIgnoreCase(p, strings);
		return result;
	}

	protected static LatLon getLatLon(OsmPrimitive p, String dptName, String shortAttribute, String longAttribute) {
		String x = getAndRemoveIgnoreCase(p, "X_"+shortAttribute, "Abscisse_"+longAttribute);
		String y = getAndRemoveIgnoreCase(p, "Y_"+shortAttribute, "Ordonnée_"+longAttribute);
		if (x != null && y != null) {
			try {
				String dptCode = getIgnoreCase(p, "Code_dept", "Code_Département");
				if (dptCode != null && dptCode.equals("97") && dptName != null) {
					if (dptName.equals("Guadeloupe")) {
						dptCode = "971";
					} else if (dptName.equals("Martinique")) {
						dptCode = "972";
					} else if (dptName.equals("Guyane")) {
						dptCode = "973";
					} else if (dptName.equals("La Réunion")) {
						dptCode = "974";
					} else if (dptName.equals("Mayotte")) {
						dptCode = "976";
					} else {
						System.err.println("Unknown French department: "+dptName);
					}
				}
				return getLatLonByDptCode(new EastNorth(Double.parseDouble(x)*100.0, Double.parseDouble(y)*100.0), dptCode, false);
			} catch (NumberFormatException e) {
				System.err.println(e.getMessage());
			}
		}
		return null;
	}
}

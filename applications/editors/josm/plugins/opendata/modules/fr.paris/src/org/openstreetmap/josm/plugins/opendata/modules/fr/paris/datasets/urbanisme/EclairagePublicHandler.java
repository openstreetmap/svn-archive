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
package org.openstreetmap.josm.plugins.opendata.modules.fr.paris.datasets.urbanisme;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.opendata.core.datasets.fr.FrenchShpHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.paris.datasets.ParisDataSetHandler;
import org.openstreetmap.josm.tools.Geometry;

public class EclairagePublicHandler extends ParisDataSetHandler {

	private final InternalShpHandler shpHandler = new InternalShpHandler();
	
	public EclairagePublicHandler() {
		super(94);
		setName("Éclairage public");
		setShpHandler(shpHandler);
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsShpFilename(filename, "eclairage_public") || acceptsZipFilename(filename, "eclairage_public");
	}

	@Override
	protected String getDirectLink() {
		return PORTAL+"hn/eclairage_public.zip";
	}
	
	private class InternalShpHandler extends FrenchShpHandler {
		
		private final Map<String, Node> nodes = new HashMap<String, Node>();
		
		public InternalShpHandler() {
			setDbfCharset(Charset.forName(CP850));
		}
		
		private Node getNode(EastNorth en, String key) {
			Node n = nodes.get(key);
			/*if (n == null) {
				for (Node node : nodes.values()) {
					if (node.getEastNorth().equalsEpsilon(en, 0.0000001)) {
						return node;
					}
				}
			}*/
			return n;
		}
		
		private Node createOrGetNode(DataSet ds, EastNorth en) {
			String key = en.getX()+"/"+en.getY();
			Node n = getNode(en, key);
			if (n == null) {
				n = new Node(en);
				nodes.put(key, n);
				ds.addPrimitive(n);
			}
			return n;
		}

		@Override
		public void notifyFeatureParsed(Object feature, DataSet result,	Set<OsmPrimitive> featurePrimitives) {
			OsmPrimitive dataPrimitive = null;
			Way closedWay = null;
			List<Way> ways = new ArrayList<Way>();
			List<Node> nodes = new ArrayList<Node>();
			for (OsmPrimitive p : featurePrimitives) {
				if (p.hasKeys()) {
					dataPrimitive = p;
				}
				if (p instanceof Way) {
					Way w = (Way) p;
					ways.add(w);
					if (w.isClosed()) {
						closedWay = w;
					}
				} else if (p instanceof Node) {
					nodes.add((Node) p);
				}
			}
			if (dataPrimitive == null) {
				System.err.println("Found no primitive with tags");
			} else if (closedWay == null) {
				// ;Objet sans identification particulière pour ce niveau et cette thématique;147;eclairage_public.zip;Niveau 18
				dataPrimitive.put("FIXME", "This way is not closed and has not been recognized as highway=street_lamp.");
			} else {
				Node centroid = createOrGetNode(result, Geometry.getCentroid(closedWay.getNodes()));
				if (!centroid.hasKeys()) {
					centroid.setKeys(dataPrimitive.getKeys());
					centroid.put("highway", "street_lamp");
					replace(centroid, "Libelle", "lamp_model:fr");
				} else if (centroid.get("lamp_model:fr") != null && (dataPrimitive.get("Libelle") == null || !dataPrimitive.get("Libelle").equals(centroid.get("lamp_model:fr")))) {
					System.err.println("Found 2 street lamps at the same position with different types: '"+centroid.get("lamp_model:fr")+"' and '"+dataPrimitive.get("Libelle")+"'.");
				}
				for (Way w : ways) {
					w.setNodes(null);
					result.removePrimitive(w);
				}
				for (Node n : nodes) {
					result.removePrimitive(n);
				}
				
				if (centroid.get("lamp_model:fr") != null) {
					if (centroid.get("lamp_model:fr").contains("mural") && !centroid.get("lamp_model:fr").contains("au sol")) {
						centroid.put("lamp_mount", "wall mounted");
					} else if (centroid.get("lamp_model:fr").contains("au sol") && !centroid.get("lamp_model:fr").contains("mural")) {
						centroid.put("lamp_mount", "ground");
					} else {
						centroid.put("lamp_mount", "pole");
					}
					centroid.remove("Info");
				}
				
				/*if (dataPrimitive.get("Info") == null) {
					System.err.println("Found no primitive with tag 'Info'");
				} else if (dataPrimitive.get("Info").equals("LEA")) {		//	LEA;Lanterne électrique axiale;2834;eclairage_public.zip;Niveau 18
				} else if (dataPrimitive.get("Info").equals("LEL")) {		//	LEL;Lampadaire électrique;61337;eclairage_public.zip;Niveau 18
				} else if (dataPrimitive.get("Info").equals("LEM")) {		//	LEM;Lanterne électrique murale;789;eclairage_public.zip;Niveau 18
				} else if (dataPrimitive.get("Info").equals("LEMB")) {		//	LEMB;Lanterne électrique murale bord;14727;eclairage_public.zip;Niveau 18
				} else if (dataPrimitive.get("Info").equals("LEMRND")) {	//	LEMRND;Lanterne électrique murale renvoi à droite;5635;eclairage_public.zip;Niveau 18
				} else if (dataPrimitive.get("Info").equals("LEMRNG")) {	//	LEMRNG;Lanterne électrique murale renvoi à gauche;3822;eclairage_public.zip;Niveau 18
				} else if (dataPrimitive.get("Info").equals("LERRND")) {	//	LERRND;Lanterne électrique murale et boite raccord BT renvoi à droite;5657;eclairage_public.zip;Niveau 18
				} else if (dataPrimitive.get("Info").equals("LERRNG")) {	//	LERRNG;Lanterne électrique murale et boite raccord BT renvoi à gauche;3377;eclairage_public.zip;Niveau 18
				} else if (dataPrimitive.get("Info").equals("LSO")) {		//	LSO;Lanterne au sol;1337;eclairage_public.zip;Niveau 18
				} else if (dataPrimitive.get("Info").equals("PHO")) {		//	PHO;Poteau horaire;17;eclairage_public.zip;Niveau 18
				} else if (dataPrimitive.get("Info").equals("PPEP")) {		//	PPEP;Poteau provisoire d'éclairage public;181;eclairage_public.zip;Niveau 18
				} else if (dataPrimitive.get("Info").equals("PPR")) {		//	PPR;poteau à projecteur;67;eclairage_public.zip;Niveau 18
				} else if (dataPrimitive.get("Info").equals("PRJ")) {		//	PRJ;Projecteur au sol ou mural;1864;eclairage_public.zip;Niveau 18
				} else if (dataPrimitive.get("Info").equals("PRJRND")) {	//	PRJRND;Projecteur au sol ou mural renvoi à droite;42;eclairage_public.zip;Niveau 18
				} else if (dataPrimitive.get("Info").equals("PRJRNG")) {	//	PRJRNG;Projecteur au sol ou mural renvoi à gauche;57;eclairage_public.zip;Niveau 18
				} else {
					System.err.println("Unsupported Info: "+dataPrimitive.get("Info"));
				}*/
			}
		}
	}
	
	@Override
	public void updateDataSet(DataSet ds) {
		// Done in notifyFeatureParsed() for drastic performance reasons
		shpHandler.nodes.clear();
	}
}

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
package org.openstreetmap.josm.plugins.opendata.modules.fr.paris.datasets.environnement;

import java.util.Set;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.opendata.modules.fr.paris.datasets.ParisDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.paris.datasets.ParisShpHandler;
import org.openstreetmap.josm.tools.Geometry;

public class MobilierVoiePubliqueHandler extends ParisDataSetHandler {

	private final InternalShpHandler shpHandler = new InternalShpHandler();
	
	public MobilierVoiePubliqueHandler() {
		super(96);
		setName("Mobiliers sur voie publique");
		setShpHandler(shpHandler);
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsShpFilename(filename, "environnement") || acceptsZipFilename(filename, "environnement");
	}

	@Override
	protected String getDirectLink() {
		return PORTAL+"hn/environnement.zip";
	}
	
	private final class InternalShpHandler extends ParisShpHandler {
		@Override
		public void notifyFeatureParsed(Object feature, DataSet result,	Set<OsmPrimitive> featurePrimitives) {
			initFeaturesPrimitives(featurePrimitives);
			String type = dataPrimitive.get("Libelle");
			if (type.startsWith("Banc ")) {
				if (closedWay == null && dataPrimitive instanceof Way && ((Way)dataPrimitive).getNodesCount() > 3) {
					// Some benches can be converted to closed ways by removing some extra nodes at the beginning
					Way copy = new Way((Way) dataPrimitive);
					while (copy.getNodesCount() > 3 && !copy.isClosed()) {
						copy.removeNode(copy.getNode(0));
					}
					if (copy.isClosed()) {
						closedWay = copy;
					} else {
						// Try in the opposite direction (remove nodes from the end)
						copy = new Way((Way) dataPrimitive);
						while (copy.getNodesCount() > 3 && !copy.isClosed()) {
							copy.removeNode(copy.getNode(copy.getNodesCount()-1));
						}
						if (copy.isClosed()) {
							closedWay = copy;
						}
					}
				}
				if (closedWay != null) {
					Node bench = createOrGetNode(result, Geometry.getCentroid(closedWay.getNodes()));
					bench.put("amenity", "bench");
					bench.put("description", type);
					removePrimitives(result);
				} else {
					System.err.println("Bench without closed area: "+type);
				}
			} else if (type.startsWith("Poubelle") || type.startsWith("Borne de propreté")) {
				if (closedWay != null) {
					Node basket = createOrGetNode(result, Geometry.getCentroid(closedWay.getNodes()));
					basket.put("amenity", "waste_basket");
					basket.put("description", type);
					removePrimitives(result);
				} else {
					System.err.println("Waste basket without closed area: "+type);
				}
			} else if (type.startsWith("Bac à sable")) {
				if (closedWay == null && dataPrimitive instanceof Way && ((Way)dataPrimitive).getNodesCount() == 2) {
					// Sandpits are badly built, fix them
					closedWay = (Way) dataPrimitive;
					for (Way w : ways) {
						if (w.getNodesCount() == 4) {
							Node n1 = w.getNode(1);
							Node n2 = w.getNode(2);
							if (closedWay.getNode(1).getCoor().distance(n1.getCoor()) < closedWay.getNode(1).getCoor().distance(n2.getCoor())) {
								closedWay.addNode(n1);
								closedWay.addNode(n2);
							} else {
								closedWay.addNode(n2);
								closedWay.addNode(n1);
							}
							closedWay.addNode(closedWay.getNode(0));
							break;
						}
					}
				}
				if (closedWay != null) {
					// FIXME: lines commented as "sandpits" in this data set seem to be false data
					/*Node pg = createOrGetNode(result, Geometry.getCentroid(closedWay.getNodes()));
					pg.put("leisure", "playground");
					pg.put("playground", "sandpit");
					pg.put("description", type);*/
					removePrimitives(result);
				} else {
					System.err.println("Sandpit without closed area: "+type);
				}
			} else if (type.startsWith("Corbeille florale")) {
				if (closedWay != null) {
					Node n = createOrGetNode(result, Geometry.getCentroid(closedWay.getNodes()));
					n.put("man_made", "floral_basket");
					n.put("description", type);
					removePrimitives(result);
				} else {
					System.err.println("Floral basket without closed area: "+type);
				}
			} else if (type.startsWith("Objet sans identification particulière")) {
				//dataPrimitive.put("FIXME", "unknown object. DO NOT upload as it !");
				removePrimitives(result);
			} else {
				System.err.println("Unsupported object type: "+type);
			}
		}
	}
	
	@Override
	public void updateDataSet(DataSet ds) {
		// Done in notifyFeatureParsed() for drastic performance reasons
		shpHandler.nodes.clear();
	}
}

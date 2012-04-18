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
package org.openstreetmap.josm.plugins.opendata.modules.fr.paris.datasets;

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

public class ParisShpHandler extends FrenchShpHandler {
	
	public final Map<String, Node> nodeMap = new HashMap<String, Node>();
	
	protected OsmPrimitive dataPrimitive;
	protected Way closedWay;
	protected List<Way> ways;
	public List<Node> nodes;
	
	public ParisShpHandler() {
		setDbfCharset(Charset.forName(CP850));
	}
	
	private Node getNode(EastNorth en, String key) {
		Node n = nodeMap.get(key);
		/*if (n == null) {
			for (Node node : nodes.values()) {
				if (node.getEastNorth().equalsEpsilon(en, 0.0000001)) {
					return node;
				}
			}
		}*/
		return n;
	}
	
	protected Node createOrGetNode(DataSet ds, EastNorth en) {
		String key = en.getX()+"/"+en.getY();
		Node n = getNode(en, key);
		if (n == null) {
			n = new Node(en);
			nodeMap.put(key, n);
			ds.addPrimitive(n);
		}
		return n;
	}

	protected final void initFeaturesPrimitives(Set<OsmPrimitive> featurePrimitives) {
		dataPrimitive = null;
		closedWay = null;
		ways = new ArrayList<Way>();
		nodes = new ArrayList<Node>();
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
	}
	
	protected final void removePrimitives(DataSet result) {
		for (Way w : ways) {
			w.setNodes(null);
			result.removePrimitive(w);
		}
		for (Node n : nodes) {
			result.removePrimitive(n);
		}
	}
}

// License: GPL. Copyright 2007 by Immanuel Scholz and others
package org.openstreetmap.josm.data.osm.visitor;

import java.util.Collection;
import java.util.HashSet;

import org.openstreetmap.josm.data.osm.Segment;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;

/**
 * Collect all nodes a specific osm primitive has.
 * 
 * @author imi
 */
public class AllNodesVisitor implements Visitor {

	/**
	 * The resulting nodes collected so far.
	 */
	public Collection<Node> nodes = new HashSet<Node>();

	/**
	 * Nodes have only itself as nodes.
	 */
	public void visit(Node n) {
		nodes.add(n);
	}

	/**
	 * Line segments have exactly two nodes: from and to.
	 */
	public void visit(Segment ls) {
		if (!ls.incomplete) {
			visit(ls.from);
			visit(ls.to);
		}
	}

	/**
	 * Ways have all nodes from their segments.
	 */
	public void visit(Way w) {
		for (Segment ls : w.segments)
			visit(ls);
	}

	/**
	 * @return All nodes the given primitive has.
	 */
	public static Collection<Node> getAllNodes(Collection<? extends OsmPrimitive> osms) {
		AllNodesVisitor v = new AllNodesVisitor();
		for (OsmPrimitive osm : osms)
			osm.visit(v);
		return v.nodes;
	}
}

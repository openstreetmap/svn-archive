package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.HashSet;

import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.validator.Severity;
import org.openstreetmap.josm.plugins.validator.Test;
import org.openstreetmap.josm.plugins.validator.TestError;

/**
 * Checks for self-intersecting ways.
 */
public class SelfIntersectingWay extends Test {
	public SelfIntersectingWay() {
		super(tr("Self-intersecting ways"),
			  tr("This test checks for ways " +
				"that contain some of their nodes more than once"));
	}

	@Override public void visit(Way w) {
		HashSet<Node> nodes = new HashSet<Node>();

		for (Node n : w.nodes) {
			if (nodes.contains(n)) {
				errors.add(new TestError(this,
					Severity.WARNING, tr("Self-intersecting ways"), w, 0));
				break;
			} else {
				nodes.add(n);
			}
		}
	}		
}

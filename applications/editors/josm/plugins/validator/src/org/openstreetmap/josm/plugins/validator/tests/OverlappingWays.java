package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.List;
import java.util.ArrayList;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.WaySegment;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.tools.Pair;
import org.openstreetmap.josm.plugins.validator.Severity;
import org.openstreetmap.josm.plugins.validator.Test;
import org.openstreetmap.josm.plugins.validator.TestError;
import org.openstreetmap.josm.plugins.validator.util.Bag;

/**
 * Tests if there are overlapping ways
 * 
 * @author frsantos
 */
public class OverlappingWays extends Test 
{
	/** Bag of all way segments */
	Bag<Pair<Node,Node>, WaySegment> nodePairs;
	
	/**
	 * Constructor
	 */
	public OverlappingWays() 
	{
		super(tr("Overlapping ways."),
			  tr("This test checks that a connection between two nodes "
				+ "is not used by more than one way."));
		
	}


	@Override
	public void startTest() 
	{
		nodePairs = new Bag<Pair<Node,Node>, WaySegment>(1000);
	}

	@Override
	public void endTest() 
	{
		for (List<WaySegment> duplicated : nodePairs.values())
		{
			if (duplicated.size() > 1)
			{
				List<OsmPrimitive> prims = new ArrayList<OsmPrimitive>();
				for (WaySegment ws : duplicated) prims.add(ws.way);
				errors.add(new TestError(this, Severity.OTHER,
					tr("Overlapping ways"), prims, duplicated));
			}
		}
		nodePairs = null;
	}

	@Override
	public void visit(Way w) 
	{
		Node lastN = null;
		int i = -2;
		for (Node n : w.nodes) {
			i++;
			if (lastN == null) {
				lastN = n;
				continue;
			}
			nodePairs.add(Pair.sort(new Pair<Node,Node>(lastN, n)),
				new WaySegment(w, i));
			lastN = n;
		}
	}
}

package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.List;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.NodePair;
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
	Bag<NodePair, OsmPrimitive> nodePairs;
	
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
		nodePairs = new Bag<NodePair, OsmPrimitive>(1000);
	}

	@Override
	public void endTest() 
	{
		for (List<OsmPrimitive> duplicated : nodePairs.values())
		{
			if (duplicated.size() > 1)
			{
				errors.add( new TestError(this, Severity.OTHER, tr("Overlapping ways"), duplicated) );
			}
		}
		nodePairs = null;
	}

	@Override
	public void visit(Way w) 
	{
		Node lastN = null;
		for (Node n : w.nodes) {
			if (lastN == null) {
				lastN = n;
				continue;
			}
			if (n.hashCode() > lastN.hashCode()) {
				nodePairs.add(new NodePair(lastN, n), w);
			} else {
				nodePairs.add(new NodePair(n, lastN), w);
			}
			lastN = n;
		}
	}
}

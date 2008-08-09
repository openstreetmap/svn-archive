package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.*;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmUtils;
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
		Map<List<Way>, List<WaySegment>> ways_seen = 
                        new HashMap<List<Way>, List<WaySegment>>(500);

		for (List<WaySegment> duplicated : nodePairs.values())
		{
			int ways = duplicated.size();

			if (ways > 1)
			{
				List<OsmPrimitive> prims = new ArrayList<OsmPrimitive>();
				List<Way> current_ways = new ArrayList<Way>();
				List<WaySegment> highlight;
				int highway = 0;
				int railway = 0;
				int area = 0;

				for (WaySegment ws : duplicated) 
				{
					if (ws.way.get("highway") != null)
						highway++;
					else if (ws.way.get("railway") != null)
						railway++;
					Boolean ar = OsmUtils.getOsmBoolean(ws.way.get("area"));
					if (ar != null && ar)
						area++;
					if (ws.way.get("landuse") != null || ws.way.get("natural") != null
					|| ws.way.get("amenity") != null || ws.way.get("leisure") != null)
					{
						area++; ways--;
					}

					prims.add(ws.way);
					current_ways.add(ws.way);
				}
				/* These ways not seen before
				 * If two or more of the overlapping ways are
 				 * highways or railways mark a seperate error
  				*/
				if ((highlight = ways_seen.get(current_ways)) == null)
                                {
					String errortype;

					if(area > 0)
					{
						if (ways == 0 || duplicated.size() == area)
							errortype = tr("Overlapping areas");
						else if (highway == ways)
							errortype = tr("Overlapping highways (with area)");
						else if (railway == ways)
							errortype = tr("Overlapping railways (with area)");
						else
							errortype = tr("Overlapping ways (with area)");
					}
					else if (highway == ways)
						errortype = tr("Overlapping highways");
					else if (railway == ways)
						errortype = tr("Overlapping railways");
					else
						errortype = tr("Overlapping ways");

					errors.add(new TestError(this, Severity.OTHER, tr(errortype), prims, duplicated));
					ways_seen.put(current_ways, duplicated);
				}
				else	/* way seen, mark highlight layer only */
				{
					for (WaySegment ws : duplicated)
						highlight.add(ws);
				}
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

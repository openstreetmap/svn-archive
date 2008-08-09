package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.*;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.plugins.validator.Severity;
import org.openstreetmap.josm.plugins.validator.Test;
import org.openstreetmap.josm.plugins.validator.TestError;
import org.openstreetmap.josm.plugins.validator.util.Bag;

/**
 * Check area type ways for errors
 *
 * @author stoecker
 */
public class UnclosedWays extends Test  {
	/** The already detected errors */
	Bag<Way, Way> _errorWays;

	/**
	 * Constructor
	 */
	public UnclosedWays()
	{
		super(tr("Unclosed Ways."),
			  tr("This tests if ways which should be circular are closed."));
	}

	@Override
	public void startTest()
	{
		_errorWays = new Bag<Way, Way>();
	}

	@Override
	public void endTest()
	{
		_errorWays = null;
	}
	
	@Override
	public void visit(Way w)
	{
		boolean force = false; /* force even if end-to-end distance is long */
		String type = null, test;
		
		if( w.deleted || w.incomplete )
			return;
		
		test = w.get("natural");
		if(test != null)
		{
			if(!"coastline".equals(test))
				force = true;
			type = tr("natural type {0}", tr(test));
		}
		test = w.get("landuse");
		if(test != null)
		{
			force = true;
			type = tr("landuse type {0}", tr(test));
		}
		test = w.get("amenities");
		if(test != null)
		{
			force = true;
			type = tr("amenities type {0}", tr(test));
		}
		test = w.get("sport");
		if(test != null)
		{
			force = true;
			type = tr("sport type {0}", tr(test));
		}
		test = w.get("tourism");
		if(test != null)
		{
			force = true;
			type = tr("tourism type {0}", tr(test));
		}
		test = w.get("shop");
		if(test != null)
		{
			force = true;
			type = tr("shop type {0}", tr(test));
		}
		test = w.get("leisure");
		if(test != null)
		{
			force = true;
			type = tr("leisure type {0}", tr(test));
		}
		test = w.get("waterway");
		if(test != null && test.equals("riverbank"))
		{
			force = true;
			type = tr("waterway type {0}", tr(test));
		}
		/*test = w.get("junction");
		if(test != null && test.equals("roundabout"))
		{
			force = true;
			type = tr("junction type {0}", tr(test));
		}*/
		test = w.get("building");
		if (test != null && ("true".equalsIgnoreCase(test) || "yes".equalsIgnoreCase(test) || "1".equals(test)))
		{
			force = true;
			type = tr("building");
		}
		test = w.get("area");
		if (test != null && ("true".equalsIgnoreCase(test) || "yes".equalsIgnoreCase(test) || "1".equals(test)))
		{
			force = true;
			type = tr("area");
		}

		if(type != null)
		{
			LatLon s = w.nodes.get(0).coor;
			LatLon e = w.nodes.get(w.nodes.size()-1).coor;
			/* take unclosed ways with end-to-end-distance below 10 km */
			if(s != e && (force || s.greatCircleDistance(e) < 10000))
			{
				List<OsmPrimitive> primitives = new ArrayList<OsmPrimitive>();
				primitives.add(w);
				errors.add(new TestError(this, Severity.WARNING, tr("Unclosed way"), type, primitives));
				_errorWays.add(w,w);
			}
		}
	}
}

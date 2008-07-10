package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.geom.Point2D;
import java.util.*;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.validator.Severity;
import org.openstreetmap.josm.plugins.validator.Test;
import org.openstreetmap.josm.plugins.validator.TestError;
import org.openstreetmap.josm.plugins.validator.util.Bag;
import org.openstreetmap.josm.plugins.validator.util.Util;

/**
 * Check cyclic ways for errors
 *
 * @author jrreid
 */
public class WronglyOrderedWays extends Test  {
	/** The already detected errors */
	Bag<Way, Way> _errorWays;

	/**
	 * Constructor
	 */
	public WronglyOrderedWays()
	{
		super(tr("Wrongly Ordered Ways."),
			  tr("This test checks the direction of water, land and coastline ways."));
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
		String errortype = "";
		
		if( w.deleted || w.incomplete )
			return;
		
		String natural = w.get("natural");
		if( natural == null)
			return;
		
		if( natural.equals("coastline") )
			errortype = tr("Reversed coastline: land not on left side");
		else if(natural.equals("water") )
			errortype = tr("Reversed water: land not on left side");
		else if( natural.equals("land") )
			errortype = tr("Reversed land: land not on left side");
		else
			return;


		/**
		 * Test the directionality of the way
		 *
		 * Assuming a closed non-looping way, compute twice the area
		 * of the polygon using the formula 2*a = sum (Xn * Yn+1 - Xn+1 * Yn)
		 * If the area is negative the way is ordered in a clockwise direction
		 *
		 */

		if(w.nodes.get(0) == w.nodes.get(w.nodes.size()-1))
		{
			double area2 = 0;

			for (int node = 1; node < w.nodes.size(); node++)
			{
				area2 += (w.nodes.get(node-1).coor.lon() * w.nodes.get(node).coor.lat()
				- w.nodes.get(node).coor.lon() * w.nodes.get(node-1).coor.lat());
			}

			if(((natural.equals("coastline") || natural.equals("land")) && area2 < 0.)
			|| (natural.equals("water") && area2 > 0.))
			{
				List<OsmPrimitive> primitives = new ArrayList<OsmPrimitive>();
				primitives.add(w);
				errors.add( new TestError(this, Severity.WARNING, errortype, primitives) );
				_errorWays.add(w,w);
			}
		}
	}
}

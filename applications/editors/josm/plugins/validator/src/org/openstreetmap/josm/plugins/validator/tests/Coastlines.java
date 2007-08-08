package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.geom.Point2D;
import java.util.*;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Segment;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.validator.Severity;
import org.openstreetmap.josm.plugins.validator.Test;
import org.openstreetmap.josm.plugins.validator.TestError;
import org.openstreetmap.josm.plugins.validator.util.Bag;
import org.openstreetmap.josm.plugins.validator.util.Util;

/**
 * Check coastlines for errors
 * 
 * @author frsantos
 */
public class Coastlines extends Test 
{
    /** All ways, grouped by cells */
    Map<Point2D,List<Way>> _cellWays;
    /** The already detected errors */
    Bag<Way, Way> _errorWays;

	/**
	 * Constructor
	 */
	public Coastlines() 
	{
		super(tr("Coastlines."),
			  tr("This test checks that coastlines are correct."));
	}

    @Override
    public void startTest() 
    {
        _cellWays = new HashMap<Point2D,List<Way>>(1000);
        _errorWays = new Bag<Way, Way>();
    }

    @Override
    public void endTest() 
    {
        _cellWays = null;
        _errorWays = null;
    }
    
	@Override
	public void visit(Way w) 
	{
        if( w.deleted )
            return;
        
        String natural = w.get("natural");
        if( natural == null || !natural.equals("coastline") )
            return;
        
        List<List<Way>> cellWays = Util.getWaysInCell(w, _cellWays);
        for( List<Way> ways : cellWays)
        {
            for( Way w2 : ways)
            {
                if( _errorWays.contains(w, w2) || _errorWays.contains(w2, w) )
                	continue;
                
                String natural2 = w.get("natural");
                if( natural2 == null || !natural2.equals("coastline") )
                    continue;
                
                int numSegments1 = w.segments.size();
                Segment start1 = w.segments.get(0);
                Segment end1 = numSegments1 > 1 ? w.segments.get(numSegments1 - 1) : start1;

                int numSegments2 = w2.segments.size();
                Segment start2 = w2.segments.get(0);
                Segment end2 = numSegments2 > 1 ? w2.segments.get(numSegments2 - 1) : start2;
                
                if( start1.from.equals(start2.from) || end1.to.equals(end2.to))
                {
                    List<OsmPrimitive> primitives = new ArrayList<OsmPrimitive>();
                    primitives.add(w);
                    primitives.add(w2);
                    errors.add( new TestError(this, Severity.ERROR, tr("Unordered coastline"), primitives) );
                    _errorWays.add(w, w2);
                }
            }
            ways.add(w);
        }
	}
}

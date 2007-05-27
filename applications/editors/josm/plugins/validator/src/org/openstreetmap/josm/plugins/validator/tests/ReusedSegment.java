package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.List;
import java.util.Map;

import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.plugins.validator.*;
import org.openstreetmap.josm.plugins.validator.util.Bag;
/**
 * Tests if there are duplicate segments
 * 
 * @author frsantos
 */
public class ReusedSegment extends Test 
{
	/** Bag of all segments */
	Bag<Segment, OsmPrimitive> segments;
	
	/**
	 * Constructor
	 */
	public ReusedSegment() 
	{
		super(tr("Reused segments."),
			  tr("This test checks if a segment is used in more than one way."));
	}


	@Override
	public void startTest() 
	{
		segments = new Bag<Segment, OsmPrimitive>(1000);
	}

	@Override
	public void endTest() 
	{
		for(Map.Entry<Segment, List<OsmPrimitive>> entry : segments.entrySet() )
		{
            Segment s = entry.getKey();
			if( entry.getValue().size() > 1)
			{
				errors.add( new TestError(this, Severity.OTHER, tr("Reused segments"), s) );
			}
		}
		segments = null;
	}

	@Override
	public void visit(Way w) 
	{
        for( Segment s : w.segments)
            if( !s.deleted ) segments.add( s, w );
	}
}

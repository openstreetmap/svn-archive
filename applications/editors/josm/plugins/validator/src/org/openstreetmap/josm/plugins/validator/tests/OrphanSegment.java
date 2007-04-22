package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.*;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.plugins.validator.Severity;
import org.openstreetmap.josm.plugins.validator.Test;
import org.openstreetmap.josm.plugins.validator.TestError;

/**
 * Check that every segment is in a way
 * 
 * @author frsantos
 */
public class OrphanSegment extends Test 
{
	/** Bag of all nodes */
	Set<Segment> segments;
	
	/**
	 * Constructor
	 */
	public OrphanSegment() 
	{
		super(tr("Orphaned segments."),
		      tr("This test checks that every segment is in a way."));
	}


	@Override
	public void startTest() 
	{
		segments = new HashSet<Segment>(1000);
	}

	@Override
	public void endTest() 
	{
		for(Segment segment : segments )
		{
			errors.add( new TestError(this, Severity.OTHER, tr("Segments not in a way"), segment) );
		}
		segments = null;
	}

    @Override
    public void visit(Collection<OsmPrimitive> selection) 
    {
        // If there is a partial selection, it may be false positives if a
        // segment is selected, but not the container way. So, in this
        // case, we must visit all ways, selected or not.

        for (OsmPrimitive p : selection)
        {
            if( !p.deleted )
            {
                if( !partialSelection || p instanceof Segment )
                    p.visit(this);
            }
        }
        
        if( partialSelection )
        {
            for( Way w : Main.ds.ways)
                visit(w);
        }
    }
    
	@Override
	public void visit(Segment s) 
	{
        
		segments.add(s);
	}
	
	@Override
	public void visit(Way w) 
	{
		segments.removeAll(w.segments);
	}
}

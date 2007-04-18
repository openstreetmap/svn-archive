package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.HashSet;
import java.util.Set;

import org.openstreetmap.josm.data.osm.Segment;
import org.openstreetmap.josm.data.osm.Way;
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

package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.data.osm.Segment;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.validator.Severity;
import org.openstreetmap.josm.plugins.validator.Test;
import org.openstreetmap.josm.plugins.validator.TestError;

/**
 * Check for unordered ways
 * 
 * @author frsantos
 */
public class UnorderedWay extends Test 
{
	/**
	 * Constructor
	 */
	public UnorderedWay() 
	{
		super(tr("Unordered ways."),
			  tr("This test checks that all segments in a way are properly ordered."));
	}

	@Override
	public void visit(Way w) 
	{
		Segment last = null;
		for(Segment s: w.segments)
		{
			if( last != null && !last.incomplete && !s.incomplete && !last.to.equals(s.from) )
			{
				errors.add( new TestError(Severity.WARNING, tr("Unordered ways"), w) );
				break;
			}
			last = s;
		}
	}
}

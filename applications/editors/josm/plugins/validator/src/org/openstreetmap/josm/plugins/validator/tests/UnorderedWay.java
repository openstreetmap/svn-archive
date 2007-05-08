package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.actions.ReorderAction;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.plugins.validator.*;

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
				errors.add( new TestError(this, Severity.WARNING, tr("Unordered ways"), w) );
				break;
			}
			last = s;
		}
	}
	
	@Override
	public Command fixError(TestError testError)
	{
		for(OsmPrimitive p : testError.getPrimitives() )
		{
            return ReorderAction.reorderWay((Way)p);
		}
		
		return null;
	}
	
	@Override
	public boolean isFixable(TestError testError)
	{
		return (testError.getTester() instanceof UnorderedWay);
	}	
}

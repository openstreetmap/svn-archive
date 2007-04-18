package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.*;

import org.openstreetmap.josm.actions.ReorderAction;
import org.openstreetmap.josm.command.*;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
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
				errors.add( new TestError(this, Severity.WARNING, tr("Unordered ways"), w) );
				break;
			}
			last = s;
		}
	}
	
	@Override
	public Command fixError(TestError testError)
	{
		List<Command> commands = new ArrayList<Command>(50);
		
		for(OsmPrimitive p : testError.getPrimitives() )
		{
			Way w = (Way)p;
			Way newWay = new Way(w);
			newWay.segments.clear();
			newWay.segments.addAll(ReorderAction.sortSegments(new HashSet<Segment>(w.segments)));
			return new ChangeCommand(p, newWay);
		}
		
		return commands.size() > 1 ? new SequenceCommand("Remove keys", commands) : commands.get(0);
	}
	
	@Override
	public boolean isFixable(TestError testError)
	{
		return (testError.getTester() instanceof UnorderedWay);
	}	
}

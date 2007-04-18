package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.data.osm.Segment;
import org.openstreetmap.josm.plugins.validator.Severity;
import org.openstreetmap.josm.plugins.validator.Test;
import org.openstreetmap.josm.plugins.validator.TestError;

/**
 * Checks that from/to nodes in a segment are different
 * 
 * @author frsantos
 */
public class SingleNodeSegment extends Test 
{
	/** Tags allowed in a segment */
	public static String[] allowedTags = new String[] { "created_by" };
	/**
	 * Constructor
	 */
	public SingleNodeSegment() 
	{
		super(tr("Single node segments."),
			  tr("This test checks that there are no segments with the same node as start and destination."));
	}

	@Override
	public void visit(Segment s) 
	{
		if( !s.incomplete && s.from.equals(s.to) )
		{
			errors.add( new TestError(this, Severity.ERROR, tr("Single node segments"), s) );
		}
	}
	
	@Override
	public Command fixError(TestError testError)
	{
		return new DeleteCommand(testError.getPrimitives());
	}
	
	@Override
	public boolean isFixable(TestError testError)
	{
		return (testError.getTester() instanceof SingleNodeSegment);
	}	
}

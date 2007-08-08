package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.*;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Segment;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.CollectBackReferencesVisitor;
import org.openstreetmap.josm.data.osm.visitor.NameVisitor;
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
		if( testError.getPrimitives().isEmpty() )
			return null;
		
        Segment s = (Segment)testError.getPrimitives().get(0);
        if( s.deleted )
            return null;

        List<Command> cmds = new ArrayList<Command>();
        cmds.add(new DeleteCommand(testError.getPrimitives()));

		CollectBackReferencesVisitor refV = new CollectBackReferencesVisitor(Main.ds);
		s.visit(refV);

		for(OsmPrimitive p : refV.data)
        {
            if( p.deleted )
                continue;

            Way newway = new Way((Way)p);
            if( newway.segments.remove(s) ) // Made changes?
            {
                // If no segments left, delete the way
                if( newway.segments.size() == 0 )
                    cmds.add(new DeleteCommand(Arrays.asList(new OsmPrimitive[]{p})));
                else
                    cmds.add(new ChangeCommand(p, newway));
            }
        }
		
		if( cmds.size() == 1 ) // Segment wasn't in any way
			return cmds.get(0);
		
		NameVisitor nameV = new NameVisitor();
		s.visit(nameV);
        return new SequenceCommand(tr("Delete")+" "+tr(nameV.className)+" "+nameV.name, cmds);
	}
	
	@Override
	public boolean isFixable(TestError testError)
	{
		return (testError.getTester() instanceof SingleNodeSegment);
	}
}

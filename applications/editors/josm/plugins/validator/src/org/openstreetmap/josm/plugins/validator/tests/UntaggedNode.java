package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.*;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Segment;
import org.openstreetmap.josm.plugins.validator.Severity;
import org.openstreetmap.josm.plugins.validator.Test;
import org.openstreetmap.josm.plugins.validator.TestError;
/**
 * Checks for untagged nodes that are in no segment
 * 
 * @author frsantos
 */
public class UntaggedNode extends Test 
{
	/** Tags allowed in a node */
	public static String[] allowedTags = new String[] { "created_by" };
	
	/** Bag of all nodes */
	Set<Node> emptyNodes;
	
	/**
	 * Constructor
	 */
	public UntaggedNode() 
	{
		super(tr("Untagged nodes."),
			  tr("This test checks for untagged nodes that are not part of any segment."));
	}

	@Override
	public void startTest() 
	{
		emptyNodes = new HashSet<Node>(100);
	}
	
	@Override
    public void visit(Collection<OsmPrimitive> selection) 
    {
		// If there is a partial selection, it may be false positives if a
		// node is selected, but not the container segment. So, in this
		// case, we must visit all segments, selected or not.

		for (OsmPrimitive p : selection)
        {
        	if( !p.deleted )
        	{
        		if( !partialSelection || p instanceof Node )
        			p.visit(this);
        	}
        }
        
		if( partialSelection )
		{
			for( Segment s : Main.ds.segments)
				visit(s);
		}
    }
    
	@Override
	public void visit(Node n) 
	{
		int numTags = 0;
		Map<String, String> tags = n.keys;
		if( tags != null )
		{
			numTags = tags.size();
			for( String tag : allowedTags)
				if( tags.containsKey(tag) ) numTags--;
		}
		
		if( numTags == 0 )
		{
			emptyNodes.add(n);
		}
	}
	
	@Override
	public void visit(Segment s) 
	{
		emptyNodes.remove(s.from);
		emptyNodes.remove(s.to);
	}
	
	@Override
	public void endTest() 
	{
		for(Node node : emptyNodes)
		{
			errors.add( new TestError(this, Severity.OTHER, tr("Untagged and unconnected nodes"), node) );
		}
		emptyNodes = null;
	}
	
	@Override
	public Command fixError(TestError testError)
	{
		return new DeleteCommand(testError.getPrimitives());
	}
	
	@Override
	public boolean isFixable(TestError testError)
	{
		return (testError.getTester() instanceof UntaggedNode);
	}		
}

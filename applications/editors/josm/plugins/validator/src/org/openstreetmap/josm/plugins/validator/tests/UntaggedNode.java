package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.josm.data.osm.Node;
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
	/** Tags allowed in a segment */
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
			errors.add( new TestError(Severity.OTHER, tr("Untagged and unconnected nodes"), node) );
		}
		emptyNodes = null;
	}
}

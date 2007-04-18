package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.*;

import org.openstreetmap.josm.command.*;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Segment;
import org.openstreetmap.josm.plugins.validator.Severity;
import org.openstreetmap.josm.plugins.validator.Test;
import org.openstreetmap.josm.plugins.validator.TestError;

/**
 * Check if a segment has tags
 * 
 * @author frsantos
 */
public class TaggedSegment extends Test 
{
	/** Tags allowed in a segment */
	public static String[] allowedTags = new String[] { "created_by", "converted_by", "source" };
	
	/**
	 * Constructor
	 */
	public TaggedSegment() 
	{
		super(tr("Tagged segments"),
			  tr("This test checks that no segment segment is tagged. Only ways should be tagged."));
	}

	@Override
	public void visit(Segment s) 
	{
		Map<String, String> tags = s.keys;
		if( tags == null )
			return;
		tags = new HashMap<String, String>(tags);
		for( String tag : allowedTags)
			tags.remove(tag);
		
		if( tags.size() > 0 )
		{
			errors.add( new TestError(this, Severity.WARNING, tr("Segments with tags"), s) );
		}
	}
	
	@Override
	public Command fixError(TestError testError)
	{
		List<Command> commands = new ArrayList<Command>(50);
		
		int i = -1;
		List<OsmPrimitive> primitives = testError.getPrimitives();
		for(OsmPrimitive p : primitives )
		{
			i++;
			Map<String, String> tags = p.keys;
			if( tags == null )
				continue;
			
			tags = new HashMap<String, String>(tags);
			for( String tag : allowedTags)
				tags.remove(tag);
			
			if( tags.size() == 0 )
				continue;
		
			for(String key : tags.keySet() )
			{
				commands.add( new ChangePropertyCommand(primitives.subList(i, i+1), key, null) );
			}
		}
		
		return commands.size() > 1 ? new SequenceCommand("Remove keys", commands) : commands.get(0);
	}
	
	@Override
	public boolean isFixable(TestError testError)
	{
		return (testError.getTester() instanceof TaggedSegment);
	}	
}

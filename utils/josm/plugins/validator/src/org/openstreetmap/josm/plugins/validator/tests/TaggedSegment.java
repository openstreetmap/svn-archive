package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Map;

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
		
		int numAllowedTags = 0;
		for( String tag : allowedTags)
			if( tags.containsKey(tag) ) numAllowedTags++;
		
		if( tags.size() - numAllowedTags > 0 )
		{
			errors.add( new TestError(Severity.WARNING, tr("Segments with tags"), s) );
		}
	}
}

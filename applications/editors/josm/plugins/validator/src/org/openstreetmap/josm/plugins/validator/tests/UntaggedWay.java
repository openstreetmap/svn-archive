package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.validator.Severity;
import org.openstreetmap.josm.plugins.validator.Test;
import org.openstreetmap.josm.plugins.validator.TestError;
/**
 * Checks for untagged ways
 * 
 * @author frsantos
 */
public class UntaggedWay extends Test 
{
	/** Empty way error */
	protected static final int EMPTY_WAY 	= 0;
	/** Untagged way error */
	protected static final int UNTAGGED_WAY = 1;
	/** Unnamed way error */
	protected static final int UNNAMED_WAY  = 2;

    /** Tags allowed in a way */
    public static final String[] ALLOWED_TAGS = new String[] { "created_by", "converted_by" };

    /** Ways that must have a name */
    public static final Set<String> NAMED_WAYS = new HashSet<String>();
    static
    {
        NAMED_WAYS.add( "motorway" ); 
        NAMED_WAYS.add( "trunk" ); 
        NAMED_WAYS.add( "primary" ); 
        NAMED_WAYS.add( "secondary" ); 
        NAMED_WAYS.add( "tertiary" ); 
        NAMED_WAYS.add( "residential" ); 
        NAMED_WAYS.add( "pedestrian" ); ;
    }
    
    /**
	 * Constructor
	 */
	public UntaggedWay() 
	{
		super(tr("Untagged ways."),
			  tr("This test checks for untagged ways."));
	}

	@Override
	public void visit(Way w) 
	{
		int numTags = 0;
		Map<String, String> tags = w.keys;
		if( tags != null )
		{
			numTags = tags.size();
			for( String tag : ALLOWED_TAGS)
				if( tags.containsKey(tag) ) numTags--;
            
            String highway = tags.get("highway");
            if( numTags != 0 && highway != null && NAMED_WAYS.contains(highway))
            {
                if( !tags.containsKey("name") && !tags.containsKey("ref") )
                {
                    boolean hasName = false;
                    for( String key : w.keySet())
                    {
                        hasName = key.startsWith("name:") || key.endsWith("_name") || key.endsWith("_ref");
                        if( hasName )
                            break;
                    }
                    
                    if( !hasName)
                        errors.add( new TestError(this, Severity.WARNING, tr("Unnamed ways"), w, UNNAMED_WAY ) );
                }
            }
		}
		
        if( numTags == 0 )
        {
            errors.add( new TestError(this, Severity.WARNING, tr("Untagged ways"), w, UNTAGGED_WAY) );
        }
        
        if( w.segments.size() == 0 )
        {
            errors.add( new TestError(this, Severity.ERROR, tr("Empty ways"), w, EMPTY_WAY) );
        }
        
	}		
	
	@Override
	public boolean isFixable(TestError testError)
	{
		if( testError.getTester() instanceof UntaggedWay )
		{
			return testError.getInternalCode() == EMPTY_WAY;
		}
		
		return false;
	}
	
	@Override
	public Command fixError(TestError testError)
	{
		return new DeleteCommand(testError.getPrimitives());
	}
}

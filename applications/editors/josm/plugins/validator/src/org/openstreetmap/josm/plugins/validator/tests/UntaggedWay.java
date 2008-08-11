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
import org.openstreetmap.josm.plugins.validator.util.Util;

/**
 * Checks for untagged ways
 *
 * @author frsantos
 */
public class UntaggedWay extends Test
{
	/** Empty way error */
	protected static final int EMPTY_WAY    = 301;
	/** Untagged way error */
	protected static final int UNTAGGED_WAY = 302;
	/** Unnamed way error */
	protected static final int UNNAMED_WAY  = 303;
	/** One node way error */
	protected static final int ONE_NODE_WAY = 304;

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
		super(tr("Untagged, empty, and one node ways."),
			  tr("This test checks for untagged, empty and one node ways."));
	}

	@Override
	public void visit(Way w)
	{
		if (w.deleted || w.incomplete) return;

		Map<String, String> tags = w.keys;
		if( tags != null )
		{
			String highway = tags.get("highway");
			if(highway != null && NAMED_WAYS.contains(highway))
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
						errors.add( new TestError(this, Severity.WARNING, tr("Unnamed ways"), UNNAMED_WAY, w) );
				}
			}
		}

		if(Util.countDataTags(w) == 0)
		{
			errors.add( new TestError(this, Severity.WARNING, tr("Untagged ways"), UNTAGGED_WAY, w) );
		}

		if( w.nodes.size() == 0 )
		{
			errors.add( new TestError(this, Severity.ERROR, tr("Empty ways"), EMPTY_WAY, w) );
		}
		else if( w.nodes.size() == 1 )
		{
			errors.add( new TestError(this, Severity.ERROR, tr("One node ways"), ONE_NODE_WAY, w) );
		}

	}

	@Override
	public boolean isFixable(TestError testError)
	{
		if( testError.getTester() instanceof UntaggedWay )
		{
			return testError.getCode() == EMPTY_WAY
				|| testError.getCode() == ONE_NODE_WAY;
		}

		return false;
	}

	@Override
	public Command fixError(TestError testError)
	{
		return new DeleteCommand(testError.getPrimitives());
	}
}

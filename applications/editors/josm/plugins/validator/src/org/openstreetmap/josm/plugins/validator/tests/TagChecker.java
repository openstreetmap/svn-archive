package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.*;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.validator.Severity;
import org.openstreetmap.josm.plugins.validator.Test;
import org.openstreetmap.josm.plugins.validator.TestError;
import org.openstreetmap.josm.plugins.validator.util.Bag;

/**
 * Check area type ways for errors
 *
 * @author stoecker
 */
public class TagChecker extends Test  {
	/** The already detected errors */
	Bag<Way, Way> _errorWays;

	/**
	 * Constructor
	 */
	public TagChecker()
	{
		super(tr("Tag Checker."),
			  tr("This tests if major tags are used as expected."));
	}

	@Override
	public void startTest()
	{
		_errorWays = new Bag<Way, Way>();
	}

	@Override
	public void endTest()
	{
		_errorWays = null;
	}
	
	@Override
	public void visit(Node n)
	{
/* ... */
	}

	@Override
	public void visit(Way w)
	{
/* ... */
	}
}

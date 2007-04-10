package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.List;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.validator.Severity;
import org.openstreetmap.josm.plugins.validator.Test;
import org.openstreetmap.josm.plugins.validator.TestError;
import org.openstreetmap.josm.plugins.validator.util.Bag;
/**
 * Tests if there are duplicate nodes
 * 
 * @author frsantos
 */
public class DuplicateNode extends Test 
{
	/** Bag of all nodes */
	Bag<LatLon, Node> nodes;
	
	/**
	 * Constructor
	 */
	public DuplicateNode() 
	{
		super(tr("Duplicated nodes."),
			  tr("This test checks that there are no nodes at the very same location."));
	}


	@Override
	public void startTest() 
	{
		nodes = new Bag<LatLon, Node>(1000);
	}

	@Override
	public void endTest() 
	{
		for(List<Node> duplicated : nodes.values() )
		{
			if( duplicated.size() > 1)
			{
				errors.add( new TestError(Severity.ERROR, tr("Duplicated nodes"), duplicated) );
			}
		}
		nodes = null;
	}

	@Override
	public void visit(Node n) 
	{
		nodes.add(n.coor, n);
	}
}

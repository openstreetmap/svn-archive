package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.*;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.*;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.*;
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
	protected static int DUPLICATE_NODE = 1;

	/** Bag of all nodes */
	Bag<LatLon, OsmPrimitive> nodes;

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
		nodes = new Bag<LatLon, OsmPrimitive>(1000);
	}

	@Override
	public void endTest()
	{
		for(List<OsmPrimitive> duplicated : nodes.values() )
		{
			if( duplicated.size() > 1)
			{
				TestError testError = new TestError(this, Severity.ERROR, tr("Duplicated nodes"), DUPLICATE_NODE, duplicated);
				errors.add( testError );
			}
		}
		nodes = null;
	}

	@Override
	public void visit(Node n)
	{
		if(!n.deleted && !n.incomplete)
			nodes.add(n.coor, n);
	}

	/**
	 * Merge the nodes into one.
	 * Copied from UtilsPlugin.MergePointsAction
	 */
	@Override
	public Command fixError(TestError testError)
	{
		Collection<? extends OsmPrimitive> sel = testError.getPrimitives();
		Collection<OsmPrimitive> nodes = new ArrayList<OsmPrimitive>();

		Node target = null;
		for (OsmPrimitive osm : sel)
			nodes.add(osm);

		if( nodes.size() < 2 )
			return null;

		for ( OsmPrimitive o : nodes )
		{
			Node n = (Node)o;
			if( target == null || target.id == 0 )
			{
				target = n;
				continue;
			}
			if( n.id == 0 )
				continue;
			if( n.id < target.id )
				target = n;
		}
		if( target == null )
			return null;

		// target is what we're merging into
		// nodes is the list of nodes to be removed
		nodes.remove(target);

		// Merge all properties
		Node newtarget = new Node(target);
		for (final OsmPrimitive o : nodes)
		{
			Node n = (Node)o;
			for ( String key : n.keySet() )
			{
				if( newtarget.keySet().contains(key) && !newtarget.get(key).equals(n.get(key)) )
				{
					JOptionPane.showMessageDialog(Main.parent, tr("Nodes have conflicting key: {0} [{1}, {2}]",
					key, newtarget.get(key), n.get(key)));
					return null;
				}
				newtarget.put( key, n.get(key) );
			}
		}

		Collection<Command> cmds = new LinkedList<Command>();

		// Now search the ways for occurences of the nodes we are about to
		// merge and replace them with the 'target' node
		for (Way w : Main.ds.ways) {
			if (w.deleted || w.incomplete) continue;
			// FIXME: use some fancy method from java.util.Collections and
			// List.replace
			Way wnew = null;
			int len = w.nodes.size();
			for (int i = 0; i < len; i++) {
				if (!nodes.contains(w.nodes.get(i))) continue;
				if (wnew == null) wnew = new Way(w);
				wnew.nodes.set(i, target);
			}
			if (wnew != null) {
				cmds.add(new ChangeCommand(w, wnew));
			}
		}

		cmds.add(DeleteCommand.delete(nodes));
		cmds.add(new ChangeCommand(target, newtarget));
		return new SequenceCommand(tr("Merge Nodes"), cmds);
	}

	@Override
	public boolean isFixable(TestError testError)
	{
		return (testError.getTester() instanceof DuplicateNode);
	}
}

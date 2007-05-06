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
				TestError testError = new TestError(this, Severity.ERROR, tr("Duplicated nodes"), duplicated);
				errors.add( testError );
			}
		}
		nodes = null;
	}

	@Override
	public void visit(Node n) 
	{
		nodes.add(n.coor, n);
	}
	
    /**
     * Merge the nodes into one.
     * Copied from UtilsPlugin.MergePointsAction
     */
	@Override
	public Command fixError(TestError testError)
	{
        Collection<OsmPrimitive> sel = testError.getPrimitives();
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
                    JOptionPane.showMessageDialog(Main.parent, tr("Nodes have conflicting key: " + key + " ["+newtarget.get(key)+", "+n.get(key)+"]"));
                    return null;
                }
                newtarget.put( key, n.get(key) ); 
            }
        }        
        
        // Since some segment may disappear, we need to track those too
        Collection<OsmPrimitive> seglist = new ArrayList<OsmPrimitive>();
                
        // Now do the merging
        Collection<Command> cmds = new LinkedList<Command>();
        for (final Segment s : Main.ds.segments) 
        {
            if( s.deleted || s.incomplete )
                continue;
            if( !nodes.contains( s.from ) && !nodes.contains( s.to ) )
                continue;
                
            Segment newseg = new Segment(s);
            if( nodes.contains( s.from ) )
                newseg.from = target;
            if( nodes.contains( s.to ) )
                newseg.to = target;

            // Is this node now a NULL node?
            if( newseg.from == newseg.to )
                seglist.add(s);
            else
                cmds.add(new ChangeCommand(s,newseg));
        }
        
        if( seglist.size() > 0 )  // Some segments to be deleted?
        {
            // We really want to delete this, but we must check if it is part of a way first
            for (final Way w : Main.ds.ways)
            {
                Way newway = null;
                if( w.deleted )
                    continue;
                for (final OsmPrimitive o : seglist )
                {
                    Segment s = (Segment)o;
                    if( w.segments.contains(s) )
                    {
                        if( newway == null )
                            newway = new Way(w);
                        newway.segments.remove(s);
                    }
                }
                if( newway != null )   // Made changes?
                {
                    // If no segments left, delete the way
                    if( newway.segments.size() == 0 )
                        cmds.add(new DeleteCommand(Arrays.asList(new OsmPrimitive[]{w})));
                    else
                        cmds.add(new ChangeCommand(w,newway));
                }
            }
            cmds.add(new DeleteCommand(seglist));
        }

        cmds.add(new DeleteCommand(nodes));
        cmds.add(new ChangeCommand(target, newtarget));
        return new SequenceCommand(tr("Merge Nodes"), cmds);
	}
	
	@Override
	public boolean isFixable(TestError testError)
	{
		return (testError.getTester() instanceof DuplicateNode);
	}	
}

package UtilsPlugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Collection;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Segment;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.SequenceCommand;

import javax.swing.AbstractAction;

class MergePointsAction extends AbstractAction {
	public MergePointsAction() {
		super("Merge Points");
	}
	public void actionPerformed(ActionEvent e) {
		Collection<OsmPrimitive> sel = Main.ds.getSelected();
		Collection<OsmPrimitive> nodes = new ArrayList<OsmPrimitive>();
		Node target = null;
		for (OsmPrimitive osm : sel)
			if (osm instanceof Node)
				nodes.add((Node)osm);
		if (nodes.size() < 2) {
			javax.swing.JOptionPane.showMessageDialog(Main.parent, tr("Must select at least two nodes."));
			return;
		}
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
//		System.out.println( "Selected: "+target.toString() );
		nodes.remove(target);
		
		// target is what we're merging into
		// nodes is the list of nodes to be removed
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
						cmds.add(makeDeleteCommand(w));
					else
						cmds.add(new ChangeCommand(w,newway));
				}
			}
			cmds.add(new DeleteCommand(seglist));
		}

		cmds.add(new DeleteCommand(nodes));
		Main.main.editLayer().add(new SequenceCommand(tr("Merge Nodes"), cmds));
		Main.map.repaint();
	}
	private DeleteCommand makeDeleteCommand(OsmPrimitive obj)
	{
	  return new DeleteCommand(Arrays.asList(new OsmPrimitive[]{obj}));
	}
}


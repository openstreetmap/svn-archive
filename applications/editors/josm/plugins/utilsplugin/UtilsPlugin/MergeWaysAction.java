package UtilsPlugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Collection;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.Main;
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
import org.openstreetmap.josm.data.osm.OsmPrimitive;

import javax.swing.AbstractAction;

class MergeWaysAction extends AbstractAction {
	public MergeWaysAction() {
		super("Merge Ways");
	}
	public void actionPerformed(ActionEvent e) {
		Collection<OsmPrimitive> sel = Main.ds.getSelected();
		Collection<OsmPrimitive> ways = new ArrayList<OsmPrimitive>();
		Way target = null;
		for (OsmPrimitive osm : sel)
			if (osm instanceof Way)
				ways.add(osm);
		if (ways.size() < 2) {
			javax.swing.JOptionPane.showMessageDialog(Main.parent, tr("Must select at least two ways."));
			return;
		}
		for ( OsmPrimitive o : ways )
		{
			Way w = (Way)o;
			if( target == null || target.id == 0 )
			{
				target = w;
				continue;
			}
			if( w.id == 0 )
				continue;
			if( w.id < target.id )
				target = w;
		}
//		System.out.println( "Selected: "+target.toString() );
		ways.remove(target);
		
		// target is what we're merging into
		// ways is the list of ways to be removed
				
		// Now do the merging
		Collection<Command> cmds = new LinkedList<Command>();
		Way newway = new Way(target);
		for (final OsmPrimitive o : ways) 
		{
			Way w = (Way)o;
			if( w.deleted || w.isIncomplete() )
			{
				javax.swing.JOptionPane.showMessageDialog(Main.parent, tr("Ways must exist and be complete."));
				return;
			}
			for ( String key : w.keySet() )
			{
				if( newway.keys.containsKey(key) && !newway.get(key).equals(w.get(key)) )
				{
					javax.swing.JOptionPane.showMessageDialog(Main.parent, tr("Ways have conflicting key: "+key+"["+newway.get(key)+","+w.get(key)+"]"));
					return;
				}
				newway.put( key, w.get(key) ); 
			}
			for (final Segment s : w.segments)
			{
				if( !newway.segments.contains( s ) )
					newway.segments.add( s );
			}
		}

		cmds.add(new ChangeCommand(target, newway));
		cmds.add(new DeleteCommand(ways));
		Main.main.editLayer().add(new SequenceCommand(tr("Merge Ways"), cmds));
		Main.map.repaint();
	}
    }


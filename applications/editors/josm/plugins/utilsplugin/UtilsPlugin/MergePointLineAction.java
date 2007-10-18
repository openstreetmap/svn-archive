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

class MergePointLineAction extends AbstractAction {
	public MergePointLineAction() {
	    super("Join Point and Segment");
	}
	public void actionPerformed(ActionEvent e) {
		Collection<OsmPrimitive> sel = Main.ds.getSelected();
	        Node node = null;
	        Segment seg = null;
	        Way way = null;

		boolean error = false;	        
		for (OsmPrimitive osm : sel)
		{
			if (osm instanceof Node)
				if( node == null )
					node = (Node)osm;
				else
					error = true;
			if (osm instanceof Segment)
				if( seg == null )
					seg = (Segment)osm;
				else
					error = true;
			if (osm instanceof Way)
				if( way == null )
					way = (Way)osm;
				else
					error = true;
		}
		if( node == null || !(seg == null ^ way == null))
			error = true;
		if( error )
		{
			javax.swing.JOptionPane.showMessageDialog(Main.parent, tr("Must select one node and one segment/way."));
			return;
		}
		if( way != null )
		{
			if( way.isIncomplete() )
			{
				javax.swing.JOptionPane.showMessageDialog(Main.parent, tr("Selected way must be complete."));
				return;
			}
			double mindist = 0;
//			System.out.println( node.toString() );
			// If the user has selected a way and a point, we need to determine the segment that is closest to the given point.
			for (Segment s : way.segments )
			{
				if( s.incomplete )
					continue;
					
//				System.out.println( s.toString() );
				double dx1 = s.from.coor.lat() - node.coor.lat();
				double dy1 = s.from.coor.lon() - node.coor.lon();
				double dx2 = s.to.coor.lat() - node.coor.lat();
				double dy2 = s.to.coor.lon() - node.coor.lon();
				
//				System.out.println( dx1+","+dx2+" && "+dy1+","+dy2 );
				double len1 = Math.sqrt(dx1*dx1+dy1*dy1);
				double len2 = Math.sqrt(dx2*dx2+dy2*dy2);
				dx1 /= len1;
				dy1 /= len1;
				dx2 /= len2;
				dy2 /= len2;
//				System.out.println( dx1+","+dx2+" && "+dy1+","+dy2 );
				
				double dist = dx1*dx2 + dy1*dy2;
//				System.out.println( "Dist: "+dist );
				if( dist < mindist )
				{
					mindist = dist;
					seg = s;
				}
			}
			if( seg == null )
			{
				javax.swing.JOptionPane.showMessageDialog(Main.parent, tr("No segment found in range"));
				return;
			}
		}

		if( seg.incomplete )
		{
			javax.swing.JOptionPane.showMessageDialog(Main.parent, tr("Both objects must be complete."));
			return;
		}
		if( node == seg.from || node == seg.to )
		{
			javax.swing.JOptionPane.showMessageDialog(Main.parent, tr("Node can't be endpoint of segment"));
			return;
		}
		// Now do the merging
		Collection<Command> cmds = new LinkedList<Command>();
		Segment newseg1 = new Segment(seg);
		newseg1.to = node;
		Segment newseg2 = new Segment(node, seg.to);
		if (seg.keys != null)
			newseg2.keys = new java.util.HashMap<String, String>(seg.keys);
		newseg2.selected = newseg1.selected;
		                                
		cmds.add(new ChangeCommand(seg,newseg1));
		cmds.add(new AddCommand(newseg2));
		
		// find ways affected and fix them up...
		for (final Way w : Main.ds.ways)
		{
			if( w.deleted )
				continue;
			int pos = w.segments.indexOf(seg);
			if( pos == -1 )
				continue;
			Way newway = new Way(w);
			newway.segments.add(pos+1, newseg2);
			cmds.add(new ChangeCommand(w,newway));
		}
		Main.main.editLayer().add(new SequenceCommand(tr("Join Node and Line"), cmds));
		Main.map.repaint();
	}
    }

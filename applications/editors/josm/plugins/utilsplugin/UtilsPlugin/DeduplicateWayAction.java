package UtilsPlugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Collection;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Segment;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.SequenceCommand;

import javax.swing.AbstractAction;

class DeduplicateWayAction extends AbstractAction {
	public DeduplicateWayAction() {
		super("Deduplicate Way");
	}
	public void actionPerformed(ActionEvent e) {
		Collection<OsmPrimitive> sel = Main.ds.getSelected();
		Collection<Way> ways = new ArrayList<Way>();
		for (OsmPrimitive osm : sel)
			if (osm instanceof Way)
				ways.add((Way)osm);

		Collection<Command> cmds = new LinkedList<Command>();
		for ( Way w : ways )
		{
			List<Segment> segs = new ArrayList<Segment>();
			
			for ( Segment s : w.segments )
			{
				if( !segs.contains(s) )
					segs.add(s);
			}
			if( segs.size() != w.segments.size() )
			{
				Way newway = new Way(w);
				newway.segments.clear();
				newway.segments.addAll(segs);
				cmds.add(new ChangeCommand(w,newway));
			}
		}
		if( cmds.size() != 0 )
			Main.main.editLayer().add(new SequenceCommand(tr("Deduplicate Ways"), cmds));
		Main.map.repaint();
	}
}


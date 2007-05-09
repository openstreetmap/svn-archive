package org.openstreetmap.josm.plugins.tways;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trn;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Segment;
import org.openstreetmap.josm.data.osm.Way;

/**
 * A plugin to add ways manipulation things
 * 
 * @author Thomas.Walraet
 */
class CreateLinearWaysAction extends AbstractAction {

    public CreateLinearWaysAction() {
	super("Create ways");
    }

    public void actionPerformed(ActionEvent e) {
	HashSet<Segment> orphanSegments = new HashSet<Segment>();
	for (OsmPrimitive osm : Main.ds.getSelected()) {
	    if (osm instanceof Segment) {
		orphanSegments.add((Segment) osm);
	    }
	}
	for (Way way : Main.ds.ways) {
	    if (!way.deleted) {
		orphanSegments.removeAll(way.segments);
	    }
	}
	
	if (orphanSegments.isEmpty()) {
	    JOptionPane.showMessageDialog(Main.parent, tr("You have to select some segments that don't belong to any way."));
	    return;
	}

	int segmentCount = orphanSegments.size();
	ParsedSegmentSet pss = new ParsedSegmentSet(Main.ds.segments);

	LinkedList<Way> ways = new LinkedList<Way>();
	Collection<Command> commands = new LinkedList<Command>();
	while (!orphanSegments.isEmpty()) {
	    Segment segment = orphanSegments.iterator().next();
	    orphanSegments.remove(segment);
	    Way way = new Way();
	    way.segments.add(segment);
	    Segment previousSegment = pss.getSegmentToNode(segment.from);
	    while (previousSegment != null
		    && orphanSegments.contains(previousSegment)
		    && pss.isOneInOneOut(previousSegment.to)) {
		way.segments.add(0, previousSegment);
		orphanSegments.remove(previousSegment);
		previousSegment = pss.getSegmentToNode(previousSegment.from);
	    }
	    Segment nextSegment = pss.getSegmentFromNode(segment.to);
	    while (nextSegment != null && orphanSegments.contains(nextSegment)
		    && pss.isOneInOneOut(nextSegment.from)) {
		way.segments.add(nextSegment);
		orphanSegments.remove(nextSegment);
		nextSegment = pss.getSegmentFromNode(nextSegment.to);
	    }
	    way.put("created_by", "Tways 0.2");
	    ways.add(way);
	    commands.add(new AddCommand(way));
	}
	
	if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(Main.parent,trn("Create {0} way from {1} segments?","Create {0} ways from {1} segments?", ways.size(), ways.size(), segmentCount),tr("Create ways"), JOptionPane.YES_NO_OPTION)) {
	    Main.main.editLayer().add(new SequenceCommand(tr("Create linear ways"), commands));
	    Main.ds.setSelected(ways);
	}
    }
}

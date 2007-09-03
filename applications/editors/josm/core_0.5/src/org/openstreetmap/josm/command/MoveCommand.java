// License: GPL. Copyright 2007 by Immanuel Scholz and others
package org.openstreetmap.josm.command;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trn;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.visitor.AllNodesVisitor;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * MoveCommand moves a set of OsmPrimitives along the map. It can be moved again
 * to collect several MoveCommands into one command.
 * 
 * @author imi
 */
public class MoveCommand extends Command {
	/**
	 * The objects that should be moved.
	 */
	public Collection<Node> objects = new LinkedList<Node>();
	/**
	 * x difference movement. Coordinates are in northern/eastern 
	 */
	private double x;
	/**
	 * y difference movement. Coordinates are in northern/eastern 
	 */
	private double y;

	/**
	 * Small helper for holding the interesting part of the old data state of the
	 * objects. 
	 */
	public static class OldState {
		LatLon latlon;
		EastNorth eastNorth;
		boolean modified;
	}
	
	/**
	 * List of all old states of the objects.
	 */
	private List<OldState> oldState = new LinkedList<OldState>();

	
	public MoveCommand(OsmPrimitive osm, double x, double y) {
		this(Collections.singleton(osm), x, y);
	}
	/**
	 * Create a MoveCommand and assign the initial object set and movement vector.
	 */
	public MoveCommand(Collection<OsmPrimitive> objects, double x, double y) {
		this.x = x;
		this.y = y;
		this.objects = AllNodesVisitor.getAllNodes(objects);
		for (Node n : this.objects) {
			OldState os = new OldState();
			os.eastNorth = n.eastNorth;
			os.latlon = n.coor;
			os.modified = n.modified;
			oldState.add(os);
		}
	}

	/**
	 * Move the same set of objects again by the specified vector. The vectors
	 * are added together and so the resulting will be moved to the previous
	 * vector plus this one.
	 * 
	 * The move is immediatly executed and any undo will undo both vectors to
	 * the original position the objects had before first moving.
	 */
	public void moveAgain(double x, double y) {
		for (Node n : objects) {
			n.eastNorth = new EastNorth(n.eastNorth.east()+x, n.eastNorth.north()+y);
			n.coor = Main.proj.eastNorth2latlon(n.eastNorth);
		}
		this.x += x;
		this.y += y;
	}
	
	@Override public void executeCommand() {
		for (Node n : objects) {
			n.eastNorth = new EastNorth(n.eastNorth.east()+x, n.eastNorth.north()+y);
			n.coor = Main.proj.eastNorth2latlon(n.eastNorth);
			n.modified = true;
		}
	}

	@Override public void undoCommand() {
		Iterator<OldState> it = oldState.iterator();
		for (Node n : objects) {
			OldState os = it.next();
			n.eastNorth = os.eastNorth;
			n.coor = os.latlon;
			n.modified = os.modified;
		}
	}

	@Override public void fillModifiedData(Collection<OsmPrimitive> modified, Collection<OsmPrimitive> deleted, Collection<OsmPrimitive> added) {
		for (OsmPrimitive osm : objects)
			modified.add(osm);
	}

	@Override public MutableTreeNode description() {
		return new DefaultMutableTreeNode(new JLabel(tr("Move")+" "+objects.size()+" "+trn("node","nodes",objects.size()), ImageProvider.get("data", "node"), JLabel.HORIZONTAL));
    }
}

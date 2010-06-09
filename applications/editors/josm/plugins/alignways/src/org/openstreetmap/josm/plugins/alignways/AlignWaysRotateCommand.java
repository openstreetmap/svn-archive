/**
 * 
 */
package org.openstreetmap.josm.plugins.alignways;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.WaySegment;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Rotates a way segment around a given pivot.
 * 
 * @author tilusnet <tilusnet@gmail.com>
 */

public class AlignWaysRotateCommand extends Command {

	private final AlignWaysAlgnSegment algnSeg;

	/**
	 * The objects to rotate.
	 */
	private Collection<Node> nodes = new HashSet<Node>();

	/**
	 * pivot point
	 */
	private final EastNorth pivot;

	/**
	 * Small helper for holding the interesting part of the old data state of
	 * the objects.
	 */
	public static class OldState {
		LatLon latlon;
		EastNorth eastNorth;
		WaySegment ws;
		boolean modified;
	}

	/**
	 * computed rotation angle to rotate the segment
	 * 
	 */
	private final double rotationAngle;

	/**
	 * List of all old states of the objects.
	 */
	private final Map<Node, OldState> oldState = new HashMap<Node, OldState>();
	private final Stack<WaySegment> oldWS = new Stack<WaySegment>();

	/**
	 * Creates an AlignWaysRotateCommand.
	 */
	public AlignWaysRotateCommand() {

		algnSeg = AlignWaysSegmentMgr.getInstance(Main.map.mapView)
		.getAlgnSeg();
		WaySegment algnWS = algnSeg.getSegment();
		WaySegment refWS = AlignWaysSegmentMgr.getInstance(Main.map.mapView)
		.getRefSeg().getSegment();

		this.pivot = algnSeg.getCurrPivotCoord();
		this.nodes = algnSeg.getSegmentEndPoints();

		for (Node n : this.nodes) {
			OldState os = new OldState();
			os.latlon = new LatLon(n.getCoor());
			os.eastNorth = n.getEastNorth();
			os.ws = algnWS;
			os.modified = n.isModified();
			oldState.put(n, os);
		}
		oldWS.push(algnWS);

		EastNorth enRefNode1 = refWS.way.getNode(refWS.lowerIndex)
		.getEastNorth();
		EastNorth enRefNode2 = refWS.way.getNode(refWS.lowerIndex + 1)
		.getEastNorth();

		EastNorth enAlgnNode1 = algnWS.way.getNode(algnWS.lowerIndex)
		.getEastNorth();
		EastNorth enAlgnNode2 = algnWS.way.getNode(algnWS.lowerIndex + 1)
		.getEastNorth();

		// Calculate the rotation angle
		double refAngle = Math.atan2(enRefNode1.north() - enRefNode2.north(),
				enRefNode1.east() - enRefNode2.east());
		double algnAngle = Math.atan2(
				enAlgnNode1.north() - enAlgnNode2.north(), enAlgnNode1.east()
				- enAlgnNode2.east());

		rotationAngle = normalise_angle(refAngle - algnAngle);

		/* For debug only
		String s = "Ref Angle: " + refAngle + " (" + Math.toDegrees(refAngle)
				+ ")\n";
		s += "Algn Angle: " + algnAngle + " (" + Math.toDegrees(algnAngle)
				+ ")\n";
		s += "Rotation angle: " + rotationAngle + " ("
				+ Math.toDegrees(rotationAngle) + ")";
		 */

		rotateNodes(true);

	}

	/**
	 * Helper for actually rotating the nodes.
	 * 
	 * @param setModified
	 *            - true if rotated nodes should be flagged "modified"
	 */
	private void rotateNodes(boolean setModified) {
		for (Node n : nodes) {
			double cosPhi = Math.cos(rotationAngle);
			double sinPhi = Math.sin(rotationAngle);
			EastNorth oldEastNorth = oldState.get(n).eastNorth;
			double x = oldEastNorth.east() - pivot.east();
			double y = oldEastNorth.north() - pivot.north();
			double nx = cosPhi * x - sinPhi * y + pivot.east();
			double ny = sinPhi * x + cosPhi * y + pivot.north();
			n.setEastNorth(new EastNorth(nx, ny));
			if (setModified) {
				n.setModified(true);
			}
		}
		algnSeg.updatePivotsEndpoints();
	}

	/**
	 * Make sure angle is in interval ( -Pi/2, Pi/2 ].
	 */
	private static double normalise_angle(double a) {
		while (a > Math.PI) {
			a -= 2 * Math.PI;
		}
		while (a <= -Math.PI) {
			a += 2 * Math.PI;
		}

		if (a > Math.PI / 2) {
			a -= Math.PI;
		} else if (a < -Math.PI / 2) {
			a += Math.PI;
		}
		return a;
	}

	@Override
	public MutableTreeNode description() {
		return new DefaultMutableTreeNode(
				new JLabel(tr("Align way segment"), ImageProvider.get(
						"", "alignways"), SwingConstants.HORIZONTAL));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openstreetmap.josm.command.Command#fillModifiedData(java.util.Collection
	 * , java.util.Collection, java.util.Collection)
	 */
	@Override
	public void fillModifiedData(Collection<OsmPrimitive> modified,
			Collection<OsmPrimitive> deleted, Collection<OsmPrimitive> added) {
		for (OsmPrimitive osm : nodes) {
			modified.add(osm);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openstreetmap.josm.command.Command#executeCommand()
	 */
	@Override
	public boolean executeCommand() {
		rotateNodes(true);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openstreetmap.josm.command.Command#undoCommand()
	 */
	@Override
	public void undoCommand() {
		for (Node n : nodes) {
			OldState os = oldState.get(n);
			n.setCoor(os.latlon);
			n.setModified(os.modified);
		}
		algnSeg.updatePivotsEndpoints();
	}

	public Collection<Node> getRotatedNodes() {
		return nodes;
	}

	public boolean areSegsConnected() {
		Collection<Node> algnNodes = nodes;
		Collection<Node> refNodes = AlignWaysSegmentMgr.getInstance(Main.map.mapView)
		.getRefSeg().getSegmentEndPoints();
		for (Node nA : algnNodes) {
			for (Node nR : refNodes) {
				if (nA.equals(nR))
					return true;
			}
		}

		return false;
	}


}

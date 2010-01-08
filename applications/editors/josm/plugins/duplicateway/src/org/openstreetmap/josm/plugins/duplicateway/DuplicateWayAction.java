// License: GPL. Copyright 2007 by Brent Easton and others
package org.openstreetmap.josm.plugins.duplicateway;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Segment;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.JVector;

/**
 * Duplicate an existing set of ordered ways, offset by a specified distance.
 *
 * This basic version just creates a completely separate way and makes no
 * attempt to attach it to any other ways.
 *
 * Planned Improvements:
 *
 * 1. After creation of the duplicate way and while it is still selected allow
 * the Mouse wheel, or the up and down arrow (probably in association with the
 * shift key) to increase/decrease the offset distance. Clicking anywhere, or
 * moving the mouse out of the view window should finish this mode.
 *
 * 2. Locate points close to the end points and pop up a dialog asking of these
 * should be joined.
 *
 * 3. Handle intersecting ways. Pop up a dialog for each asking if the
 * intersecting way should be carried accross to intersect the newly created
 * way. Handle multiple intersecting ways at a point.
 *
 *
 * @author Brent Easton
 *
 */
public class DuplicateWayAction extends MapMode implements
        SelectionChangedListener, MouseListener {

    private static final long serialVersionUID = 1L;

    protected Cursor oldCursor;

    protected List<Way> selectedWays;

    protected MapMode previousMode;

    /**
     * Create new DuplicateWay Action
     *
     * @param name
     */
    public DuplicateWayAction() {
        super(tr("Duplicate Way"), "duplicateway",
                tr("Duplicate selected ways."), KeyEvent.VK_W, null,
                ImageProvider.getCursor("crosshair", "duplicate"));
        setEnabled(false);
        DataSet.listeners.add(this);
    }

    @Override public void enterMode() {
        super.enterMode();
        Main.map.mapView.addMouseListener(this);
    }

    @Override public void exitMode() {
        super.exitMode();
        Main.map.mapView.removeMouseListener(this);
    }

    /**
     * The Duplicate Way button has been clicked
     *
     * @param e
     *            Action Event
     */
    @Override public void actionPerformed(ActionEvent e) {

        selectedWays = new ArrayList<Way>();
        for (OsmPrimitive osm : Main.ds.getSelected()) {
            if (osm instanceof Way) {
                Way way = (Way)osm;
                EastNorth last = null;
                for (Segment seg : way.segments) {
                    if (last != null) {
                        if (!seg.from.eastNorth.equals(last)) {
                            JOptionPane.showMessageDialog(Main.parent,
                                    tr("Can't duplicate unordered way."));
                            return;
                        }
                    }
                    last = seg.to.eastNorth;
                }
                selectedWays.add(way);
            }
        }

        if (Main.map == null) {
            JOptionPane.showMessageDialog(Main.parent, tr("No data loaded."));
            return;
        }

        if (selectedWays.isEmpty()) {
            JOptionPane.showMessageDialog(Main.parent,
                    tr("You must select at least one way."));
            return;
        }
        previousMode = Main.map.mapMode;
        super.actionPerformed(e);
    }

    /**
     * Create a new Node object at a specified Easting/Northing location
     *
     * @param east
     *            Easting of new Node
     * @param north
     *            Northing of new node
     * @return new Node
     */
    public static Node createNode(double east, double north) {
        return new Node(Main.proj.eastNorth2latlon(new EastNorth(east, north)));
    }

    /**
     * Duplicate the selected ways. The distance to be offset is determined by
     * finding the distance of the 'offset' point from the nearest segment.
     *
     * @param clickPoint
     *            The point in screen co-ordinates used to calculate the offset
     *            distance
     */
    protected void duplicate(Point clickPoint) {

        EastNorth clickEN = Main.map.mapView.getEastNorth(clickPoint.x,
                clickPoint.y);

        /*
         * First, find the nearest Segment belonging to a selected way
         */
        Segment cs = null;
        for (Way way : selectedWays) {
            double minDistance = Double.MAX_VALUE;
            // segments
            for (Segment ls : way.segments) {
                if (ls.deleted || ls.incomplete)
                    continue;
                double perDist = JVector.perpDistance(ls, clickEN);
                if (perDist < minDistance) {
                    minDistance = perDist;
                    cs = ls;
                }
            }
        }

        if (cs == null) {
            return;
        }

        /*
         * Find the distance we need to offset the new way +ve offset is to the
         * right of the initial way, -ve to the left
         */
        JVector closestSegment = new JVector(cs);
        double offset = closestSegment.calculateOffset(clickEN);

        Collection<Command> commands = new LinkedList<Command>();
        Collection<Way> ways = new LinkedList<Way>();

        /*
         * First new node is offset 90 degrees from the first point
         */
        for (Way way : selectedWays) {
            Way newWay = new Way();

            Node lastNode = null;
            JVector lastLine = null;

            for (Segment seg : way.segments) {
                JVector currentLine = new JVector(seg);
                Node newNode = null;

                if (lastNode == null) {
                    JVector perpVector = new JVector(currentLine);
                    perpVector.rotate90(offset);
                    newNode = createNode(perpVector.getP2().getX(), perpVector
                            .getP2().getY());
                    commands.add(new AddCommand(newNode));
                } else {
                    JVector bisector = lastLine.bisector(currentLine, offset);
                    newNode = createNode(bisector.getP2().getX(), bisector
                            .getP2().getY());
                    commands.add(new AddCommand(newNode));
                    Segment s = new Segment(newNode, lastNode);
                    commands.add(new AddCommand(s));
                    newWay.segments.add(0, s);
                }

                lastLine = currentLine;
                lastNode = newNode;

            }
            lastLine.reverse();
            lastLine.rotate90(-offset);
            Node newNode = createNode(lastLine.getP2().getX(), lastLine.getP2()
                    .getY());
            commands.add(new AddCommand(newNode));
            Segment s = new Segment(newNode, lastNode);
            commands.add(new AddCommand(s));
            newWay.segments.add(0, s);

            for (String key : way.keySet()) {
                newWay.put(key, way.get(key));
            }
            commands.add(new AddCommand(newWay));
            ways.add(newWay);
        }

        Main.main.undoRedo.add(new SequenceCommand(tr("Create duplicate way"),
                commands));
        Main.ds.setSelected(ways);
    }

    /**
     * Enable the "Duplicate way" menu option if at least one way is selected
     *
     * @param newSelection
     */
    public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
        for (OsmPrimitive osm : newSelection) {
            if (osm instanceof Way) {
                setEnabled(true);
                return;
            }
        }
        setEnabled(false);
    }

    /**
     * User has clicked on map to indicate the offset. Create the
     * duplicate way and exit duplicate mode
     *
     * @param e
     */
    public void mouseClicked(MouseEvent e) {
        duplicate(e.getPoint());
        exitMode();
        Main.map.selectMapMode(previousMode);
    }
}

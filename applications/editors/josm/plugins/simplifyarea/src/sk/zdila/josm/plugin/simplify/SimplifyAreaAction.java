// License: GPL. Copyright 2007 by Immanuel Scholz and others
package sk.zdila.josm.plugin.simplify;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;
import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trn;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.MoveCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSource;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.HelpAwareOptionPane;
import org.openstreetmap.josm.gui.HelpAwareOptionPane.ButtonSpec;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

public class SimplifyAreaAction extends JosmAction {

    private static final long serialVersionUID = 6854238214548011750L;

    public SimplifyAreaAction() {
        super(tr("Simplify Area"), "simplify", tr("Delete unnecessary nodes from an area."),
                Shortcut.registerShortcut("tools:simplifyArea", tr("Tool: {0}", tr("Simplify Area")), KeyEvent.VK_A, Shortcut.GROUP_EDIT, Shortcut.SHIFT_DEFAULT), true);
    }


    private List<Bounds> getCurrentEditBounds() {
        final LinkedList<Bounds> bounds = new LinkedList<Bounds>();
        final OsmDataLayer dataLayer = Main.map.mapView.getEditLayer();
        for (final DataSource ds : dataLayer.data.dataSources) {
            if (ds.bounds != null) {
                bounds.add(ds.bounds);
            }
        }
        return bounds;
    }


    private boolean isInBounds(final Node node, final List<Bounds> bounds) {
        for (final Bounds b : bounds) {
            if (b.contains(node.getCoor())) {
                return true;
            }
        }
        return false;
    }


    private boolean confirmWayWithNodesOutsideBoundingBox() {
        final ButtonSpec[] options = new ButtonSpec[] { new ButtonSpec(tr("Yes, delete nodes"), ImageProvider.get("ok"), tr("Delete nodes outside of downloaded data regions"), null),
                new ButtonSpec(tr("No, abort"), ImageProvider.get("cancel"), tr("Cancel operation"), null) };
        final int ret = HelpAwareOptionPane.showOptionDialog(
                Main.parent,
                "<html>" + trn("The selected way has nodes outside of the downloaded data region.", "The selected ways have nodes outside of the downloaded data region.", getCurrentDataSet().getSelectedWays().size())
                + "<br>" + tr("This can lead to nodes being deleted accidentally.") + "<br>" + tr("Do you want to delete them anyway?") + "</html>",
                tr("Delete nodes outside of data regions?"), JOptionPane.WARNING_MESSAGE, null, // no special icon
                options, options[0], null);
        return ret == 0;
    }


    private void alertSelectAtLeastOneWay() {
        HelpAwareOptionPane.showOptionDialog(Main.parent, tr("Please select at least one way to simplify."), tr("Warning"), JOptionPane.WARNING_MESSAGE, null);
    }


    private boolean confirmSimplifyManyWays(final int numWays) {
        final ButtonSpec[] options = new ButtonSpec[] { new ButtonSpec(tr("Yes"), ImageProvider.get("ok"), tr("Simplify all selected ways"), null),
                new ButtonSpec(tr("Cancel"), ImageProvider.get("cancel"), tr("Cancel operation"), null) };
        final int ret = HelpAwareOptionPane.showOptionDialog(Main.parent, tr("The selection contains {0} ways. Are you sure you want to simplify them all?", numWays), tr("Simplify ways?"),
                JOptionPane.WARNING_MESSAGE, null, // no special icon
                options, options[0], null);
        return ret == 0;
    }


    @Override
    public void actionPerformed(final ActionEvent e) {
        final Collection<OsmPrimitive> selection = getCurrentDataSet().getSelected();

        final List<Bounds> bounds = getCurrentEditBounds();
        for (final OsmPrimitive prim : selection) {
            if (prim instanceof Way && bounds.size() > 0) {
                final Way way = (Way) prim;
                // We check if each node of each way is at least in one download
                // bounding box. Otherwise nodes may get deleted that are necessary by
                // unloaded ways (see Ticket #1594)
                for (final Node node : way.getNodes()) {
                    if (!isInBounds(node, bounds)) {
                        if (!confirmWayWithNodesOutsideBoundingBox()) {
                            return;
                        }
                        break;
                    }
                }
            }
        }
        final List<Way> ways = OsmPrimitive.getFilteredList(selection, Way.class);
        if (ways.isEmpty()) {
            alertSelectAtLeastOneWay();
            return;
        } else if (ways.size() > 10) {
            if (!confirmSimplifyManyWays(ways.size())) {
                return;
            }
        }

        final Collection<Command> allCommands = new LinkedList<Command>();
        for (final Way way : ways) {
            final SequenceCommand simplifyCommand = simplifyWay(way);
            if (simplifyCommand == null) {
                continue;
            }
            allCommands.add(simplifyCommand);
        }

        if (!allCommands.isEmpty()) {
            final SequenceCommand rootCommand = new SequenceCommand(trn("Simplify {0} way", "Simplify {0} ways", allCommands.size(), allCommands.size()), allCommands);
            Main.main.undoRedo.add(rootCommand);
            Main.map.repaint();
        }
    }


    /**
     * Replies true if <code>node</code> is a required node which can't be removed in order to simplify the way.
     *
     * @param way
     *            the way to be simplified
     * @param node
     *            the node to check
     * @return true if <code>node</code> is a required node which can't be removed in order to simplify the way.
     */
    private boolean isRequiredNode(final Way way, final Node node) {
        final List<OsmPrimitive> parents = new LinkedList<OsmPrimitive>(node.getReferrers());
        parents.remove(way);
        return !parents.isEmpty() || node.isTagged();
    }


    /**
     * Simplifies a way
     *
     * @param w
     *            the way to simplify
     */
    private SequenceCommand simplifyWay(final Way w) {
        final double angleThreshold = Main.pref.getDouble("simplify-area.angle.threshold", 10);
        final double angleFactor = Main.pref.getDouble("simplify-area.angle.factor", 1.0);
        final double mergeThreshold = Main.pref.getDouble("simplify-area.merge.threshold", 0.2);
        final double areaThreshold = Main.pref.getDouble("simplify-area.area.threshold", 5.0);
        final double areaFactor = Main.pref.getDouble("simplify-area.area.factor", 1.0);
        final double distanceThreshold = Main.pref.getDouble("simplify-area.dist.threshold", 3);
        final double distanceFactor = Main.pref.getDouble("simplify-area.dist.factor", 3);

        final List<Node> nodes = w.getNodes();
        final int size = nodes.size();

        if (size == 0) {
            return null;
        }

        final boolean closed = nodes.get(0).equals(nodes.get(size - 1));

        if (closed) {
            nodes.remove(size - 1); // remove end node ( = start node)
        }

        // remove nodes within threshold

        while (true) {
            Node prevNode = null;
            LatLon coord1 = null;
            LatLon coord2 = null;

            double minWeight = Double.MAX_VALUE;
            Node bestMatch = null;

            for (int i = 0, len = nodes.size() + (closed ? 2 : 1); i < len; i++) {
                final Node n = nodes.get(i % nodes.size());
                final LatLon coord3 = n.getCoor();

                if (coord1 != null) {
                    final double angleWeight = computeConvectAngle(coord1, coord2, coord3) / angleThreshold;
                    final double areaWeight = computeArea(coord1, coord2, coord3) / areaThreshold;
                    final double distanceWeight = Math.abs(crossTrackError(coord1, coord2, coord3)) / distanceThreshold;

                    final double weight = isRequiredNode(w, prevNode) ||
                    !closed && i == len - 1 || // don't remove last node of the not closed way
                    angleWeight > 1.0 || areaWeight > 1.0 || distanceWeight > 1.0 ? Double.MAX_VALUE :
                        angleWeight * angleFactor + areaWeight * areaFactor + distanceWeight * distanceFactor;

                    if (weight < minWeight) {
                        minWeight = weight;
                        bestMatch = prevNode;
                    }
                }

                coord1 = coord2;
                coord2 = coord3;
                prevNode = n;
            }

            if (bestMatch == null) {
                break;
            }

            nodes.remove(bestMatch);
        }


        // average nearby nodes

        final Map<Node, LatLon> coordMap = new HashMap<Node, LatLon>();
        for (final Node n : nodes) {
            coordMap.put(n, n.getCoor());
        }

        final Map<Node, MoveCommand> moveCommandList = new HashMap<Node, MoveCommand>();

        while (true) {
            double minDist = Double.MAX_VALUE;
            Node node1 = null;
            Node node2 = null;

            for (int i = 0, len = nodes.size() + (closed ? 2 : 1); i < len; i++) {
                final Node n1 = nodes.get(i % nodes.size());
                final Node n2 = nodes.get((i + 1) % nodes.size());

                if (isRequiredNode(w, n1) || isRequiredNode(w, n2)) {
                    continue;
                }

                final double dist = coordMap.get(n1).greatCircleDistance(coordMap.get(n2));
                if (dist < minDist && dist < mergeThreshold) {
                    minDist = dist;
                    node1 = n1;
                    node2 = n2;
                }
            }

            if (node1 == null || node2 == null) {
                break;
            }


            final LatLon coord = coordMap.get(node1).getCenter(coordMap.get(node2));
            coordMap.put(node1, coord);
            moveCommandList.put(node1, new MoveCommand(node1, coord));

            nodes.remove(node2);
            coordMap.remove(node2);
            moveCommandList.remove(node2);
        }


        if (closed) {
            nodes.add(nodes.get(0)); // set end node ( = start node)
        }

        final HashSet<Node> delNodes = new HashSet<Node>(w.getNodes());
        delNodes.removeAll(nodes);

        if (delNodes.isEmpty()) {
            return null;
        }

        final Collection<Command> cmds = new LinkedList<Command>();
        final Way newWay = new Way(w);
        newWay.setNodes(nodes);

        cmds.addAll(moveCommandList.values());
        cmds.add(new ChangeCommand(w, newWay));
        cmds.add(new DeleteCommand(delNodes));
        return new SequenceCommand(trn("Simplify Area (remove {0} node)", "Simplify Area (remove {0} nodes)", delNodes.size(), delNodes.size()), cmds);
    }


    public static double computeConvectAngle(final LatLon coord1, final LatLon coord2, final LatLon coord3) {
        final double angle =  Math.abs(heading(coord2, coord3) - heading(coord1, coord2));
        return Math.toDegrees(angle < Math.PI ? angle : 2 * Math.PI - angle);
    }


    public static double computeArea(final LatLon coord1, final LatLon coord2, final LatLon coord3) {
        final double a = coord1.greatCircleDistance(coord2);
        final double b = coord2.greatCircleDistance(coord3);
        final double c = coord3.greatCircleDistance(coord1);

        final double p = (a + b + c) / 2.0;

        final double q = p * (p - a) * (p - b) * (p - c); // I found this negative in one case (:-o) when nodes were in line on a small area
        return q < 0.0 ? 0.0 : Math.sqrt(q);
    }


    public static double R = 6378135;

    public static double crossTrackError(final LatLon l1, final LatLon l2, final LatLon l3) {
        return R * Math.asin(sin(l1.greatCircleDistance(l2) / R) * sin(heading(l1, l2) - heading(l1, l3)));
    }


    public static double heading(final LatLon a, final LatLon b) {
        double hd = Math.atan2(sin(toRadians(a.lon() - b.lon())) * cos(toRadians(b.lat())),
                cos(toRadians(a.lat())) * sin(toRadians(b.lat())) -
                sin(toRadians(a.lat())) * cos(toRadians(b.lat())) * cos(toRadians(a.lon() - b.lon())));
        hd %= 2 * Math.PI;
        if (hd < 0) {
            hd += 2 * Math.PI;
        }
        return hd;
    }


    @Override
    protected void updateEnabledState() {
        if (getCurrentDataSet() == null) {
            setEnabled(false);
        } else {
            updateEnabledState(getCurrentDataSet().getSelected());
        }
    }


    @Override
    protected void updateEnabledState(final Collection<? extends OsmPrimitive> selection) {
        setEnabled(selection != null && !selection.isEmpty());
    }

}

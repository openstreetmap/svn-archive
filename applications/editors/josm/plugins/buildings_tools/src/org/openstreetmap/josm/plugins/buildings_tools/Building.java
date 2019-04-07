// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.buildings_tools;

import static org.openstreetmap.josm.plugins.buildings_tools.BuildingsToolsPlugin.eastNorth2latlon;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.CreateCircleAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.tools.Geometry;

class Building {
    private final EastNorth[] en = new EastNorth[4];

    double meter;

    private double len;
    private double width;
    private double heading;
    private AngleSnap angleSnap = new AngleSnap();
    private Double drawingAngle;
    private static final double EQUAL_NODE_DIST_TOLERANCE = 1e-6;

    public void clearAngleSnap() {
        angleSnap.clear();
        drawingAngle = null;
    }

    public void addAngleSnap(Node[] nodes) {
        drawingAngle = angleSnap.addSnap(nodes);
    }

    public void addAngleSnap(Way way) {
        angleSnap.addSnap(way);
        if (drawingAngle == null) {
            drawingAngle = angleSnap.getAngle();
        }
    }

    public double getLength() {
        return len;
    }

    public double getWidth() {
        return width;
    }

    public boolean isRectDrawing() {
        return drawingAngle != null && ToolSettings.getWidth() == 0 && ToolSettings.getLenStep() == 0
                && ToolSettings.Shape.RECTANGLE == ToolSettings.getShape();
    }

    public Double getDrawingAngle() {
        return drawingAngle;
    }

    public void reset() {
        len = 0;

        for (int i = 0; i < 4; i++) {
            en[i] = null;
        }
    }

    public EastNorth getPoint(int num) {
        return en[num];
    }

    private void updMetrics() {
        meter = 1 / Math.cos(Math.toRadians(eastNorth2latlon(en[0]).lat()));
        len = 0;
    }

    public void setBase(EastNorth base) {
        en[0] = base;
        updMetrics();
    }

    /**
     * @param p
     *            The point to project
     * @return Projection of the point to the heading vector in metres
     */
    private double projection1(EastNorth p) {
        final EastNorth vec = p.subtract(en[0]);
        return (Math.sin(heading) * vec.east() + Math.cos(heading) * vec.north()) / meter;
    }

    /**
     * @param p
     *            The point to project
     * @return Projection of the point to the perpendicular of the heading
     *         vector in metres
     */
    private double projection2(EastNorth p) {
        final EastNorth vec = p.subtract(en[0]);
        return (Math.cos(heading) * vec.east() - Math.sin(heading) * vec.north()) / meter;
    }

    private void updatePos() {
        if (len == 0)
            return;
        final EastNorth p1 = en[0];
        en[1] = new EastNorth(p1.east() + Math.sin(heading) * len * meter, p1.north() + Math.cos(heading) * len * meter);
        if (ToolSettings.Shape.RECTANGLE == ToolSettings.getShape()) {
            en[2] = new EastNorth(p1.east() + Math.sin(heading) * len * meter + Math.cos(heading) * width * meter,
                    p1.north() + Math.cos(heading) * len * meter - Math.sin(heading) * width * meter);
            en[3] = new EastNorth(p1.east() + Math.cos(heading) * width * meter,
                    p1.north() - Math.sin(heading) * width * meter);
        }
    }

    private void setLengthWidth(double length, double width) {
        this.len = length;
        this.width = width;
        updatePos();
    }

    public void setWidth(EastNorth p3) {
        this.width = projection2(p3);
        updatePos();
    }

    public void setPlace(EastNorth p2, double width, double lenstep, boolean ignoreConstraints) {
        if (en[0] == null)
            throw new IllegalStateException("setPlace() called without the base point");
        this.heading = en[0].heading(p2);
        if (!ignoreConstraints)
            this.heading = angleSnap.snapAngle(this.heading);

        this.width = width;
        this.len = projection1(p2);
        if (lenstep > 0 && !ignoreConstraints)
            this.len = Math.round(this.len / lenstep) * lenstep;

        updatePos();

        MainApplication.getMap().statusLine.setHeading(Math.toDegrees(heading));
        if (this.drawingAngle != null && !ignoreConstraints) {
            double ang = Math.toDegrees(heading - this.drawingAngle);
            if (ang < 0)
                ang += 360;
            if (ang > 360)
                ang -= 360;
            MainApplication.getMap().statusLine.setAngle(ang);
        }
    }

    public void setPlaceCircle(EastNorth p2, double width, boolean ignoreConstraints) {
        if (en[0] == null)
            throw new IllegalStateException("setPlace() called without the base point");
        this.heading = en[0].heading(p2);
        if (!ignoreConstraints)
            this.heading = angleSnap.snapAngle(this.heading);
        this.len = width;

        updatePos();
    }

    public void setPlaceRect(EastNorth p2) {
        if (en[0] == null)
            throw new IllegalStateException("SetPlaceRect() called without the base point");
        if (!isRectDrawing())
            throw new IllegalStateException("Invalid drawing mode");
        heading = drawingAngle;
        setLengthWidth(projection1(p2), projection2(p2));
        MainApplication.getMap().statusLine.setHeading(Math.toDegrees(heading));
    }

    public void angFix(EastNorth point) {
        EastNorth en3 = en[2];
        EastNorth mid = en[0].getCenter(en3);
        double radius = en3.distance(mid);
        heading = mid.heading(point);
        heading = en[0].heading(mid.add(Math.sin(heading) * radius, Math.cos(heading) * radius));
        setLengthWidth(projection1(en3), projection2(en3));
        en[2] = en3;
    }

    public void paint(Graphics2D g, MapView mv) {
        if (len == 0)
            return;
        GeneralPath b = new GeneralPath();
        Point pp1 = mv.getPoint(eastNorth2latlon(en[0]));
        Point pp2 = mv.getPoint(eastNorth2latlon(en[1]));
        b.moveTo(pp1.x, pp1.y);
        b.lineTo(pp2.x, pp2.y);
        if (ToolSettings.Shape.RECTANGLE == ToolSettings.getShape()) {
            Point pp3 = mv.getPoint(eastNorth2latlon(en[2]));
            Point pp4 = mv.getPoint(eastNorth2latlon(en[3]));
            b.lineTo(pp3.x, pp3.y);
            b.lineTo(pp4.x, pp4.y);
        }
        b.lineTo(pp1.x, pp1.y);
        g.draw(b);
    }

    private static Node findNode(EastNorth pos) {
        MapView mv = MainApplication.getMap().mapView;
        Node n = mv.getNearestNode(mv.getPoint(eastNorth2latlon(pos)), OsmPrimitive::isSelectable);
        if (n == null)
            return null;
        return n.getEastNorth().distance(pos) <= EQUAL_NODE_DIST_TOLERANCE ? n : null;
    }

    /**
     * Returns a node with address tags under the building.
     *
     * @param w
     *            the building way
     * @return A node with address tags under the building.
     */
    private static Node getAddressNode(Way w) {
        BBox bbox = w.getBBox();
        List<Node> nodes = new LinkedList<>();
        Area area = Geometry.getArea(w.getNodes());
        for (Node n : MainApplication.getLayerManager().getEditDataSet().searchNodes(bbox)) {
            if (n.isUsable() && findUsableTag(n)) {
                EastNorth enTest = n.getEastNorth();
                if (area.contains(enTest.getX(), enTest.getY())) {
                    boolean useNode = true;
                    for (OsmPrimitive p : n.getReferrers()) {
                        // Don't use nodes if they're referenced by ways
                        if (p.getType() == OsmPrimitiveType.WAY) {
                            useNode = false;
                            break;
                        }
                    }
                    if (useNode) {
                        nodes.add(n);
                    }
                }
            }
        }
        if (nodes.size() != 1)
            return null;
        return nodes.get(0);
    }

    static boolean findUsableTag(OsmPrimitive p) {
        for (String key : p.getKeys().keySet()) {
            if ("building".equals(key) || key.startsWith("addr:")) {
                return true;
            }
        }
        return false;
    }

    public Way createCircle() {
        DataSet ds = MainApplication.getLayerManager().getEditDataSet();
        Collection<OsmPrimitive> selectedPrimitives = ds.getAllSelected();
        ds.clearSelection();

        if (len == 0)
            return null;
        final boolean[] created = new boolean[2];
        final Node[] nodes = new Node[2];
        for (int i = 0; i < 2; i++) {

            Node n = findNode(en[i]);
            if (n == null) {
                nodes[i] = new Node(eastNorth2latlon(en[i]));
                created[i] = true;
            } else {
                nodes[i] = n;
                created[i] = false;
            }
            if (nodes[i].isOutSideWorld()) {
                JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                        tr("Cannot place building outside of the world."));
                return null;
            }
        }
        Way w = new Way();
        w.addNode(nodes[0]);
        w.addNode(nodes[1]);

        Collection<Command> addNodesCmd = new LinkedList<>();

        for (int i = 0; i < 2; i++) {
            if (created[i]) {
                AddCommand addNode = new AddCommand(ds, nodes[i]);
                addNodesCmd.add(addNode);
            }
        }

        snapBuildings(w, nodes, addNodesCmd);
        if (!addNodesCmd.isEmpty()) {
            Command addNodes = new SequenceCommand(tr("Add nodes for building"), addNodesCmd);
            UndoRedoHandler.getInstance().add(addNodes);
        }

        // Nodes must be selected for create circle action
        ds.addSelected(w.getNodes());

        int oldNumWays = ds.getWays().size();
        CreateCircleAction action = new CreateCircleAction();
        action.setEnabled(true);
        action.actionPerformed(null);

        // restore selection
        ds.clearSelection();
        ds.addSelected(selectedPrimitives);
        if (oldNumWays < ds.getWays().size()) {
            // get the way with the smallest id with the assumption that it is
            // newest way created by CreateCirclAction
            List<Way> ways = new ArrayList<>(ds.getWays());
            Collections.sort(ways);
            w = ways.get(0);

            addAddress(w, null);

            return w;
        } else {
            // CreateCircleAction failed
            if (!addNodesCmd.isEmpty()) {
                UndoRedoHandler.getInstance().undo(1);
            }
            return null;
        }
    }

    public Way createRectangle(boolean ctrl) {
        if (len == 0)
            return null;
        final boolean[] created = new boolean[4];
        final Node[] nodes = new Node[4];
        final boolean snap = !ctrl; // don't snap if Ctrl is held
        for (int i = 0; i < 4; i++) {
            Node n = null;
            if (snap) {
                n = findNode(en[i]);
            }
            if (n == null) {
                nodes[i] = new Node(eastNorth2latlon(en[i]));
                created[i] = true;
            } else {
                nodes[i] = n;
                created[i] = false;
            }
            if (nodes[i].isOutSideWorld()) {
                JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                        tr("Cannot place building outside of the world."));
                return null;
            }
        }
        Way w = new Way();
        w.addNode(nodes[0]);
        if (projection2(en[2]) > 0 ^ len < 0) {
            w.addNode(nodes[1]);
            w.addNode(nodes[2]);
            w.addNode(nodes[3]);
        } else {
            w.addNode(nodes[3]);
            w.addNode(nodes[2]);
            w.addNode(nodes[1]);
        }
        w.addNode(nodes[0]);
        DataSet ds = MainApplication.getLayerManager().getEditDataSet();
        Collection<Command> cmds = new LinkedList<>();
        for (int i = 0; i < 4; i++) {
            if (created[i])
                cmds.add(new AddCommand(ds, nodes[i]));
        }
        cmds.add(new AddCommand(ds, w));

        addAddress(w, cmds);

        if (snap) {
            snapBuildings(w, nodes, cmds);
        }
        Command c = new SequenceCommand(tr("Create building"), cmds);
        UndoRedoHandler.getInstance().add(c);
        return w;
    }

    private static void snapBuildings(Way w, Node[] nodes, Collection<Command> cmds) {
        // calculate BBox which is slightly larger than the new building
        List<Node> wayNodes = w.getNodes();
        // find the ways which might be snapped to the new building
        Set<Way> others = new LinkedHashSet<>();
        MapView mv = MainApplication.getMap().mapView;
        for (Node n : nodes) {
            Way w2 = mv.getNearestWay(mv.getPoint(n), OsmPrimitive::isSelectable);
            if (w2 != null && w2.get("building") != null)
                others.add(w2);
        }
        // add nodes of existing buildings to the new one
        for (Way other : others) {
            snapToWay(wayNodes, other.getNodes());
            w.setNodes(wayNodes);
        }
        // add new nodes to existing buildings
        for (Way other : others) {
            List<Node> otherNodes = other.getNodes();
            snapToWay(otherNodes, Arrays.asList(nodes));
            if (otherNodes.size() != other.getNodesCount()) {
                Way newWay = new Way(other);
                newWay.setNodes(otherNodes);
                cmds.add(new ChangeCommand(other, newWay));
            }
        }

    }

    /**
     * Add all nodes in otherNodes to wayNodes that are within snap distance to
     * the segments described by wayNodes.
     *
     * @param wayNodes
     *            List of nodes that might be changed
     * @param otherNodes
     *            other nodes
     */
    private static void snapToWay(List<Node> wayNodes, Collection<Node> otherNodes) {
        int i = 0;
        while (i < wayNodes.size()) {
            Node n0 = wayNodes.get(i);
            Node n1 = wayNodes.get(i + 1 == wayNodes.size() ? 0 : i + 1);
            for (Node n2 : otherNodes) {
                if (n2 != n0 && n2 != n1) {
                    EastNorth x = Geometry.closestPointToSegment(n0.getEastNorth(), n1.getEastNorth(),
                            n2.getEastNorth());
                    if (x.distance(n2.getEastNorth()) <= EQUAL_NODE_DIST_TOLERANCE && !wayNodes.contains(n2)) {
                        wayNodes.add(i + 1, n2);
                        // we may add multiple nodes to one segment, so repeat
                        // it
                        i--;
                        break;
                    }
                }
            }
            i++;
        }
    }

    private static void addAddress(Way w, Collection<Command> cmdList) {
        if (ToolSettings.PROP_USE_ADDR_NODE.get()) {
            Node addrNode = getAddressNode(w);
            if (addrNode != null) {
                Collection<Command> addressCmds = cmdList != null ? cmdList : new LinkedList<>();
                for (Entry<String, String> entry : addrNode.getKeys().entrySet()) {
                    w.put(entry.getKey(), entry.getValue());
                }
                for (OsmPrimitive p : addrNode.getReferrers()) {
                    Relation r = (Relation) p;
                    Relation rnew = new Relation(r);
                    for (int i = 0; i < r.getMembersCount(); i++) {
                        RelationMember member = r.getMember(i);
                        if (addrNode.equals(member.getMember())) {
                            rnew.removeMember(i);
                            rnew.addMember(i, new RelationMember(member.getRole(), w));
                        }
                    }
                    addressCmds.add(new ChangeCommand(r, rnew));
                }
                addressCmds.add(new DeleteCommand(addrNode));
                if (cmdList == null) {
                    Command c = new SequenceCommand(tr("Add address for building"), addressCmds);
                    UndoRedoHandler.getInstance().add(c);
                }
            }
        }
    }
}

package buildings_tools;

import static org.openstreetmap.josm.tools.I18n.tr;

import static buildings_tools.BuildingsToolsPlugin.eastNorth2latlon;
import static buildings_tools.BuildingsToolsPlugin.latlon2eastNorth;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.coor.*;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MapView;

class Building {
    private final EastNorth[] en = new EastNorth[4];

    double meter = 0;

    private double len = 0;
    private double width;
    private double heading;
    private AngleSnap angleSnap = new AngleSnap();
    private Double drawingAngle;

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
        return drawingAngle != null && ToolSettings.getWidth() == 0 && ToolSettings.getLenStep() == 0;
    }

    public Double getDrawingAngle() {
        return drawingAngle;
    }

    public void reset() {
        len = 0;

        for (int i = 0; i < 4; i++)
            en[i] = null;
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

    public void setBase(Node base) {
        en[0] = latlon2eastNorth(base.getCoor());
        updMetrics();
    }

    /**
     * @returns Projection of the point to the heading vector in metres
     */
    private double projection1(EastNorth p) {
        final EastNorth vec = en[0].sub(p);
        return (Math.sin(heading) * vec.east() + Math.cos(heading) * vec.north()) / meter;
    }

    /**
     * @returns Projection of the point to the perpendicular of the heading
     *          vector in metres
     */
    private double projection2(EastNorth p) {
        final EastNorth vec = en[0].sub(p);
        return (Math.cos(heading) * vec.east() - Math.sin(heading) * vec.north()) / meter;
    }

    private void updatePos() {
        if (len == 0)
            return;
        final EastNorth p1 = en[0];
        en[1] = new EastNorth(p1.east() + Math.sin(heading) * len * meter, p1.north() + Math.cos(heading) * len * meter);
        en[2] = new EastNorth(p1.east() + Math.sin(heading) * len * meter + Math.cos(heading) * width * meter,
                p1.north() + Math.cos(heading) * len * meter - Math.sin(heading) * width * meter);
        en[3] = new EastNorth(p1.east() + Math.cos(heading) * width * meter,
                p1.north() - Math.sin(heading) * width * meter);
    }

    public void setLengthWidth(double length, double width) {
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

        Main.map.statusLine.setHeading(Math.toDegrees(heading));
        if (this.drawingAngle != null && !ignoreConstraints) {
            double ang = Math.toDegrees(heading - this.drawingAngle);
            if (ang < 0)
                ang += 360;
            if (ang > 360)
                ang -= 360;
            Main.map.statusLine.setAngle(ang);
        }
    }

    public void setPlaceRect(EastNorth p2) {
        if (en[0] == null)
            throw new IllegalStateException("SetPlaceRect() called without the base point");
        if (!isRectDrawing())
            throw new IllegalStateException("Invalid drawing mode");
        heading = drawingAngle;
        setLengthWidth(projection1(p2), projection2(p2));
        Main.map.statusLine.setHeading(Math.toDegrees(heading));
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
        Point pp3 = mv.getPoint(eastNorth2latlon(en[2]));
        Point pp4 = mv.getPoint(eastNorth2latlon(en[3]));

        b.moveTo(pp1.x, pp1.y);
        b.lineTo(pp2.x, pp2.y);
        b.lineTo(pp3.x, pp3.y);
        b.lineTo(pp4.x, pp4.y);
        b.lineTo(pp1.x, pp1.y);
        g.draw(b);
    }

    private Node findNode(EastNorth en) {
        DataSet ds = Main.main.getCurrentDataSet();
        LatLon l = eastNorth2latlon(en);
        List<Node> nodes = ds.searchNodes(new BBox(l.lon() - 0.0000001, l.lat() - 0.0000001,
                l.lon() + 0.0000001, l.lat() + 0.0000001));
        Node bestnode = null;
        double mindist = 0.0003;
        for (Node n : nodes) {
            double dist = n.getCoor().distanceSq(l);
            if (dist < mindist && OsmPrimitive.isUsablePredicate.evaluate(n)) {
                bestnode = n;
                mindist = dist;
            }
        }
        return bestnode;
    }

    /**
     * Returns a node with "building=yes" tag under the building
     *
     * @return
     */
    private Node getAddressNode() {
        BBox bbox = new BBox(eastNorth2latlon(en[0]), eastNorth2latlon(en[1]));
        bbox.add(eastNorth2latlon(en[2]));
        bbox.add(eastNorth2latlon(en[3]));
        List<Node> nodes = new LinkedList<Node>();
        nodesloop:
        for (Node n : Main.main.getCurrentDataSet().searchNodes(bbox)) {
            if (!n.isUsable() || n.getKeys().get("building") == null)
                continue;
            double x = projection1(latlon2eastNorth(n.getCoor()));
            double y = projection2(latlon2eastNorth(n.getCoor()));
            if (Math.signum(x) != Math.signum(len) || Math.signum(y) != Math.signum(width))
                continue;
            if (Math.abs(x) > Math.abs(len) || Math.abs(y) > Math.abs(width))
                continue;
            for (OsmPrimitive p : n.getReferrers()) {
                // Don't use nodes if they're referenced by ways
                if (p.getType() == OsmPrimitiveType.WAY)
                    continue nodesloop;
            }
            nodes.add(n);
        }
        if (nodes.size() != 1)
            return null;
        return nodes.get(0);
    }

    public Way create() {
        if (len == 0)
            return null;
        final boolean[] created = new boolean[4];
        final Node[] nodes = new Node[4];
        for (int i = 0; i < 4; i++) {

            Node n = findNode(en[i]);
            if (n == null) {
                nodes[i] = new Node(eastNorth2latlon(en[i]));
                created[i] = true;
            } else {
                nodes[i] = n;
                created[i] = false;
            }
            if (nodes[i].getCoor().isOutSideWorld()) {
                JOptionPane.showMessageDialog(Main.parent,
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
        Collection<Command> cmds = new LinkedList<Command>();
        for (int i = 0; i < 4; i++) {
            if (created[i])
                cmds.add(new AddCommand(nodes[i]));
        }
        cmds.add(new AddCommand(w));
        Node addrNode;
        if (ToolSettings.PROP_USE_ADDR_NODE.get() && (addrNode = getAddressNode()) != null) {
            w.setKeys(addrNode.getKeys());
            for (OsmPrimitive p : addrNode.getReferrers()) {
                Relation r = (Relation) p;
                Relation rnew = new Relation(r);
                for (int i = 0; i < r.getMembersCount(); i++) {
                    RelationMember member = r.getMember(i);
                    if (member.getMember() == addrNode) {
                        rnew.removeMember(i);
                        rnew.addMember(i, new RelationMember(member.getRole(), w));
                    }
                }
                cmds.add(new ChangeCommand(r, rnew));
            }
            cmds.add(new DeleteCommand(addrNode));
        } else {
            w.setKeys(ToolSettings.getTags());
        }
        Command c = new SequenceCommand(tr("Create building"), cmds);
        Main.main.undoRedo.add(c);
        return w;
    }
}

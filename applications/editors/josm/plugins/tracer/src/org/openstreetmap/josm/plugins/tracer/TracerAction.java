/**
 * Tracer - plugin for JOSM
 * Jan Bilak
 * This program is free software and licensed under GPL.
 */
package org.openstreetmap.josm.plugins.tracer;

import static org.openstreetmap.josm.tools.I18n.tr;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;
import org.xml.sax.SAXException;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

class TracerAction extends MapMode implements MouseListener {

    private static final long serialVersionUID = 1L;
    protected boolean cancel;
    private boolean ctrl;
    private boolean alt;
    private boolean shift;
    protected TracerServer server = new TracerServer();

    public TracerAction(MapFrame mapFrame) {
        super(tr("Tracer"), "tracer-sml", tr("Tracer."), Shortcut.registerShortcut("tools:tracer", tr("Tool: {0}", tr("Tracer")), KeyEvent.VK_T, Shortcut.GROUP_EDIT), mapFrame, getCursor());
    }

    @Override
    public void enterMode() {
        if (!isEnabled()) {
            return;
        }
        super.enterMode();
        Main.map.mapView.setCursor(getCursor());
        Main.map.mapView.addMouseListener(this);
    }

    @Override
    public void exitMode() {
        super.exitMode();
        Main.map.mapView.removeMouseListener(this);
    }

    private static Cursor getCursor() {
        return ImageProvider.getCursor("crosshair", "tracer-sml");
    }

    protected void traceAsync(Point clickPoint) {
        cancel = false;
        /**
         * Positional data
         */
        final LatLon pos = Main.map.mapView.getLatLon(clickPoint.x, clickPoint.y);

        try {
            PleaseWaitRunnable tracerTask = new PleaseWaitRunnable(tr("Tracing")) {

                @Override
                protected void realRun() throws SAXException {
                    traceSync(pos, progressMonitor.createSubTaskMonitor(ProgressMonitor.ALL_TICKS, false));
                }

                @Override
                protected void finish() {
                }

                @Override
                protected void cancel() {
                    TracerAction.this.cancel();
                }
            };
            Thread executeTraceThread = new Thread(tracerTask);
            executeTraceThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isBuilding(Way w) {
        return (w.getKeys().get("building") == null ? false : w.getKeys().get("building").equals("yes"));
    }

    private boolean isInBuilding(Node n) {
        for (OsmPrimitive op : n.getReferrers()) {
            if (op instanceof Way) {
                if (isBuilding((Way) op)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Try connect way to other buidings.
     * @param way Way to connect.
     * @return Commands.
     */
    private Command connectObjects(Way way) {
        final double MIN_DISTANCE = 0.000015;

        List<Command> cmds = new LinkedList<Command>();
        Way newWay = new Way(way);
        for (int i = 1; i < way.getNodesCount(); i++) {
            Node n = way.getNode(i);
            LatLon ll = n.getCoor();
            BBox bbox = new BBox(
                    ll.getX() - MIN_DISTANCE,
                    ll.getY() - MIN_DISTANCE,
                    ll.getX() + MIN_DISTANCE,
                    ll.getY() + MIN_DISTANCE);

            // bude se node slucovat s jinym?
            double minDistanceSq = MIN_DISTANCE;
            List<Node> nodes = Main.main.getCurrentDataSet().searchNodes(bbox);
            Node nearestNode = null;
            for (Node nn : nodes) {
                if (!nn.isUsable() || way.containsNode(nn) || newWay.containsNode(nn) || !isInBuilding(nn)) {
                    continue;
                }
                double dist = nn.getCoor().distance(ll);
                if (dist < minDistanceSq) {
                    minDistanceSq = dist;
                    nearestNode = nn;
                }
            }

            //System.out.println("Nearest: " + nearestNode);
            //System.out.println("-------");
            if (nearestNode == null) {
                tryConnectNodeToAnyWay(way, newWay, n, cmds);
            } else {
                nearestNode.setCoor(ll.getCenter(nearestNode.getCoor()));
                int j = newWay.getNodes().indexOf(n);
                newWay.addNode(j, nearestNode);
                if (j == 0) {
                    // first + last point
                    newWay.addNode(newWay.getNodesCount(), nearestNode);
                }
                newWay.removeNode(n);
                cmds.add(new DeleteCommand(n));
            }
        }
        trySplitWayByAnyNodes(way, newWay);

        cmds.add(new ChangeCommand(way, newWay));

        Command cmd = new SequenceCommand(tr("Merge objects nodes"), cmds);
        return cmd;
    }

    /**
     * Try connect node "node" from way "oldWay" to way of other building.
     *
     * Zkusi zjistit, zda node neni tak blizko nejake usecky existujici budovy,
     * ze by mel byt zacnenen do teto usecky. Pokud ano, provede to.
     *
     * @param oldWay Way which contains node "node".
     * @param newWay Modified way.
     * @param node Node to connect.
     * @param cmds Command list.
     * @throws IllegalStateException
     * @throws IndexOutOfBoundsException
     */
    private void tryConnectNodeToAnyWay(Way oldWay, Way newWay, Node node, List<Command> cmds)
            throws IllegalStateException, IndexOutOfBoundsException {

        final double MIN_DISTANCE = 0.000015;

        LatLon ll = node.getCoor();
        BBox bbox = new BBox(
                ll.getX() - MIN_DISTANCE,
                ll.getY() - MIN_DISTANCE,
                ll.getX() + MIN_DISTANCE,
                ll.getY() + MIN_DISTANCE);

        // node nebyl slouceny s jinym
        // hledani pripadne blizke usecky, kam bod pridat
        List<Way> ways = Main.main.getCurrentDataSet().searchWays(bbox);
        double minDist = Double.MAX_VALUE;
        Way nearestWay = null;
        int nearestNodeIndex = 0;
        for (Way ww : ways) {
            if (!ww.isUsable() || ww == oldWay || ww == newWay || !isBuilding(ww)) {
                continue;
            }
            for (int nindex = 0; nindex < ww.getNodesCount(); nindex++) {
                Node n1 = ww.getNode(nindex);
                Node n2 = ww.getNode((nindex + 1) % ww.getNodesCount());
                double dist = TracerGeometry.distanceFromSegment(ll, n1.getCoor(), n2.getCoor());
                if (dist < minDist) {
                    minDist = dist;
                    nearestWay = ww;
                    nearestNodeIndex = nindex;
                }
            }
        }
        //System.out.println("Nearest way:" + nearestWay);
        if (minDist < MIN_DISTANCE) {
            Way newNWay = new Way(nearestWay);
            newNWay.addNode(nearestNodeIndex + 1, node);
            //System.out.println("New way:" + newNWay);
            cmds.add(new ChangeCommand(nearestWay, newNWay));
        }
    }

    /**
     * Try split way by any existing buiding nodes.
     *
     * Zkusi zjistit zda nejake usecka z oldWay by nemela prochazet nejakym existujicim bodem,
     * ktery je ji velmi blizko. Pokud ano, tak puvodni usecku rozdeli na dve tak, aby
     * prochazela takovym bodem.
     *
     * @param oldWay Way to split.
     * @param newWay Modified way.
     * @throws IndexOutOfBoundsException
     * @throws IllegalStateException
     */
    private void trySplitWayByAnyNodes(Way oldWay, Way newWay)
            throws IndexOutOfBoundsException, IllegalStateException {

        // projdi kazdou novou usecku a zjisti, zda by nemela vest pres existujici body
        int i = 0;
        while (i < newWay.getNodesCount()) {
            // usecka n1, n2
            LatLon n1 = newWay.getNodes().get(i).getCoor();
            LatLon n2 = newWay.getNodes().get((i + 1) % newWay.getNodesCount()).getCoor();
            //System.out.println(newWay.getNodes().get(i) + "-----" + newWay.getNodes().get((i + 1) % newWay.getNodesCount()));
            double minDistanceSq = 0.000015;
            double maxAngle = 15;
            List<Node> nodes = Main.main.getCurrentDataSet().searchNodes(new BBox(Math.min(n1.getX(), n2.getX()) - minDistanceSq, Math.min(n1.getY(), n2.getY()) - minDistanceSq, Math.max(n1.getX(), n2.getX()) + minDistanceSq, Math.max(n1.getY(), n2.getY()) + minDistanceSq));
            Node nearestNode = null;
            for (Node nod : nodes) {
                if (!nod.isUsable() || oldWay.containsNode(nod) || newWay.containsNode(nod) || !isInBuilding(nod)) {
                    continue;
                }
                LatLon nn = nod.getCoor();
                double dist = TracerGeometry.distanceFromSegment(nn, n1, n2);
                double angle = TracerGeometry.angleOfLines(n1, nn, nn, n2);
                if (!n1.equalsEpsilon(nn) && !n2.equalsEpsilon(nn) && dist < minDistanceSq && Math.abs(angle) < maxAngle) {
                    maxAngle = angle;
                    nearestNode = nod;
                }
            }
            //System.out.println("Nearest_: " + nearestNode);
            //System.out.println("");
            if (nearestNode == null) {
                // tato usecka se nerozdeli
                i++;
                continue;
            } else {
                // rozdeleni usecky
                newWay.addNode(i + 1, nearestNode);
                continue; // i nezvetsuji, treba bude treba rozdelit usecku znovu
            }
        }
    }

    private void tagBuilding(Way way) {
        way.put("building", "yes");
        way.put("source", "cuzk:km");
    }

    private void traceSync(LatLon pos, ProgressMonitor progressMonitor) {
        Collection<Command> commands = new LinkedList<Command>();

        progressMonitor.beginTask(null, 3);
        try {
            ArrayList<LatLon> coordList = server.trace(pos);

            if (coordList.size() == 0) {
                return;
            }

            // make nodes a way
            Way way = new Way();
            Node firstNode = null;
            for (LatLon coord : coordList) {
                Node node = new Node(coord);
                if (firstNode == null) {
                    firstNode = node;
                }
                commands.add(new AddCommand(node));
                way.addNode(node);
            }
            way.addNode(firstNode);

            tagBuilding(way);
            commands.add(new AddCommand(way));

            // connect to other buildings
            if (!ctrl) {
                commands.add(connectObjects(way));
            }

            if (!commands.isEmpty()) {
                Main.main.undoRedo.add(new SequenceCommand(tr("Tracer building"), commands));

                if (shift) {
                    Main.main.getCurrentDataSet().addSelected(way);
                } else {
                    Main.main.getCurrentDataSet().setSelected(way);
                }
            } else {
                System.out.println("Failed");
            }

        } finally {
            progressMonitor.finishTask();
        }
    }

    public void cancel() {
        cancel = true;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!Main.map.mapView.isActiveLayerDrawable()) {
            return;
        }

        updateKeyModifiers(e);
        if (e.getButton() == MouseEvent.BUTTON1) {
            traceAsync(e.getPoint());
        }
    }

    private void updateKeyModifiers(MouseEvent e) {
        ctrl = (e.getModifiers() & ActionEvent.CTRL_MASK) != 0;
        alt = (e.getModifiers() & (ActionEvent.ALT_MASK | InputEvent.ALT_GRAPH_MASK)) != 0;
        shift = (e.getModifiers() & ActionEvent.SHIFT_MASK) != 0;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }
}

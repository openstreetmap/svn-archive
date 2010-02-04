/**
 * Tracer - plugin for JOSM
 * Jan Bilak
 * This program is free software and licensed under GPL.
 */
package org.openstreetmap.josm.plugins.tracer;

import static org.openstreetmap.josm.tools.I18n.tr;
import java.awt.Cursor;
import java.awt.Point;
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

class TracerAction extends MapMode implements MouseListener {

    protected TracerServer traceServer = new TracerServer();
    protected boolean cancel;
    protected Collection<Command> commands = new LinkedList<Command>();
    protected Collection<Way> ways = new ArrayList<Way>();

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
            Thread executeThread = new Thread(tracerTask);
            executeThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void traceSync(LatLon pos, ProgressMonitor progressMonitor) {
        
        progressMonitor.beginTask(null, 3);
        try {
            ArrayList<double[]> nodelist = new ArrayList<double[]>();
            nodelist = traceServer.trace(pos);

            if (nodelist.size() == 0) {
                return;
            }

            /**
             * Turn the arraylist into osm nodes
             */
            Way way = new Way();
            Node n = null;
            Node fn = null;

            double eastOffset = 0;
            double northOffset = 0;


            int nodesinway = 0;

            for (int i = 0; i < nodelist.size(); i++) {
                if (cancel) {
                    return;
                }

                try {
                    LatLon ll = new LatLon(nodelist.get(i)[0] + northOffset, nodelist.get(i)[1] + eastOffset);

                    double minDistanceSq = 0.00001;
                    BBox box = new BBox(
                            ll.getX() - minDistanceSq,
                            ll.getY() - minDistanceSq,
                            ll.getX() + minDistanceSq,
                            ll.getY() + minDistanceSq);
                    List<Node> nodes = Main.main.getCurrentDataSet().searchNodes(box);
                    List<Way> nodeWays = Main.main.getCurrentDataSet().searchWays(box);
                    Node nearestNode = null;
                    for (Node nn : nodes) {
                        if (!nn.isUsable()) {
                            continue;
                        }

                        boolean buildingNode = false;
                        for (Way w : nodeWays) {
                            if (w.containsNode(nn) && w.hasKey("building")) {
                                buildingNode = true;
                                break;
                            }
                        }
                        if (!buildingNode) {
                            continue;
                        }

                        LatLon ll2 = nn.getCoor();
                        double dist = ll2.distance(ll);
                        if (dist < minDistanceSq) {
                            minDistanceSq = dist;
                            nearestNode = nn;
                        }
                    }

                    if (nearestNode == null) {
                        // hledani blizke usecky, kam bod pridat ... nebo vytvorit samostatny bod?
                        List<Way> ways = Main.main.getCurrentDataSet().searchWays(new BBox(
                                ll.getX() - minDistanceSq,
                                ll.getY() - minDistanceSq,
                                ll.getX() + minDistanceSq,
                                ll.getY() + minDistanceSq));
                        double minDist = Double.MAX_VALUE;
                        Way nearestWay = null;
                        int nearestNodeIndex = 0;


                        for (Way ww : ways) {
                            if (!ww.hasKey("building")) {
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

                        if (minDist < 0.00001) {
                            n = new Node(ll);
                            commands.add(new AddCommand(n));
                            Way newWay = new Way(nearestWay);
                            newWay.addNode(nearestNodeIndex + 1, n);
                            commands.add(new ChangeCommand(nearestWay, newWay));
                        } else {
                            n = new Node(ll);
                            commands.add(new AddCommand(n));
                        }
                    } else {
                        n = nearestNode;
                    }

                    if (fn == null) {
                        fn = n;
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                way.addNode(n);
                nodesinway++;
            }
            /**/
            // projdi kazdou novou usecku a zjisti, zda by nemela vest pres existujici body
            int i = 0;
            while (i < nodelist.size()) {
                if (cancel) {
                    return;
                }

                traceServer.log("i/" + i);

                // usecka n1, n2
                double d[] = nodelist.get(i);
                LatLon n1 = new LatLon(d[0], d[1]);
                d = nodelist.get((i + 1) % nodelist.size());
                LatLon n2 = new LatLon(d[0], d[1]);

                double minDistanceSq = 0.00001;
                double maxAngle = 10;
                BBox box = new BBox(
                        Math.min(n1.getX(), n2.getX()) - minDistanceSq,
                        Math.min(n1.getY(), n2.getY()) - minDistanceSq,
                        Math.max(n1.getX(), n2.getX()) + minDistanceSq,
                        Math.max(n1.getY(), n2.getY()) + minDistanceSq);
                List<Node> nodes = Main.main.getCurrentDataSet().searchNodes(box);
                List<Way> nodeWays = Main.main.getCurrentDataSet().searchWays(box);
                Node nearestNode = null;
                for (Node nod : nodes) {
                    if (!nod.isUsable()) {
                        continue;
                    }
                    boolean buildingNode = false;
                    for (Way w : nodeWays) {
                        if (w.containsNode(nod) && w.hasKey("building")) {
                            buildingNode = true;
                            break;
                        }
                    }
                    if (!buildingNode) {
                        continue;
                    }

                    LatLon nn = nod.getCoor();
                    double dist = TracerGeometry.distanceFromSegment(nn, n1, n2);
                    double angle = TracerGeometry.angleOfLines(n1, nn, nn, n2);

                    if (!n1.equalsEpsilon(nn) && !n2.equalsEpsilon(nn) && dist < minDistanceSq) {
                        traceServer.log("n1/" + n1.toDisplayString());
                        traceServer.log("nn/" + nn.toDisplayString());
                        traceServer.log("n2/" + n2.toDisplayString());
                        traceServer.log("angle/" + Math.abs(angle));
                    }
                    if (!n1.equalsEpsilon(nn) && !n2.equalsEpsilon(nn) && dist < minDistanceSq && Math.abs(angle) < maxAngle) {
                        traceServer.log("ok");
                        maxAngle = angle;
                        nearestNode = nod;
                    }
                }

                if (nearestNode == null) {
                    // tato usecka se nerozdeli
                    i++;
                    continue;
                } else {
                    // rozdeleni usecky
                    nodelist.add(i + 1, new double[]{nearestNode.getCoor().getY(), nearestNode.getCoor().getX()});
                    way.addNode(i + 1, nearestNode);
                    continue; // i nezvetsuji, treba bude treba rozdelit usecku znovu
                }

            }
            /**/
            way.put("building", "yes");
            way.put("source", "cuzk:km");

            way.addNode(fn);

            commands.add(new AddCommand(way));

            if (!commands.isEmpty()) {
                Main.main.undoRedo.add(new SequenceCommand(tr("Tracer building"), commands));
                Main.main.getCurrentDataSet().setSelected(ways);
            } else {
                System.out.println("Failed");
            }

            commands = new LinkedList<Command>();
            ways = new ArrayList<Way>();
        } finally {
            progressMonitor.finishTask();
        }
    }

    public void cancel() {
        cancel = true;
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        if (!Main.map.mapView.isActiveLayerDrawable()) {
            return;
        }

        if (e.getButton() == MouseEvent.BUTTON1) {
            traceAsync(e.getPoint());
        }
    }

    public void mouseReleased(MouseEvent e) {
    }
}

/**
 * Tracer - plugin for JOSM
 * Jan Bilak
 * This program is free software and licensed under GPL.
 */

package tracer;

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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

/**
 * Interface to Darryl Shpak's Tracer module
 *
 * @author Brent Easton
 */
class TracerAction extends MapMode implements MouseListener {

    private static final long serialVersionUID = 1L;
    protected Thread executeThread;
    protected boolean cancel;
    protected Collection<Command> commands = new LinkedList<Command>();
    protected Collection<Way> ways = new ArrayList<Way>();

    public TracerAction(MapFrame mapFrame) {
        super(tr("Tracer"), "tracer-sml", tr("Tracer."), Shortcut.registerShortcut("tools:tracer", tr("Tool: {0}", tr("Tracer")), KeyEvent.VK_T, Shortcut.GROUP_EDIT), mapFrame, getCursor());
    }

   @Override public void enterMode() {
       if (!isEnabled())
           return;
       super.enterMode();
       Main.map.mapView.setCursor(getCursor());
       Main.map.mapView.addMouseListener(this);
   }

   @Override public void exitMode() {
       super.exitMode();
       Main.map.mapView.removeMouseListener(this);
   }

   private static Cursor getCursor() {
         return ImageProvider.getCursor("crosshair", "tracer-sml");
    }



    /**
     * check for presence of cache folder and trim cache to specified size.
     * size/age limit is on a per folder basis.
     */
    protected void trace(Point clickPoint) {
        cancel = false;
        /**
         * Positional data
         */
        final LatLon pos = Main.map.mapView.getLatLon(clickPoint.x, clickPoint.y);
        final LatLon topLeft = Main.map.mapView.getLatLon(0, 0);
        final LatLon botRight = Main.map.mapView.getLatLon(Main.map.mapView.getWidth(), Main.map.mapView.getHeight());

        try {
            PleaseWaitRunnable tracerTask = new PleaseWaitRunnable(tr("Tracing")) {

                @Override
                protected void realRun() throws SAXException {
                    processnodelist(pos, topLeft, botRight,
                            progressMonitor.createSubTaskMonitor(ProgressMonitor.ALL_TICKS, false));
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

    private String callWeb(String urlString) {
        try {
            URL url = new URL(urlString);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

            StringBuilder sb = new StringBuilder();
            String sx;
            while ((sx = reader.readLine()) != null) {
                sb.append(sx);
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private ArrayList<double[]> getNodeList(LatLon pos) {
        try {
            String content = callWeb("http://localhost:5050/trace/simple/" + pos.lat() + ";" + pos.lon());
            ArrayList<double[]> nodelist = new ArrayList<double[]>();
            String[] lines = content.split("\\|");
            for (String line : lines) {
                String[] items = line.split(";");
                double x = Double.parseDouble(items[0]);
                double y = Double.parseDouble(items[1]);
                double[] d = new double[2];
                d[0] = x;
                d[1] = y;
                nodelist.add(d);
            }

            return nodelist;
        } catch (Exception e) {
            ArrayList<double[]> nodelist = new ArrayList<double[]>();
            return nodelist;
        }
    }

    private void processnodelist(LatLon pos, LatLon topLeft, LatLon botRight, ProgressMonitor progressMonitor) {
        progressMonitor.beginTask(null, 3);
        try {
            ArrayList<double[]> nodelist = new ArrayList<double[]>();
            nodelist = getNodeList(pos);

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
                    List<Node> nodes = Main.main.getCurrentDataSet().searchNodes(new BBox(
                            ll.getX() - minDistanceSq,
                            ll.getY() - minDistanceSq,
                            ll.getX() + minDistanceSq,
                            ll.getY() + minDistanceSq
                            ));
                    Node nearestNode = null;
                    for (Node nn : nodes) {
                        if (!nn.isUsable()) {
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
                                ll.getY() + minDistanceSq
                                ));
                        double minDist = Double.MAX_VALUE;
                        Way nearestWay = null;
                        int nearestNodeIndex = 0;


                        for (Way ww : ways) {
                            for (int nindex = 0; nindex < ww.getNodesCount(); nindex++) {
                                Node n1 = ww.getNode(nindex);
                                Node n2 = ww.getNode((nindex + 1) % ww.getNodesCount());

                                double dist = distanceFromSegment(ll, n1.getCoor(), n2.getCoor());
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

                callWeb("http://localhost:5050/log/i/" + i);

                // usecka n1, n2
                double d[] = nodelist.get(i);
                LatLon n1 = new LatLon(d[0], d[1]);
                d = nodelist.get((i + 1) % nodelist.size());
                LatLon n2 = new LatLon(d[0], d[1]);

                double minDistanceSq = 0.00001;
                double maxAngle = 10;
                List<Node> nodes = Main.main.getCurrentDataSet().searchNodes(new BBox(
                        Math.min(n1.getX(), n2.getX()) - minDistanceSq,
                        Math.min(n1.getY(), n2.getY()) - minDistanceSq,
                        Math.max(n1.getX(), n2.getX()) + minDistanceSq,
                        Math.max(n1.getY(), n2.getY()) + minDistanceSq
                        ));
                Node nearestNode = null;
                for (Node nod : nodes) {
                    if (!nod.isUsable()) {
                        continue;
                    }
                    LatLon nn = nod.getCoor();
                    double dist = distanceFromSegment(nn, n1, n2);
                    double angle = angleOfLines(n1, nn, nn, n2);

                    if (!n1.equalsEpsilon(nn) && !n2.equalsEpsilon(nn) && dist < minDistanceSq)
                    {
                        callWeb("http://localhost:5050/log/n1/" + n1.toDisplayString());
                        callWeb("http://localhost:5050/log/nn/" + nn.toDisplayString());
                        callWeb("http://localhost:5050/log/n2/" + n2.toDisplayString());
                        callWeb("http://localhost:5050/log/angle/" + Math.abs(angle));
                    }
                    if (!n1.equalsEpsilon(nn) && !n2.equalsEpsilon(nn) && dist < minDistanceSq && Math.abs(angle) < maxAngle) {
                        callWeb("http://localhost:5050/log/ok");
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
                    nodelist.add(i+1, new double[] { nearestNode.getCoor().getY(), nearestNode.getCoor().getX() });
                    way.addNode(i+1, nearestNode);
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

    private double angleOfLines(LatLon a, LatLon b, LatLon c, LatLon d) {
        return (Math.abs(Math.atan2(a.lat() - b.lat(), a.lon() - b.lon()) - Math.atan2(c.lat() - d.lat(), c.lon() - d.lon())) / Math.PI * 180) % 360;
    }

    private double distanceFromSegment(LatLon c, LatLon a, LatLon b) {
        return distanceFromSegment(c.getX(), c.getY(), a.getX(), a.getY(), b.getX(), b.getY());
    }

    private double distanceFromSegment(double cx, double cy, double ax, double ay, double bx, double by) {
        double r_numerator = (cx - ax) * (bx - ax) + (cy - ay) * (by - ay);
        double r_denomenator = (bx - ax) * (bx - ax) + (by - ay) * (by - ay);
        double r = r_numerator / r_denomenator;
        double s = ((ay - cy) * (bx - ax) - (ax - cx) * (by - ay)) / r_denomenator;
        double distanceLine = Math.abs(s) * Math.sqrt(r_denomenator);

        if ((r >= 0) && (r <= 1)) {
            return distanceLine;
        } else {
            double dist1 = (cx - ax) * (cx - ax) + (cy - ay) * (cy - ay);
            double dist2 = (cx - bx) * (cx - bx) + (cy - by) * (cy - by);
            if (dist1 < dist2) {
                return Math.sqrt(dist1);
            } else {
                return Math.sqrt(dist2);
            }
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
        if(!Main.map.mapView.isActiveLayerDrawable())
           return;

        if (e.getButton() == MouseEvent.BUTTON1) {
            trace(e.getPoint());
        }
    }

    public void mouseReleased(MouseEvent e) {
    }
}

package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.io.ProgressInputStream;

public class DownloadSVGBuilding extends PleaseWaitRunnable {

    private WMSLayer wmsLayer;
    private CadastreGrabber grabber = CadastrePlugin.cadastreGrabber;
    private CadastreInterface wmsInterface;
    private String svg = null;
    private static EastNorthBound currentView = null;
    private EastNorthBound viewBox = null;

    public DownloadSVGBuilding(WMSLayer wmsLayer) {
        super(tr("Downloading {0}", wmsLayer.getName()));

        this.wmsLayer = wmsLayer;
        this.wmsInterface = grabber.getWmsInterface();
    }

    @Override
    public void realRun() throws IOException, OsmTransferException {
    	progressMonitor.indeterminateSubTask(tr("Contacting WMS Server..."));
        try {
            if (wmsInterface.retrieveInterface(wmsLayer)) {
                svg = grabBoundary(currentView);
                if (svg == null)
                    return;
                getViewBox(svg);
                if (viewBox == null)
                    return;
                createBuildings(svg);
            }
        } catch (DuplicateLayerException e) {
            System.err.println("removed a duplicated layer");
        }
    }

    @Override
    protected void cancel() {
        grabber.getWmsInterface().cancel();
    }

    @Override
    protected void finish() {
    }

    private boolean getViewBox(String svg) {
        double[] box = new SVGParser().getViewBox(svg);
        if (box != null) {
            viewBox = new EastNorthBound(new EastNorth(box[0], box[1]),
                    new EastNorth(box[0]+box[2], box[1]+box[3]));
            return true;
        }
        System.out.println("Unable to parse SVG data (viewBox)");
        return false;
    }

    /**
     *  The svg contains more than one commune boundary defined by path elements. So detect
     *  which path element is the best fitting to the viewBox and convert it to OSM objects
     */
    private void createBuildings(String svg) {
        String[] SVGpaths = new SVGParser().getClosedPaths(svg);
        ArrayList<ArrayList<EastNorth>> eastNorths = new ArrayList<ArrayList<EastNorth>>();

        // convert SVG nodes to eastNorth coordinates
        for (int i=0; i< SVGpaths.length; i++) {
            ArrayList<EastNorth> eastNorth = new ArrayList<EastNorth>();
            createNodes(SVGpaths[i], eastNorth);
            if (eastNorth.size() > 2)
                eastNorths.add(eastNorth);
        }

        // create nodes and closed ways
        DataSet svgDataSet = new DataSet();
        for (ArrayList<EastNorth> path : eastNorths) {
            Way wayToAdd = new Way();
            for (EastNorth eastNorth : path) {
                Node nodeToAdd = new Node(Main.proj.eastNorth2latlon(eastNorth));
                // check if new node is not already created by another new path
                Node nearestNewNode = checkNearestNode(nodeToAdd, svgDataSet.nodes);
                if (nearestNewNode == nodeToAdd)
                    svgDataSet.addPrimitive(nearestNewNode);
                wayToAdd.addNode(nearestNewNode); // either a new node or an existing one
            }
            wayToAdd.addNode(wayToAdd.getNode(0)); // close the way
            svgDataSet.addPrimitive(wayToAdd);
        }

        // TODO remove small boxes (4 nodes with less than 1 meter distance)
        /*
        for (Way w : svgDataSet.ways)
            if (w.nodes.size() == 5)
                for (int i = 0; i < w.nodes.size()-2; i++) {
                    if (w.nodes.get(i).eastNorth.distance(w.nodes.get(i+1).eastNorth))
                }*/

        // simplify ways and check if we can reuse existing OSM nodes
        for (Way wayToAdd : svgDataSet.ways)
            new SimplifyWay().simplifyWay(wayToAdd, svgDataSet, 0.5);
        // check if the new way or its nodes is already in OSM layer
        for (Node n : svgDataSet.nodes) {
            Node nearestNewNode = checkNearestNode(n, Main.main.getCurrentDataSet().nodes);
            if (nearestNewNode != n) {
                // replace the SVG node by the OSM node
                for (Way w : svgDataSet.ways) {
                    int replaced = 0;
                    for (Node node : w.getNodes())
                        if (node == n) {
                            node = nearestNewNode;
                            replaced++;
                        }
                    if (w.getNodesCount() == replaced)
                        w.setDeleted(true);
                }
                n.setDeleted(true);
            }

        }

        Collection<Command> cmds = new LinkedList<Command>();
        for (Node node : svgDataSet.nodes)
            if (!node.isDeleted())
                cmds.add(new AddCommand(node));
        for (Way way : svgDataSet.ways)
            if (!way.isDeleted())
                cmds.add(new AddCommand(way));
        Main.main.undoRedo.add(new SequenceCommand(tr("Create buildings"), cmds));
        Main.map.repaint();
    }

    private void createNodes(String SVGpath, ArrayList<EastNorth> eastNorth) {
        // looks like "M981283.38 368690.15l143.81 72.46 155.86 ..."
        String[] coor = SVGpath.split("[MlZ ]"); //coor[1] is x, coor[2] is y
        double dx = Double.parseDouble(coor[1]);
        double dy = Double.parseDouble(coor[2]);
        for (int i=3; i<coor.length; i+=2){
            if (coor[i].equals("")) {
                eastNorth.clear(); // some paths are just artifacts
                return;
            }
            double east = dx+=Double.parseDouble(coor[i]);
            double north = dy+=Double.parseDouble(coor[i+1]);
            eastNorth.add(new EastNorth(east,north));
        }
        // flip the image (svg using a reversed Y coordinate system)
        double pivot = viewBox.min.getY() + (viewBox.max.getY() - viewBox.min.getY()) / 2;
        for (EastNorth en : eastNorth) {
            en.setLocation(en.east(), 2 * pivot - en.north());
        }
        return;
    }

    /**
     * Check if node can be reused.
     * @param nodeToAdd the candidate as new node
     * @return the already existing node (if any), otherwise the new node candidate.
     */
    private Node checkNearestNode(Node nodeToAdd, Collection<Node> nodes) {
        double epsilon = 0.05; // smallest distance considering duplicate node
        for (Node n : nodes) {
            if (!n.isDeleted() && !n.incomplete) {
                double dist = n.getEastNorth().distance(nodeToAdd.getEastNorth());
                if (dist < epsilon) {
                    return n;
                }
            }
        }
        return nodeToAdd;
    }

    private String grabBoundary(EastNorthBound bbox) throws IOException, OsmTransferException {
        try {
            URL url = null;
            url = getURLsvg(bbox);
            return grabSVG(url);
        } catch (MalformedURLException e) {
            throw (IOException) new IOException(tr("CadastreGrabber: Illegal url.")).initCause(e);
        }
    }

    private URL getURLsvg(EastNorthBound bbox) throws MalformedURLException {
        String str = new String(wmsInterface.baseURL+"/scpc/wms?version=1.1&request=GetMap");
        str += "&layers=";
        str += "CDIF:LS2";
        str += "&format=image/svg";
        str += "&bbox="+bbox.min.east()+",";
        str += bbox.min.north() + ",";
        str += bbox.max.east() + ",";
        str += bbox.max.north();
        str += "&width=800&height=600"; // maximum allowed by wms server
        str += "&exception=application/vnd.ogc.se_inimage";
        str += "&styles=";
        str += "LS2_90";
        System.out.println("URL="+str);
        return new URL(str.replace(" ", "%20"));
    }

    private String grabSVG(URL url) throws IOException, OsmTransferException {
        wmsInterface.urlConn = (HttpURLConnection)url.openConnection();
        wmsInterface.urlConn.setRequestMethod("GET");
        wmsInterface.setCookie();
        InputStream is = new ProgressInputStream(wmsInterface.urlConn, NullProgressMonitor.INSTANCE);
        File file = new File(CadastrePlugin.cacheDir + "building.svg");
        String svg = new String();
        try {
            if (file.exists())
                file.delete();
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file, true));
            InputStreamReader isr =new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line="";
            while ( null!=(line=br.readLine())){
                line += "\n";
                bos.write(line.getBytes());
                svg += line;
            }
            bos.close();
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
        is.close();
        return svg;
    }

    public static void download(WMSLayer wmsLayer) {
        MapView mv = Main.map.mapView;
        currentView = new EastNorthBound(mv.getEastNorth(0, mv.getHeight()),
                mv.getEastNorth(mv.getWidth(), 0));
        if ((currentView.max.east() - currentView.min.east()) > 1000 ||
                (currentView.max.north() - currentView.min.north() > 1000)) {
            JOptionPane.showMessageDialog(Main.parent,
                    tr("To avoid cadastre WMS overload,\nbuilding import size is limited to 1 km2 max."));
            return;
        }
        if (CadastrePlugin.autoSourcing == false) {
            JOptionPane.showMessageDialog(Main.parent,
                    tr("Please, enable auto-sourcing and check cadastre millesime."));
            return;
        }
        Main.worker.execute(new DownloadSVGBuilding(wmsLayer));
    }

}

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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.io.ProgressInputStream;
/**
 * Grab the SVG administrative boundaries of the active commune layer (cadastre),
 * isolate the SVG path of the concerned commune (other municipalities are also
 * downloaded in the SVG data), convert to OSM nodes and way plus simplify.
 * Thanks to Frederic Rodrigo for his help.
 */
public class DownloadSVGTask extends PleaseWaitRunnable {

    private WMSLayer wmsLayer;
    private CadastreGrabber grabber = CadastrePlugin.cadastreGrabber;
    private CadastreInterface wmsInterface;
    private String svg = null;
    private EastNorthBound viewBox = null;

    public DownloadSVGTask(WMSLayer wmsLayer) {
        super(tr("Downloading {0}", wmsLayer.getName()));

        this.wmsLayer = wmsLayer;
        this.wmsInterface = grabber.getWmsInterface();
    }

    @Override
    public void realRun() throws IOException, OsmTransferException {
    	progressMonitor.indeterminateSubTask(tr("Contacting WMS Server..."));
        try {
            if (wmsInterface.retrieveInterface(wmsLayer)) {
                svg = grabBoundary(wmsLayer.getCommuneBBox());
                if (svg == null)
                    return;
                progressMonitor.indeterminateSubTask(tr("Extract SVG ViewBox..."));
                getViewBox(svg);
                if (viewBox == null)
                    return;
                progressMonitor.indeterminateSubTask(tr("Extract best fitting boundary..."));
                createWay(svg);
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
    private void createWay(String svg) {
        String[] SVGpaths = new SVGParser().getClosedPaths(svg);
        ArrayList<Double> fitViewBox = new ArrayList<Double>();
        ArrayList<ArrayList<EastNorth>> eastNorths = new ArrayList<ArrayList<EastNorth>>();
        for (int i=0; i< SVGpaths.length; i++) {
            ArrayList<EastNorth> eastNorth = new ArrayList<EastNorth>();
            fitViewBox.add( createNodes(SVGpaths[i], eastNorth) );
            eastNorths.add(eastNorth);
        }
        // the smallest fitViewBox indicates the best fitting path in viewBox
        Double min = Collections.min(fitViewBox);
        int bestPath = fitViewBox.indexOf(min);
        List<Node> nodeList = new ArrayList<Node>();
        for (EastNorth eastNorth : eastNorths.get(bestPath)) {
            nodeList.add(new Node(Main.proj.eastNorth2latlon(eastNorth)));
        }
        Way wayToAdd = new Way();
        Collection<Command> cmds = new LinkedList<Command>();
        for (Node node : nodeList) {
            cmds.add(new AddCommand(node));
            wayToAdd.addNode(node);
        }
        wayToAdd.addNode(wayToAdd.getNode(0)); // close the circle

        // simplify the way
        double threshold = Double.parseDouble(Main.pref.get("cadastrewms.simplify-way-boundary", "1.0"));
        new SimplifyWay().simplifyWay(wayToAdd, Main.main.getCurrentDataSet(), threshold);

        cmds.add(new AddCommand(wayToAdd));
        Main.main.undoRedo.add(new SequenceCommand(tr("Create boundary"), cmds));
        Main.map.repaint();
    }

    private double createNodes(String SVGpath, ArrayList<EastNorth> eastNorth) {
        // looks like "M981283.38 368690.15l143.81 72.46 155.86 ..."
        String[] coor = SVGpath.split("[MlZ ]"); //coor[1] is x, coor[2] is y
        double dx = Double.parseDouble(coor[1]);
        double dy = Double.parseDouble(coor[2]);
        double minY = Double.MAX_VALUE;
        double minX = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        double maxX = Double.MIN_VALUE;
        for (int i=3; i<coor.length; i+=2){
            double east = dx+=Double.parseDouble(coor[i]);
            double north = dy+=Double.parseDouble(coor[i+1]);
            eastNorth.add(new EastNorth(east,north));
            minX = minX > east ? east : minX;
            minY = minY > north ? north : minY;
            maxX = maxX < east ? east : maxX;
            maxY = maxY < north ? north : maxY;
        }
        // flip the image (svg using a reversed Y coordinate system)
        double pivot = viewBox.min.getY() + (viewBox.max.getY() - viewBox.min.getY()) / 2;
        for (EastNorth en : eastNorth) {
            en.setLocation(en.east(), 2 * pivot - en.north());
        }
        return Math.abs(minX - viewBox.min.getX())+Math.abs(maxX - viewBox.max.getX())
        +Math.abs(minY - viewBox.min.getY())+Math.abs(maxY - viewBox.max.getY());
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
        str += "CDIF:COMMUNE";
        str += "&format=image/svg";
        str += "&bbox="+bbox.min.east()+",";
        str += bbox.min.north() + ",";
        str += bbox.max.east() + ",";
        str += bbox.max.north();
        str += "&width=800&height=600"; // maximum allowed by wms server
        str += "&styles=";
        str += "COMMUNE_90";
        System.out.println("URL="+str);
        return new URL(str.replace(" ", "%20"));
    }

    private String grabSVG(URL url) throws IOException, OsmTransferException {
        wmsInterface.urlConn = (HttpURLConnection)url.openConnection();
        wmsInterface.urlConn.setRequestMethod("GET");
        wmsInterface.setCookie();
        InputStream is = new ProgressInputStream(wmsInterface.urlConn, NullProgressMonitor.INSTANCE);
        File file = new File(CadastrePlugin.cacheDir + "boundary.svg");
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
        if (CadastrePlugin.autoSourcing == false) {
            JOptionPane.showMessageDialog(Main.parent,
                    tr("Please, enable auto-sourcing and check cadastre millesime."));
            return;
        }
        Main.worker.execute(new DownloadSVGTask(wmsLayer));
    }

}

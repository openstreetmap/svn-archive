package org.openstreetmap.josm.plugins.remotecontrol;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.AutoScaleAction;
import org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.download.DownloadDialog.DownloadTask;
import org.openstreetmap.josm.gui.progress.PleaseWaitProgressMonitor;

/**
 * Processes HTTP "remote control" requests.
 */
public class RequestProcessor extends Thread
{
    /** The socket this processor listens on */
    private Socket request;

    private class AlreadyLoadedException extends Exception {};
    private class LoadDeniedException extends Exception {};

    /**
     * Constructor
     *
     * @param request
     */
    public RequestProcessor(Socket request)
    {
        super("RemoteControl request processor");
        this.setDaemon(true);
        this.request = request;
    }

    /**
     * Spawns a new thread for the request
     *
     * @param request The WMS request
     */
    public static void processRequest(Socket request)
    {
        RequestProcessor processor = new RequestProcessor(request);
        processor.start();
    }

    /**
     * The work is done here.
     */
    public void run()
    {
        Writer out = null;
        try
        {
            OutputStream raw = new BufferedOutputStream( request.getOutputStream());
            out = new OutputStreamWriter(raw);
            Reader in = new InputStreamReader(new BufferedInputStream(request.getInputStream()), "ASCII");

            StringBuffer requestLine = new StringBuffer();
            while (requestLine.length() < 1024)
            {
                int c = in.read();
                if (c == '\r' || c == '\n') break;
                requestLine.append((char) c);
            }

            System.out.println("RemoteControl received: " + requestLine);
            String get = requestLine.toString();
            StringTokenizer st = new StringTokenizer(get);
            if (!st.hasMoreTokens()) { sendError(out); return; }
            String method = st.nextToken();
            if (!st.hasMoreTokens()) { sendError(out); return; }
            String url = st.nextToken();

            if (!method.equals("GET")) {
                sendNotImplemented(out);
                return;
            }

            st = new StringTokenizer(url, "&?");
            String command = null;
            HashMap<String,String> args = new HashMap<String,String>();
            while (st.hasMoreTokens())
            {
                String param = st.nextToken();
                if (command == null) {
                    command = param;
                } else {
                    int eq = param.indexOf("=");
                    if (eq>-1) args.put(param.substring(0,eq), param.substring(eq+1));
                }
            }

            if (command.equals("/load_and_zoom")) {
                if (Main.pref.getBoolean("remotecontrol.always-confirm", false)) {
                    if (JOptionPane.showConfirmDialog(Main.parent,
                        "<html>" + tr("Remote Control has been asked to load data from the API.") +
                        "<br>" + tr("Request details: {0}", url) + "<br>" + tr("Do you want to allow this?"),
                        tr("Confirm Remote Control action"),
                        JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                            sendForbidden(out);
                            return;
                    }
                }
                DownloadTask osmTask = new DownloadOsmTask();
                if (!(args.containsKey("bottom") && args.containsKey("top") &&
                    args.containsKey("left") && args.containsKey("right"))) {
                    sendBadRequest(out);
                    System.out.println("load_and_zoom remote control request must have bottom,top,left,right parameters");
                    return;
                }
                double minlat = 0;
                double maxlat = 0;
                double minlon = 0;
                double maxlon = 0;
                try {
                    minlat = Double.parseDouble(args.get("bottom"));
                    maxlat = Double.parseDouble(args.get("top"));
                    minlon = Double.parseDouble(args.get("left"));
                    maxlon = Double.parseDouble(args.get("right"));

                    if (!Main.pref.getBoolean("remotecontrol.permission.load-data", true))
                        throw new LoadDeniedException();

                    // find out whether some data has already been downloaded
                    Area present = null;
                    DataSet ds = Main.main.getCurrentDataSet();
                    if (ds != null)
                        present = ds.getDataSourceArea();
                    if (present != null && !present.isEmpty()) {
                        Area toDownload = new Area(new Rectangle2D.Double(minlon,minlat,maxlon-minlon,maxlat-minlat));
                        toDownload.subtract(present);
                        if (toDownload.isEmpty()) throw new AlreadyLoadedException();
                        // the result might not be a rectangle (L shaped etc)
                        Rectangle2D downloadBounds = toDownload.getBounds2D();
                        minlat = downloadBounds.getMinY();
                        minlon = downloadBounds.getMinX();
                        maxlat = downloadBounds.getMaxY();
                        maxlon = downloadBounds.getMaxX();
                    }
                    osmTask.download(null, minlat,minlon,maxlat,maxlon, new PleaseWaitProgressMonitor());
                } catch (AlreadyLoadedException ex) {
                    System.out.println("RemoteControl: no download necessary");
                } catch (LoadDeniedException ex) {
                    System.out.println("RemoteControl: download forbidden by preferences");
                } catch (Exception ex) {
                    sendError(out);
                    System.out.println("RemoteControl: Error parsing load_and_zoom remote control request:");
                    ex.printStackTrace();
                    return;
                }
                if (args.containsKey("select") && Main.pref.getBoolean("remotecontrol.permission.change-selection", true)) {
                    // select objects after downloading, zoom to selection.
                    final String selection = args.get("select");
                    Main.worker.execute(new Runnable() {
                        public void run() {
                            HashSet<Long> ways = new HashSet<Long>();
                            HashSet<Long> nodes = new HashSet<Long>();
                            HashSet<Long> relations = new HashSet<Long>();
                            HashSet<OsmPrimitive> newSel = new HashSet<OsmPrimitive>();
                            for (String item : selection.split(",")) {
                                if (item.startsWith("way")) {
                                    ways.add(Long.parseLong(item.substring(3)));
                                } else if (item.startsWith("node")) {
                                    nodes.add(Long.parseLong(item.substring(4)));
                                } else if (item.startsWith("relation")) {
                                    relations.add(Long.parseLong(item.substring(8)));
                                } else {
                                    System.out.println("RemoteControl: invalid selection '"+item+"' ignored");
                                }
                            }
                            for (Way w : Main.main.getCurrentDataSet().ways) if (ways.contains(w.getId())) newSel.add(w);
                            for (Node n : Main.main.getCurrentDataSet().nodes) if (nodes.contains(n.getId())) newSel.add(n);
                            for (Relation r : Main.main.getCurrentDataSet().relations) if (relations.contains(r.getId())) newSel.add(r);
                            Main.main.getCurrentDataSet().setSelected(newSel);
                            if (Main.pref.getBoolean("remotecontrol.permission.change-viewport", true))
                                new AutoScaleAction("selection").actionPerformed(null);
                        }
                    });
                } else if (Main.pref.getBoolean("remotecontrol.permission.change-viewport", true)) {
                    // after downloading, zoom to downloaded area.
                    final Bounds bounds = new Bounds(new LatLon(minlat, minlon),
                        new LatLon(maxlat, maxlon));

                    Main.worker.execute(new Runnable() {
                        public void run() {
                            BoundingXYVisitor bbox = new BoundingXYVisitor();
                            bbox.visit(bounds);
                            Main.map.mapView.recalculateCenterScale(bbox);
                        }
                    });
                }
            } else if (command.equals("/add_node")) {
                if (!Main.pref.getBoolean("remotecontrol.permission.create-objects", true)) {
                    sendForbidden(out);
                    return;
                }
                if (Main.pref.getBoolean("remotecontrol.always-confirm", false)) {
                    if (JOptionPane.showConfirmDialog(Main.parent,
                        "<html>" + tr("Remote Control has been asked to create a new node.") +
                        "<br>" + tr("Do you want to allow this?"),
                        tr("Confirm Remote Control action"),
                        JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                            sendForbidden(out);
                            return;
                    }
                }
                addNode(args, out);
            } else if (command.equals("/import")) {
                if (Main.pref.getBoolean("remotecontrol.always-confirm", false)) {
                    if (JOptionPane.showConfirmDialog(Main.parent,
                        "<html>" + tr("Remote Control has been asked to import data from the following URL:") +
                        "<br>" + url +
                        "<br>" + tr("Do you want to allow this?"),
                        tr("Confirm Remote Control action"),
                        JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                            sendForbidden(out);
                            return;
                    }
                }
                if (!(args.containsKey("url"))) {
                    sendBadRequest(out);
                    System.out.println("'import' remote control request must have url parameter");
                    return;
                }
                try {
                    if (!Main.pref.getBoolean("remotecontrol.permission.import", true))
                        throw new LoadDeniedException();

                    DownloadTask osmTask = new DownloadOsmTask();
                    osmTask.loadUrl(false, URLDecoder.decode(args.get("url"), "UTF-8"), null);
                } catch (LoadDeniedException ex) {
                    System.out.println("RemoteControl: import forbidden by preferences");
                } catch (Exception ex) {
                    sendError(out);
                    System.out.println("RemoteControl: Error parsing import remote control request:");
                    ex.printStackTrace();
                    return;
                }
                // TODO: select/zoom to downloaded
            }
            sendHeader(out, "200 OK", "text/plain", false);
            out.write("Content-length: 4\r\n");
            out.write("\r\n");
            out.write("OK\r\n");
            out.flush();
        }
        catch (IOException ioe) { }
        catch(Exception e) {
            e.printStackTrace();
            try {
                sendError(out);
            } catch (IOException e1) { }
        } finally {
            try {
                request.close();
            } catch (IOException e) {}
        }
    }

    /**
     * Adds a node, reacts to the GET /add_node?lon=...&amp;lat=... request.
     * @param args
     * @param out 
     * @throws IOException 
     */
    private void addNode(HashMap<String, String> args, Writer out) throws IOException {
        if(!args.containsKey("lat") || !args.containsKey("lon")) {
            sendBadRequest(out);
            return;
        }
        
        // Parse the arguments
        double lat = Double.parseDouble(args.get("lat"));
        double lon = Double.parseDouble(args.get("lon"));
        System.out.println("Adding node at (" + lat + ", " + lon + ")");
        
        // Create a new node
        LatLon ll = new LatLon(lat, lon);
        Node nnew = new Node(ll);
        
        // Now execute the commands to add this node.
        Main.main.undoRedo.add(new AddCommand(nnew));
        Main.main.getCurrentDataSet().setSelected(nnew);
        Main.map.mapView.repaint();
        
    }

    /**
     * Sends a 500 error: server error
     * @param out The writer where the error is written
     * @throws IOException If the error can not be written
     */
    private void sendError(Writer out) throws IOException
    {
        sendHeader(out, "500 Internal Server Error", "text/html", true);
        out.write("<HTML>\r\n");
        out.write("<HEAD><TITLE>Internal Error</TITLE>\r\n");
        out.write("</HEAD>\r\n");
        out.write("<BODY>");
        out.write("<H1>HTTP Error 500: Internal Server Error</h2>\r\n");
        out.write("</BODY></HTML>\r\n");
        out.flush();
    }

    /**
     * Sends a 501 error: not implemented
     * @param out The writer where the error is written
     * @throws IOException If the error can not be written
     */
    private void sendNotImplemented(Writer out) throws IOException
    {
        sendHeader(out, "501 Not Implemented", "text/html", true);
        out.write("<HTML>\r\n");
        out.write("<HEAD><TITLE>Not Implemented</TITLE>\r\n");
        out.write("</HEAD>\r\n");
        out.write("<BODY>");
        out.write("<H1>HTTP Error 501: Not Implemented</h2>\r\n");
        out.write("</BODY></HTML>\r\n");
        out.flush();
    }

    /**
     * Sends a 403 error: forbidden
     * @param out The writer where the error is written
     * @throws IOException If the error can not be written
     */
    private void sendForbidden(Writer out) throws IOException
    {
        sendHeader(out, "403 Forbidden", "text/html", true);
        out.write("<HTML>\r\n");
        out.write("<HEAD><TITLE>Forbidden</TITLE>\r\n");
        out.write("</HEAD>\r\n");
        out.write("<BODY>");
        out.write("<H1>HTTP Error 403: Forbidden</h2>\r\n");
        out.write("</BODY></HTML>\r\n");
        out.flush();
    }
    /**
     * Sends a 403 error: forbidden
     * @param out The writer where the error is written
     * @throws IOException If the error can not be written
     */
    private void sendBadRequest(Writer out) throws IOException
    {
        sendHeader(out, "400 Bad Request", "text/html", true);
        out.write("<HTML>\r\n");
        out.write("<HEAD><TITLE>Bad Request</TITLE>\r\n");
        out.write("</HEAD>\r\n");
        out.write("<BODY>");
        out.write("<H1>HTTP Error 400: Bad Request</h2>\r\n");
        out.write("</BODY></HTML>\r\n");
        out.flush();
    }

    /**
     * Send common HTTP headers to the client.
     *
     * @param out The Writer
     * @param status The status string ("200 OK", "500", etc)
     * @param contentType The content type of the data sent
     * @param endHeaders If true, adds a new line, ending the headers.
     * @throws IOException When error
     */
    private void sendHeader(Writer out, String status, String contentType, boolean endHeaders) throws IOException
    {
        out.write("HTTP/1.1 " + status + "\r\n");
        Date now = new Date();
        out.write("Date: " + now + "\r\n");
        out.write("Server: JOSM RemoteControl\r\n");
        out.write("Content-type: " + contentType + "\r\n");
        if (endHeaders)
            out.write("\r\n");
    }
}

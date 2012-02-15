package org.openstreetmap.josm.plugins.lakewalker;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;
import org.xml.sax.SAXException;

/**
 * Interface to Darryl Shpak's Lakewalker module
 *
 * @author Brent Easton
 */
class LakewalkerAction extends JosmAction implements MouseListener {

    private static final long serialVersionUID = 1L;
    protected String name;
    protected Cursor oldCursor;
    protected Thread executeThread;
    protected boolean cancel;
    protected boolean active = false;

    protected Collection<Command> commands = new LinkedList<Command>();
    protected Collection<Way> ways = new ArrayList<Way>();

    public LakewalkerAction(String name) {
        super(name, "lakewalker-sml", tr("Lake Walker."),
        Shortcut.registerShortcut("tools:lakewalker", tr("Tool: {0}", tr("Lake Walker")),
        KeyEvent.VK_L, Shortcut.GROUP_DIRECT2+Shortcut.GROUPS_ALT2), true);
        this.name = name;
        setEnabled(true);
    }

    public void actionPerformed(ActionEvent e) {
        if(Main.map == null || Main.map.mapView == null || active)
            return;

        active = true;
        oldCursor = Main.map.mapView.getCursor();
        Main.map.mapView.setCursor(ImageProvider.getCursor("crosshair", "lakewalker-sml"));
        Main.map.mapView.addMouseListener(this);
    }

    /**
    * check for presence of cache folder and trim cache to specified size.
    * size/age limit is on a per folder basis.
    */
    private void cleanupCache() {
        final long maxCacheAge = System.currentTimeMillis()-Main.pref.getInteger(LakewalkerPreferences.PREF_MAXCACHEAGE, 100)*24*60*60*1000L;
        final long maxCacheSize = Main.pref.getInteger(LakewalkerPreferences.PREF_MAXCACHESIZE, 300)*1024*1024L;

        for (String wmsFolder : LakewalkerPreferences.WMSLAYERS) {
            String wmsCacheDirName = Main.pref.getPreferencesDir()+"plugins/Lakewalker/"+wmsFolder;
            File wmsCacheDir = new File(wmsCacheDirName);

            if (wmsCacheDir.exists() && wmsCacheDir.isDirectory()) {
                File wmsCache[] = wmsCacheDir.listFiles();

                // sort files by date (most recent first)
                Arrays.sort(wmsCache, new Comparator<File>() {
                    public int compare(File f1, File f2) {
                        return (int)(f2.lastModified()-f1.lastModified());
                    }
                });

                // delete aged or oversized, keep newest. Once size/age limit was reached delete all older files
                long folderSize = 0;
                boolean quickdelete = false;
                for (File cacheEntry : wmsCache) {
                    if (!cacheEntry.isFile()) continue;
                    if (!quickdelete) {
                        folderSize += cacheEntry.length();
                        if (folderSize > maxCacheSize) {
                            quickdelete = true;
                        } else if (cacheEntry.lastModified() < maxCacheAge) {
                            quickdelete = true;
                        }
                    }

                    if (quickdelete) {
                        cacheEntry.delete();
                    }
                }

            } else {
                // create cache directory
                if (!wmsCacheDir.mkdirs()) {
                    JOptionPane.showMessageDialog(Main.parent, tr("Error creating cache directory: {0}", wmsCacheDirName));
                }
            }
        }
    }

    protected void lakewalk(Point clickPoint){
        /**
        * Positional data
        */
        final LatLon pos = Main.map.mapView.getLatLon(clickPoint.x, clickPoint.y);
        final LatLon topLeft = Main.map.mapView.getLatLon(0, 0);
        final LatLon botRight = Main.map.mapView.getLatLon(Main.map.mapView.getWidth(),
            Main.map.mapView.getHeight());

        /**
        * Cache/working directory location
        */
        final File working_dir = new File(Main.pref.getPreferencesDir(), "plugins/Lakewalker");

        /*
        * Collect options
        */
        final int waylen = Main.pref.getInteger(LakewalkerPreferences.PREF_MAX_SEG, 500);
        final int maxnode = Main.pref.getInteger(LakewalkerPreferences.PREF_MAX_NODES, 50000);
        final int threshold = Main.pref.getInteger(LakewalkerPreferences.PREF_THRESHOLD_VALUE, 90);
        final double epsilon = Main.pref.getDouble(LakewalkerPreferences.PREF_EPSILON, 0.0003);
        final int resolution = Main.pref.getInteger(LakewalkerPreferences.PREF_LANDSAT_RES, 4000);
        final int tilesize = Main.pref.getInteger(LakewalkerPreferences.PREF_LANDSAT_SIZE, 2000);
        final String startdir = Main.pref.get(LakewalkerPreferences.PREF_START_DIR, "east");
        final String wmslayer = Main.pref.get(LakewalkerPreferences.PREF_WMS, "IR1");

        try {
            PleaseWaitRunnable lakewalkerTask = new PleaseWaitRunnable(tr("Tracing")){
                @Override protected void realRun() throws SAXException {
                    progressMonitor.subTask(tr("checking cache..."));
                    cleanupCache();
                    processnodelist(pos, topLeft, botRight, waylen, maxnode, threshold,
                            epsilon,resolution,tilesize,startdir,wmslayer, working_dir,
                            progressMonitor.createSubTaskMonitor(ProgressMonitor.ALL_TICKS, false));
                }
                @Override protected void finish() {
                }

                @Override protected void cancel() {
                    LakewalkerAction.this.cancel();
                }
            };
            Thread executeThread = new Thread(lakewalkerTask);
            executeThread.start();
        }
        catch (Exception ex) {
          System.out.println("Exception caught: " + ex.getMessage());
        }
    }

    private void processnodelist(LatLon pos, LatLon topLeft, LatLon botRight, int waylen, int maxnode, int threshold, double epsilon, int resolution, int tilesize, String startdir, String wmslayer, File workingdir, ProgressMonitor progressMonitor){
        progressMonitor.beginTask(null, 3);
        try {
            ArrayList<double[]> nodelist = new ArrayList<double[]>();

            Lakewalker lw = new Lakewalker(waylen,maxnode,threshold,epsilon,resolution,tilesize,startdir,wmslayer,workingdir);
            try {
                nodelist = lw.trace(pos.lat(),pos.lon(),topLeft.lon(),botRight.lon(),topLeft.lat(),botRight.lat(),
                        progressMonitor.createSubTaskMonitor(1, false));
            } catch(LakewalkerException e){
                System.out.println(e.getMessage());
            }

            System.out.println(nodelist.size()+" nodes generated");

            /**
            * Run the nodelist through a vertex reduction algorithm
            */

            progressMonitor.subTask(tr("Running vertex reduction..."));

            nodelist = lw.vertexReduce(nodelist, epsilon);

            //System.out.println("After vertex reduction "+nodelist.size()+" nodes remain.");

            /**
            * And then through douglas-peucker approximation
            */

            progressMonitor.worked(1);
            progressMonitor.subTask(tr("Running Douglas-Peucker approximation..."));

            nodelist = lw.douglasPeucker(nodelist, epsilon, 0);

            //System.out.println("After Douglas-Peucker approximation "+nodelist.size()+" nodes remain.");

            /**
            * And then through a duplicate node remover
            */

            progressMonitor.worked(1);
            progressMonitor.subTask(tr("Removing duplicate nodes..."));

            nodelist = lw.duplicateNodeRemove(nodelist);

            //System.out.println("After removing duplicate nodes, "+nodelist.size()+" nodes remain.");


            // if for some reason (image loading failed, ...) nodelist is empty, no more processing required.
            if (nodelist.size() == 0) {
                return;
            }

            /**
            * Turn the arraylist into osm nodes
            */

            Way way = new Way();
            Node n = null;
            Node fn = null;

            double eastOffset = Main.pref.getDouble(LakewalkerPreferences.PREF_EAST_OFFSET, 0.0);
            double northOffset = Main.pref.getDouble(LakewalkerPreferences.PREF_NORTH_OFFSET, 0.0);

            int nodesinway = 0;

            for(int i = 0; i< nodelist.size(); i++){
                if (cancel) {
                    return;
                }

                try {
                    LatLon ll = new LatLon(nodelist.get(i)[0]+northOffset, nodelist.get(i)[1]+eastOffset);
                    n = new Node(ll);
                    if(fn==null){
                        fn = n;
                    }
                    commands.add(new AddCommand(n));

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                way.addNode(n);

                if(nodesinway > Main.pref.getInteger(LakewalkerPreferences.PREF_MAX_SEG, 500)){
                    String waytype = Main.pref.get(LakewalkerPreferences.PREF_WAYTYPE, "water");

                    if(!waytype.equals("none")){
                        way.put("natural",waytype);
                    }

                    way.put("source", Main.pref.get(LakewalkerPreferences.PREF_SOURCE, "Landsat"));
                    commands.add(new AddCommand(way));

                    way = new Way();

                    way.addNode(n);

                    nodesinway = 0;
                }
                nodesinway++;
            }


            String waytype = Main.pref.get(LakewalkerPreferences.PREF_WAYTYPE, "water");

            if(!waytype.equals("none")){
                way.put("natural",waytype);
            }

            way.put("source", Main.pref.get(LakewalkerPreferences.PREF_SOURCE, "Landsat"));

            way.addNode(fn);

            commands.add(new AddCommand(way));

            if (!commands.isEmpty()) {
                Main.main.undoRedo.add(new SequenceCommand(tr("Lakewalker trace"), commands));
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
        if(active)
        {
            active = false;
            Main.map.mapView.removeMouseListener(this);
            Main.map.mapView.setCursor(oldCursor);
            lakewalk(e.getPoint());
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }
}

// License: GPL. For details, see LICENSE file.
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
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Logging;
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

    protected Collection<Command> commands = new LinkedList<>();
    protected Collection<Way> ways = new ArrayList<>();

    LakewalkerAction(String name) {
        super(name, "lakewalker-sml", tr("Lake Walker."),
        Shortcut.registerShortcut("tools:lakewalker", tr("Tool: {0}", tr("Lake Walker")),
        KeyEvent.VK_L, Shortcut.ALT_CTRL_SHIFT), true);
        this.name = name;
        setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!MainApplication.isDisplayingMapView() || active)
            return;

        active = true;
        MapView mapView = MainApplication.getMap().mapView;
        oldCursor = mapView.getCursor();
        mapView.setCursor(ImageProvider.getCursor("crosshair", "lakewalker-sml"));
        mapView.addMouseListener(this);
    }

    /**
    * check for presence of cache folder and trim cache to specified size.
    * size/age limit is on a per folder basis.
    */
    private void cleanupCache() {
        final long maxCacheAge = System.currentTimeMillis()-Config.getPref().getInt(LakewalkerPreferences.PREF_MAXCACHEAGE, 100)*24*60*60*1000L;
        final long maxCacheSize = Config.getPref().getInt(LakewalkerPreferences.PREF_MAXCACHESIZE, 300)*1024*1024L;

        for (String wmsFolder : LakewalkerPreferences.WMSLAYERS) {
            File wmsCacheDir = new File(LakewalkerPlugin.getLakewalkerCacheDir(), wmsFolder);

            if (wmsCacheDir.exists() && wmsCacheDir.isDirectory()) {
                File[] wmsCache = wmsCacheDir.listFiles();

                // sort files by date (most recent first)
                Arrays.sort(wmsCache, Comparator.comparingLong(File::lastModified));

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
                    JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                            tr("Error creating cache directory: {0}", wmsCacheDir.getPath()));
                }
            }
        }
    }

    protected void lakewalk(Point clickPoint) {
        // Positional data
        MapView mapView = MainApplication.getMap().mapView;
        final LatLon pos = mapView.getLatLon(clickPoint.x, clickPoint.y);
        final LatLon topLeft = mapView.getLatLon(0, 0);
        final LatLon botRight = mapView.getLatLon(mapView.getWidth(), mapView.getHeight());

        /*
        * Collect options
        */
        final int waylen = Config.getPref().getInt(LakewalkerPreferences.PREF_MAX_SEG, 500);
        final int maxnode = Config.getPref().getInt(LakewalkerPreferences.PREF_MAX_NODES, 50000);
        final int threshold = Config.getPref().getInt(LakewalkerPreferences.PREF_THRESHOLD_VALUE, 90);
        final double epsilon = Config.getPref().getDouble(LakewalkerPreferences.PREF_EPSILON, 0.0003);
        final int resolution = Config.getPref().getInt(LakewalkerPreferences.PREF_LANDSAT_RES, 4000);
        final int tilesize = Config.getPref().getInt(LakewalkerPreferences.PREF_LANDSAT_SIZE, 2000);
        final String startdir = Config.getPref().get(LakewalkerPreferences.PREF_START_DIR, "east");
        final String wmslayer = Config.getPref().get(LakewalkerPreferences.PREF_WMS, "IR1");

        try {
            PleaseWaitRunnable lakewalkerTask = new PleaseWaitRunnable(tr("Tracing")) {
                @Override protected void realRun() throws SAXException {
                    progressMonitor.subTask(tr("checking cache..."));
                    cleanupCache();
                    processnodelist(new Lakewalker(waylen, maxnode, threshold, epsilon, resolution, tilesize, startdir, wmslayer),
                            pos, topLeft, botRight, epsilon, progressMonitor.createSubTaskMonitor(ProgressMonitor.ALL_TICKS, false));
                }

                @Override protected void finish() {
                }

                @Override protected void cancel() {
                    LakewalkerAction.this.cancel();
                }
            };
            new Thread(lakewalkerTask).start();
        } catch (Exception ex) {
            Logging.error(ex);
        }
    }

    private void processnodelist(Lakewalker lw, LatLon pos, LatLon topLeft, LatLon botRight, double epsilon, ProgressMonitor progressMonitor) {
        progressMonitor.beginTask(null, 3);
        try {
            List<double[]> nodelist = new ArrayList<>();

            try {
                nodelist = lw.trace(pos.lat(), pos.lon(), topLeft.lon(), botRight.lon(), topLeft.lat(), botRight.lat(),
                        progressMonitor.createSubTaskMonitor(1, false));
            } catch (LakewalkerException e) {
                Logging.error(e);
            }

            // Run the nodelist through a vertex reduction algorithm

            progressMonitor.subTask(tr("Running vertex reduction..."));

            nodelist = lw.vertexReduce(nodelist, epsilon);

            // And then through douglas-peucker approximation

            progressMonitor.worked(1);
            progressMonitor.subTask(tr("Running Douglas-Peucker approximation..."));

            nodelist = lw.douglasPeucker(nodelist, epsilon, 0);

            // And then through a duplicate node remover

            progressMonitor.worked(1);
            progressMonitor.subTask(tr("Removing duplicate nodes..."));

            nodelist = lw.duplicateNodeRemove(nodelist);

            // if for some reason (image loading failed, ...) nodelist is empty, no more processing required.
            if (nodelist.isEmpty()) {
                return;
            }

            // Turn the arraylist into osm nodes

            Way way = new Way();
            Node n = null;
            Node fn = null;

            double eastOffset = Config.getPref().getDouble(LakewalkerPreferences.PREF_EAST_OFFSET, 0.0);
            double northOffset = Config.getPref().getDouble(LakewalkerPreferences.PREF_NORTH_OFFSET, 0.0);

            int nodesinway = 0;
            DataSet ds = getLayerManager().getEditDataSet();

            for (int i = 0; i < nodelist.size(); i++) {
                if (cancel) {
                    return;
                }

                try {
                    LatLon ll = new LatLon(nodelist.get(i)[0]+northOffset, nodelist.get(i)[1]+eastOffset);
                    n = new Node(ll);
                    if (fn == null) {
                        fn = n;
                    }
                    commands.add(new AddCommand(ds, n));

                } catch (Exception ex) {
                    Logging.error(ex);
                }

                way.addNode(n);

                if (nodesinway > Config.getPref().getInt(LakewalkerPreferences.PREF_MAX_SEG, 500)) {
                    String waytype = Config.getPref().get(LakewalkerPreferences.PREF_WAYTYPE, "water");

                    if (!waytype.equals("none")) {
                        way.put("natural", waytype);
                    }

                    way.put("source", Config.getPref().get(LakewalkerPreferences.PREF_SOURCE, "Landsat"));
                    commands.add(new AddCommand(ds, way));

                    way = new Way();

                    way.addNode(n);

                    nodesinway = 0;
                }
                nodesinway++;
            }


            String waytype = Config.getPref().get(LakewalkerPreferences.PREF_WAYTYPE, "water");

            if (!waytype.equals("none")) {
                way.put("natural", waytype);
            }

            way.put("source", Config.getPref().get(LakewalkerPreferences.PREF_SOURCE, "Landsat"));

            way.addNode(fn);

            commands.add(new AddCommand(ds, way));

            if (!commands.isEmpty()) {
                UndoRedoHandler.getInstance().add(new SequenceCommand(tr("Lakewalker trace"), commands));
                ds.setSelected(ways);
            } else {
                System.out.println("Failed");
            }

            commands = new LinkedList<>();
            ways = new ArrayList<>();
        } finally {
            progressMonitor.finishTask();
        }
    }

    public void cancel() {
        cancel = true;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (active) {
            active = false;
            MapView mapView = MainApplication.getMap().mapView;
            mapView.removeMouseListener(this);
            mapView.setCursor(oldCursor);
            lakewalk(e.getPoint());
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }
}

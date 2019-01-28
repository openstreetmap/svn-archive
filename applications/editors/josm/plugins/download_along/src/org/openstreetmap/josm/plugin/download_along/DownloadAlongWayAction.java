// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugin.download_along;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.DownloadAlongAction;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.gui.layer.gpx.DownloadAlongPanel;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Shortcut;
import org.openstreetmap.josm.tools.Utils;

/**
 * Calculate area around selected ways and split it into reasonable parts so
 * that they can be downloaded.
 *
 */
class DownloadAlongWayAction extends DownloadAlongAction {

    private static final String PREF_DOWNLOAD_ALONG_WAY_DISTANCE = "downloadAlongWay.distance";
    private static final String PREF_DOWNLOAD_ALONG_WAY_AREA = "downloadAlongWay.area";

    private static final String PREF_DOWNLOAD_ALONG_WAY_OSM = "downloadAlongWay.download.osm";
    private static final String PREF_DOWNLOAD_ALONG_WAY_GPS = "downloadAlongWay.download.gps";

    DownloadAlongWayAction() {
        super(tr("Download along..."), "download_along", tr("Download OSM data along the selected ways."), 
                Shortcut.registerShortcut("tools:download_along", tr("Tool: {0}", tr("Download Along")), 
                        KeyEvent.VK_D, Shortcut.ALT_SHIFT), true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Collection<Way> selectedWays = getLayerManager().getEditDataSet().getSelectedWays();

        if (selectedWays.isEmpty()) {
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("Please select 1 or more ways to download along"));
            return;
        }

        final DownloadAlongPanel panel = new DownloadAlongPanel(
                PREF_DOWNLOAD_ALONG_WAY_OSM, PREF_DOWNLOAD_ALONG_WAY_GPS,
                PREF_DOWNLOAD_ALONG_WAY_DISTANCE, PREF_DOWNLOAD_ALONG_WAY_AREA, null);

        if (0 != panel.showInDownloadDialog(tr("Download from OSM along selected ways"), HelpUtil.ht("/Tools/DownloadAlong"))) {
            return;
        }

        Logging.info("Starting area computation");
        long start = System.currentTimeMillis();

        double scale = calcScale(selectedWays);
        
        /*
         * Compute buffer zone extents and maximum bounding box size. Note
         * that the maximum we ever offer is a bbox area of 0.002, while the
         * API theoretically supports 0.25, but as soon as you touch any
         * built-up area, that kind of bounding box will download forever
         * and then stop because it has more than 50k nodes.
         */
        double bufferDist = panel.getDistance();
        double bufferY = bufferDist / 100000.0;
        double bufferX = bufferY / scale;
        double maxArea = panel.getArea() / 10000.0 / scale;
        Path2D path = new Path2D.Double();
        Rectangle2D r = new Rectangle2D.Double();

        /*
         * Collect the combined area of all points plus buffer zones
         * around them. We ignore points that lie closer to the previous
         * point than the given buffer size because otherwise this operation
         * takes ages.
         */
        for (Way way : selectedWays) {
            LatLon previous = null;
            for (Node p : way.getNodes()) {
                LatLon c = p.getCoor();
                for (LatLon d : calcBetween(previous, c, bufferDist)) {
                    // we add a buffer around the point.
                    r.setRect(d.lon() - bufferX, d.lat() - bufferY, 2 * bufferX, 2 * bufferY);
                    path.append(r, false);
                }
                previous = c;
            }
        }
        Area a = new Area(path);
        
        Logging.info("Area computed in " + Utils.getDurationString(System.currentTimeMillis() - start));
        confirmAndDownloadAreas(a, maxArea, panel.isDownloadOsmData(), panel.isDownloadGpxData(), 
                tr("Download from OSM along selected ways"), NullProgressMonitor.INSTANCE);
    }

    /**
     * Calculate list of points between two given points so that the distance between two consecutive points is below a limit.     
     * @param p1 first point or null 
     * @param p2 second point (must not be null)
     * @param bufferDist the maximum distance 
     * @return a list of points with at least one point (p2) and maybe more.
     */
    private static Collection<? extends LatLon> calcBetween(LatLon p1, LatLon p2, double bufferDist) {
        ArrayList<LatLon> intermediateNodes = new ArrayList<>();
        intermediateNodes.add(p2);
        if (p1 != null && p2.greatCircleDistance(p1) > bufferDist) {
            Double d = p2.greatCircleDistance(p1) / bufferDist;
            int nbNodes = d.intValue();
            if (Logging.isDebugEnabled()) {
                Logging.debug(tr("{0} intermediate nodes to download.", nbNodes));
                Logging.debug(tr("between {0} {1} and {2} {3}", p2.lat(), p2.lon(), p1.lat(), p1.lon()));
            }
            double latStep = (p2.lat() - p1.lat()) / (nbNodes + 1);
            double lonStep = (p2.lon() - p1.lon()) / (nbNodes + 1);
            for (int i = 1; i <= nbNodes; i++) {
                LatLon intermediate = new LatLon(p1.lat() + i * latStep, p1.lon() + i * lonStep);
                intermediateNodes.add(intermediate);
                if (Logging.isTraceEnabled()) {
                    Logging.trace(tr("  adding {0} {1}", intermediate.lat(), intermediate.lon()));
                }
            }
        }
        return intermediateNodes;
    }

    /**
     * Find the average latitude for the data we're contemplating, so we
     * can know how many metres per degree of longitude we have.
     * @param selectedWays collection of ways
     */
    private static double calcScale(Collection<Way> selectedWays) {
        double latsum = 0;
        int latcnt = 0;

        for (Way way : selectedWays) {
            for (Node n : way.getNodes()) {
                latsum += n.getCoor().lat();
                latcnt++;
            }
        }
        if (latcnt > 0) {
            double avglat = latsum / latcnt;
            return Math.cos(Math.toRadians(avglat));
        }
        return 1;
    }

    @Override
    protected void updateEnabledState() {
        if (getLayerManager().getEditDataSet() == null) {
            setEnabled(false);
        } else {
            updateEnabledState(getLayerManager().getEditDataSet().getSelected());
        }
    }

    @Override
    protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        setEnabled(selection.stream().anyMatch(Way.class::isInstance));
    }
}

// License: Public Domain. For details, see LICENSE file.
package livegps;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.spi.preferences.Config;

public class LiveGpsLayer extends GpxLayer implements PropertyChangeListener {
    public static final String LAYER_NAME = tr("LiveGPS layer");

    private static final int DEFAULT_REFRESH_INTERVAL = 250;
    private static final int DEFAULT_CENTER_INTERVAL = 5000;
    private static final int DEFAULT_CENTER_FACTOR = 80;
    private static final String oldC_REFRESH_INTERVAL = "livegps.refreshinterval";     /* in seconds */
    private static final String C_REFRESH_INTERVAL = "livegps.refresh_interval_msec";  /* in msec */
    private static final String C_CENTER_INTERVAL = "livegps.center_interval_msec";  /* in msec */
    private static final String C_CENTER_FACTOR = "livegps.center_factor" /* in percent */;
    private int refreshInterval;
    private int centerInterval;
    private double centerFactor;
    private long lastRedraw = 0;
    private long lastCenter = 0;

    LiveGpsData lastData;
    LatLon lastPos;
    WayPoint lastPoint;
    private final AppendableGpxTrackSegment trackSegment;
    boolean autocenter;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public LiveGpsLayer(GpxData data) {
        super(data, LAYER_NAME);
        trackSegment = new AppendableGpxTrackSegment();

        Map<String, Object> attr = new HashMap<>();
        attr.put("desc", "josm live gps");

        GpxTrack trackBeingWritten = new SingleSegmentGpxTrack(trackSegment, attr);
        data.tracks.add(trackBeingWritten);

        initIntervals();
    }

    @Override
    protected LayerPainter createMapViewPainter(MapViewEvent event) {
        return new LiveGpsDrawHelper(this);
    }

    void setCurrentPosition(double lat, double lon) {
        LatLon thisPos = new LatLon(lat, lon);
        if ((lastPos != null) && (thisPos.equalsEpsilon(lastPos)))
            // no change in position
            // maybe show a "paused" cursor or some such
            return;

        lastPos = thisPos;
        lastPoint = new WayPoint(thisPos);
        lastPoint.attr.put("time", dateFormat.format(new Date()));
        trackSegment.addWaypoint(lastPoint);

        if (autocenter)
            conditionalCenter(thisPos);
    }

    public void center() {
        if (lastPoint != null)
            MainApplication.getMap().mapView.zoomTo(lastPoint.getCoor());
    }

    public void conditionalCenter(LatLon Pos) {
        Point2D P = MainApplication.getMap().mapView.getPoint2D(Pos);
        Rectangle rv = MainApplication.getMap().mapView.getBounds(null);
        Date date = new Date();
        long current = date.getTime();

        rv.grow(-(int) (rv.getHeight() * centerFactor), -(int) (rv.getWidth() * centerFactor));

        if (!rv.contains(P) || (centerInterval > 0 && current - lastCenter >= centerInterval)) {
            MainApplication.getMap().mapView.zoomTo(Pos);
            lastCenter = current;
        }
    }

    public void setAutoCenter(boolean ac) {
        autocenter = ac;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!isVisible()) {
            return;
        }
        if ("gpsdata".equals(evt.getPropertyName())) {
            lastData = (LiveGpsData) evt.getNewValue();
            if (lastData.isFix()) {
                setCurrentPosition(lastData.getLatitude(), lastData.getLongitude());
                if (allowRedraw())
                    this.setFilterStateChanged();
            }
        }
    }

    /**
     * Check, if a redraw is currently allowed.
     *
     * @return true, if a redraw is permitted, false, if a re-draw
     * should be suppressed.
     */
    private boolean allowRedraw() {
        Date date = new Date();
        long current = date.getTime();

        if (current - lastRedraw >= refreshInterval) {
            lastRedraw = current;
            return true;
        } else
            return false;
    }

    /**
     * Retrieve the refreshInterval and centerInterval from the configuration. Be compatible
     * with old version that stored refreshInterval in seconds. If no such configuration key
     * exists, it will be initialized here.
     */
    private void initIntervals() {
        if ((refreshInterval = Config.getPref().getInt(oldC_REFRESH_INTERVAL, 0)) != 0) {
            refreshInterval *= 1000;
            Config.getPref().put(oldC_REFRESH_INTERVAL, null);
        } else
            refreshInterval = Config.getPref().getInt(C_REFRESH_INTERVAL, DEFAULT_REFRESH_INTERVAL);

        centerInterval = Config.getPref().getInt(C_CENTER_INTERVAL, DEFAULT_CENTER_INTERVAL);
        centerFactor = Config.getPref().getInt(C_CENTER_FACTOR, DEFAULT_CENTER_FACTOR);
        if (centerFactor <= 1 || centerFactor >= 99)
            centerFactor = DEFAULT_CENTER_FACTOR;

            Config.getPref().putInt(C_REFRESH_INTERVAL, refreshInterval);
            Config.getPref().putInt(C_CENTER_INTERVAL, centerInterval);
        Config.getPref().putInt(C_CENTER_FACTOR, (int) centerFactor);

        /*
         * Do one time conversion of factor: user value means "how big is inner rectangle
         * comparing to screen in percent", machine value means "what is the shrink ratio
         * for each dimension on _both_ sides".
         */

        centerFactor = (100 - centerFactor) / 2 / 100;
    }
}

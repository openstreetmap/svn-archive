package livegps;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.GpxLayer;

public class LiveGpsLayer extends GpxLayer implements PropertyChangeListener {
    public static final String LAYER_NAME = tr("LiveGPS layer");
    public static final String C_LIVEGPS_COLOR_POSITION = "color.livegps.position";
    public static final String C_LIVEGPS_COLOR_POSITION_ESTIMATE = "color.livegps.position_estimate";

    private static final int DEFAULT_REFRESH_INTERVAL = 250;
    private static final int DEFAULT_CENTER_INTERVAL = 5000;
    private static final int DEFAULT_CENTER_FACTOR = 80;
    private static final String oldC_REFRESH_INTERVAL = "livegps.refreshinterval";     /* in seconds */
    private static final String C_REFRESH_INTERVAL = "livegps.refresh_interval_msec";  /* in msec */
    private static final String C_CENTER_INTERVAL = "livegps.center_interval_msec";  /* in msec */
    private static final String C_CENTER_FACTOR = "livegps.center_factor" /* in percent */;
    private static final String C_CURSOR_H = "livegps.cursor_height"; /* in pixels */
    private static final String C_CURSOR_W = "livegps.cursor_width"; /* in pixels */
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

        Map<String, Object> attr = new HashMap<String, Object>();
        attr.put("desc", "josm live gps");

        GpxTrack trackBeingWritten = new SingleSegmentGpxTrack(trackSegment, attr);
        data.tracks.add(trackBeingWritten);

	initIntervals();
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
            Main.map.mapView.zoomTo(lastPoint.getCoor());
    }

    public void conditionalCenter(LatLon Pos) {
	Point2D P = Main.map.mapView.getPoint2D(Pos);
	Rectangle rv = Main.map.mapView.getBounds(null);
	Date date = new Date();
	long current = date.getTime();

	rv.grow(-(int)(rv.getHeight() * centerFactor), -(int)(rv.getWidth() * centerFactor));

	if (!rv.contains(P) || (centerInterval > 0 && current - lastCenter >= centerInterval)) {
		Main.map.mapView.zoomTo(Pos);
		lastCenter = current;
	}
    }

    public void setAutoCenter(boolean ac) {
        autocenter = ac;
    }

    @Override
    public void paint(Graphics2D g, MapView mv, Bounds bounds) {
        super.paint(g, mv, bounds);

	if (lastPoint == null)
		return;

	Point screen = mv.getPoint(lastPoint.getCoor());

	int TriaHeight = Main.pref.getInteger(C_CURSOR_H, 20);
	int TriaWidth = Main.pref.getInteger(C_CURSOR_W, 10);

	/*
	 * Draw a bold triangle.
	 * In case of deep zoom draw also a thin DOP oval.
	 */

	g.setColor(Main.pref.getColor(C_LIVEGPS_COLOR_POSITION_ESTIMATE, Color.CYAN));
	int w, h;
	double ppm = 100 / mv.getDist100Pixel();	/* pixels per metre */

	w = (int )Math.round(lastData.getEpx() * ppm);
	h = (int )Math.round(lastData.getEpy() * ppm);

	if (w > TriaWidth || h > TriaWidth) {
		int xo, yo;

		yo = screen.y - Math.round(h/2);
		xo = screen.x - Math.round(w/2);

		g.drawOval(xo, yo, w, h);
	}

	int[] x = new int[4];
	int[] y = new int[4];
	float course = lastData.getCourse();
	float csin = (float )Math.sin(Math.toRadians(course));
	float ccos = (float )Math.cos(Math.toRadians(course));
	float csin120 = (float )Math.sin(Math.toRadians(course + 120));
	float ccos120 = (float )Math.cos(Math.toRadians(course + 120));
	float csin240 = (float )Math.sin(Math.toRadians(course + 240));
	float ccos240 = (float )Math.cos(Math.toRadians(course + 240));

	g.setColor(Main.pref.getColor(C_LIVEGPS_COLOR_POSITION, Color.RED));

	for (int i = 0; i <= 3; i++, TriaHeight--, TriaWidth--) {

		x[0] = screen.x + Math.round(TriaHeight * csin);
		y[0] = screen.y - Math.round(TriaHeight * ccos);
		x[1] = screen.x + Math.round(TriaWidth * csin120);
		y[1] = screen.y - Math.round(TriaWidth * ccos120);
		x[2] = screen.x;
		y[2] = screen.y;
		x[3] = screen.x + Math.round(TriaWidth * csin240);
		y[3] = screen.y - Math.round(TriaWidth * ccos240);

		g.drawPolygon(x, y, 4);
	}

    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (!isVisible()) {
            return;
        }
        if ("gpsdata".equals(evt.getPropertyName())) {
            lastData = (LiveGpsData) evt.getNewValue();
            if (lastData.isFix()) {
                setCurrentPosition(lastData.getLatitude(), lastData.getLongitude());
                if (allowRedraw())
                    Main.map.repaint();
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
	if ((refreshInterval = Main.pref.getInteger(oldC_REFRESH_INTERVAL, 0)) != 0) {
		refreshInterval *= 1000;
		Main.pref.put(oldC_REFRESH_INTERVAL, null);
	} else
		refreshInterval = Main.pref.getInteger(C_REFRESH_INTERVAL, DEFAULT_REFRESH_INTERVAL);

	centerInterval = Main.pref.getInteger(C_CENTER_INTERVAL, DEFAULT_CENTER_INTERVAL);
	centerFactor = Main.pref.getInteger(C_CENTER_FACTOR, DEFAULT_CENTER_FACTOR);
	if (centerFactor <= 1 || centerFactor >= 99)
		centerFactor = DEFAULT_CENTER_FACTOR;

        Main.pref.putInteger(C_REFRESH_INTERVAL, refreshInterval);
        Main.pref.putInteger(C_CENTER_INTERVAL, centerInterval);
	Main.pref.putInteger(C_CENTER_FACTOR, (int )centerFactor);

	/*
	 * Do one time conversion of factor: user value means "how big is inner rectangle
	 * comparing to screen in percent", machine value means "what is the shrink ratio
	 * for each dimension on _both_ sides".
	 */

	centerFactor = (100 - centerFactor) / 2 / 100;
    }
}

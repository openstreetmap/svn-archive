package livegps;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
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
    public static final String KEY_LIVEGPS_COLOR = "color.livegps.position";

    private static final int DEFAULT_SLEEP_TIME = 500;	/* Default sleep time is 0.5 seconds. */
    private static final String oldConfigKey = "livegps.refreshinterval";     /* in seconds */
    private static final String ConfigKey = "livegps.refresh_interval_msec";  /* in msec */
    private int sleepTime;

    LatLon lastPos;
    WayPoint lastPoint;
    private final AppendableGpxTrackSegment trackSegment;
    float speed;
    float course;
    // JLabel lbl;
    boolean autocenter;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private long lastUpdate = 0;

    public LiveGpsLayer(GpxData data) {
        super(data, LAYER_NAME);
        trackSegment = new AppendableGpxTrackSegment();

        Map<String, Object> attr = new HashMap<String, Object>();
        attr.put("desc", "josm live gps");

        GpxTrack trackBeingWritten = new SingleSegmentGpxTrack(trackSegment, attr);
        data.tracks.add(trackBeingWritten);

	initSleepTime();
    }

    void setCurrentPosition(double lat, double lon) {
        // System.out.println("adding pos " + lat + "," + lon);
        LatLon thisPos = new LatLon(lat, lon);
        if ((lastPos != null) && (thisPos.equalsEpsilon(lastPos))) {
            // no change in position
            // maybe show a "paused" cursor or some such
            return;
        }

        lastPos = thisPos;
        lastPoint = new WayPoint(thisPos);
        lastPoint.attr.put("time", dateFormat.format(new Date()));
        trackSegment.addWaypoint(lastPoint);
        if (autocenter && allowRedraw()) {
            center();
        }
    }

    public void center() {
        if (lastPoint != null)
            Main.map.mapView.zoomTo(lastPoint.getCoor());
    }

    // void setStatus(String status)
    // {
    // this.status = status;
    // Main.map.repaint();
    // System.out.println("LiveGps status: " + status);
    // }

    void setSpeed(float metresPerSecond) {
        speed = metresPerSecond;
    }

    void setCourse(float degrees) {
        course = degrees;
    }

    public void setAutoCenter(boolean ac) {
        autocenter = ac;
    }

    @Override
    public void paint(Graphics2D g, MapView mv, Bounds bounds) {
        super.paint(g, mv, bounds);

        // int statusHeight = 50;
        // Rectangle mvs = mv.getBounds();
        // mvs.y = mvs.y + mvs.height - statusHeight;
        // mvs.height = statusHeight;
        // g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.8f));
        // g.fillRect(mvs.x, mvs.y, mvs.width, mvs.height);

        if (lastPoint != null) {
            Point screen = mv.getPoint(lastPoint.getCoor());
            g.setColor(Main.pref.getColor(KEY_LIVEGPS_COLOR, Color.RED));
            g.drawOval(screen.x - 10, screen.y - 10, 20, 20);
            g.drawOval(screen.x - 9, screen.y - 9, 18, 18);
        }

        // lbl.setText("gpsd: "+status+" Speed: " + speed +
        // " Course: "+course);
        // lbl.setBounds(0, 0, mvs.width-10, mvs.height-10);
        // Graphics sub = g.create(mvs.x+5, mvs.y+5, mvs.width-10,
        // mvs.height-10);
        // lbl.paint(sub);

        // if(status != null) {
        // g.setColor(Color.WHITE);
        // g.drawString("gpsd: " + status, 5, mv.getBounds().height - 15);
        // // lower left corner
        // }
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (!isVisible()) {
            return;
        }
        if ("gpsdata".equals(evt.getPropertyName())) {
            LiveGpsData data = (LiveGpsData) evt.getNewValue();
            if (data.isFix()) {
                setCurrentPosition(data.getLatitude(), data.getLongitude());
                if (!Float.isNaN(data.getSpeed())) {
                    setSpeed(data.getSpeed());
                }
                if (!Float.isNaN(data.getCourse())) {
                    setCourse(data.getCourse());
                }
                if (!autocenter && allowRedraw()) {
                    Main.map.repaint();
                }
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

	if (current - lastUpdate >= sleepTime) {
		lastUpdate = current;
		return true;
	} else
		return false;
    }

    /**
     * Retrieve the sleepTime from the configuration. Be compatible with old
     * version that stored value in seconds. If no such configuration key exists,
     * it will be initialized here.
     */
    private void initSleepTime() {
        if ((sleepTime = Main.pref.getInteger(ConfigKey, 0)) == 0) {
                if ((sleepTime = Main.pref.getInteger(oldConfigKey, 0)) != 0) {
                        sleepTime *= 1000;
                        Main.pref.put(oldConfigKey, null);
                } else
                        sleepTime = DEFAULT_SLEEP_TIME;
        }

        // creates the setting, if none present.
        Main.pref.putInteger(ConfigKey, sleepTime);
    }
}

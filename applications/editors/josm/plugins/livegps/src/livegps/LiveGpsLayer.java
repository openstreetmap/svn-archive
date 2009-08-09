package livegps;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.tools.ColorHelper;

public class LiveGpsLayer extends GpxLayer implements PropertyChangeListener {
    public static final String LAYER_NAME = tr("LiveGPS layer");
    public static final String KEY_LIVEGPS_COLOR ="color.livegps.position";
    LatLon lastPos;
    WayPoint lastPoint;
    GpxTrack trackBeingWritten;
    Collection<WayPoint> trackSegment;
    float speed;
    float course;
    String status;
    //JLabel lbl;
    boolean autocenter;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public LiveGpsLayer(GpxData data)
    {
        super (data, LAYER_NAME);
        trackBeingWritten = new GpxTrack();
        trackBeingWritten.attr.put("desc", "josm live gps");
        trackSegment = new ArrayList<WayPoint>();
        trackBeingWritten.trackSegs.add(trackSegment);
        data.tracks.add(trackBeingWritten);
    }

    void setCurrentPosition(double lat, double lon)
    {
        //System.out.println("adding pos " + lat + "," + lon);
        LatLon thisPos = new LatLon(lat, lon);
        if ((lastPos != null) && (thisPos.equalsEpsilon(lastPos))) {
            // no change in position
            // maybe show a "paused" cursor or some such
            return;
        }

        lastPos = thisPos;
        lastPoint = new WayPoint(thisPos);
        lastPoint.attr.put("time", dateFormat.format(new Date()));
        // synchronize when adding data, as otherwise the autosave action
        // needs concurrent access and this results in an exception!
        synchronized (LiveGpsLock.class) {
            trackSegment.add(lastPoint);
        }
        if (autocenter) {
            center();
        }

        //Main.map.repaint();
    }

    public void center()
    {
        if (lastPoint != null)
            Main.map.mapView.zoomTo(lastPoint.getCoor());
    }

//  void setStatus(String status)
//  {
//      this.status = status;
//      Main.map.repaint();
//        System.out.println("LiveGps status: " + status);
//  }

    void setSpeed(float metresPerSecond)
    {
        speed = metresPerSecond;
        //Main.map.repaint();
    }

    void setCourse(float degrees)
    {
        course = degrees;
        //Main.map.repaint();
    }

    public void setAutoCenter(boolean ac)
    {
        autocenter = ac;
    }

    @Override public void paint(Graphics g, MapView mv)
    {
        //System.out.println("in paint");
        synchronized (LiveGpsLock.class) {
            //System.out.println("in synced paint");
            super.paint(g, mv);
//          int statusHeight = 50;
//          Rectangle mvs = mv.getBounds();
//          mvs.y = mvs.y + mvs.height - statusHeight;
//          mvs.height = statusHeight;
//          g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.8f));
//          g.fillRect(mvs.x, mvs.y, mvs.width, mvs.height);

            if (lastPoint != null)
            {
                Point screen = mv.getPoint(lastPoint.getCoor());
                g.setColor(Main.pref.getColor(KEY_LIVEGPS_COLOR, Color.RED));
                g.drawOval(screen.x-10, screen.y-10,20,20);
                g.drawOval(screen.x-9, screen.y-9,18,18);
            }

//          lbl.setText("gpsd: "+status+" Speed: " + speed + " Course: "+course);
//          lbl.setBounds(0, 0, mvs.width-10, mvs.height-10);
//          Graphics sub = g.create(mvs.x+5, mvs.y+5, mvs.width-10, mvs.height-10);
//          lbl.paint(sub);

//          if(status != null) {
//          g.setColor(Color.WHITE);
//          g.drawString("gpsd: " + status, 5, mv.getBounds().height - 15); // lower left corner
//          }
        }
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if(!isVisible()) {
            return;
        }
        if("gpsdata".equals(evt.getPropertyName())) {
            LiveGpsData data = (LiveGpsData) evt.getNewValue();
            if(data.isFix()) {
                setCurrentPosition(data.getLatitude(), data.getLongitude());
                if(!Float.isNaN(data.getSpeed())) {
                    setSpeed(data.getSpeed());
                }
                if(!Float.isNaN(data.getCourse())) {
                    setCourse(data.getCourse());
                }
                Main.map.repaint();
            }
        }
    }
}

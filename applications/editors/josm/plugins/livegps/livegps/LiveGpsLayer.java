package livegps;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.RawGpsLayer;

public class LiveGpsLayer extends RawGpsLayer implements PropertyChangeListener {
    public final static String LAYER_NAME = "LiveGPS layer";
	LatLon lastPos;
	GpsPoint lastPoint;
	Collection<GpsPoint> trackBeingWritten;
	float speed;
	float course;
	String status;
	//JLabel lbl;
	boolean autocenter;
	
	public LiveGpsLayer(Collection<Collection<GpsPoint>> data)
	{
		super (data, LAYER_NAME, null);
		if (data.isEmpty())
		{
			data.add(new ArrayList<GpsPoint>());
		}
		// use last track in collection:
		for (Collection<GpsPoint> track : data) { 
		    trackBeingWritten = track;
		}
		//lbl = new JLabel();
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
		lastPoint = new GpsPoint (thisPos, new Date().toString());
		// synchronize when adding data, as otherwise the autosave action
		// needs concurrent access and this results in an exception!
		synchronized (LiveGpsLock.class) {
		    trackBeingWritten.add(lastPoint);            
        }
		if (autocenter) {
		    center();
		}
		
		//Main.map.repaint();
	}

	public void center()
	{
		if (lastPoint != null) 
			Main.map.mapView.zoomTo(lastPoint.eastNorth, Main.map.mapView.getScale());
	}
	
//	void setStatus(String status)
//	{
//		this.status = status;
//		Main.map.repaint();
//        System.out.println("LiveGps status: " + status);
//	}
	
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
	
	void setAutoCenter(boolean ac)
	{
		autocenter = ac;
	}

	@Override public void paint(Graphics g, MapView mv)
	{
	    //System.out.println("in paint");
	    synchronized (LiveGpsLock.class) {
	        //System.out.println("in synced paint");
	        super.paint(g, mv);
//	        int statusHeight = 50;
//	        Rectangle mvs = mv.getBounds();
//	        mvs.y = mvs.y + mvs.height - statusHeight;
//	        mvs.height = statusHeight;
//	        g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.8f)); 
//	        g.fillRect(mvs.x, mvs.y, mvs.width, mvs.height);

	        if (lastPoint != null)
	        {
	            Point screen = mv.getPoint(lastPoint.eastNorth);
	            g.setColor(Color.RED);
	            g.drawOval(screen.x-10, screen.y-10,20,20);
	            g.drawOval(screen.x-9, screen.y-9,18,18);
	        }

//	        lbl.setText("gpsd: "+status+" Speed: " + speed + " Course: "+course);
//	        lbl.setBounds(0, 0, mvs.width-10, mvs.height-10);
//	        Graphics sub = g.create(mvs.x+5, mvs.y+5, mvs.width-10, mvs.height-10);
//	        lbl.paint(sub);

//	        if(status != null) {
//	        g.setColor(Color.WHITE);
//	        g.drawString("gpsd: " + status, 5, mv.getBounds().height - 15); // lower left corner
//	        }
	    }
	}
    
    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if(!visible) {
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

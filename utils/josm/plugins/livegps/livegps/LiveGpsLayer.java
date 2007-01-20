package livegps;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.swing.JLabel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.layer.RawGpsLayer;
import org.openstreetmap.josm.gui.layer.RawGpsLayer.GpsPoint;

public class LiveGpsLayer extends RawGpsLayer {

	LatLon lastPos;
	GpsPoint lastPoint;
	Collection<GpsPoint> trackBeingWritten;
	float speed;
	float course;
	String status;
	JLabel lbl;
	boolean autocenter;
	
	public LiveGpsLayer(Collection<Collection<GpsPoint>> data)
	{
		super (data, "LiveGPS layer", null);
		if (data.isEmpty())
		{
			data.add(new ArrayList<GpsPoint>());
		}
		for (Collection<GpsPoint> track : data) trackBeingWritten = track;
		lbl = new JLabel();
	}
	
	void setCurrentPosition(double lat, double lon)
	{
		LatLon thisPos = new LatLon(lat, lon);
		if ((lastPos != null) && (thisPos.equalsEpsilon(lastPos))) {
			// no change in position
			// maybe show a "paused" cursor or some such
			return;
		}
			
		lastPos = thisPos;
		lastPoint = new GpsPoint (thisPos, new Date().toString());
		trackBeingWritten.add(lastPoint);
		if (autocenter) center();
		
		Main.map.repaint();
	}

	public void center()
	{
		if (lastPoint != null) 
			Main.map.mapView.zoomTo(lastPoint.eastNorth, Main.map.mapView.getScale());
	}
	
	void setStatus(String status)
	{
		this.status = status;
		Main.map.repaint();
	}
	
	void setSpeed(float metresPerSecond)
	{
		speed = metresPerSecond;
		Main.map.repaint();
	}

	void setCourse(float degrees)
	{
		course = degrees;
		Main.map.repaint();
	}
	
	void setAutoCenter(boolean ac)
	{
		autocenter = ac;
	}

	@Override public void paint(Graphics g, MapView mv)
	{
		super.paint(g, mv);
		int statusHeight = 50;
		Rectangle mvs = mv.getBounds();
		mvs.y = mvs.y + mvs.height - statusHeight;
		mvs.height = statusHeight;
		g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.8f)); 
		g.fillRect(mvs.x, mvs.y, mvs.width, mvs.height);
		
		if (lastPoint != null)
		{
			Point screen = mv.getPoint(lastPoint.eastNorth);
			g.setColor(Color.RED);
			g.drawOval(screen.x-10, screen.y-10,20,20);
			g.drawOval(screen.x-9, screen.y-9,18,18);
		}
		lbl.setText("gpsd: "+status+" Speed: " + speed + " Course: "+course);
		lbl.setBounds(0, 0, mvs.width-10, mvs.height-10);
		Graphics sub = g.create(mvs.x+5, mvs.y+5, mvs.width-10, mvs.height-10);
		lbl.paint(sub);
	}
}

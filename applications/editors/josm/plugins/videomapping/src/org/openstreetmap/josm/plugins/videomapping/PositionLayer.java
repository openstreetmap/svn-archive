package org.openstreetmap.josm.plugins.videomapping;


import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;

import javax.swing.*;

import static org.openstreetmap.josm.tools.I18n.*;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.GpxTrackSegment;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.videomapping.video.GPSVideoPlayer;

//Basic rendering and GPS layer interaction
public class PositionLayer extends Layer implements MouseListener,MouseMotionListener {
    private List<WayPoint> ls;
    private List<WayPoint> ipos;
    public GpsPlayer gps;
    private boolean dragIcon=false; //do we move the icon by hand?
    private WayPoint iconPosition;
    private Point mouse;
    private ImageIcon icon;
    private SimpleDateFormat gpsTimeCode;
    public GPSVideoPlayer gpsVP;
        
    public PositionLayer(File video, GpxLayer GpsLayer) {
        super(video.getName());
        ls=copyGPSLayer(GpsLayer.data); //TODO This might be outsourced to a seperated track        
        gps= new GpsPlayer(ls);        
        icon = new ImageIcon("images/videomapping.png");
        gpsTimeCode= new SimpleDateFormat("HH:mm:ss");//TODO replace with DF small
        Main.map.mapView.addMouseListener(this);
        Main.map.mapView.addMouseMotionListener(this);                          
        gpsVP = new GPSVideoPlayer(video, gps);
        gps.goTo(0);
        ipos=gps.interpolate();
        iconPosition=gps.getCurr();        
    }
    
    //make a flat copy
    private List<WayPoint> copyGPSLayer(GpxData route)
    { 
        ls = new LinkedList<WayPoint>();
        for (GpxTrack trk : route.tracks) {
            for (GpxTrackSegment segment : trk.getSegments()) {
                ls.addAll(segment.getWayPoints());
            }
        }
        Collections.sort(ls); //sort basing upon time
        return ls;
    }


    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public Object getInfoComponent() {
        String temp;
        String sep=System.getProperty("line.separator");
        temp=tr("{0} {1}% of GPS track",gpsVP.getVideo().getName(),gpsVP.getCoverage()*10+sep);
        temp=temp+gpsVP.getNativePlayerInfos();
        return temp;
    }

    @Override
	public Action[] getMenuEntries() {
        return new Action[]{
                LayerListDialog.getInstance().createActivateLayerAction(this),
                LayerListDialog.getInstance().createShowHideLayerAction(),
                LayerListDialog.getInstance().createDeleteLayerAction(),
                SeparatorLayerAction.INSTANCE,
                //TODO here my stuff
                SeparatorLayerAction.INSTANCE,
                new LayerListPopup.InfoAction(this)};//TODO here infos about the linked videos
	}

      


    @Override
    public String getToolTipText() {
        return tr("Shows current position in the video");
    }

    // no merging necessary
    @Override
    public boolean isMergable(Layer arg0) {
        return false;
    }

    @Override
    public void mergeFrom(Layer arg0) {
        
    }

    
    
    @Override
    //Draw the current position, infos, waypoints
    public void paint(Graphics2D g, MapView map, Bounds bound) {
        Point p;
        //TODO Source out redundant calculations
        //draw all GPS points
        g.setColor(Color.YELLOW); //new Color(0,255,0,128)
        for(WayPoint n: ls) {
            p = Main.map.mapView.getPoint(n.getEastNorth());
            g.drawOval(p.x - 2, p.y - 2, 4, 4);
        }
        //draw synced points
        g.setColor(Color.GREEN);
        for(WayPoint n: ls) {
            if(n.attr.containsKey("synced"))
            {
                p = Main.map.mapView.getPoint(n.getEastNorth());
                g.drawOval(p.x - 2, p.y - 2, 4, 4);
            }
        }
        //draw current segment points
        g.setColor(Color.YELLOW);
        if(gps.getPrev()!=null)
        {
            p = Main.map.mapView.getPoint(gps.getPrev().getEastNorth());
            g.drawOval(p.x - 2, p.y - 2, 4, 4);
            Point p2 = Main.map.mapView.getPoint(gps.getCurr().getEastNorth());
            g.drawLine(p.x, p.y, p2.x, p2.y);
        }
        if(gps.getNext()!=null)
        {
            p = Main.map.mapView.getPoint(gps.getNext().getEastNorth());
            g.drawOval(p.x - 2, p.y - 2, 4, 4);
            Point p2 = Main.map.mapView.getPoint(gps.getCurr().getEastNorth());
            g.drawLine(p.x, p.y, p2.x, p2.y);
        }
        //draw interpolated points
        g.setColor(Color.CYAN);
        g.setBackground(Color.CYAN);
        //LinkedList<WayPoint> ipo=(LinkedList<WayPoint>) gps.getInterpolatedLine(5);
        for (WayPoint wp : ipos) {
            p=Main.map.mapView.getPoint(wp.getEastNorth());
            g.fillArc(p.x, p.y, 4, 4, 0, 360);
            //g.drawOval(p.x - 2, p.y - 2, 4, 4);
        }
        //draw cam icon
        g.setColor(Color.RED);
        if(dragIcon)
        {
            if(iconPosition!=null)
            {
                p=Main.map.mapView.getPoint(iconPosition.getEastNorth());
                icon.paintIcon(null, g, p.x-icon.getIconWidth()/2, p.y-icon.getIconHeight()/2);             
                //g.drawString(mins.format(iconPosition.getTime()),p.x-10,p.y-10); //TODO when synced we might wan't to use a different layout
                g.drawString(gpsTimeCode.format(iconPosition.getTime()),p.x-15,p.y-15);
            }
        }
        else
        {
            if (gps.getCurr()!=null){
            p=Main.map.mapView.getPoint(gps.getIPO().getEastNorth());
            icon.paintIcon(null, g, p.x-icon.getIconWidth()/2, p.y-icon.getIconHeight()/2);         
            g.drawString(gpsTimeCode.format(gps.getCurr().getTime()),p.x-15,p.y-15);
            }
        }
    }
    
    //finds the first waypoint that is nearby the given point
    private WayPoint getNearestWayPoint(Point mouse)
    {
        final int MAX=10;
        Point p;
        Rectangle rect = new Rectangle(mouse.x-MAX/2,mouse.y-MAX/2,MAX,MAX);
        //iterate through all possible notes
        for(WayPoint n : ls) //TODO this is not very clever, what better way to find this WP? Hashmaps? Divide and Conquer?
        {
            p = Main.map.mapView.getPoint(n.getEastNorth());
            if (rect.contains(p))
            {               
                return n;
            }
            
        }
        return null;
        
    }
    
    //upper left corner like rectangle
    private Rectangle getIconRect()
    {
        Point p = Main.map.mapView.getPoint(gps.getCurr().getEastNorth());
        return new Rectangle(p.x-icon.getIconWidth()/2,p.y-icon.getIconHeight()/2,icon.getIconWidth(),icon.getIconHeight());
    }


    @Override
    public void visitBoundingBox(BoundingXYVisitor arg0) {
        // TODO don't know what to do here

    }

    public void mouseClicked(MouseEvent e) {        
    }

    public void mouseEntered(MouseEvent arg0) { 
    }

    public void mouseExited(MouseEvent arg0) {
    }

    //init drag&drop
    public void mousePressed(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1) {
            //is it on the cam icon?
            if (gps.getCurr()!=null)
            {
                if (getIconRect().contains(e.getPoint()))
                {
                    mouse=e.getPoint();
                    dragIcon=true;
                }
            }
        }
        
    }
    
    //
    public void mouseReleased(MouseEvent e) {       
        //only leftclicks on our layer
        if(e.getButton() == MouseEvent.BUTTON1) {
            if(dragIcon)
            {
                dragIcon=false;
            }
            else
            {            	
                //simple click
            	WayPoint wp = getNearestWayPoint(e.getPoint());            	
                if(wp!=null)
                {
                	//jump if unsynced
                	if (gpsVP.isSynced())
                	{
                		//jump if we know position
                        if(wp.attr.containsKey("synced"))
                        {
                        	gps.goTo(wp);
                            if(gpsVP!=null) gpsVP.jumpToGPSTime(new Date(gps.getRelativeTime())); //call videoplayers to set right position
                        }
                	}
                	else
                	{
                		//otherwise let user mark possible sync point
                		gps.goTo(wp);
                	}
                    
                }
            }
            Main.map.mapView.repaint();
        }
        
    }
    
    //slide and restrict during movement
    public void mouseDragged(MouseEvent e) {        
        if(dragIcon)
        {           
            mouse=e.getPoint();
            //restrict to GPS track
            iconPosition=gps.getInterpolatedWaypoint(mouse);
            Main.map.mapView.repaint();
        }
    }

    //visualize drag&drop
    public void mouseMoved(MouseEvent e) {      
        if (gps.getCurr()!=null)
        {                       
            if (getIconRect().contains(e.getPoint()))
            {
                Main.map.mapView.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            else
            {
                Main.map.mapView.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }
        
    }

	public  void setVideopPlayer(GPSVideoPlayer player) {
		gpsVP=player;
		
	}
	
	public GPSVideoPlayer getVideoPlayer()
	{
		return gpsVP;
	}
	
	public String getGPSTime()
	{
		return gpsTimeCode.format(iconPosition.getTime());
	}
}

/*
    Copyright (C) 2005 Nick Whitelegg, Hogweed Software, nick@hogweed.org 

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111 USA

 */
import com.bbn.openmap.Layer;
import com.bbn.openmap.event.*;
import java.awt.event.MouseEvent;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;
import java.io.FileReader;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;
import java.awt.Graphics;

import java.io.PrintWriter;
import java.io.FileWriter;

class GPXLayer extends Layer implements GPXComponents, MapMouseListener
{
	public Track track;
	public OMGraphicList waypoints;

	public static final int SEGMENTISE = 0, DELETE_POINTS = 1,
						EDIT_WAYPOINTS = 2; 

	String segType="byway";
	int x1=-1,y1=-1,x2,y2,mode=SEGMENTISE;


	public GPXLayer()
	{
		track=new Track();
		waypoints=new OMGraphicList();
	}

	public void loadGPX(String gpxFile) throws Exception
	{
		XMLReader xmlReader=XMLReaderFactory.createXMLReader();
		GPXHandler handler=new GPXHandler();
		xmlReader.setContentHandler(handler);
		xmlReader.setErrorHandler(handler);
		handler.setComponents(this);
		xmlReader.parse(new InputSource(new FileReader(gpxFile)));
	}

	public void saveGPX(String filename) throws java.io.IOException
	{
		PrintWriter pw=new PrintWriter(new FileWriter(filename),true);
		pw.println("<gpx version=\"1.0\" creator=\"OSMApplet\" "+
					"xmlns=\"http://www.topografix.com/GPX/1/0\">");
		track.toGPX(pw);
		pw.println("</gpx>");
	}

	public void projectionChanged(com.bbn.openmap.event.ProjectionEvent pe)
	{
		Projection proj=setProjection(pe);
		if(proj!=null)
		{
			
			track.generate(proj);
			waypoints.generate(proj);

			repaint();
		}
		fireStatusUpdate(LayerStatusEvent.FINISH_WORKING);
	}

	public void paint(Graphics g)
	{
		track.render(g);
		waypoints.render(g);
	}

	public void setMode(int mode)
	{
		System.out.println("GPX mode now: "+mode);
		this.mode=mode;
	}

	// GPXComponents interface implementations
	public void setTrackID(String id)
	{
		track.setID(id);
	}

	public void addTrackpoint(int seg,String timestamp, float lat, float lon)
	{
		track.addTrackpoint(seg,timestamp,lat,lon);
	}	

	public void addWaypoint(String name,float lat,float lon,String type)
	{
		waypoints.addOMGraphic(new Waypoint(name,lat,lon,type));
	}

	public void setSegType(int seg,String type)
	{
		track.setSegType(seg,type);
	}

	public void newSegment()
	{
		track.newSegment();
	}

	// map mouse listener  stuff

	// tell other objects in the OpenMap system that the layer handles its own
	// mouse events
	public MapMouseListener getMapMouseListener()
	{
		return this;
	}

	public boolean mouseClicked(MouseEvent e)
	{
		return true;
	}	
	public boolean mouseDragged(MouseEvent e)
	{
		return true;
	}	
	public void mouseEntered(MouseEvent e)
	{
	}	
	public void mouseExited(MouseEvent e)
	{
	}	
	public boolean mouseMoved(MouseEvent e)
	{
		return true;
	}	
	public boolean mousePressed(MouseEvent e)
	{
		return true;
	}	

	public boolean mouseReleased(MouseEvent e)
	{
		System.out.println("mouseReleased");
		if(mode==EDIT_WAYPOINTS)
		{
			Waypoint wp=(Waypoint)waypoints.findClosest(e.getX(),e.getY(),5);
			if(wp!=null)
			{
				System.out.println("Found a waypoint!");
				WaypointDialogue d=new WaypointDialogue
						(wp.getName(),wp.getType());
				if(d.okPressed())
				{
					System.out.println("ok pressed!");
					wp.alter(d.getName(),d.getType());
				}
			}
		}
		else if(x1<0 && y1<0) 
		{
			x1=e.getX();
			y1=e.getY();
			System.out.println("First point " + x1+" " +y1);
		}
		else
		{
			x2=e.getX();
			y2=e.getY();
			System.out.println("Second point " + x2+" " +y2);
			switch(mode)
			{
				case SEGMENTISE:
					track.segmentise(segType,x1,y1,x2,y2);
					break;
				case DELETE_POINTS:
					track.deletePoints(x1,y1,x2,y2);
					break;
			}
			x1=x2=y1=y2=-1;
		}

		repaint();
		return true;
	}	

	public void mouseMoved()
	{
	}

	
	public String[] getMouseModeServiceList()
	{
		return new String[] { SelectMouseMode.modeID };
	}	

	public void setSegType(String segType)
	{
		this.segType=segType;
	}
}	

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

import java.io.PrintWriter;
import java.awt.Color;
import java.awt.Graphics;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.omGraphics.geom.BasicGeometry.*;
import com.bbn.openmap.proj.Projection;



class TrackSeg extends OMGraphic 
{
	String id, type;

	OMGraphicList points, lines;

	Track parent;

	public TrackSeg(Track parent) 
	{ 
		id="";
		type="track";
		this.parent=parent;
		points=new OMGraphicList();
		lines=new OMGraphicList();
	}

	public TrackSeg(Track parent,String id,String type)
	{
		this.id=id;
		this.type=type;
		this.parent=parent;
	}
	public void setID(String id)
	{ 
		this.id=id; 
	}
	public String getID() 
	{ 
		return id; 
	}

	// Sets the segment's type. This also has the effect of joining the dots
	// with OMLines of the appropriate colour.
	public void setType(String type)
	{ 
		this.type=type;
		TrackPoint previous, current;
		Color lineColour=LookAndFeel.getColour(type);
		for(int count=0; count<points.size()-1; count++)
		{
			previous=(TrackPoint)points.getOMGraphicAt(count);
			current=(TrackPoint)points.getOMGraphicAt(count+1);
			OMLine l=formLine(previous,current);
			l.setLinePaint(lineColour);
			current.setLinePaint(lineColour);
			lines.addOMGraphic(l);
		}
	}

	private OMLine formLine(TrackPoint pt1,TrackPoint pt2)
	{
		OMLine l=new OMLine(pt1.getLat(),pt1.getLon(),
							pt2.getLat(),pt2.getLon(), 
							LINETYPE_STRAIGHT);
		return l;
	}

	public boolean deletePoints(int start, int end,boolean rejoin)
	{
		System.out.println("deletePoints:deleting: " +start+" " +end);
		System.out.println("initial points.size()"+points.size());
		System.out.println("initial lines.size()"+lines.size());

		if(start>=0&&start<points.size()&&end>=0&&end<points.size())
		{
			for(int count=0; count<(end-start)+1; count++)
			{
				points.removeOMGraphicAt(start);
				lines.removeOMGraphicAt(start-1);
				System.out.println("points.size()"+points.size());
				System.out.println("lines.size()"+lines.size());
			}
			if(rejoin)
			{
				lines.removeOMGraphicAt(start-1);
				TrackPoint beforeBreak=
						(TrackPoint)points.getOMGraphicAt(start-1);
				TrackPoint afterBreak=(TrackPoint)points.getOMGraphicAt(start);
				OMLine l=formLine(beforeBreak,afterBreak);
				l.setLinePaint(LookAndFeel.getColour(type));
				lines.insertOMGraphicAt(l,start-1);
			}
			return true;
		}

		return false;
	}

	public void addPoint(TrackPoint pt)
	{
		points.addOMGraphic(pt);
	}

	public void toGPX(PrintWriter pw)
	{
		pw.println("<trkseg><extensions>");
		pw.println("<type>" + type + "</type>");
		pw.println("</extensions>");
		for(int count=0; count<points.size(); count++)
			((TrackPoint)points.getOMGraphicAt(count)).toGPX(pw);
		pw.println("</trkseg>");
	}

	public void render(Graphics g)
	{
		lines.render(g);
		points.render(g);
	}

	public void segmentise (String newType, int x1, int y1, int x2, int y2)
	{

		System.out.println("x1=" + x1 + " y1=" + y1 + " x2=" + x2
							+ " y2=" + y2);
		TrackSeg newSeg=new TrackSeg(parent), postSeg=new TrackSeg(parent);

		int p1 = points.findIndexOfClosest(x1,y1,5),
			   		p2 = points.findIndexOfClosest(x2,y2,5),
					p3=Math.min(p1,p2),
					p4=Math.max(p1,p2);
			
		System.out.println("p1="+p1+" p2="+p2+" p3="+p3+" p4="+p4);	
		if(p3>=0 && p4>=0)
		{
			for(int count=p3; count<=p4; count++)
				newSeg.addPoint((TrackPoint)points.getOMGraphicAt(count));

			for(int count=p4; count<points.size(); count++)
				postSeg.addPoint((TrackPoint)points.getOMGraphicAt(count));
		
			postSeg.setType(type);
			newSeg.setType(newType);		

			parent.addOMGraphic(newSeg);
			parent.addOMGraphic(postSeg);

			deletePoints(p4+1,points.size()-1,false);
		}
	}

	public void deletePoints(int x1,int y1,int x2,int y2)
	{
		int p1 = points.findIndexOfClosest(x1,y1,5),
			   		p2 = points.findIndexOfClosest(x2,y2,5),
					p3=Math.min(p1,p2),
					p4=Math.max(p1,p2);
	
		if(p3>=0 && p4>=0)
			deletePoints(p3,p4,true);
	}

	public boolean generate(Projection proj)
	{
		lines.generate(proj);
		points.generate(proj);
		return true;
	}

	public float distance(int x,int y)
	{
		return points.distance(x,y);
	}
}

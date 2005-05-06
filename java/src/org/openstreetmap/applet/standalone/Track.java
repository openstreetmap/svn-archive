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
import com.bbn.openmap.omGraphics.OMGraphicList;


class Track  extends OMGraphicList
{
	String id;

	public void toGPX(PrintWriter pw)
	{
		pw.println("<trk>");
		for(int count=0; count<size(); count++)
			((TrackSeg)getOMGraphicAt(count)).toGPX(pw);
		pw.println("</trk>");
	}

	public void setID(String id)
	{
		this.id=id;
	}

	public boolean addTrackpoint(int seg,String timestamp,float lat,float lon)
	{
		if(seg>=0 && seg<size())
		{
			System.out.println("size=" + size());
			System.out.println("seg: "+seg+" adding point");
			((TrackSeg)getOMGraphicAt(seg)).addPoint
					(new TrackPoint(timestamp,lat,lon));	
			return true;
		}
		return false;
	}

	public boolean setSegType(int seg,String type)
	{
		if(seg>=0 && seg<size())
		{
			((TrackSeg)getOMGraphicAt(seg)).setType(type);
			return true;	
		}
		return false;
	}

	public void segmentise(String newType,int x1, int y1, int x2, int y2)
	{
		System.out.println("x1=" + x1 + " y1=" + y1 + " x2=" + x2
							+ " y2=" + y2);
		TrackSeg nearestSeg = (TrackSeg)findClosest((x1+x2)/2,(y1+y2)/2);
		if(nearestSeg!=null)nearestSeg.segmentise(newType,x1,y1,x2,y2);
	}

	public void deletePoints(int x1,int y1,int x2,int y2)
	{
		TrackSeg nearestSeg = (TrackSeg)findClosest((x1+x2)/2,(y1+y2)/2);
		if(nearestSeg!=null)nearestSeg.deletePoints(x1,y1,x2,y2);
	}

	public void newSegment()
	{
		addOMGraphic(new TrackSeg(this));
	}
}

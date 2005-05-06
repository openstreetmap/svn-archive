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
import com.bbn.openmap.omGraphics.OMPoint;
import java.awt.Color;
import java.io.PrintWriter;
import com.bbn.openmap.LatLonPoint;

class TrackPoint extends OMPoint
{
	// 10/04/05 now storing the timestamp as the standard GPX format
	String timestamp;

	public TrackPoint(String t, float lt, float ln)
	{ 
		super(lt,ln,2);
		timestamp=t; 
		setLinePaint(Color.gray);
		setFillPaint(Color.gray);
	}
	public String getTimestamp() 
	{ 
		return timestamp; 
	}

	public void toGPX(PrintWriter pw)
	{
		pw.println( "<trkpt lat=\"" + getLat() + 
				"\" lon=\"" + getLon()+ "\">");
		pw.println("<time>"+timestamp+"</name>");
		pw.println("</trkpt>");
	}
}

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

import com.bbn.openmap.omGraphics.OMRaster;
import java.io.PrintWriter;

class Waypoint extends OMRaster
{
	String name, type;
		 
	public Waypoint(String name,float lat,float lon,String type)
	{ 
		//super(lat,lon,LookAndFeel.getImageIcon(type));
		super(lat,lon,LookAndFeel.getImageIcon(type));
		this.name=name; 
		this.type=type;
	}

	public String getName()
	{
		return name;
	}
	public String getType()
	{
		return type;
	}

	public void alter( String newName, String newType)
	{
		name=newName;
		type=newType;
		setImageIcon(LookAndFeel.getImageIcon(newType));
	}

	public void toGPX(PrintWriter pw)
	{
		pw.println( "<wpt lat=\"" + getLat() + 
				"\" lon=\"" + getLon()+ "\">");
		pw.println("<name>"+name+"</name>");
		pw.println("<type>"+type+"</type>");
		pw.println("</wpt>");
	}

}


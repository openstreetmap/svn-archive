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
import javax.swing.JFrame;

public class OSMEditor extends JFrame
{

	public OSMEditor(float scale,float lat,float lon)
	{
    	super("OpenStreetMap");
		pack();
		setVisible(true);

		osmDisplay hd = new osmDisplay(scale,lat,lon,getContentPane());
	}
	public static void main(String args[])
	{
		

    //	float fScale = 10404.917f;
		float scale=Float.parseFloat(args[0]),
			   lat = Float.parseFloat(args[1]),
			   lon = Float.parseFloat(args[2]);

		new OSMEditor(scale,lat,lon);

	}
}

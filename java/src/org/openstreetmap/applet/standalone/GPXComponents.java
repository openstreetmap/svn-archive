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

public interface GPXComponents
{
	// Set a track ID. 
	public void setTrackID(String id);

	// Add a trackpoint
	public void addTrackpoint(int seg,String timestamp, float lat, float lon);

	// Add a waypoint
	public void addWaypoint(String name,float lat,float lon,String type);

	// Set segment type
	public void setSegType(int seg,String type);

	// Add a new segment
	public void newSegment();
}

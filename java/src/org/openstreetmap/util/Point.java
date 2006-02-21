/*
 * Copyright (C) 2005 Tom Carden (tom@somethingmodern.com)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 *  
 */

package org.openstreetmap.util;

import org.openstreetmap.client.Tile;

/**
 * Minimal representation of OpenStreetMap GPX point (lat/lon pair)
 */
public class Point {

	public double lat, lon;
	public float x,y;
	public boolean projected = false;

	public Point(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}

	public Point(float x, float y, Tile projection) {
		this.x = x;
		this.y = y;
		this.lat = projection.lat(y);
		this.lon = projection.lon(x);
		projected = true;
	}

	public void project(Tile projection){
		x = (float)projection.x(lon);
		y = (float)projection.y(lat);
		projected = true;
	}

	public void unproject(Tile projection){
		this.lat = projection.lat(y);
		this.lon = projection.lon(x);
	}

	public float distance(Point other) {
		return distance(other.x,other.y);
	}

	public float distance(float x, float y) {
		if (projected && this.x != x && this.y != y) {
			return (float)Math.sqrt((x-this.x)*(x-this.x)+(y-this.y)*(y-this.y));
		}
		return 0.0f;
	}
}


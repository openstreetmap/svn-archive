/*
 * Copyright (C) 2005 Steve Coast (steve@asklater.com)
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

/**
 * Minimal representation of OpenStreetMap node (lat/lon pair, with uid)
 */
public class Tag {
	/**
	 * The tag string for this node.
	 */
	public String key;

	/**
	 * The tag string for this node.
	 */
	public String value;

	public Tag(String key, String value)
	{
		this.key = key;
		this.value = value;
	}

	public String toString() {
		return "[tag " + key + "=" + value + "]";

	}
}

//    JOSM tag2link plugin.
//    Copyright (C) 2011 Don-vip & FrViPofm
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
package org.openstreetmap.josm.plugins.tag2link;

import javax.swing.ImageIcon;

import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Main constants of JOSM Imagery XML Bounds plugin.
 * @author Don-vip
 *
 */
public interface Tag2LinkConstants {

	/**
	 * XML Schema
	 */
	public static final String XML_LOCATION = "resource://data/tag2link_sources.xml";
	
    /**
     * XML tags
     */
    // TODO

	/**
	 * File encoding.
	 */
	public static final String ENCODING = "UTF-8";
	
	/**
	 * Plugin icons.
	 */
	public static ImageIcon ICON_16 = ImageProvider.get("tag2link_16.png");
	public static ImageIcon ICON_24 = ImageProvider.get("tag2link_24.png");
    public static ImageIcon ICON_48 = ImageProvider.get("tag2link_48.png");
}

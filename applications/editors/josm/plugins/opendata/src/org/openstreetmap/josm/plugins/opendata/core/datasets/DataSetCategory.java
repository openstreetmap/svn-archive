//    JOSM opendata plugin.
//    Copyright (C) 2011-2012 Don-vip
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
package org.openstreetmap.josm.plugins.opendata.core.datasets;

import javax.swing.ImageIcon;

import org.openstreetmap.josm.plugins.opendata.core.util.OdUtils;

public class DataSetCategory {

	private final String name;
	private final ImageIcon icon;
	
	public DataSetCategory(String name, ImageIcon icon) {
		this.name = name;
		this.icon = icon;
	}
	
	public DataSetCategory(String name, String iconName) {
		this(name, iconName != null && !iconName.isEmpty() ? OdUtils.getImageIcon(iconName) : null);
	}

	public final String getName() {
		return name;
	}

	public final ImageIcon getIcon() {
		return icon;
	}
}

//    JOSM Imagery XML Bounds plugin.
//    Copyright (C) 2011 Don-vip
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
package org.openstreetmap.josm.plugins.imageryxmlbounds.actions;

import java.util.Collection;

import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

/**
 * 
 * @author Don-vip
 *
 */
public class ShowBoundsSelectionAction extends ShowBoundsAction implements SelectionChangedListener {

	public ShowBoundsSelectionAction() {
		putValue("toolbar", "xml-imagery-bounds");
	}

	@Override
	public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
		updateOsmPrimitives(newSelection);
	}
}

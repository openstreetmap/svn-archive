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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openstreetmap.josm.data.osm.OsmPrimitive;

/**
 * 
 * @author Don-vip
 *
 */
public class ShowBoundsListAction extends ShowBoundsAction implements ListSelectionListener {

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			JList list = (JList) e.getSource();
			if (list.getModel().getSize() > list.getSelectionModel().getMaxSelectionIndex()) {
				List<OsmPrimitive> primitives = new ArrayList<OsmPrimitive>();
				for (Object value : list.getSelectedValues()) {
					if (value instanceof OsmPrimitive) {
						primitives.add((OsmPrimitive) value);
					}
				}
				updateOsmPrimitives(primitives);
			}
		}
	}
}

/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.openstreetmap.josm.plugins.fixAddresses.gui;

import java.awt.Graphics;
import java.awt.Point;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.MapRectangle;
import org.openstreetmap.josm.data.osm.BBox;

public class BBoxMapRectangle implements MapRectangle {
	private BBox bbox;

	/**
	 * @param bbox
	 */
	public BBoxMapRectangle(BBox bbox) {
		super();
		this.bbox = bbox;
	}

	@Override
	public Coordinate getBottomRight() {
		return new Coordinate(bbox.getBottomRight().lat(), bbox.getBottomRight().lon());
	}

	@Override
	public Coordinate getTopLeft() {
		return new Coordinate(bbox.getTopLeft().lat(), bbox.getTopLeft().lon());
	}

	@Override
	public void paint(Graphics g, Point topLeft, Point bottomRight) {
			// do nothing here
	}

}

// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses.gui;

import java.awt.Graphics;
import java.awt.Point;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.MapRectangleImpl;
import org.openstreetmap.josm.data.osm.BBox;

/**
 * A {@link MapRectangleImpl} constructed from a {@link BBox}.
 */
public class BBoxMapRectangle extends MapRectangleImpl {
    private BBox bbox;

    public BBoxMapRectangle(BBox bbox) {
        super(null, null);
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

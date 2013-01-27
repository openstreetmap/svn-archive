/*
 * GPLv2 or 3, Copyright (c) 2010  Andrzej Zaborowski
 *
 * Implements a fake MapView that we can pass to WMSLayer's .paint,
 * this will give us two things:
 *  # We'll be able to tell WMSLayer.paint() what area we want it
 *    to download (it ignores the "bounds" parameter) and override
 *    isVisible and friends as needed, and
 *  # We'll receive notifications from Grabber when we need to
 *    repaint (and call WMSLayer's .paint again) because the
 *    Grabber downloaded some or all of the tiles that we asked
 *    WMSLayer for and WMSLayer created the Grabber passing it
 *    our MapView.  Otherwise we wouldn't be able to tell when
 *    this happened and could only guess.
 */
package wmsturbochallenge;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

import org.openstreetmap.josm.data.ProjectionBounds;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.gui.MapView;

class fake_map_view extends MapView {
    public ProjectionBounds view_bounds;
    public MapView parent;

    public Graphics2D graphics;
    public BufferedImage ground_image;
    public int ground_width = -1;
    public int ground_height = -1;
    public double scale;
    public double max_east_west;

    public fake_map_view(MapView parent, double scale) {
        super(null, null); //TODO MapView constructor contains registering listeners and other code, that probably shouldn't be called in fake map view
        this.parent = parent;
        this.scale = scale;

        ProjectionBounds parent_bounds = parent.getProjectionBounds();
        max_east_west =
            parent_bounds.maxEast - parent_bounds.minEast;
    }

    public void setProjectionBounds(ProjectionBounds bounds) {
        view_bounds = bounds;

        if (bounds.maxEast - bounds.minEast > max_east_west) {
            max_east_west = bounds.maxEast - bounds.minEast;

            /* We need to set the parent MapView's bounds (i.e.
             * zoom level) to the same as ours max possible
             * bounds to avoid WMSLayer thinking we're zoomed
             * out more than we are or it'll pop up an annoying
             * "requested area is too large" popup.
             */
            EastNorth parent_center = parent.getCenter();
            parent.zoomTo(new ProjectionBounds(
                    new EastNorth(
                        parent_center.east() -
                        max_east_west / 2,
                        parent_center.north()),
                    new EastNorth(
                        parent_center.east() +
                        max_east_west / 2,
                        parent_center.north())));

            /* Request again because NavigatableContent adds
             * a border just to be sure.
             */
            ProjectionBounds new_bounds =
                parent.getProjectionBounds();
            max_east_west =
                new_bounds.maxEast - new_bounds.minEast;
        }

        Point vmin = getPoint(bounds.getMin());
        Point vmax = getPoint(bounds.getMax());
        int w = vmax.x + 1;
        int h = vmin.y + 1;

        if (w <= ground_width && h <= ground_height) {
            graphics.setClip(0, 0, w, h);
            return;
        }

        if (w > ground_width)
            ground_width = w;
        if (h > ground_height)
            ground_height = h;

        ground_image = new BufferedImage(ground_width,
                ground_height,
                BufferedImage.TYPE_INT_RGB);
        graphics = ground_image.createGraphics();
        graphics.setClip(0, 0, w, h);
    }

    public ProjectionBounds getProjectionBounds() {
        return view_bounds;
    }

    public Point getPoint(EastNorth p) {
        double x = p.east() - view_bounds.minEast;
        double y = view_bounds.maxNorth - p.north();
        x /= this.scale;
        y /= this.scale;

        return new Point((int) x, (int) y);
    }

    public EastNorth getEastNorth(int x, int y) {
        return new EastNorth(
            view_bounds.minEast + x * this.scale,
            view_bounds.minNorth - y * this.scale);
    }

    public boolean isVisible(int x, int y) {
        return true;
    }

    public Graphics getGraphics() {
        return graphics;
    }

    public void repaint() {
    }
}

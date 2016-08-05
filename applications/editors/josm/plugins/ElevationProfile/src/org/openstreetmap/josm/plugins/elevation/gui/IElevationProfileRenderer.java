// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.elevation.gui;

import java.awt.Color;
import java.awt.Graphics;

import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.plugins.elevation.IElevationProfile;
import org.openstreetmap.josm.plugins.elevation.gpx.ElevationWayPointKind;

/**
 * Basic interface for all elevation profile renderers. First, therenderer determines the color
 * for a given way point, so that as well the dialog as the layer can share the color scheme.
 * Second, the layer can simply pass the painting stuff to a renderer without taking care of
 * details.
 *
 * @author Oliver Wieland &lt;oliver.wieland@online.de&gt;
 *
 */
public interface IElevationProfileRenderer {
    /**
     * Gets the color for a given way point.
     * @param profile The elevation profile that contains the way point.
     * @param wpt The way point to get the color for.
     * @param kind The way point kind (see {@link ElevationWayPointKind}).
     * @return The color for the way point or null, if invalid arguments have been specified.
     */
    Color getColorForWaypoint(IElevationProfile profile, WayPoint wpt, ElevationWayPointKind kind);

    /**
     * Renders the way point with the lowest elevation.
     *
     * @param g The graphics context.
     * @param profile The elevation profile that contains the way point.
     * @param mv the associated view
     * @param wpt The way point to render.
     * @param kind The way point kind (see {@link ElevationWayPointKind}).
     */
    void renderWayPoint(Graphics g, IElevationProfile profile, MapView mv, WayPoint wpt, ElevationWayPointKind kind);

    /**
     * Render line between two way points. This is intended to render speed or slope.
     *
     * @param g The graphics context.
     * @param profile The elevation profile that contains the way point.
     * @param mv the associated view
     * @param wpt1 the first way point
     * @param wpt2 the second way point
     */
    void renderLine(Graphics g, IElevationProfile profile, MapView mv, WayPoint wpt1, WayPoint wpt2, ElevationWayPointKind kind);

    /**
     * Notifies the renderer that rendering starts.
     */
    void beginRendering();

    /**
     * Notifies the renderer that rendering has been finished.
     */
    void finishRendering();
}

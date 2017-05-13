// License: WTFPL. For details, see LICENSE file.
package iodb;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.text.MessageFormat;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.imagery.OffsetBookmark;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.AbstractTileSourceLayer;

/**
 * Some common static methods for querying and processing imagery layers.
 *
 * @author Zverik
 * @license WTFPL
 */
public final class ImageryOffsetTools {
    /**
     * A title for all dialogs created in this plugin.
     */
    public static final String DIALOG_TITLE = tr("Imagery Offset Database");

    private ImageryOffsetTools() {
        // Hide default constructor for utilities classes
    }

    /**
     * Returns the topmost visible imagery layer.
     * @return the layer, or null if it hasn't been found.
     */
    public static AbstractTileSourceLayer getTopImageryLayer() {
        if (Main.map == null || Main.map.mapView == null)
            return null;
        List<AbstractTileSourceLayer> layers = Main.getLayerManager().getLayersOfType(AbstractTileSourceLayer.class);
        for (AbstractTileSourceLayer layer : layers) {
            String url = layer.getInfo().getUrl();
            if (layer.isVisible() && url != null && !url.contains("gps-")) {
                return layer;
            }
        }
        return null;
    }

    /**
     * Calculates the center of a visible map area.
     * @return the center point, or (0; 0) if there's no map on the screen.
     */
    public static LatLon getMapCenter() {
        Projection proj = Main.getProjection();
        return Main.map == null || Main.map.mapView == null
                ? new LatLon(0, 0) : proj.eastNorth2latlon(Main.map.mapView.getCenter());
    }

    /**
     * Calculates an imagery layer offset.
     * @param center The center of a visible map area.
     * @return Coordinates of a point on the imagery which correspond to the
     * center point on the map.
     * @see #applyLayerOffset
     */
    public static LatLon getLayerOffset(AbstractTileSourceLayer layer, LatLon center) {
        Projection proj = Main.getProjection();
        EastNorth offsetCenter = Main.map.mapView.getCenter();
        EastNorth centerOffset = offsetCenter.add(-layer.getDisplaySettings().getDx(),
                -layer.getDisplaySettings().getDy());
        LatLon offsetLL = proj.eastNorth2latlon(centerOffset);
        return offsetLL;
    }

    /**
     * Applies the offset to the imagery layer.
     * @see #calculateOffset(iodb.ImageryOffset)
     * @see #getLayerOffset
     */
    public static void applyLayerOffset(AbstractTileSourceLayer layer, ImageryOffset offset) {
        OffsetBookmark bookmark = calculateOffset(offset);
        layer.getDisplaySettings().setOffsetBookmark(bookmark);
    }

    /**
     * Calculate dx and dy for imagery offset.
     * @return An array of [dx, dy].
     * @see #applyLayerOffset
     */
    public static OffsetBookmark calculateOffset(ImageryOffset offset) {
        Projection proj = Main.getProjection();
        EastNorth center = proj.latlon2eastNorth(offset.getPosition());
        EastNorth offsetPos = proj.latlon2eastNorth(offset.getImageryPos());
        EastNorth offsetXY = new EastNorth(center.getX() - offsetPos.getX(), center.getY() - offsetPos.getY());
        OffsetBookmark b = new OffsetBookmark(proj.toCode(), offset.getImagery(), "Autogenerated",
                offsetXY.getX(), offsetXY.getY(), offset.getPosition().lon(), offset.getPosition().lat());
        return b;
    }

    /**
     * Generate unique imagery identifier based on its type and URL.
     * @param layer imagery layer.
     * @return imagery id.
     */
    public static String getImageryID(AbstractTileSourceLayer layer) {
        return layer == null ? null :
            ImageryIdGenerator.getImageryID(layer.getInfo().getUrl(), layer.getInfo().getImageryType());
    }

    // Following three methods were snatched from TMSLayer
    private static double latToTileY(double lat, int zoom) {
        double l = lat / 180 * Math.PI;
        double pf = Math.log(Math.tan(l) + (1 / Math.cos(l)));
        return Math.pow(2.0, zoom - 1) * (Math.PI - pf) / Math.PI;
    }

    private static double lonToTileX(double lon, int zoom) {
        return Math.pow(2.0, zoom - 3) * (lon + 180.0) / 45.0;
    }

    public static int getCurrentZoom() {
        if (Main.map == null || Main.map.mapView == null) {
            return 1;
        }
        MapView mv = Main.map.mapView;
        LatLon topLeft = mv.getLatLon(0, 0);
        LatLon botRight = mv.getLatLon(mv.getWidth(), mv.getHeight());
        double x1 = lonToTileX(topLeft.lon(), 1);
        double y1 = latToTileY(topLeft.lat(), 1);
        double x2 = lonToTileX(botRight.lon(), 1);
        double y2 = latToTileY(botRight.lat(), 1);

        int screenPixels = mv.getWidth() * mv.getHeight();
        double tilePixels = Math.abs((y2 - y1) * (x2 - x1) * 256 * 256);
        if (screenPixels == 0 || tilePixels == 0) {
            return 1;
        }
        double factor = screenPixels / tilePixels;
        double result = Math.log(factor) / Math.log(2) / 2 + 1;
        int intResult = (int) Math.floor(result);
        return intResult;
    }

    /**
     * Converts distance in meters to a human-readable string.
     */
    public static String formatDistance(double d) {
        // CHECKSTYLE.OFF: SingleSpaceSeparator
        if (d < 0.0095) return formatDistance(d * 1000, tr("mm"), true);
        if (d < 0.095)  return formatDistance(d * 100,  tr("cm"), true);
        if (d < 0.95)   return formatDistance(d * 100,  tr("cm"), false);
        if (d < 9.5)    return formatDistance(d,        tr("m"),  true);
        if (d < 950)    return formatDistance(d,        tr("m"),  false);
        if (d < 9500)   return formatDistance(d / 1000, tr("km"), true);
        if (d < 1e6)    return formatDistance(d / 1000, tr("km"), false);
        // CHECKSTYLE.ON: SingleSpaceSeparator
        return "\u221E";
    }

    /**
     * Constructs a distance string.
     * @param d Distance.
     * @param si Units of measure for distance.
     * @param floating Whether a floating point is needed.
     * @return A formatted string.
     */
    private static String formatDistance(double d, String si, boolean floating) {
        return MessageFormat.format(floating ? "{0,number,0.0} {1}" : "{0,number,0} {1}", d, si);
    }
}

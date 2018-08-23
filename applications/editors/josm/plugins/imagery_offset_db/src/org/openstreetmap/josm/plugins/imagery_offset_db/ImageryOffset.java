// License: WTFPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.imagery_offset_db;

import java.util.Map;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.coor.conversion.DecimalDegreesCoordinateFormat;

/**
 * An imagery offset. Contains imagery identifier, zoom bracket and a location
 * of the position point on the imagery layer. The offset is then calculated
 * as a difference between the two.
 *
 * @author Zverik
 * @license WTFPL
 */
public class ImageryOffset extends ImageryOffsetBase {
    private LatLon imageryPos;
    private String imagery;
    private int minZoom, maxZoom;

    public ImageryOffset(String imagery, LatLon imageryPos) {
        this.imageryPos = imageryPos;
        this.imagery = imagery;
        this.minZoom = 0;
        this.maxZoom = 30;
    }

    public void setMaxZoom(int maxZoom) {
        this.maxZoom = maxZoom;
    }

    public void setMinZoom(int minZoom) {
        this.minZoom = minZoom;
    }

    public LatLon getImageryPos() {
        return imageryPos;
    }

    public String getImagery() {
        return imagery;
    }

    public int getMaxZoom() {
        return maxZoom;
    }

    public int getMinZoom() {
        return minZoom;
    }

    @Override
    public void putServerParams(Map<String, String> map) {
        super.putServerParams(map);
        map.put("imagery", imagery);
        map.put("imlat", DecimalDegreesCoordinateFormat.INSTANCE.latToString(imageryPos));
        map.put("imlon", DecimalDegreesCoordinateFormat.INSTANCE.lonToString(imageryPos));
        if (minZoom > 0)
            map.put("minzoom", String.valueOf(minZoom));
        if (maxZoom < 30)
            map.put("maxzoom", String.valueOf(maxZoom));
    }

    @Override
    public String toString() {
        return "ImageryOffset{" + "imageryPos=" + imageryPos + ", imagery=" + imagery + "position=" + position + ", date=" + date +
                ", author=" + author + ", description=" + description + ", abandonDate=" + abandonDate + '}';
    }
}

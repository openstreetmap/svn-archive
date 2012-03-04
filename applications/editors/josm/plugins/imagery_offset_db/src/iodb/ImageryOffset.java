package iodb;

import org.openstreetmap.josm.data.coor.LatLon;

/**
 * An offset.
 * 
 * @author zverik
 */
public class ImageryOffset extends ImageryOffsetBase {
    private LatLon imageryPos;
    private String imagery;
    private int minZoom, maxZoom;

    public ImageryOffset( String imagery, LatLon imageryPos ) {
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
}

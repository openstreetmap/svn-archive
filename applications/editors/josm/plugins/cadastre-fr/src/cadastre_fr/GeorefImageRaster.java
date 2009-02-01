package cadastre_fr;

import java.awt.image.BufferedImage;

import org.openstreetmap.josm.data.coor.EastNorth;

public class GeorefImageRaster extends GeorefImage {

    private static final long serialVersionUID = 1L;

    public GeorefImageRaster(BufferedImage img, EastNorth min, EastNorth max) {
        super(img, min, max);
    }

}

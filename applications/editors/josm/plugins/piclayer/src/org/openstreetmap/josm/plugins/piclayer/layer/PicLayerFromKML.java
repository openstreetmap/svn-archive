package org.openstreetmap.josm.plugins.piclayer.layer;

import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.plugins.piclayer.layer.kml.KMLGroundOverlay;

public class PicLayerFromKML extends PicLayerAbstract {

    private KMLGroundOverlay calibration;
    private File picture;
    private String pictureName;

    public PicLayerFromKML(File main, KMLGroundOverlay calibration) {

        picture = new File(main.getParent() + File.separatorChar + calibration.getFileName());
        this.calibration = calibration;

        pictureName = calibration.getName();

        // Set the name of the layer as the base name of the file
        setName(picture.getName());
    }
    @Override
    protected Image createImage() throws IOException {
        Image image = ImageIO.read(picture);
        return image;
    }

    @Override
    protected void lookForCalibration() throws IOException {
        if (calibration != null)
            loadCalibration(calibration);

    }

    @Override
    public String getPicLayerName() {
        return pictureName;
    }

    public void loadCalibration(KMLGroundOverlay cal) {
        int w = image.getWidth(null);
        int h = image.getHeight(null);
        LatLon coord1 = new LatLon(cal.getNorth(), cal.getEast());
        LatLon coord2 = new LatLon(cal.getSouth(), cal.getWest());

        EastNorth en1 = projection.latlon2eastNorth(coord1);
        EastNorth en2 = projection.latlon2eastNorth(coord2);


        EastNorth imagePosition = new EastNorth((en1.getX()+en2.getX())/2, (en1.getY()+en2.getY())/2);
        transformer.setImagePosition(imagePosition);

        initialImageScale = 100*getMetersPerEasting(imagePosition);

        AffineTransform transform = AffineTransform.getScaleInstance((en1.getX()-en2.getX())/w, (en1.getY()-en2.getY())/h);
        transform.rotate(cal.getRotate()/180.0*Math.PI);

        transformer.resetCalibration();
        transformer.getTransform().concatenate(transform);
    }

}

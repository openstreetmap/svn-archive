/***************************************************************************
 *   Copyright (C) 2009 by Tomasz Stelmach                                 *
 *   http://www.stelmach-online.net/                                       *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/

package org.openstreetmap.josm.plugins.piclayer.layer;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.RenameLayerAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.piclayer.actions.LoadPictureCalibrationAction;
import org.openstreetmap.josm.plugins.piclayer.actions.ResetCalibrationAction;
import org.openstreetmap.josm.plugins.piclayer.actions.SavePictureCalibrationAction;
import org.openstreetmap.josm.plugins.piclayer.transform.PictureTransform;
import org.openstreetmap.josm.tools.Utils;

/**
 * Base class for layers showing images. Actually it does all the showing. The
 * subclasses are supposed only to create images in different ways (load from
 * files, copy from clipboard, hack into a spy satellite and download them,
 * anything...)
 */
public abstract class PicLayerAbstract extends Layer {
    // Counter - just for naming of layers
    private static int imageCounter = 0;

    // This is the main image to be displayed
    protected Image image = null;
    // Tiles of pin images
    private static Image pinTiledImage;

    // Initial position of the image in the real world
    protected EastNorth initialImagePosition;

    // Position of the image in the real world
    protected EastNorth imagePosition;

    // The scale that was set on the map during image creation
    protected double initialImageScale = 1.0;

    // Layer icon
    private Icon layerIcon = null;

    private boolean drawMarkers = true;

    public void setDrawPoints(boolean value) {
        drawMarkers = value;
    }

    protected PictureTransform transformer;

    public PictureTransform getTransformer() {
        return transformer;
    }

    // Keys for loading from old/new Properties
    private static final String INITIAL_POS_X = "INITIAL_POS_X";
    private static final String INITIAL_POS_Y = "INITIAL_POS_y";
    private static final String POSITION_X = "POSITION_X";
    private static final String POSITION_Y = "POSITION_Y";
    private static final String ANGLE = "ANGLE";
    private static final String INITIAL_SCALE = "INITIAL_SCALE";
    private static final String SCALEX = "SCALEX";
    private static final String SCALEY = "SCALEY";
    private static final String SHEARX = "SHEARX";
    private static final String SHEARY = "SHEARY";
    // new properties
    private static final String MATRIXm00 = "M00";
    private static final String MATRIXm01 = "M01";
    private static final String MATRIXm10 = "M10";
    private static final String MATRIXm11 = "M11";
    private static final String MATRIXm02 = "M02";
    private static final String MATRIXm12 = "M12";

    // pin images properties - tile anchors, width and offset
    // TODO: load these from properties file in images folder...
    private static final int pinAnchorX = 31;
    private static final int pinAnchorY = 31;
    private static final int[] pinTileOffsetX = {74, 0, 74, 0};
    private static final int[] pinTileOffsetY = {0, 74, 74, 0};
    private static final int pinWidth = 64;
    private static final int pinHeight = 64;

    /**
     * Constructor
     */
    public PicLayerAbstract() {
        super("PicLayer #" + imageCounter);

        //Increase number
        imageCounter++;

        // Load layer icon
        layerIcon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(getClass().getResource("/images/layericon.png")));

        if (pinTiledImage == null) {
            // allow system to load the image and use it in future
            pinTiledImage = new ImageIcon(Toolkit.getDefaultToolkit().createImage(getClass().getResource("/images/v6_64.png"))).getImage();
        }
    }

    /**
     * Initializes the image. Gets the image from a subclass and stores some
     * initial parameters. Throws exception if something fails.
     */
    public void initialize() throws IOException {
        // First, we initialize the calibration, so that createImage() can rely on it

        // If the map does not exist - we're screwed. We should not get into this situation in the first place!
        if ( Main.map != null && Main.map.mapView != null ) {

            EastNorth center = Main.map.mapView.getCenter();

            imagePosition = new EastNorth(center.east(), center.north());
            initialImagePosition = new EastNorth(imagePosition.east(), imagePosition.north());
            // Initial scale at which the image was loaded
            initialImageScale = Main.map.mapView.getDist100Pixel();
        } else {
            throw new IOException(tr("Could not find the map object."));
        }

        // Create image
        image = createImage();
        if ( image == null ) {
            throw new IOException(tr("PicLayer failed to load or import the image."));
        }
        // Load image completely
        (new ImageIcon(image)).getImage();

        transformer = new PictureTransform();

        lookForCalibration();
    }

    /**
     * To be overridden by subclasses. Provides an image from an external sources.
     * Throws exception if something does not work.
     *
     * TODO: Replace the IOException by our own exception.
     */
    protected abstract Image createImage() throws IOException;

    protected abstract void lookForCalibration() throws IOException;
    /**
     * To be overridden by subclasses. Returns the user readable name of the layer.
     */
    public abstract String getPicLayerName();

    @Override
    public Icon getIcon() { return layerIcon; }

    @Override
    public Object getInfoComponent() { return null; }

    @Override
    public Action[] getMenuEntries() {
        // Main menu
        return new Action[] {
                new ResetCalibrationAction(this, transformer),
                SeparatorLayerAction.INSTANCE,
                new SavePictureCalibrationAction(this),
                new LoadPictureCalibrationAction(this),
                SeparatorLayerAction.INSTANCE,
                new RenameLayerAction(null,this),
        };
    }

    @Override
    public String getToolTipText() {
        return getPicLayerName();
    }

    @Override
    public boolean isMergable(Layer arg0) {
        return false;
    }

    @Override
    public void mergeFrom(Layer arg0) {}

    @Override
    public void paint(Graphics2D g2, MapView mv, Bounds bounds) {

        if ( image != null) {

            // Position image at the right graphical place
            EastNorth center = Main.map.mapView.getCenter();
            EastNorth leftop = Main.map.mapView.getEastNorth( 0, 0 );
            // Number of pixels for one unit in east north space.
            // This is the same in x- and y- direction.
            double pixel_per_en = ( Main.map.mapView.getWidth() / 2.0 ) / ( center.east() - leftop.east() );

            //     This is now the offset in screen pixels
            double pic_offset_x = (( imagePosition.east() - leftop.east() ) * pixel_per_en);
            double pic_offset_y = (( leftop.north() - imagePosition.north() ) * pixel_per_en);

            Graphics2D g = (Graphics2D)g2.create();
            // Move
            g.translate( pic_offset_x, pic_offset_y );

            // Scale
            double scalex = initialImageScale * pixel_per_en / getMetersPerEasting(imagePosition) / 100;
            double scaley = initialImageScale * pixel_per_en / getMetersPerNorthing(imagePosition) / 100;
            g.scale( scalex, scaley );

            g.transform(transformer.getTransform());

            // Draw picture
            g.drawImage( image, -image.getWidth(null) / 2, -image.getHeight(null) / 2, null );

            // Draw additional rectangle for the active pic layer
            if ( Main.map.mapView.getActiveLayer() == this ) {
                g.setColor( new Color( 0xFF0000 ) );
                g.drawRect(
                    -image.getWidth(null) / 2,
                    -image.getHeight(null) / 2,
                    image.getWidth(null),
                    image.getHeight(null)
                );
            }
            if (drawMarkers ) {
                // draw markers for selection
                Graphics2D gPoints = (Graphics2D)g2.create();

                gPoints.translate(pic_offset_x, pic_offset_y);

                gPoints.setColor(Color.RED); // red color for points output

                AffineTransform tr = AffineTransform.getScaleInstance(scalex, scaley);
                tr.concatenate(transformer.getTransform());

                for (int i = 0; i < transformer.getOriginPoints().size(); i++) {
                   Point2D trP = tr.transform(transformer.getOriginPoints().get(i), null);
                   int x = (int)trP.getX(), y = (int)trP.getY();

                   int dstx = x-pinAnchorX;
                   int dsty = y-pinAnchorY;
                   gPoints.drawImage(pinTiledImage, dstx, dsty, dstx+pinWidth, dsty+pinHeight, pinTileOffsetX[i], pinTileOffsetY[i], pinTileOffsetX[i]+pinWidth, pinTileOffsetY[i]+pinHeight, null);
                }
            }
        } else {
            // TODO: proper logging
            System.out.println( "PicLayerAbstract::paint - general drawing error (image is null or Graphics not 2D" );
        }
    }

    /**
     * Returns the distance in meter, that corresponds to one unit in east north
     * space. For normal projections, it is about 1 (but usually changing with
     * latitude).
     * For EPSG:4326, it is the distance from one meridian of full degree to the
     * next (a couple of kilometers).
     */
    protected double getMetersPerEasting(EastNorth en) {
        /* Natural scale in east/north units per pixel.
         * This means, the projection should be able to handle
         * a shift of that size in east north space without
         * going out of bounds.
         *
         * Also, this should get us somewhere in the range of meters,
         * so we get the result at the point 'en' and not some average.
         */
        double naturalScale = Main.getProjection().getDefaultZoomInPPD();
        naturalScale *= 0.01; // make a little smaller

        LatLon ll1 = Main.getProjection().eastNorth2latlon(
                new EastNorth(en.east() - naturalScale, en.north()));
        LatLon ll2 = Main.getProjection().eastNorth2latlon(
                new EastNorth(en.east() + naturalScale, en.north()));

        double dist = ll1.greatCircleDistance(ll2) / naturalScale / 2;
        return dist;
    }

    /* see getMetersPerEasting */
    private double getMetersPerNorthing(EastNorth en) {
        double naturalScale = Main.getProjection().getDefaultZoomInPPD();
        naturalScale *= 0.01;

        LatLon ll1 = Main.getProjection().eastNorth2latlon(
                new EastNorth(en.east(), en.north()- naturalScale));
        LatLon ll2 = Main.getProjection().eastNorth2latlon(
                new EastNorth(en.east(), en.north() + naturalScale));

        double dist = ll1.greatCircleDistance(ll2) / naturalScale / 2;
        return dist;
    }

    @Override
    /**
     * Computes the (rough) bounding box.
     * We ignore the rotation, the resulting bounding box contains any possible
     * rotation.
     */
    public void visitBoundingBox(BoundingXYVisitor arg0) {
        if ( image == null )
            return;
        String projcode = Main.getProjection().toCode();

        // TODO: bounding box only supported when coordinates are in meters
        // The reason for that is that this .cal think makes us a hard time.
        // The position is stored as a raw data (can be either in degrees or
        // in meters, depending on the projection used at creation), but the
        // initial scale is in m/100pix
        // So for now, we support the bounding box only when everything is in meters
        if (projcode.equals("EPSG:4326") )
            return;

        EastNorth center = imagePosition;
        double w = image.getWidth(null);
        double h = image.getHeight(null);
        double diag_pix = Math.sqrt(w*w+h*h);

        // initialImageScale is a the scale (unit: m/100pix) at creation time
        double diag_m = (diag_pix/100) * initialImageScale;

        AffineTransform trans = transformer.getTransform();
        double factor = Math.max(trans.getScaleX(), trans.getScaleY());

        double offset = factor * diag_m / 2.0;

        EastNorth topleft = center.add(-offset, -offset);
        EastNorth bottomright = center.add(offset, offset);
        arg0.visit(topleft);
        arg0.visit(bottomright);
    }

    /**
     * Saves the calibration data into properties structure
     * @param props Properties to save to
     */
    public void saveCalibration( Properties props ) {
        // Save
        double[] matrix = new double[6];
        transformer.getTransform().getMatrix(matrix);

        props.put(MATRIXm00, Double.toString(matrix[0]));
        props.put(MATRIXm01, Double.toString(matrix[1]));
        props.put(MATRIXm10, Double.toString(matrix[2]));
        props.put(MATRIXm11, Double.toString(matrix[3]));
        props.put(MATRIXm02, Double.toString(matrix[4]));
        props.put(MATRIXm12, Double.toString(matrix[5]));
        props.put(POSITION_X, Double.toString(imagePosition.getX()));
        props.put(POSITION_Y, Double.toString(imagePosition.getY()));
        props.put(INITIAL_SCALE, Double.toString(initialImageScale));

        transformer.resetModified();
    }

    /**
     * Loads calibration data from file
     * @param file The file to read from
     * @return
     */
    public void loadCalibration(InputStream is) throws IOException {
        Properties props = new Properties();
        props.load(is);
        loadCalibration(props);
    }

    /**
     * Loads calibration data from properties structure
     * @param props Properties to load from
     * @return
     */
    public void loadCalibration( Properties props ) {
        // Load

        AffineTransform transform;

        double pos_x = Double.valueOf(props.getProperty(POSITION_X, "0"));
        double pos_y = Double.valueOf(props.getProperty(POSITION_Y, "0"));

        imagePosition = new EastNorth(pos_x, pos_y);
        initialImageScale = Double.valueOf(props.getProperty(INITIAL_SCALE, "1")); //in_scale
        if (props.containsKey(SCALEX)) {// old format
            double in_pos_x = Double.valueOf(props.getProperty(INITIAL_POS_X, "0"));
            double in_pos_y = Double.valueOf(props.getProperty(INITIAL_POS_Y, "0"));
            double angle = Double.valueOf(props.getProperty(ANGLE, "0"));
            double scale_x = Double.valueOf(props.getProperty(SCALEX, "1"));
            double scale_y = Double.valueOf(props.getProperty(SCALEY, "1"));
            double shear_x = Double.valueOf(props.getProperty(SHEARX, "0"));
            double shear_y = Double.valueOf(props.getProperty(SHEARY, "0"));

            initialImagePosition.setLocation(in_pos_x, in_pos_y);

            // transform to matrix from these values - need testing
            transform = AffineTransform.getRotateInstance(angle/180*Math.PI);
            transform.scale(scale_x, scale_y);
            transform.shear(shear_x, shear_y);
        } else {
            // initialize matrix
            double[] matrix = new double[6];
            matrix[0] = Double.parseDouble(props.getProperty(MATRIXm00, "1"));
            matrix[1] = Double.parseDouble(props.getProperty(MATRIXm01, "0"));
            matrix[2] = Double.parseDouble(props.getProperty(MATRIXm10, "0"));
            matrix[3] = Double.parseDouble(props.getProperty(MATRIXm11, "1"));
            matrix[4] = Double.parseDouble(props.getProperty(MATRIXm02, "0"));
            matrix[5] = Double.parseDouble(props.getProperty(MATRIXm12, "0"));

            transform = new AffineTransform(matrix);
        }
        transformer.resetCalibration();
        transformer.getTransform().concatenate(transform);

        // Refresh
        Main.map.mapView.repaint();
    }

    public void loadWorldfile(InputStream is) throws IOException {
        BufferedReader br = null;
        try {
            Reader reader = new InputStreamReader(is);
            br = new BufferedReader(reader);
            double e[] = new double[6];
            for (int i=0; i<6; ++i) {
                String line = br.readLine();
                e[i] = Double.parseDouble(line);
            }
            double sx=e[0], ry=e[1], rx=e[2], sy=e[3], dx=e[4], dy=e[5];
            int w = image.getWidth(null);
            int h = image.getHeight(null);
            imagePosition.setLocation(
                    dx + w/2*sx + h/2*rx,
                    dy + w/2*ry + h/2*sy
            );
            initialImagePosition.setLocation(imagePosition);
//            m_angle = 0;
            double scalex = 100*sx*getMetersPerEasting(imagePosition);
            double scaley = -100*sy*getMetersPerNorthing(imagePosition);
            double shearx = rx / sx;
            double sheary = ry / sy;

            transformer.resetCalibration();
            AffineTransform tr = transformer.getTransform();
            tr.scale(scalex, scaley);
            tr.shear(shearx, sheary);

            initialImageScale = 1;
            Main.map.mapView.repaint();
        } finally {
            Utils.close(br);
        }
    }

    public Point2D transformPoint(Point p) throws NoninvertibleTransformException {
        // Position image at the right graphical place

        EastNorth center = Main.map.mapView.getCenter();
        EastNorth leftop = Main.map.mapView.getEastNorth( 0, 0 );
        // Number of pixels for one unit in east north space.
        // This is the same in x- and y- direction.
        double pixel_per_en = ( Main.map.mapView.getWidth() / 2.0 ) / ( center.east() - leftop.east() );

        //     This is now the offset in screen pixels
        double pic_offset_x = (( imagePosition.east() - leftop.east() ) * pixel_per_en);
        double pic_offset_y = (( leftop.north() - imagePosition.north() ) * pixel_per_en); // something bad...

        AffineTransform pointTrans = AffineTransform.getTranslateInstance(pic_offset_x, pic_offset_y);

        double scalex = initialImageScale * pixel_per_en / getMetersPerEasting(imagePosition) / 100;
        double scaley = initialImageScale * pixel_per_en / getMetersPerNorthing(imagePosition) / 100;

        pointTrans.scale(scalex, scaley); // ok here

        pointTrans.concatenate(transformer.getTransform());

        Point2D result = pointTrans.inverseTransform(p, null);
        return result;
    }

    /**
     * Moves the picture. Scaled in EastNorth...
     */
    public void movePictureBy(double x, double y) {
        imagePosition = imagePosition.add(x, y);
        transformer.setModified();
    }


    public void rotatePictureBy(double angle) {
        try {
            Point2D trans = transformPoint(new Point(Main.map.mapView.getWidth()/2, Main.map.mapView.getHeight()/2));

            transformer.concatenateTransformPoint(AffineTransform.getRotateInstance(angle), trans);
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        }
    }

    public void scalePictureBy(double scalex, double scaley) {
        try {
            Point2D trans = transformPoint(new Point(Main.map.mapView.getWidth()/2, Main.map.mapView.getHeight()/2));

            transformer.concatenateTransformPoint(AffineTransform.getScaleInstance(scalex, scaley), trans);
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        }
    }

    public void shearPictureBy(double shx, double shy) {
        try {
            Point2D trans = transformPoint(new Point(Main.map.mapView.getWidth()/2, Main.map.mapView.getHeight()/2));

            transformer.concatenateTransformPoint(AffineTransform.getShearInstance(shx, shy), trans);
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        }
    }

    public void resetCalibration() {
        transformer.resetCalibration();
        imagePosition.setLocation(initialImagePosition);
    }

    // get image coordinates by mouse coords
    public Point2D findSelectedPoint(Point point) {
        if (image == null)
            return null;

        Point2D selected = null;
        try {
            Point2D pressed = transformPoint(point);
            double mindist = 10;
            for (Point2D p : transformer.getOriginPoints())
                if (p.distance(pressed) < mindist) { // if user clicked to select some of origin point
                    selected = p;
                    mindist = p.distance(pressed);
                }
            return selected;
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        }
        return selected;
    }

    public void saveTransformCommand() {

    }
}

// License: GPL. v2 and later. Copyright 2008-2009 by Pieren <pieren3@gmail.com> and others
package cadastre_fr;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.gui.NavigatableComponent;

public class GeorefImage implements Serializable {
    private static final long serialVersionUID = 1L;

    // bbox of the georeferenced image (the nice horizontal and vertical box)
    public EastNorth min;
    public EastNorth max;
    // bbox of the georeferenced original image (raster only) (inclined if rotated and before cropping)
    // P[0] is bottom,left then next are clockwise. 
    private EastNorth[] orgRaster = new EastNorth[4];
    // bbox of the georeferenced original image (raster only) after cropping 
    private EastNorth[] orgCroppedRaster = new EastNorth[4];
    // angle with georeferenced original image after rotation (raster images only)(in radian)
    private double angle = 0;

    public BufferedImage image;

    private double pixelPerEast;
    private double pixelPerNorth;

    public GeorefImage(BufferedImage img, EastNorth min, EastNorth max) {
        image = img;
        this.min = min;
        this.max = max;
        this.orgRaster[0] = min;
        this.orgRaster[1] = new EastNorth(min.east(), max.north());
        this.orgRaster[2] = max;
        this.orgRaster[3] = new EastNorth(max.east(), min.north());
        this.orgCroppedRaster[0] = min;
        this.orgCroppedRaster[1] = new EastNorth(min.east(), max.north());
        this.orgCroppedRaster[2] = max;
        this.orgCroppedRaster[3] = new EastNorth(max.east(), min.north());
        updatePixelPer();
    }

    public static GraphicsConfiguration getDefaultConfiguration() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        return gd.getDefaultConfiguration();
    }

    /**
     * Recalculate the new bounding box of the image based on the previous [min,max] bbox 
     * and the new box after rotation [c,d]. 
     * The new bbox defined in [min.max] will retain the extreme values of both boxes. 
     * @param oldMin the original box min point, before rotation
     * @param oldMax the original box max point, before rotation
     * @param c the new box min point, after rotation
     * @param d the new box max point, after rotation
     */
    private EastNorthBound getNewBounding(EastNorth oldMin, EastNorth oldMax, EastNorth c, EastNorth d) {
        EastNorth pt[] = new EastNorth[4];
        pt[0] = oldMin;
        pt[1] = oldMax;
        pt[2] = c;
        pt[3] = d;
        double smallestEast = Double.MAX_VALUE;
        double smallestNorth = Double.MAX_VALUE;
        double highestEast = Double.MIN_VALUE;
        double highestNorth = Double.MIN_VALUE;
        for(int i=0; i<=3; i++) {
            smallestEast = Math.min(pt[i].east(), smallestEast);
            smallestNorth = Math.min(pt[i].north(), smallestNorth);
            highestEast = Math.max(pt[i].east(), highestEast);
            highestNorth = Math.max(pt[i].north(), highestNorth);
        }
        return new EastNorthBound(new EastNorth(smallestEast, smallestNorth),
                new EastNorth(highestEast, highestNorth));
    }

    public boolean contains(EastNorth en) {
        return min.east() <= en.east() && en.east() <= max.east() && min.north() <= en.north()
                && en.north() <= max.north();
    }

    public void paint(Graphics2D g, NavigatableComponent nc, boolean backgroundTransparent, float transparency,
            boolean drawBoundaries) {
        if (image == null || min == null || max == null)
            return;

        Point minPt = nc.getPoint(min), maxPt = nc.getPoint(max);

        if (!g.hitClip(minPt.x, maxPt.y, maxPt.x - minPt.x, minPt.y - maxPt.y))
            return;

        if (backgroundTransparent && transparency < 1.0f)
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
        if (drawBoundaries) {
            g.setColor(Color.green);
            if (orgCroppedRaster == null) {
                // this is the old cache format where only [min,max] bbox is stored
                g.drawRect(minPt.x, maxPt.y, maxPt.x - minPt.x, minPt.y - maxPt.y);
            } else {
                Point[] croppedPoint = new Point[5];
                for (int i=0; i<4; i++)
                    croppedPoint[i] = nc.getPoint(orgCroppedRaster[i]);
                croppedPoint[4] = croppedPoint[0]; 
                for (int i=0; i<4; i++)
                    g.drawLine(croppedPoint[i].x, croppedPoint[i].y, croppedPoint[i+1].x, croppedPoint[i+1].y);
            }
        }
        g.drawImage(image, minPt.x, maxPt.y, maxPt.x, minPt.y, // dest
                0, 0, image.getWidth(), image.getHeight(), // src
                null);
        if (backgroundTransparent && transparency < 1.0f)
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    /**
     * Is the given bbox overlapping this image ?
     */
    public boolean overlap(GeorefImage georefImage) {
        if (this.contains(georefImage.min) || this.contains(georefImage.max))
            return true;
        if (this.contains(new EastNorth(georefImage.min.east(), georefImage.max.north()))
                || this.contains(new EastNorth(georefImage.max.east(), georefImage.min.north())))
            return true;
        return false;
    }

    /**
     * Make all pixels masked by the given georefImage transparent in this image
     *
     * @param georefImage
     */
    public void withdraw(GeorefImage georefImage) {
        double minMaskEast = (georefImage.min.east() > this.min.east()) ? georefImage.min.east() : this.min.east();
        double maxMaskEast = (georefImage.max.east() < this.max.east()) ? georefImage.max.east() : this.max.east();
        double minMaskNorth = (georefImage.min.north() > this.min.north()) ? georefImage.min.north() : this.min.north();
        double maxMaskNorth = (georefImage.max.north() < this.max.north()) ? georefImage.max.north() : this.max.north();
        if ((maxMaskNorth - minMaskNorth) > 0 && (maxMaskEast - minMaskEast) > 0) {
            double pixelPerEast = (max.east() - min.east()) / image.getWidth();
            double pixelPerNorth = (max.north() - min.north()) / image.getHeight();
            int minXMaskPixel = (int) ((minMaskEast - min.east()) / pixelPerEast);
            int minYMaskPixel = (int) ((max.north() - maxMaskNorth) / pixelPerNorth);
            int widthXMaskPixel = Math.abs((int) ((maxMaskEast - minMaskEast) / pixelPerEast));
            int heightYMaskPixel = Math.abs((int) ((maxMaskNorth - minMaskNorth) / pixelPerNorth));
            Graphics g = image.getGraphics();
            for (int x = minXMaskPixel; x < minXMaskPixel + widthXMaskPixel; x++)
                for (int y = minYMaskPixel; y < minYMaskPixel + heightYMaskPixel; y++)
                    image.setRGB(x, y, VectorImageModifier.cadastreBackgroundTransp);
            g.dispose();
        }
    }

    /**
     * Method required by BufferedImage serialization.
     * Save only primitives to keep cache independent of software changes.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        if (WMSLayer.currentFormat == 2 || WMSLayer.currentFormat == 3) {
            max = new EastNorth(in.readDouble(), in.readDouble());
            min = new EastNorth(in.readDouble(), in.readDouble());
        }
        if (WMSLayer.currentFormat == 3) {
            orgRaster = new EastNorth[4];
            orgCroppedRaster = new EastNorth[4];
            angle = in.readDouble();
            orgRaster[0] = new EastNorth(in.readDouble(), in.readDouble());
            orgRaster[1] = new EastNorth(in.readDouble(), in.readDouble());
            orgRaster[2] = new EastNorth(in.readDouble(), in.readDouble());
            orgRaster[3] = new EastNorth(in.readDouble(), in.readDouble());
            orgCroppedRaster[0] = new EastNorth(in.readDouble(), in.readDouble());
            orgCroppedRaster[1] = new EastNorth(in.readDouble(), in.readDouble());
            orgCroppedRaster[2] = new EastNorth(in.readDouble(), in.readDouble());
            orgCroppedRaster[3] = new EastNorth(in.readDouble(), in.readDouble());
        } else {
            orgRaster = null;
            orgCroppedRaster = null;
            angle = 0;
        }
        image = (BufferedImage) ImageIO.read(ImageIO.createImageInputStream(in));
        updatePixelPer();
    }

    /**
     * Method required by BufferedImage serialization.
     * Use only primitives for stability in time (not influenced by josm-core changes).
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeDouble(max.getX()); out.writeDouble(max.getY());
        out.writeDouble(min.getX()); out.writeDouble(min.getY());
        if (orgRaster == null) { // just in case we save an old format layer already cached
            orgRaster = new EastNorth[4];
            orgCroppedRaster = new EastNorth[4];
        }
        out.writeDouble(angle);
        out.writeDouble(orgRaster[0].getX()); out.writeDouble(orgRaster[0].getY());
        out.writeDouble(orgRaster[1].getX()); out.writeDouble(orgRaster[1].getY());
        out.writeDouble(orgRaster[2].getX()); out.writeDouble(orgRaster[2].getY());
        out.writeDouble(orgRaster[3].getX()); out.writeDouble(orgRaster[3].getY());
        out.writeDouble(orgCroppedRaster[0].getX()); out.writeDouble(orgCroppedRaster[0].getY());
        out.writeDouble(orgCroppedRaster[1].getX()); out.writeDouble(orgCroppedRaster[1].getY());
        out.writeDouble(orgCroppedRaster[2].getX()); out.writeDouble(orgCroppedRaster[2].getY());
        out.writeDouble(orgCroppedRaster[3].getX()); out.writeDouble(orgCroppedRaster[3].getY());
        ImageIO.write(image, "png", ImageIO.createImageOutputStream(out));
    }

    private void updatePixelPer() {
        pixelPerEast = image.getWidth()/(max.east()-min.east());
        pixelPerNorth = image.getHeight()/(max.north()-min.north());
    }

    public double getPixelPerEast() {
        return pixelPerEast;
    }

    public double getPixelPerNorth() {
        return pixelPerNorth;
    }

    @Override
    public String toString() {
        return "GeorefImage[min=" + min + ", max=" + max + ", image" + image + "]";
    }

    /*
     * Following methods are used for affine transformation of two points p1 and p2
     */
    /**
     * Add a translation (dx, dy) to this image min,max coordinates
     * @param dx delta added to X image coordinate
     * @param dy delta added to Y image coordinate
     */
    public void shear(double dx, double dy) {
        min = new EastNorth(min.east() + dx, min.north() + dy);
        max = new EastNorth(max.east() + dx, max.north() + dy);
        for (int i=0; i<4; i++) {
            orgRaster[i] = new EastNorth(orgRaster[i].east() + dx, orgRaster[i].north() + dy);
            orgCroppedRaster[i] = new EastNorth(orgCroppedRaster[i].east() + dx, orgCroppedRaster[i].north() + dy);
        }
    }
    
    /**
     * Change this image scale by moving the min,max coordinates around an anchor
     * @param anchor 
     * @param proportion
     */
    public void scale(EastNorth anchor, double proportion) {
        min = anchor.interpolate(min, proportion);
        max = anchor.interpolate(max, proportion);
        for (int i=0; i<4; i++) {
            orgRaster[i] = anchor.interpolate(orgRaster[i], proportion);
            orgCroppedRaster[i] = anchor.interpolate(orgCroppedRaster[i], proportion);
        }
        updatePixelPer();
    }

    /**
     * Rotate this image and its min/max coordinates around anchor point
     * @param anchor anchor of rotation
     * @param ang angle of rotation (in radian)
     */
    public void rotate(EastNorth anchor, double ang) {
        // rotate the bounding boxes coordinates first
        EastNorth min2 = new EastNorth(orgRaster[0].east(), orgRaster[2].north());
        EastNorth max2 = new EastNorth(orgRaster[2].east(), orgRaster[0].north());
        for (int i=0; i<4; i++) {
            orgRaster[i] = orgRaster[i].rotate(anchor, ang);
            orgCroppedRaster[i] = orgCroppedRaster[i].rotate(anchor, ang);
        }
        min2 = min2.rotate(anchor, ang);
        max2 = max2.rotate(anchor, ang);
        EastNorthBound enb = getNewBounding(orgCroppedRaster[0], orgCroppedRaster[2], min2, max2);
        min = enb.min;
        max = enb.max;
        angle=+ang;
        
        // rotate the image now
        double sin = Math.abs(Math.sin(ang)), cos = Math.abs(Math.cos(ang));
        int w = image.getWidth(), h = image.getHeight();
        int neww = (int)Math.floor(w*cos+h*sin), newh = (int)Math.floor(h*cos+w*sin);
        GraphicsConfiguration gc = getDefaultConfiguration();
        BufferedImage result = gc.createCompatibleImage(neww, newh, Transparency.TRANSLUCENT);
        Graphics2D g = result.createGraphics();
        g.translate((neww-w)/2, (newh-h)/2);
        g.rotate(ang, w/2, h/2);
        g.drawRenderedImage(image, null);
        g.dispose();
        image = result;
    }

}

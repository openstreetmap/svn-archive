// License: GPL. v2 and later. Copyright 2008-2009 by Pieren <pieren3@gmail.com> and others
package cadastre_fr;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.gui.NavigatableComponent;

public class GeorefImage implements Serializable, ImageObserver, Cloneable {
    private static final long serialVersionUID = 1L;

    // bbox of the georeferenced image (the nice horizontal and vertical box)
    public EastNorth min;
    public EastNorth max;
    // offset for vector images temporarily shifted (correcting Cadastre artifacts), in pixels
    public double deltaEast=0;
    public double deltaNorth=0;
    // bbox of the georeferenced original image (raster only) (inclined if rotated and before cropping)
    // P[0] is bottom,left then next are clockwise.
    public EastNorth[] orgRaster = new EastNorth[4];
    // bbox of the georeferenced original image (raster only) after cropping
    public EastNorth[] orgCroppedRaster = new EastNorth[4];
    // angle with georeferenced original image after rotation (raster images only)(in radian)
    public double angle = 0;
    public int imageOriginalHeight = 0;
    public int imageOriginalWidth = 0;

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
        // img can be null for a hack used in overlapping detection
        this.imageOriginalHeight = (img == null ? 1 : img.getHeight());
        this.imageOriginalWidth = (img == null ? 1 : img.getWidth());
        updatePixelPer();
    }

    public static GraphicsConfiguration getDefaultConfiguration() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        return gd.getDefaultConfiguration();
    }

    /**
     * Recalculate the new bounding box of the image based on the four points provided as parameters.
     * The new bbox defined in [min.max] will retain the extreme values of both boxes.
     * @param p1 one of the bounding box corner
     * @param p2 one of the bounding box corner
     * @param p3 one of the bounding box corner
     * @param p4 one of the bounding box corner
     */
    private EastNorthBound computeNewBounding(EastNorth p1, EastNorth p2, EastNorth p3, EastNorth p4) {
        EastNorth pt[] = new EastNorth[4];
        pt[0] = p1;
        pt[1] = p2;
        pt[2] = p3;
        pt[3] = p4;
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

        // apply offsets defined manually when vector images are translated manually (not saved in cache)
        Point minPt = nc.getPoint(new EastNorth(min.east()+deltaEast, min.north()+deltaNorth));
        Point maxPt = nc.getPoint(new EastNorth(max.east()+deltaEast, max.north()+deltaNorth));

        if (!g.hitClip(minPt.x, maxPt.y, maxPt.x - minPt.x, minPt.y - maxPt.y))
            return;

        if (backgroundTransparent && transparency < 1.0f)
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
        if (drawBoundaries) {
            if (orgCroppedRaster == null) {
                // this is the old cache format where only [min,max] bbox is stored
                g.setColor(Color.green);
                g.drawRect(minPt.x, maxPt.y, maxPt.x - minPt.x, minPt.y - maxPt.y);
            } else {
                Point[] croppedPoint = new Point[5];
                for (int i=0; i<4; i++)
                    croppedPoint[i] = nc.getPoint(orgCroppedRaster[i]);
                croppedPoint[4] = croppedPoint[0];
                for (int i=0; i<4; i++) {
                    g.setColor(Color.green);
                    g.drawLine(croppedPoint[i].x, croppedPoint[i].y, croppedPoint[i+1].x, croppedPoint[i+1].y);
                }
                /*
                //Uncomment this section to display the original image size (before cropping)
                Point[] orgPoint = new Point[5];
                for (int i=0; i<4; i++)
                    orgPoint[i] = nc.getPoint(orgRaster[i]);
                orgPoint[4] = orgPoint[0];
                for (int i=0; i<4; i++) {
                  g.setColor(Color.red);
                  g.drawLine(orgPoint[i].x, orgPoint[i].y, orgPoint[i+1].x, orgPoint[i+1].y);
                }
                */
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
            double pxPerEast = (max.east() - min.east()) / image.getWidth();
            double pxPerNorth = (max.north() - min.north()) / image.getHeight();
            int minXMaskPixel = (int) ((minMaskEast - min.east()) / pxPerEast);
            int minYMaskPixel = (int) ((max.north() - maxMaskNorth) / pxPerNorth);
            int widthXMaskPixel = Math.abs((int) ((maxMaskEast - minMaskEast) / pxPerEast));
            int heightYMaskPixel = Math.abs((int) ((maxMaskNorth - minMaskNorth) / pxPerNorth));
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
        if (WMSLayer.currentFormat >= 2) {
            max = new EastNorth(in.readDouble(), in.readDouble());
            min = new EastNorth(in.readDouble(), in.readDouble());
        }
        orgRaster = null;
        orgCroppedRaster = null;
        if (WMSLayer.currentFormat >= 3) {
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
        }
        if (WMSLayer.currentFormat >= 4) {
            imageOriginalHeight = in.readInt();
            imageOriginalWidth =  in.readInt();
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
        // Write image as a format 3 if cache was loaded with this format to avoid incompatibilities.
        if (WMSLayer.currentFormat >= 4) {
            out.writeInt(imageOriginalHeight);
            out.writeInt(imageOriginalWidth);
        }
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
     * @param old_ang previous angle of image before rotation (0 the first time)(in radian)
     * @param delta_ang angle of rotation (in radian)
     */
    public void rotate(EastNorth anchor, double delta_ang) {
        if (orgRaster == null || orgCroppedRaster == null)
            return;
        // rotate the bounding boxes coordinates first
        for (int i=0; i<4; i++) {
            orgRaster[i] = orgRaster[i].rotate(anchor, delta_ang);
            orgCroppedRaster[i] = orgCroppedRaster[i].rotate(anchor, delta_ang);
        }
        // rotate the image now
        double sin = Math.abs(Math.sin(angle+delta_ang)), cos = Math.abs(Math.cos(angle+delta_ang));
        int w = imageOriginalWidth, h = imageOriginalHeight;
        int neww = (int)Math.floor(w*cos+h*sin);
        int newh = (int)Math.floor(h*cos+w*sin);
        GraphicsConfiguration gc = getDefaultConfiguration();
        BufferedImage result = gc.createCompatibleImage(neww, newh, image.getTransparency());
        Graphics2D g = result.createGraphics();
        g.translate((neww-image.getWidth())/2, (newh-image.getHeight())/2);
        g.rotate(delta_ang, image.getWidth()/2, image.getHeight()/2);
        g.drawRenderedImage(image, null);
        g.dispose();
        image = result;
        EastNorthBound enb = computeNewBounding(orgCroppedRaster[0], orgCroppedRaster[1], orgCroppedRaster[2], orgCroppedRaster[3]);
        min = enb.min;
        max = enb.max;
        angle+=delta_ang;
    }

    /**
     * Crop the image based on new bbox coordinates adj1 and adj2 (for raster images only).
     * @param adj1 is the new corner bottom, left
     * @param adj2 is the new corner top, right
     */
    public void crop(EastNorth adj1, EastNorth adj2) {
        // s1 and s2 have 0,0 at top, left where all EastNorth coord. have 0,0 at bottom, left
        int sx1 = (int)((adj1.getX() - min.getX())*getPixelPerEast());
        int sy1 = (int)((max.getY() - adj2.getY())*getPixelPerNorth());
        int sx2 = (int)((adj2.getX() - min.getX())*getPixelPerEast());
        int sy2 = (int)((max.getY() - adj1.getY())*getPixelPerNorth());
        int newWidth = Math.abs(sx2 - sx1);
        int newHeight = Math.abs(sy2 - sy1);
        BufferedImage new_img = new BufferedImage(newWidth, newHeight, image.getType());
        Graphics g = new_img.getGraphics();
        g.drawImage(image, 0, 0, newWidth-1, newHeight-1,
                sx1, sy1, sx2, sy2,
                this);
        image = new_img;
        this.min = adj1;
        this.max = adj2;
        this.orgCroppedRaster[0] = min;
        this.orgCroppedRaster[1] = new EastNorth(min.east(), max.north());
        this.orgCroppedRaster[2] = max;
        this.orgCroppedRaster[3] = new EastNorth(max.east(), min.north());
        this.imageOriginalWidth = newWidth;
        this.imageOriginalHeight = newHeight;
        updatePixelPer();
    }

    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y,
            int width, int height) {
        return false;
    }

    /**
     * Add a temporary translation (dx, dy) to this image (for vector images only)
     * @param dx delta added to X image coordinate
     * @param dy delta added to Y image coordinate
     */
    public void tempShear(double dx, double dy) {
        this.deltaEast+=dx;
        this.deltaNorth+=dy;
    }
}

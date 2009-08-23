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

    public EastNorth min;
    public EastNorth max;
    public BufferedImage image;

    private double pixelPerEast;
    private double pixelPerNorth;

    public GeorefImage(BufferedImage img, EastNorth min, EastNorth max) {
        image = img;
        this.min = min;
        this.max = max;
        updatePixelPer();
    }

    public static GraphicsConfiguration getDefaultConfiguration() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        return gd.getDefaultConfiguration();
    }

    private void getNewBounding(EastNorth min, EastNorth max, EastNorth c, EastNorth d) {
        EastNorth pt[] = new EastNorth[4];
        pt[0] = min;
        pt[1] = max;
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
        min.setLocation(smallestEast, smallestNorth);
        max.setLocation(highestEast, highestNorth);
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
            g.drawRect(minPt.x, maxPt.y, maxPt.x - minPt.x, minPt.y - maxPt.y);
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

    /*
     * Method required by BufferedImage serialization
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        max = new EastNorth((Double)in.readObject(), (Double)in.readObject());
        min = new EastNorth((Double)in.readObject(), (Double)in.readObject());
        image = (BufferedImage) ImageIO.read(ImageIO.createImageInputStream(in));
        updatePixelPer();
    }

    /*
     * Method required by BufferedImage serialization
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(max.getX());
        out.writeObject(max.getY());
        out.writeObject(min.getX());
        out.writeObject(min.getY());
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
    }
    
    /**
     * Change this image scale by moving the min,max coordinates around an anchor
     * @param anchor 
     * @param proportion
     */
    public void scale(EastNorth anchor, double proportion) {
        min = anchor.interpolate(min, proportion);
        max = anchor.interpolate(max, proportion);
        updatePixelPer();
    }

    /**
     * Rotate this image and its min/max coordinates around anchor point
     * @param anchor anchor of rotation
     * @param angle angle of rotation (in radians)
     */
    public void rotate(EastNorth anchor, double angle) {
        EastNorth min2 = new EastNorth(min.east(), max.north());
        EastNorth max2 = new EastNorth(max.east(), min.north());
        min = min.rotate(anchor, angle);
        max = max.rotate(anchor, angle);
        min2 = min2.rotate(anchor, angle);
        max2 = max2.rotate(anchor, angle);
        getNewBounding(min, max, min2, max2);
        image = tilt(image, angle);
    }

    /**
     * Rotate by copying original buffered image into a new one with new dimensions 
     * @param image
     * @param angle
     * @return
     */
    public static BufferedImage tilt(BufferedImage image, double angle) {
        double sin = Math.abs(Math.sin(angle)), cos = Math.abs(Math.cos(angle));
        int w = image.getWidth(), h = image.getHeight();
        int neww = (int)Math.floor(w*cos+h*sin), newh = (int)Math.floor(h*cos+w*sin);
        GraphicsConfiguration gc = getDefaultConfiguration();
        BufferedImage result = gc.createCompatibleImage(neww, newh, Transparency.TRANSLUCENT);
        Graphics2D g = result.createGraphics();
        g.translate((neww-w)/2, (newh-h)/2);
        g.rotate(angle, w/2, h/2);
        g.drawRenderedImage(image, null);
        g.dispose();
        return result;
    }

}

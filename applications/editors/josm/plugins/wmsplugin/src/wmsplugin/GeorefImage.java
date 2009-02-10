package wmsplugin;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.gui.NavigatableComponent;

public class GeorefImage implements Serializable {
    public BufferedImage image = null;
    private BufferedImage reImg = null;
    private Dimension reImgHash = new Dimension(0, 0);
    public EastNorth min, max;
    public boolean downloadingStarted;

    public GeorefImage(boolean downloadingStarted) {
        this.downloadingStarted = downloadingStarted;
    }

    public boolean contains(EastNorth en, double dx, double dy) {
        return min.east()+dx <= en.east() && en.east() <= max.east()+dx
            && min.north()+dy <= en.north() && en.north() <= max.north()+dy;
    }

    /* this does not take dx and dy offset into account! */
    public boolean isVisible(NavigatableComponent nc) {
        Point minPt = nc.getPoint(min), maxPt = nc.getPoint(max);
        Graphics g = nc.getGraphics();

        return (g.hitClip(minPt.x, maxPt.y,
                maxPt.x - minPt.x, minPt.y - maxPt.y));
    }

    public boolean paint(Graphics g, NavigatableComponent nc, double dx, double dy) {
        if (image == null || min == null || max == null) return false;

        EastNorth mi = new EastNorth(min.east()+dx, min.north()+dy);
        EastNorth ma = new EastNorth(max.east()+dx, max.north()+dy);
        Point minPt = nc.getPoint(mi), maxPt = nc.getPoint(ma);

        // downloadAndPaintVisible in WMSLayer.java requests visible images only
        // so this path is never hit. 
        /* this is isVisible() but taking dx, dy into account */
        /*if(!(g.hitClip(minPt.x, maxPt.y, maxPt.x - minPt.x, minPt.y - maxPt.y))) {
            return false;
        }*/
        
        // Width and height flicker about 2 pixels due to rounding errors, typically only 1
        int width = Math.abs(maxPt.x-minPt.x);
        int height = Math.abs(minPt.y-maxPt.y);
        int diffx = reImgHash.width - width;
        int diffy = reImgHash.height - height;
        
        // We still need to re-render if the requested size is larger (otherwise we'll have black lines)
        // If it's only up to two pixels smaller, just draw the old image, the errors are minimal
        // but the performance improvements when moving are huge
        // Zooming is still slow because the images need to be resized
        if(diffx >= 0 && diffx <= 2 && diffy >= 0 && diffy <= 2 && reImg != null) {
            g.drawImage(reImg, minPt.x, maxPt.y, null);
            return true;
        }

        // We haven't got a saved resized copy, so resize and cache it        
        reImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        reImg.getGraphics().drawImage(image,
            0, 0, width, height, // dest
            0, 0, image.getWidth(), image.getHeight(), // src
            null);

        reImgHash.setSize(width, height);        
        g.drawImage(reImg, minPt.x, maxPt.y, null);
        return true;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        max = (EastNorth) in.readObject();
        min = (EastNorth) in.readObject();
        image = (BufferedImage) ImageIO.read(ImageIO.createImageInputStream(in));
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(max);
        out.writeObject(min);
        if(image == null)
            out.writeObject(null);
        else
            ImageIO.write(image, "png", ImageIO.createImageOutputStream(out));
    }
}

package wmsplugin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.gui.NavigatableComponent;

public class GeorefImage implements Serializable {
    public BufferedImage image = null;
    private Image reImg = null;
    private Dimension reImgHash = new Dimension(0, 0);
    public EastNorth min, max;
    public boolean downloadingStarted;
    public boolean failed = false;

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

        /* this is isVisible() but taking dx, dy into account */
        if(!(g.hitClip(minPt.x, maxPt.y, maxPt.x - minPt.x, minPt.y - maxPt.y))) {
            return false;
        }

        // Width and height flicker about 2 pixels due to rounding errors, typically only 1
        int width = Math.abs(maxPt.x-minPt.x);
        int height = Math.abs(minPt.y-maxPt.y);
        int diffx, diffy;
        try {
            diffx = reImgHash.width - width;
            diffy = reImgHash.height - height;
        } catch(Exception e) {
            reImgHash = new Dimension(0, 0);
            diffx = 99;
            diffy = 99;
        }
        // This happens if you zoom outside the world
        if(width == 0 || height == 0)
            return false;

        // We still need to re-render if the requested size is larger (otherwise we'll have black lines)
        // If it's only up to two pixels smaller, just draw the old image, the errors are minimal
        // but the performance improvements when moving are huge
        // Zooming is still slow because the images need to be resized
        if(diffx >= 0 && diffx <= 2 && diffy >= 0 && diffy <= 2 && reImg != null) {
            /*g.setColor(Color.RED);
              g.drawRect(minPt.x, minPt.y-height, width, height);*/
            g.drawImage(reImg, minPt.x, maxPt.y, null);
            return true;
        }

        boolean alphaChannel = Main.pref.getBoolean("wmsplugin.alpha_channel");

        try {
            if(reImg != null) reImg.flush();
            long freeMem = Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory();
            //System.out.println("Free Memory:           "+ (freeMem/1024/1024) +" MB");
            // Notice that this value can get negative due to integer overflows
            //System.out.println("Img Size:              "+ (width*height*3/1024/1024) +" MB");

            int multipl = alphaChannel ? 4 : 3;
            // This happens when requesting images while zoomed out and then zooming in
            // Storing images this large in memory will certainly hang up JOSM. Luckily
            // traditional rendering is as fast at these zoom levels, so it's no loss.
            // Also prevent caching if we're out of memory soon
            if(width > 2000 || height > 2000 || width*height*multipl > freeMem) {
                fallbackDraw(g, image, minPt, maxPt);
            } else {
                // We haven't got a saved resized copy, so resize and cache it
                reImg = new BufferedImage(width, height,
                    alphaChannel
                        ? BufferedImage.TYPE_INT_ARGB
                        : BufferedImage.TYPE_3BYTE_BGR  // This removes alpha
                    );
                reImg.getGraphics().drawImage(image,
                    0, 0, width, height, // dest
                    0, 0, image.getWidth(null), image.getHeight(null), // src
                    null);
                reImg.getGraphics().dispose();

                reImgHash.setSize(width, height);
                /*g.setColor(Color.RED);
                  g.drawRect(minPt.x, minPt.y-height, width, height);*/
                g.drawImage(reImg, minPt.x, maxPt.y, null);
            }
        } catch(Exception e) {
            fallbackDraw(g, image, minPt, maxPt);
        }
        return true;
    }

    private void fallbackDraw(Graphics g, Image img, Point min, Point max) {
        if(reImg != null) reImg.flush();
        reImg = null;
        g.drawImage(img,
            min.x, max.y, max.x, min.y, // dest
            0, 0, img.getWidth(null), img.getHeight(null), // src
            null);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        max = (EastNorth) in.readObject();
        min = (EastNorth) in.readObject();
        boolean hasImage = in.readBoolean();
        if (hasImage)
        	image = (BufferedImage) ImageIO.read(ImageIO.createImageInputStream(in));
        else {
        	in.readObject(); // read null from input stream
        	image = null;
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(max);
        out.writeObject(min);
        if(image == null) {
        	out.writeBoolean(false);
            out.writeObject(null);
        } else {
        	out.writeBoolean(true);
            ImageIO.write(image, "png", ImageIO.createImageOutputStream(out));
        }
    }

    public void flushedResizedCachedInstance() {
        reImg = null;
    }
}

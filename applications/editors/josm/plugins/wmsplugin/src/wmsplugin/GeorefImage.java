package wmsplugin;

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

        /* this is isVisible() but taking dx, dy into account */
        if(!(g.hitClip(minPt.x, maxPt.y,
                maxPt.x - minPt.x, minPt.y - maxPt.y)))
            return false;

        g.drawImage(image,
            minPt.x, maxPt.y, maxPt.x, minPt.y, // dest
            0, 0, image.getWidth(), image.getHeight(), // src
            null);

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

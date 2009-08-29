package wmsplugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.ProjectionBounds;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.io.CacheFiles;

abstract public class Grabber extends Thread {
    protected ProjectionBounds b;
    protected Projection proj;
    protected double pixelPerDegree;
    protected MapView mv;
    protected WMSLayer layer;
    protected GeorefImage image;
    protected CacheFiles cache;

    Grabber(ProjectionBounds b, GeorefImage image, MapView mv, WMSLayer layer, CacheFiles cache)
    {
        this.b = b;
        this.proj = Main.proj;
        this.pixelPerDegree = layer.pixelPerDegree;
        this.image = image;
        this.mv = mv;
        this.layer = layer;
        this.cache = cache;
    }

    abstract void fetch() throws Exception; // the image fetch code

    int width(){
        return (int) ((b.max.north() - b.min.north()) * pixelPerDegree);
    }
    int height(){
        return (int) ((b.max.east() - b.min.east()) * pixelPerDegree);
    }

    protected void grabError(Exception e){ // report error when grabing image
        e.printStackTrace();

        BufferedImage img = new BufferedImage(width(), height(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.getGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, width(), height());
        Font font = g.getFont();
        Font tempFont = font.deriveFont(Font.PLAIN).deriveFont(36.0f);
        g.setFont(tempFont);
        g.setColor(Color.BLACK);
        g.drawString(tr("Exception occurred"), 10, height()/2);
        image.image = img;
        image.flushedResizedCachedInstance();
        image.failed = true;
        g.setFont(font);
    }

    protected void attempt(){ // try to fetch the image
        int maxTries = 5; // n tries for every image
        for (int i = 1; i <= maxTries; i++) {
            try {
                fetch();
                break; // break out of the retry loop
            } catch (Exception e) {
                try { // sleep some time and then ask the server again
                    Thread.sleep(random(1000, 2000));
                } catch (InterruptedException e1) {}

                if(i == maxTries) grabError(e);
            }
        }
    }

    public static int random(int min, int max) {
        return (int)(Math.random() * ((max+1)-min) ) + min;
    }
}

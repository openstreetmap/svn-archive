package org.openstreetmap.josm.plugins.walkingpapers;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Class that contains information about one single slippy map tile.
 * 
 * @author Frederik Ramm <frederik@remote.org>
 * @author LuVar <lubomir.varga@freemap.sk>
 * @author Dave Hansen <dave@sr71.net>
 * 
 */
public class WalkingPapersTile {
    private Image tileImage;
	long timestamp;

    int x;
    int y;
    int z;
    
    WalkingPapersLayer parentLayer;


    public WalkingPapersTile(int x, int y, int z, WalkingPapersLayer parent) {
        this.x = x;
        this.y = y;
        this.z = z;
        parentLayer = parent;
		timestamp = System.currentTimeMillis();
    }

    public URL getImageUrl() {
        try {
            return new URL("http://paperwalking-uploads.s3.amazonaws.com/scans/" + parentLayer.getWalkingPapersId() + "/" + z + "/" + x + "/" + y + ".jpg");
        } catch (MalformedURLException mfu) {
        	mfu.printStackTrace();
        }
        return null;
    }

    public void loadImage() {
        URL imageUrl = this.getImageUrl();
        tileImage = Toolkit.getDefaultToolkit().createImage(imageUrl);
		Toolkit.getDefaultToolkit().sync();
   		timestamp = System.currentTimeMillis();
    }

    public Image getImage() {
        timestamp = System.currentTimeMillis();
        return tileImage;
    }

    public void dropImage() {
		tileImage = null;
		//  This should work in theory but doesn't seem to actually
		//  reduce the X server memory usage
		//tileImage.flush();
    }

    public long access_time() {
        return timestamp;
    }

    public boolean equals(Object o) {
        if (!(o instanceof WalkingPapersTile))
            return false;
        WalkingPapersTile other = (WalkingPapersTile) o;
        return (this.x == other.x && this.y == other.y && this.z == other.z);
    }
}

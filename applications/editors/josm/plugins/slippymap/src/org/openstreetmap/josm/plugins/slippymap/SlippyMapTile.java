package org.openstreetmap.josm.plugins.slippymap;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
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
public class SlippyMapTile implements ImageObserver
{
    private Image  tileImage;
	private long timestamp;

	private int x;
	private int y;
	private int z;
	// Setting this to pending is a bit of a hack
	// as it requires knowledge that SlippyMapLayer
	// will put this tile in a queue before it calls
	// loadImage().  But, this gives the best message
	// to the user.
	private String status = "pending download";
    
    private boolean imageDownloaded = false;

    private String metadata;

    public SlippyMapTile(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
		timestamp = System.currentTimeMillis();
    }

    public String getStatus()
    {
        return status;
    }
    public String getMetadata()
    {
        return metadata;
    }

    public URL getImageURL()
    {
        try
        {
            return new URL(SlippyMapPreferences.getMapUrl() + "/" + z + "/" + x + "/" + y + ".png");
        }
        catch (MalformedURLException mfu)
        {
            mfu.printStackTrace();
        }
            return null;
        }

    public Image loadImage()
    {
		// We do not update the timestamp in this function
		// The download code prioritizes the most recent
		// downloads and will download the oldest tiles last.
        URL imageURL = this.getImageURL();
        tileImage = Toolkit.getDefaultToolkit().createImage(imageURL);
        Toolkit.getDefaultToolkit().prepareImage(tileImage, -1, -1, this);
		Toolkit.getDefaultToolkit().sync();
		status = "being downloaded";
		return tileImage;
    }
	public String toString()
	{
			return "SlippyMapTile{zoom=" + z + " (" + x + "," + y + ") '" + status + "'}";
	}
	synchronized public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
	{
		if ((infoflags & ALLBITS) != 0) {
    	    imageDownloaded = true;
	        status = "downloaded";
			if (tileImage == null) {
                System.out.println("completed null'd image: " + this.toString());
			}
			tileImage = img;
			return false;
        }
		return true;
	}

    public Image getImageNoTimestamp() {
    	return tileImage;
    }
    
    public Image getImage()
    {
        timestamp = System.currentTimeMillis();
        return tileImage;
    }

    public int getZoom() {
    	return z;
    }
    
    synchronized public void dropImage()
    {
		if(tileImage != null) {
			tileImage.flush();
		    status = "dropped";
		}
		tileImage = null;
		//  This should work in theory but doesn't seem to actually
		//  reduce the X server memory usage
		//tileImage.flush();
	    imageDownloaded = false;
    }
    
    public boolean isDownloaded() {
    	return imageDownloaded;
    }
    
    public void loadMetadata()
    {
        try
        {
            URL dev = new URL(
                    "http://tah.openstreetmap.org/Tiles/info_short.php?x=" + x
                            + "&y=" + y + "&z=" + z + "/layer=tile");
            URLConnection devc = dev.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(devc
                    .getInputStream()));
            metadata = tr(in.readLine());
        }
        catch (Exception ex)
        {
            metadata = tr("error loading metadata" + ex.toString());
        }

    }

    public void requestUpdate()
    {
		if (z != 12) {
            metadata = tr("error requesting update: not zoom-level 12");
		}
        try
        {
            URL dev = new URL("http://tah.openstreetmap.org/Request/create/?x=" + x
                    + "&y=" + y + "&priority=1&src=slippymap_plugin");
            URLConnection devc = dev.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(devc
                    .getInputStream()));
			timestamp = System.currentTimeMillis();
            metadata = tr("requested: {0}", tr(in.readLine()));
        }
        catch (Exception ex)
        {
            metadata = tr("error requesting update");
        }
    }

    public long access_time()
    {
        return timestamp;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof SlippyMapTile))
            return false;
        SlippyMapTile other = (SlippyMapTile) o;
        return (this.x == other.x && this.y == other.y && this.z == other.z);
    }
}

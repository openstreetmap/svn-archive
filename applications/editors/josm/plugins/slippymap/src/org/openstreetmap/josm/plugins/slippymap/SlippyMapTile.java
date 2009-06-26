package org.openstreetmap.josm.plugins.slippymap;

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
public class SlippyMapTile
{
    private Image  tileImage;
	private long timestamp;

	private int x;
	private int y;
	private int z;
    
    private boolean imageDownloaded = false;

    private String metadata;

    public SlippyMapTile(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
		timestamp = System.currentTimeMillis();
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

    public void loadImage()
    {
        URL imageURL = this.getImageURL();
        tileImage = Toolkit.getDefaultToolkit().createImage(imageURL);
		Toolkit.getDefaultToolkit().sync();
   		timestamp = System.currentTimeMillis();
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
    
    public void dropImage()
    {
		if(tileImage != null) {
			tileImage.flush();
		}
		tileImage = null;
		//  This should work in theory but doesn't seem to actually
		//  reduce the X server memory usage
		//tileImage.flush();
    }
    
    public void markAsDownloaded() {
    	imageDownloaded = true;
    }
    
    public boolean isDownloaded() {
    	return imageDownloaded;
    }
    
    public void abortDownload() {
    	if (imageDownloaded) {
    		return;
    	}
    	dropImage();
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

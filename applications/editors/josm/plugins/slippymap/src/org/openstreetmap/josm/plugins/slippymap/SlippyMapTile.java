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
 * 
 */
public class SlippyMapTile
{
    private Image  tileImage;

    int            x;
    int            y;
    int            z;

    private String metadata;

    public SlippyMapTile(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String getMetadata()
    {
        return metadata;
    }

    public void loadImage()
    {
        try
        {
            URL imageURL = new URL(SlippyMapPreferences.getMapUrl() + "/" + z
                    + "/" + x + "/" + y + ".png");

            tileImage = Toolkit.getDefaultToolkit().createImage(imageURL);
        }
        catch (MalformedURLException mfu)
        {
            mfu.printStackTrace();
        }
    }

    public Image getImage()
    {
        return tileImage;
    }

    public void loadMetadata()
    {
        try
        {
            URL dev = new URL(
                    "http://tah.openstreetmap.org/Tiles/info_short.php?x=" + x
                            + "&y=" + y + "&z=12/layer=tile");
            URLConnection devc = dev.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(devc
                    .getInputStream()));
            metadata = tr(in.readLine());
        }
        catch (Exception ex)
        {
            metadata = tr("error loading metadata");
        }

    }

    public void requestUpdate()
    {
        try
        {
            URL dev = new URL("http://tah.openstreetmap.org/NeedRender?x=" + x
                    + "&y=" + y + "&priority=1&src=slippymap_plugin");
            URLConnection devc = dev.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(devc
                    .getInputStream()));
            metadata = tr("requested: {0}", tr(in.readLine()));
        }
        catch (Exception ex)
        {
            metadata = tr("error requesting update");
        }
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof SlippyMapTile))
            return false;
        SlippyMapTile other = (SlippyMapTile) o;
        return (this.x == other.x && this.y == other.y && this.z == other.z);
    }
}

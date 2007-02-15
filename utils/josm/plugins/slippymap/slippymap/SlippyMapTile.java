package slippymap;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

/**
 * Class that contains information about one single slippy map tile.
 * 
 * @author Frederik Ramm <frederik@remote.org>
 *
 */
public class SlippyMapTile 
{

	private Image tileImage;
	
	int x;
	int y;
	int z;
	
	public SlippyMapTile(int x, int y, int z) 
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public String getMetadata()
	{
		return "";
	}
	
	public void loadImage()
	{
		try
		{
			// tileImage = new ImageIcon(new URL("http://dev.openstreetmap.org/~ojw/Tiles/tile.php/"+z+"/"+x+"/"+y+".png"));
			//tileImage = Toolkit.getDefaultToolkit().createImage(new URL("http://openstreetmap.gryph.de/slippymap/tiles/"+z+"/"+x+"/"+y+".png"));
			tileImage = Toolkit.getDefaultToolkit().createImage(new URL("http://dev.openstreetmap.org/~ojw/Tiles/tile.php/"+z+"/"+x+"/"+y+".png"));
//				
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
	}
	
	public boolean equals(Object o)
	{
		if (!(o instanceof SlippyMapTile)) return false;
		SlippyMapTile other = (SlippyMapTile) o;
		return (this.x == other.x && this.y == other.y && this.z == other.z);
	}
	
}

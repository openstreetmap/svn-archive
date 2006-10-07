package landsat;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.NavigatableComponent;

public class LandsatImage
{
	String constURL;
	Image theImage;
	double grabbedScale;
	EastNorth topLeft;

	public LandsatImage(String constURL)
	{
		this.constURL = constURL;
	}

	public void grab(NavigatableComponent nc) throws IOException
	{

		EastNorth topLeft  = nc.getEastNorth(0,0);
		grabbedScale =  nc.getScale();  // scale is enPerPixel

		this.topLeft = topLeft;

		try
		{
			URL url = getURL(nc);
			doGrab(url);
		}
		catch(MalformedURLException e)
		{
			System.out.println("Illegal url. Error="+e);
		}
	}

	public void grab(NavigatableComponent nc,double minlat,double minlon,
			double maxlat,double maxlon) throws IOException
			{
		LatLon p = new LatLon(minlat,minlon),
		p2 = new LatLon(maxlat,maxlon);

		grabbedScale = nc.getScale(); // enPerPixel

		topLeft = Main.proj.latlon2eastNorth(new LatLon(maxlat,minlon));
		EastNorth bottomRight = Main.proj.latlon2eastNorth
		(new LatLon(minlat,maxlon));

		int widthPx = (int)((bottomRight.east()-topLeft.east())/grabbedScale),
		heightPx = (int)
		((topLeft.north()-bottomRight.north()) / grabbedScale);

		try
		{
			URL url =  doGetURL(p,p2,widthPx,heightPx);
			doGrab(url);
		}
		catch(MalformedURLException e)
		{
			System.out.println("Illegal url. Error="+e);
		}
			}

	private URL getURL(NavigatableComponent nc) throws MalformedURLException
	{
		double widthEN = nc.getWidth()*grabbedScale,
		heightEN = nc.getHeight()*grabbedScale;
		LatLon p = Main.proj.eastNorth2latlon(new EastNorth
				(topLeft.east(), topLeft.north()-heightEN));
		LatLon p2 = Main.proj.eastNorth2latlon(new EastNorth
				(topLeft.east()+widthEN, topLeft.north()));
		return doGetURL(p,p2,(int)(widthEN/grabbedScale),
				(int)(heightEN/grabbedScale) );
	}

	private URL doGetURL(LatLon p, LatLon p2,int w, int h)
	throws MalformedURLException
	{
		String str = constURL + "&bbox=" + p.lon() +"," + p.lat() + ","+
		p2.lon()+","+p2.lat() + "&width=" + w
		+ "&height=" + h;
		return new URL(str);
	}

	private void doGrab (URL url) throws IOException
	{
		InputStream is = url.openStream();
		theImage = ImageIO.read(is) ;
		is.close();
		Main.map.repaint();
	}

	public void paint(Graphics g,NavigatableComponent nc) /*,EastNorth bottomLeft,
		   int x1, int y1, int x2, int y2) */
	{
		if(theImage!=null)
		{
			/*
			System.out.println("x1="+x1+" y1="+y1+" x2="+x2+" y2="+y2+
								" img w="+theImage.getWidth(null) +
								" img h="+theImage.getHeight(null)) ;
			 */
			double zoomInFactor = grabbedScale / nc.getScale();

			// Find the image x and y of the supplied bottom left
			// This will be the difference in EastNorth units, divided by the
			// grabbed scale in EastNorth/pixel.

			/*
			double ix = (bottomLeft.east()-this.bottomLeft.east())/grabbedScale,
			   	   iy = theImage.getHeight(null) -
				 ((bottomLeft.north() - this.bottomLeft.north())/grabbedScale);

			g.drawImage(theImage,x1,y1,x2,y2,(int)ix,(int)iy,
					(int)(ix+((x2-x1)/zoomInFactor)),
					(int)(iy+((y2-y1)/zoomInFactor)), null);
			 */
			int w = theImage.getWidth(null), h=theImage.getHeight(null);
			Point p = nc.getPoint(topLeft);
			/*
			System.out.println("topleft: e=" + topLeft.east() + " n="+
							topLeft.north());
			System.out.println("Drawing at "+p.x+","+p.y);
			 */
			g.drawImage(theImage,p.x,p.y,
					(int)(p.x+w*zoomInFactor),
					(int)(p.y+h*zoomInFactor),
					0,0,w,h,null);
		}
	}
}
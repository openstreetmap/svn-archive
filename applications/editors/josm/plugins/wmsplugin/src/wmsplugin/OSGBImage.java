package wmsplugin;

import uk.me.jstott.jcoord.OSRef;
import uk.me.jstott.jcoord.LatLng;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.gui.NavigatableComponent;

import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.Color;

public class OSGBImage extends WMSImage
{
	public OSGBImage(String constURL)
	{
		super(constURL);
	}

	public void grab(NavigatableComponent nc,double minlat,double minlon,
			double maxlat,double maxlon) throws IOException
	{
		// To deal with the fact that grid refs and lat/lon don't align
		LatLng ll1 = new LatLng(minlat,minlon),
				ll2 = new LatLng(maxlat,maxlon),
				ll3 = new LatLng(maxlat,minlon),
				ll4 = new LatLng(minlat,maxlon);

		ll1.toOSGB36();
		ll2.toOSGB36();
		ll3.toOSGB36();
		ll4.toOSGB36();

		OSRef bottomLeftGR = ll1.toOSRef(),
			  topRightGR = ll2.toOSRef(),
		 	topLeftGR =  ll3.toOSRef(),
			  bottomRightGR =  ll4.toOSRef();

		double w = Math.min(bottomLeftGR.getEasting(),
								topLeftGR.getEasting()),
			   s = Math.min(bottomLeftGR.getNorthing(),
							   bottomRightGR.getNorthing()),
			   e = Math.max(bottomRightGR.getEasting(),
							   topRightGR.getEasting()),
			   n = Math.max(topLeftGR.getNorthing(),
							   topRightGR.getNorthing());

		// Adjust topLeft and bottomRight due to messing around with
		// projections
		LatLng tl2 = new OSRef(w,n).toLatLng();
		LatLng br2 = new OSRef(e,s).toLatLng();
		tl2.toWGS84();
		br2.toWGS84();

		topLeft = Main.proj.latlon2eastNorth
					(new LatLon(tl2.getLat(),tl2.getLng() ));
		bottomRight = Main.proj.latlon2eastNorth
					(new LatLon(br2.getLat(),br2.getLng() ));

		grabbedScale = nc.getScale(); // enPerPixel

		int widthPx = (int)((bottomRight.east()-topLeft.east())/grabbedScale),
			heightPx = (int)
				((topLeft.north()-bottomRight.north()) / grabbedScale);

		try
		{
			URL url =  doGetURL(w,s,e,n,widthPx,heightPx);
			doGrab(url);
		}
		catch(MalformedURLException ex)
		{
			System.out.println("Illegal url. Error="+ex);
		}
	}

	public void paint(Graphics g,NavigatableComponent nc) 
	{
		if(theImage!=null)
		{
			super.paint(g,nc);
			Graphics2D g2d = (Graphics2D)g;
			g2d.setStroke(new BasicStroke(2));

			// Display markers at the OSGB intersections.
			// The code is very convoluted - projections really are fun
			// things to deal with :-)
			// Oh well, at least I can let someone else do the maths :-)

			double zoomInFactor = grabbedScale / nc.getScale();

			EastNorth topLeftDisplaced  = 
				new EastNorth(topLeft.east()+dEast, topLeft.north()+dNorth);
			EastNorth bottomRightDisplaced  = 
				new EastNorth(bottomRight.east()+dEast,
								bottomRight.north()+dNorth);

			LatLon ll5 = Main.proj.eastNorth2latlon(topLeftDisplaced),
				ll6 = Main.proj.eastNorth2latlon(bottomRightDisplaced);

			LatLng ll7 = new LatLng(ll5.lat(),ll5.lon());
			LatLng ll8 = new LatLng(ll6.lat(),ll6.lon());
			ll7.toOSGB36();
			ll8.toOSGB36();

			LatLng curLatLng; 
			EastNorth curEN;

			
			OSRef osgb1 = ll7.toOSRef(),
				 osgb2 = ll8.toOSRef();

			for(int easting=(int)(osgb1.getEasting()/1000) + 1; 
					easting<=(int)(osgb2.getEasting()/1000);
					easting++)
			{
				for (int northing=(int)(osgb1.getNorthing()/1000) ;
					 northing>(int)(osgb2.getNorthing()/1000);
					 northing--)
				{
					// Now we have to convert the OSGB eastings and northings
					// *back* to EastNorth units so we can draw the 
					// intersections....
					// Not to mention converting between JOSM LatLon and
					// JCoord LatLng....
				

					curLatLng = new OSRef(easting*1000,northing*1000).
											toLatLng();
					curLatLng.toWGS84();
					curEN = Main.proj.latlon2eastNorth
								(new LatLon(curLatLng.getLat(),
											curLatLng.getLng() ) );

					// draw a cross at the intersection 
					Point p = Main.map.mapView.getPoint(curEN);
					g.setColor(Color.BLUE);
					g.drawLine(p.x-5,p.y,p.x+5,p.y);
					g.drawLine(p.x,p.y-5,p.x,p.y+5);
				}
			}
			g2d.setStroke(new BasicStroke(1));
		}
	}
}

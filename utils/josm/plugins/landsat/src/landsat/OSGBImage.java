package landsat;

import uk.me.jstott.jcoord.OSRef;
import uk.me.jstott.jcoord.LatLng;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.NavigatableComponent;

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
		OSRef bottomLeftGR = 
				new LatLng(minlat,minlon).toOSRef(),
			  topRightGR = 
				new LatLng(maxlat,maxlon).toOSRef(),
		 	topLeftGR = 
				new LatLng(maxlat,minlon).toOSRef(),
			  bottomRightGR = 
				new LatLng(minlat,maxlon).toOSRef();

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
			System.out.println("OSGB URL=" + url);
			doGrab(url);
		}
		catch(MalformedURLException ex)
		{
			System.out.println("Illegal url. Error="+ex);
		}
	}
}

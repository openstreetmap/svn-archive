import javax.microedition.location.*;
import javax.microedition.io.*;
import java.io.*;
import javax.microedition.lcdui.*;

public class GPSListener implements LocationListener
{
	LocationProvider lp;
	Location location;
	FMCanvas canvas;


	public GPSListener(FMCanvas canvas)
	{
		this.canvas=canvas;
	}
	
	public GPSListener(Location location)
	{

		try
		{
			this.location=location;
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}	
	

	public void locationUpdated(LocationProvider provider, Location location)
	{
		if(location!=null)
		{
			if(location.isValid())
			{
				System.out.println("locationUpdated");
				Coordinates c = location.getQualifiedCoordinates();
				canvas.updatePosition(c.getLongitude(),c.getLatitude());
			}
			else 
			{
				canvas.setState(FMCanvas.GPS_FAILED);
			}
		}
	}

	public void providerStateChanged(LocationProvider p, int st)
	{
	}

	public void startGPSListen()
	{
		try
		{
			System.out.println("starting...");
			if(lp==null)
			{
				Criteria cr=new Criteria();
				cr.setHorizontalAccuracy(500);
				lp=LocationProvider.getInstance(cr);
			}
			lp.setLocationListener(this,60,-1,-1);
		}	
		catch(LocationException e) 
		{
		 	System.out.println(e);	
		}
	}

	public void stopGPSListen()
	{
		System.out.println("stopping GPS listener");
		lp.setLocationListener(null,-1,-1,-1);
	}
}

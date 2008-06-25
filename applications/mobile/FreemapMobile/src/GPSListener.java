import javax.microedition.location.*;
import javax.microedition.io.*;
import java.io.*;
import javax.microedition.lcdui.*;

public class GPSListener implements LocationListener
{
    LocationProvider lp;
    Location location;
    FreemapMobile app;
    int interval;

    public GPSListener(FreemapMobile app)
    {
        this.app=app;
        interval=60;
    }
    
    public void setInterval (int interval)
    {
        this.interval=interval;
    }

    public void locationUpdated(LocationProvider provider, Location location)
    {
        updatePosition(location);
    }

    public void forceUpdate()
    {
        try
        {
            updatePosition(lp.getLocation(10));
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }

    private void updatePosition(Location location)
    {
        if(location!=null)
        {
            if(location.isValid())
            {
                System.out.println("locationUpdated");
                Coordinates c = location.getQualifiedCoordinates();
                app.updatePosition(c.getLongitude(),c.getLatitude());
            }
            else 
            {
                // handle unable to get GPS position
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
            lp.setLocationListener(this,interval,-1,-1);
        }    
        catch(LocationException e) 
        {
             System.out.println(e);    
        }
    }

    public void stopGPSListen()
    {
        if(lp!=null)
        {
            System.out.println("stopping GPS listener");
            lp.setLocationListener(null,-1,-1,-1);
        }
    }
}

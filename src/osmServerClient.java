/*
Copyright (C) 2004 Stephen Coast (steve@fractalus.com)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/


import java.util.*;
import java.net.*;
import java.lang.*;
import java.io.*;
import com.bbn.openmap.LatLonPoint;
import org.apache.xmlrpc.applet.*;

public class osmServerClient
{
  

  public osmServerClient()
  {


  } // osmServerClient


  public Vector getPoints(LatLonPoint llp1,
                          LatLonPoint llp2)
  {
    Vector gpsPoints = new Vector();
    
  
    try{
      
      SimpleXmlRpcClient xmlrpc = new SimpleXmlRpcClient("128.40.59.181", 4000);


      Vector params = new Vector();

      params.addElement( "applet" ); 
      params.addElement( new Double((double)llp1.getLatitude()) );
      params.addElement( new Double((double)llp1.getLongitude()) );
      params.addElement( new Double((double)llp2.getLatitude()) );
      params.addElement( new Double((double)llp2.getLongitude()) );

      Vector results = (Vector) xmlrpc.execute("openstreetmap.getPoints",params);

      System.out.println("reading POINTS");
      
      Enumeration e = results.elements();

      while(e.hasMoreElements())
      {
        
        //gpsPoints.add( 
            
          
        //new gpspoint( 
        float lat = (float)((Double)e.nextElement()).doubleValue();
        float lon = (float)((Double)e.nextElement()).doubleValue();
      
        System.out.println(lat+"," + lon);
        
        gpsPoints.add( new gpspoint(lat,lon,0,0) );
        
      }

      System.out.println("done getting points");
    
    }
    catch(Exception e)
    {
      System.out.println("oh de-ar " + e);
      e.printStackTrace();
     
      
      System.exit(-1);

    }

    return gpsPoints;
  
  } // getPoints


} // osmServerClient

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
  private String sUsername = "";
  private String sPassword = "";
  private String sLoginToken = "";
  private long loginTime = 0;
  
      
  SimpleXmlRpcClient xmlrpc;

  public osmServerClient()
  {

    try
    {
    
      xmlrpc = new SimpleXmlRpcClient("128.40.59.181", 4000);

    }
    catch(Exception e)
    {
      System.out.println("oh de-ar " + e);
      e.printStackTrace();


      System.exit(-1);


    }

  } // osmServerClient


  public synchronized boolean deletePoint(double lat, double lon)
  {
    Vector params = new Vector();

    params.addElement(sLoginToken);
    params.addElement(new Double(lon));
    params.addElement(new Double(lat));

    Boolean bYesNo;
    
    try{
      
      bYesNo = (Boolean)xmlrpc.execute("openstreetmap.dropPoint", params);

    }
    catch(Exception e)
    {
      System.out.println("oh de-ar " + e);
      e.printStackTrace();

      return false;

    }

    return  bYesNo.booleanValue();


    
  } // deletePoint


  
  public synchronized boolean login(String user, String pass)
  {
    System.out.println("trying to login with '" + user + "' , '" + pass +"'...");
    Vector params = new Vector();

    params.addElement(user);

    params.addElement(pass);


    String token = "hum";
    try{
      
      token = (String)xmlrpc.execute("openstreetmap.login", params);

    }
    catch(Exception e)
    {
      System.out.println("oh de-ar " + e);
      e.printStackTrace();

      return false;

    }

    if( token.equals("ERROR"))
    {
      return false;
    }

    sUsername = user;
    sPassword = pass;
    sLoginToken = token;
    loginTime = System.currentTimeMillis() + (1000 * 60 * 9);
    // set logout time for 9 mins hence

    return true;


  } // login


  public synchronized boolean loggedIn()
  {
    if( loginTime > System.currentTimeMillis() )
    {
      return true;

    }

    return false;
        
  }



  public synchronized Vector getPoints(LatLonPoint llp1,
      LatLonPoint llp2)
  {
    Vector gpsPoints = new Vector();


    try{

      Vector params = new Vector();

      params.addElement( "applet" ); 
      params.addElement( new Double((double)llp1.getLatitude()) );
      params.addElement( new Double((double)llp1.getLongitude()) );
      params.addElement( new Double((double)llp2.getLatitude()) );
      params.addElement( new Double((double)llp2.getLongitude()) );

      Vector results = (Vector) xmlrpc.execute("openstreetmap.getPoints",params);

      System.out.println("reading points...");

      Enumeration e = results.elements();

      while(e.hasMoreElements())
      {

        //gpsPoints.add( 


        //new gpspoint( 
        float lat = (float)((Double)e.nextElement()).doubleValue();
        float lon = (float)((Double)e.nextElement()).doubleValue();


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

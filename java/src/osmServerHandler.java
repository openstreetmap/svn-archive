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
import java.lang.*;
import java.net.*;
import java.io.*;
import org.apache.xmlrpc.*;


public class osmServerHandler
{


  public String login(String user, String pass)
  {
    osmServerSQLHandler osmSQLH = new osmServerSQLHandler("jdbc:mysql://127.0.0.1/openstreetmap", "openstreetmap","openstreetmap");

    return( osmSQLH.login(user,pass) );

     
  } // login
  

  public boolean addPoint(String token,
      double lat,
      double lon,
      double alt,
      Date date)
  {


    osmServerSQLHandler osmSQLH = new osmServerSQLHandler("jdbc:mysql://127.0.0.1/openstreetmap", "openstreetmap","openstreetmap");


    int uid = osmSQLH.validateToken(token);

    if( uid == -1)
    {
      return false;

    }

    return osmSQLH.addPoint((float)lat,(float)lon,(float)alt,date.getTime(), uid);

  } // addPoint



  public Vector getPoints(
      String token,
      double p1lat,
      double p1lon,
      double p2lat,
      double p2lon)
  {
    try{

      osmServerSQLHandler osmSQLH = new osmServerSQLHandler("jdbc:mysql://127.0.0.1/openstreetmap", "openstreetmap","openstreetmap");

      if( !token.equals("applet") && osmSQLH.validateToken(token) == -1 )
      {
        return null;
      }

      Vector v = osmSQLH.getPoints((float)p1lat, (float)p1lon, (float)p2lat, (float)p2lon);

      Vector results = new Vector();

      if( osmSQLH.SQLSuccessful() )
      {

        Enumeration e = v.elements();

        while(e.hasMoreElements())
        {
          gpspoint g = (gpspoint)e.nextElement();

          results.addElement( new Double(g.getLatitude()) );
          results.addElement( new Double(g.getLongitude()) );

        }


        return results;
      }
      else
      {
        System.out.println("error....");
      }
    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();
      System.exit(-1);

    }

    return null;
  } // getPoints


   public Vector getFullPoints(
      String token,
      double p1lat,
      double p1lon,
      double p2lat,
      double p2lon)
  {
    try{

      osmServerSQLHandler osmSQLH = new osmServerSQLHandler("jdbc:mysql://127.0.0.1/openstreetmap", "openstreetmap","openstreetmap");

      Vector v = osmSQLH.getFullPoints((float)p1lat, (float)p1lon, (float)p2lat, (float)p2lon);

      return v;

    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();
      System.exit(-1);

    }

    return null;

  } // getFullPoints
 

} // osmServerHandler

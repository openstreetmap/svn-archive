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
import java.util.zip.*;

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
      
      Socket s = new Socket("128.40.59.181", 2001);;

      BufferedReader in = new BufferedReader(new InputStreamReader(
            s.getInputStream()));

      BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
            s.getOutputStream()));


      out.write("GETPOINTS "
                + llp1.getLatitude()  + " "
                + llp1.getLongitude() + " "
                + llp2.getLatitude()  + " "
                + llp2.getLongitude() + " "
                +"\n");

      out.flush();

      BufferedReader br = new BufferedReader(new InputStreamReader(
          new GZIPInputStream(s.getInputStream())));


      System.out.println("reading POINTS");
      
      String sLine = br.readLine();

      System.out.println("Server said " + sLine);

      while( (sLine = br.readLine()) != null)
      {

        if(sLine.equals("END"))
        {
          System.out.println("breaking on END");
          break;
        }

        
        StringTokenizer st = new StringTokenizer(sLine);

        String a = st.nextToken();
        String b = st.nextToken();
        String c = st.nextToken();
        String d = st.nextToken();

        gpsPoints.add( new gpspoint(a,b,c,d) );
      
        
      }

      System.out.println("done getting points");
    
    }
    catch(Exception e)
    {
      System.out.println("oh de-ar " + e);
      
      System.exit(-1);

    }

    return gpsPoints;
  
  } // getPoints


} // osmServerClient

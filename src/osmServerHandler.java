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
import java.util.zip.*;

public class osmServerHandler implements Runnable
{

  Socket sock;
  osmServerSQLHandler osmSQLH;

  
  public osmServerHandler(Socket sT,
                          osmServerSQLHandler osmTSQLH)
  {
    sock = sT;
    osmSQLH = osmTSQLH;

  } // osmServerHandler


 
  public void run()
  {
    try{

      BufferedReader in = new BufferedReader(new InputStreamReader(
            sock.getInputStream()));

      BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
            sock.getOutputStream()));

      String sLine;

      boolean bKeepTalking = true;

      while( (sLine = in.readLine()) != null && bKeepTalking)
      {

        System.out.println("client said " + sLine);
       
        if(sLine.equals("LOGIN"))
        {

          // puth authentication type things here
        }

        if(sLine.startsWith("GETPOINTS"))
        {
          StringTokenizer st = new StringTokenizer(sLine);

          String s = st.nextToken();

          float p1lat = Float.parseFloat( st.nextToken() );
          float p1lon = Float.parseFloat( st.nextToken() );
          float p2lat = Float.parseFloat( st.nextToken() );
          float p2lon = Float.parseFloat( st.nextToken() );

          Vector v = osmSQLH.getPoints(p1lat, p1lon, p2lat, p2lon);
          
          if( osmSQLH.SQLSuccessful() )
          {
            out.write("POINTS\n");

                  
            GZIPOutputStream innergs = new GZIPOutputStream( sock.getOutputStream() ); 
            BufferedWriter gs = new BufferedWriter(
                new OutputStreamWriter( 
                  
                    innergs 
                  ) 
                );

            Enumeration e = v.elements();

            
            while(e.hasMoreElements())
            {
              gpspoint g = (gpspoint)e.nextElement();

              gs.write(g + "\n");
              
            }
        
            gs.write("END\n");

            gs.flush();

            innergs.finish();


            out.close();
            

          }
          else
          {
            System.out.println("error....");
            out.write("ERROR\n");
            bKeepTalking = false;
          }

        }

      }

    }
    catch(Exception e)
    {
      
      System.out.println("Something went screwy " + e);
    
//      System.exit(-1);
    }
    
    
  } // run


} // osmServerHandler

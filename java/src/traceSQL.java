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


import java.net.Socket;
import java.net.InetAddress;
import java.io.*;
import java.lang.*;
import java.util.*;
import java.sql.*;

public class traceSQL {

  public static void main(String[] args){

    ssocket cheese = new ssocket();

    cheese.go();

  } // main

}

class ssocket extends Thread
{
  ssocket()
  {

  } // socket


  public void go()
  {
    String hostName = "localhost";

    try {
      BufferedWriter fileout = new BufferedWriter(new FileWriter("gpsdata", true));
      Socket serverSocket = new Socket(hostName, 2947);


      BufferedWriter out = new BufferedWriter( new OutputStreamWriter( serverSocket.getOutputStream()));
      BufferedReader is = new BufferedReader(
          new InputStreamReader(serverSocket.getInputStream()));


      // database crap

      Class.forName("com.mysql.jdbc.Driver").newInstance(); 


      Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/openstreetmap","openstreetmap", "openstreetmap");


      int s = 0;
      float x=0;
      float y=0;
      float a=0;
      while(true)
      {
        // Read from the socket

        out.write("aps\n");
        out.flush();
        String line;

        line = is.readLine();

        StringTokenizer myTokenizer = new StringTokenizer(line, ",");

        while(myTokenizer.hasMoreTokens() )
        {
          String sToken = myTokenizer.nextToken();

          if(sToken.startsWith("P="))
          {
            //position

            StringTokenizer posTokenizer = new StringTokenizer(sToken.substring(2));

            x = Float.parseFloat( posTokenizer.nextToken() );
            y = Float.parseFloat( posTokenizer.nextToken() );

            //        System.out.println("x is " + x );
            //      System.out.println("y is " + y );


          }

          if(sToken.startsWith("A="))
          {
            //position

            a = Float.parseFloat( sToken.substring(2) );

            //          System.out.println("a is " + a );


          }


          if(sToken.startsWith("S="))
          {
            //position

            s = Integer.parseInt( sToken.substring(2) );

            //            System.out.println("s is " + s );


          }



        }

        if( s == 1)
        {
          //satellites in view
          //

          //fileout.write(x + " " + y + " " + a + " " + System.currentTimeMillis() + "\n");
          //fileout.flush();
          
          String sqlUpdate = "insert into tempPoints values ("
            + " GeomFromText('Point("  + x + " " + y + ")'),"
            + " " + a + ", "
            + " " + System.currentTimeMillis() + ");";

         
          Statement stmt = conn.createStatement();

          stmt.execute(sqlUpdate);
          System.out.print(".");
        }
        else
        {
          System.out.print("_");
        }



        this.sleep(1000);
      }
    } catch (Exception e) {

      e.printStackTrace();

    }


  } // go


}


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
import java.sql.*;

public class osmServerSQLHandler extends Thread
{
  String sSQLConnection;
  String sUser;
  String sPass;

  boolean bSQLSuccess = false;

  
  public osmServerSQLHandler(String sTSQLConnection,
                             String sTUser,
                             String sTPass)
           
  {
  
    sSQLConnection = sTSQLConnection;
    sUser = sTUser;
    sPass = sTPass;

    System.out.println("osmSQLHandler instantiated");
  } // osmServerSQLHandler


  
  public boolean SQLSuccessful()
  {
    
    return bSQLSuccess;

  } // SQLSuccessful


  public synchronized String login(String user, String pass)
  {
    // FIXME: add all the letters plus upper case etc
    char letters[] = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
    
    System.out.println("login " + user + " " + pass);

    if( user.length() < 5 ||
          user.length() > 30 ||
          pass.length() < 5 ||
          pass.length() > 30 ||
          user.indexOf(" ") == -1 ||
          user.indexOf("@") == -1 )
    {
       return "ERROR";
       
    }
    
    
    try{

      Class.forName("com.mysql.jdbc.Driver").newInstance(); 


      Connection conn = DriverManager.getConnection(sSQLConnection,
                                                    sUser,
                                                    sPass);

      Statement stmt = conn.createStatement();

      String sSQL = "select uid from user where user='" + user + "' and pass='" + pass;

      System.out.println("querying with sql \n " + sSQL);
      
      ResultSet rs = stmt.executeQuery(sSQL);

      if( rs.next() )
      {
        String token = "";
        Random r = new Random();
        
        for(int i = 1; i < 30; i++)
        {
          token = token + letters[ 1 + r.nextInt(letters.length -1)];
          
        }
        int uid = rs.getInt(1);
         sSQL = "update tempPoints set timeout=" + (System.currentTimeMillis() + (1000 * 10)) 
          + " where uid = " + uid;

        stmt.execute(sSQL);

        sSQL = "update tempPoints set token='" + token + "' where uid = " + uid;

        stmt.execute(sSQL);

      }
      else
      {
        return "ERROR";
      }

    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

      System.exit(-1);

    }

    return "OK";
    

  } // login



  public synchronized boolean validateToken(String token)
  {
    if(token.length() > 30 || token.indexOf(" ") != -1)
    {
      return false;

    }

    try{

      Class.forName("com.mysql.jdbc.Driver").newInstance(); 


      Connection conn = DriverManager.getConnection(sSQLConnection,
          sUser,
          sPass);

      Statement stmt = conn.createStatement();

      String sSQL = "select uid from user where token='" + token +"' and timeout > "+System.currentTimeMillis();

      System.out.println("querying with sql \n " + sSQL);

      ResultSet rs = stmt.executeQuery(sSQL);

      if( rs.next() )
      {
        int uid = rs.getInt(1);

        sSQL = "update tempPoints set timeout=" + (System.currentTimeMillis() + (1000 * 10)) 
          + " where uid = " + uid;

        stmt.execute(sSQL);

        return true;
      }
      else
      {
        return false;
      }

    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

      System.exit(-1);

    }

    return false;


  } // validateLoginToken



  public synchronized Vector getPoints(float p1lat,
      float p1lon,
      float p2lat,
      float p2lon
      )
  {

    System.out.println("getPoints");

    try{

      Class.forName("com.mysql.jdbc.Driver").newInstance(); 


      Connection conn = DriverManager.getConnection(sSQLConnection,
          sUser,
          sPass);

      Statement stmt = conn.createStatement();

      String sSQL = "select Y(g),X(g),altitude,timestamp from tempPoints"
        + " where X(g) < " + p1lat
        + " and X(g) > " + p2lat
        + " and Y(g) > " + p1lon
        + " and Y(g) < " + p2lon
        + " limit 10000";

      //System.out.println("querying with sql \n " + sSQL);

      ResultSet rs = stmt.executeQuery(sSQL);

      boolean bFirst = true;

      gpspoint gpFirst = new gpspoint(0,0,0,0);

      gpspoint gpLastPoint = new gpspoint(0,0,0,0);

      Vector v = new Vector();

      while(rs.next())
      {
        v.add( new gpspoint(rs.getFloat(2),
              rs.getFloat(1),
              rs.getFloat(3),
              rs.getLong(4) ));

      }

      bSQLSuccess = true;

      return v;

    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

      System.exit(-1);

    }

    return null;

  } // getPoints

} // osmServerSQLHandler

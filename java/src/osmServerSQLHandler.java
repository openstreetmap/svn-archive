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

    if( 
          user.length() > 30 ||
          pass.length() < 5 ||
          pass.length() > 30 ||
          user.indexOf(" ") != -1 )
    {
       return "ERROR";
       
    }
    
    
    try{

      Class.forName("com.mysql.jdbc.Driver").newInstance(); 


      Connection conn = DriverManager.getConnection(sSQLConnection,
                                                    sUser,
                                                    sPass);

      Statement stmt = conn.createStatement();

      String sSQL = "select uid from user where user='" + user + "' and pass='" + pass + "'";

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
         sSQL = "update user set timeout=" + (System.currentTimeMillis() + (1000 * 10)) 
          + " where uid = " + uid;

        stmt.execute(sSQL);

        sSQL = "update user set token='" + token + "' where uid = " + uid;

        stmt.execute(sSQL);

        return token;
      }

    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

      System.exit(-1);

    }

    return "ERROR";
    

  } // login



  public synchronized int validateToken(String token)
  {
    if(token.length() > 30 || token.indexOf(" ") != -1)
    {
      System.out.println("didnt validate " + token );
      return -1;
      

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

        sSQL = "update user set timeout=" + (System.currentTimeMillis() + (1000 * 10)) 
          + " where uid = " + uid;

        stmt.execute(sSQL);

        System.out.println("validated token " + token);
        return uid;
        
      }

    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

      System.exit(-1);

    }

    return -1;


  } // validateLoginToken



  public synchronized boolean addPoint(float lat,
      float lon,
      float alt,
      long timestamp,
      int uid)
  {

    System.out.println("addPoint");

    try{

      Class.forName("com.mysql.jdbc.Driver").newInstance(); 


      Connection conn = DriverManager.getConnection(sSQLConnection,
          sUser,
          sPass);

      Statement stmt = conn.createStatement();

      String sSQL = "insert into tempPoints values ("
            + " GeomFromText('Point("  + lon + " " + lat + ")'),"
            + " " + alt + ", "
            + " " + timestamp + ", " + uid + ");";


      //System.out.println("querying with sql \n " + sSQL);

      stmt.execute(sSQL);

    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

      return false;
    }

    return true;

  }
  

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


  public synchronized Vector getFullPoints(
      float p1lat,
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

      String sSQL = "select X(g) as lat,"
        + " Y(g) as lon,"
        + " altitude,"
        + " timestamp, "
        + " hor_dilution, "
        + " vert_dilution, "
        + " trackid, "
        + " quality, "
        + " satellites, "
        + " user, "
        + " last_time "
        
        + " from tempPoints, user"
        
        + " where X(g) < " + p1lat
        + " and X(g) > " + p2lat
        + " and Y(g) > " + p1lon
        + " and Y(g) < " + p2lon
        + " and tempPoints.uid = user.uid"
        + " limit 10000";

      System.out.println("querying with sql \n " + sSQL);

      ResultSet rs = stmt.executeQuery(sSQL);

      boolean bFirst = true;

      gpspoint gpFirst = new gpspoint(0,0,0,0);

      gpspoint gpLastPoint = new gpspoint(0,0,0,0);

      Vector v = new Vector();

      while(rs.next())
      {

        v.add( new Double(rs.getDouble("lat")) ); // lat
        v.add( new Double(rs.getDouble("lon")) ); // lon
        v.add( new Double(rs.getDouble("altitude")) ); // alt
        v.add( new java.util.Date(rs.getLong("timestamp") )); // time point was taken
        v.add( new Double( rs.getDouble("hor_dilution")));
        v.add( new Double( rs.getDouble("vert_dilution")));
        v.add( new Long( rs.getLong("trackid")));
        v.add( new Integer( rs.getInt("quality")));
        v.add( new Integer( rs.getInt("satellites")));
        v.add( rs.getString("user"));
        v.add( new Long(rs.getLong("last_time")));
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

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

package org.openstreetmap.server;

import java.util.*;
import java.lang.*;
import java.io.*;
import java.sql.*;

import org.openstreetmap.util.gpspoint;

public class osmServerSQLHandler extends Thread
{
  String sSQLConnection;
  String sUser;
  String sPass;

  Connection conn;
  
  boolean bTokenValidated = false;
  long lValidationTimeout = 0;
  int nLastUID = -1;
  
  long lTimeout = 1000 * 60 * 10; // ten mins

  boolean bSQLSuccess = false;

  boolean bConnectSuccess = false;


  public osmServerSQLHandler(String sTSQLConnection,
      String sTUser,
      String sTPass)

  {

    sSQLConnection = sTSQLConnection;
    sUser = sTUser;
    sPass = sTPass;

    try{


      Class.forName("com.mysql.jdbc.Driver").newInstance(); 

      conn = DriverManager.getConnection(sSQLConnection,
          sUser,
          sPass);

      bConnectSuccess = true;

      System.out.println("sql connect apparently successful in sql handler");

    }
    catch(Exception e)
    {
      System.out.println("sql connect failure");
      System.out.println(e);
      e.printStackTrace();

    }


    //    System.out.println("osmSQLHandler instantiated");
  } // osmServerSQLHandler


  public boolean SQLConnectSuccess()
  {
    return bConnectSuccess;

  } // SQLConnectSuccess;


  public boolean SQLSuccessful()
  {

    return bSQLSuccess;

  } // SQLSuccessful



  public synchronized String login(String user, String pass)
  {
    // FIXME: add all the letters plus upper case etc
    char letters[] = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h' , 'i' , 'j'};

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

      Statement stmt = conn.createStatement();

      String sSQL = "select uid,active from user where user='" + user + "' and pass='" + pass + "'";

      System.out.println("querying with sql \n " + sSQL);

      ResultSet rs = stmt.executeQuery(sSQL);

      if( rs.next() && rs.getInt("active") == 1)
      {
        String token = "";
        Random r = new Random();

        for(int i = 1; i < 30; i++)
        {
          token = token + letters[ 1 + r.nextInt(letters.length -1)];

        }
        int uid = rs.getInt(1);
       
        sSQL = "update user set timeout=" + (System.currentTimeMillis() + lTimeout) 
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


    }

    return "ERROR";


  } // login



  public synchronized int validateToken(String token)
  {
    if( bTokenValidated
        && System.currentTimeMillis() < lValidationTimeout)
    {
//      System.out.println("validated cached token returning " + nLastUID);
      return nLastUID;

    }


    if(token.length() > 30 || token.indexOf(" ") != -1)
    {
      System.out.println("didnt validate " + token );
      bTokenValidated = false;
      return -1;


    }

    try{

      Statement stmt = conn.createStatement();

      String sSQL = "select uid from user where token='" + token +"' and timeout > "+System.currentTimeMillis();

      System.out.println("querying with sql \n " + sSQL);

      ResultSet rs = stmt.executeQuery(sSQL);

      if( rs.next() )
      {
        int uid = rs.getInt(1);

        sSQL = "update user set timeout=" + (System.currentTimeMillis() + lTimeout) 
          + " where uid = " + uid;

        stmt.execute(sSQL);

        System.out.println("validated token " + token);

        lValidationTimeout = System.currentTimeMillis() + (1000 * 60 * 1); // timeout in 1 minute
        bTokenValidated = true;
        nLastUID = uid;

        return uid;

      }

    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

    }

    bTokenValidated = false;
    return -1;


  } // validateLoginToken




  public synchronized Integer addNewStreet(
      String street_name,
      float lat1,
      float lon1,
      float lat2,
      float lon2,
      int uid)
  {
    // creates a new street
    // returns the street_uid created

    System.out.println("addNewStreet");

    try{

      Statement stmt = conn.createStatement();

      long l = System.currentTimeMillis();

      String sSQL = "lock table streets write, streetSegments write;";

      System.out.println("querying with sql \n " + sSQL);
      stmt.execute(sSQL);

      sSQL = "start transaction; ";


      System.out.println("querying with sql \n " + sSQL);
      stmt.execute(sSQL);

      sSQL = 
        "insert into streets(name, timestamp, user_uid, visible) values ("
        + " '" + street_name + "', "
        + " " + l + ", "
        + " " + uid + ", "
        + " true "
        + "); ";

      System.out.println("querying with sql \n " + sSQL);
      stmt.execute(sSQL);

      sSQL = "set @id = last_insert_id(); ";

      System.out.println("querying with sql \n " + sSQL);
      stmt.execute(sSQL);

      sSQL = "insert into streetSegments(uid_of_street, lon1, lat1, lon2, lat2, timestamp, user_uid, visible ) values ("
        + "  last_insert_id() , "
        + " " + lon1 + ", "
        + " " + lat1 + ", "
        + " " + lon2 + ", "
        + " " + lat2 + ", "
        + " " + l + ", "
        + " " + uid + ", "
        + " true "
        + "); ";

      System.out.println("querying with sql \n " + sSQL);
      stmt.execute(sSQL);

      sSQL = "commit;";

      System.out.println("querying with sql \n " + sSQL);
      stmt.execute(sSQL);

      sSQL = "unlock tables; ";



      System.out.println("querying with sql \n " + sSQL);

      stmt.execute(sSQL);

      sSQL = "select @id; ";

      System.out.println("querying with sql \n " + sSQL);

      ResultSet rs = stmt.executeQuery(sSQL);

      rs.next();

      Integer i = new Integer(rs.getInt(1));

      System.out.println("returned int on insert street was = " + i);

      return i;

    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

    }

    return new Integer(-1);

  } // addNewStreet





  public synchronized boolean addStreetSegment(
      int street_uid,
      float lat1,
      float lon1,
      float lat2,
      float lon2,
      int uid)
  {

    System.out.println("addStreetSegment");

    try{

      Statement stmt = conn.createStatement();

      String sSQL = "insert into streetSegments(uid_of_street, lon1, lat1, lon2, lat2, timestamp, user_uid, visible ) values ("
        + " " + street_uid + ", "
        + " " + lon1 + ", "
        + " " + lat1 + ", "
        + " " + lon2 + ", "
        + " " + lat2 + ", "
        + " " + System.currentTimeMillis() + ", "
        + " " + uid + ", "
        + " true "
        + ");";


      System.out.println("querying with sql \n " + sSQL);

      stmt.execute(sSQL);

    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

      return false;
    }

    return true;

  } // addStreetSegment



  public synchronized boolean addPoint(
      float lat,
      float lon,
      float alt,
      long timestamp,
      float hor_dilution,
      float vert_dilution,
      int trackid,
      int quality,
      int satellites,
      int uid)
  {

    //    System.out.println("addPoint");

    try{

      Statement stmt = conn.createStatement();

      String sSQL = "insert into tempPoints values ("
        + " GeomFromText('Point("  + lon + " " + lat + ")'),"
        + " " + alt + ", "
        + " " + timestamp + ", "
        + " " + uid + ", "
        + " " + hor_dilution + ", "
        + " " + vert_dilution + ", "
        + " " + trackid + ", "
        + " " + quality + ", "
        + " " + satellites + ", "
        + " " + System.currentTimeMillis() + ", 1,0);";


      //      System.out.println("querying with sql \n " + sSQL);

      stmt.execute(sSQL);

    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

      System.out.println("uh-oh!");
      return false;
    }

    //    System.out.println("added point ok, returning");

    return true;

  } // addPoint


  /*

     public synchronized boolean addPoints(
     float[] lat,
     float[] lon,
     float[] alt,
     long[] timestamp,
     float[] hor_dilution,
     float[] vert_dilution,
     int[] trackid,
     int[] quality,
     int[] satellites,
     int nPoints,
     int uid
     )
     {

     System.out.println("addPoint");

     try{

     Class.forName("com.mysql.jdbc.Driver").newInstance(); 


     Connection conn = DriverManager.getConnection(sSQLConnection,
     sUser,
     sPass);

     Statement stmt = conn.createStatement();

     for(int i = 0; i< nPoints; i++)
     {

     String sSQL = "insert into tempPoints values ("
     + " GeomFromText('Point("  + lon[i] + " " + lat[i] + ")'),"
     + " " + alt[i] + ", "
     + " " + timestamp[i] + ", "
     + " " + uid + ", "
     + " " + hor_dilution[i] + ", "
     + " " + vert_dilution[i] + ", "
     + " " + trackid[i] + ", "
     + " " + quality[i] + ", "
     + " " + satellites[i] + ", "
     + " " + System.currentTimeMillis() + ", 1,0);";


  //System.out.println("querying with sql \n " + sSQL);

  stmt.execute(sSQL);
  }
  }
  catch(Exception e)
  {
  System.out.println(e);
  e.printStackTrace();

  return false;
  }

  return true;

  } // addPoints
   */

  public synchronized Vector getStreets(
      float p1lat,
      float p1lon,
      float p2lat,
      float p2lon
      )
  {

    System.out.println("getStreets");

    try{

      Statement stmt = conn.createStatement();

      String sSQL = "select uid_of_street, lon1, lat1, lon2, lat2 from streetSegments"
        + " where ( "
        + "     lat1 < " + p1lat
        + " and lat1 > " + p2lat
        + " and lon1 > " + p1lon
        + " and lon1 < " + p2lon
        + " ) or ( "
        + "     lat2 < " + p1lat
        + " and lat2 > " + p2lat
        + " and lon2 > " + p1lon
        + " and lon2 < " + p2lon
        + " ) "
        + " and visible=1 limit 10000";


      System.out.println("querying with sql \n " + sSQL);


      ResultSet rs = stmt.executeQuery(sSQL);

      Vector v = new Vector();

      while(rs.next())
      {
        v.add( new Integer( rs.getInt(1) ) );
        v.add( new Float( rs.getDouble(2)));
        v.add( new Float( rs.getDouble(3)));
        v.add( new Float( rs.getDouble(4)));
        v.add( new Float( rs.getDouble(5)));

      }

      bSQLSuccess = true;

      return v;

    }
    catch(Exception e)
    {


      System.out.println(e);
      e.printStackTrace();


    }

    return null;

  } // getStreets




  public synchronized Vector getPoints(float p1lat,
      float p1lon,
      float p2lat,
      float p2lon
      )
  {

    System.out.println("getPoints");

    try{

      Statement stmt = conn.createStatement();

      String sSQL = "select Y(g),X(g),altitude,timestamp from tempPoints"
        + " where X(g) < " + p1lat
        + " and X(g) > " + p2lat
        + " and Y(g) > " + p1lon
        + " and Y(g) < " + p2lon
        + " and visible=1 limit 10000";

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
        + " and visible = 1"
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
        v.add( new Integer( rs.getInt("trackid")));
        v.add( new Integer( rs.getInt("quality")));
        v.add( new Integer( rs.getInt("satellites")));
        v.add( rs.getString("user"));
        v.add( new java.util.Date(rs.getLong("last_time")));
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




  public synchronized boolean dropPoint(
      float lon,
      float lat,
      int uid)
  {

    try{

      Statement stmt = conn.createStatement();

      String sSQL = "update tempPoints set visible=0, dropped_by=" +uid+"  where "
        + " X(g) = " + lat 
        + " and Y(g) = " + lon;

      System.out.println("querying with sql \n " + sSQL);

      stmt.execute(sSQL);

    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

      return false;
    }

    return true;
  } // dropPoint



  public synchronized boolean dropPointsInArea(
      float lon1,
      float lat1,
      float lon2,
      float lat2,

      int uid)
  {

    try{

      Statement stmt = conn.createStatement();

      String sSQL = "update tempPoints set visible=0, dropped_by=" +uid+"  where "
        + " X(g) <= " + lat1
        + " and X(g) >= " + lat2
        + " and Y(g) >= " + lon1
        + " and Y(g) <= " + lon2;

      System.out.println("querying with sql \n " + sSQL);

      stmt.execute(sSQL);

    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

      return false;
    }

    return true;
  } // dropPoint



  public synchronized String addUser(String user, String pass)
  {
    // FIXME: add all the letters plus upper case etc
    char letters[] = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h' , 'i' , 'j'};

    String token = "";
    System.out.println("addUser " + user + " " + pass);

    if( user.length() < 4 ||
        user.length() > 30 ||
        pass.length() < 5 ||
        pass.length() > 30 ||
        user.indexOf(" ") != -1 )
    {
      System.out.println("returning error");
      return "ERROR";

    }


    try{

      Statement stmt = conn.createStatement();


      Random r = new Random();

      for(int i = 1; i < 30; i++)
      {
        token = token + letters[ 1 + r.nextInt(letters.length -1)];

      }

      String sSQL = "insert into user (user, pass, timeout, token) values (" +
        "'" + user + "', " +
        "'" + pass + "', " +
        " " + System.currentTimeMillis() + ", " +
        " '" + token + "')";

      System.out.println("querying with sql \n " + sSQL);

      stmt.execute(sSQL);


    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();


    }

    System.out.println("returning token " + token);
    return token;


  } // addUser



  public synchronized boolean confirmUser(String user, String token)
  {

    System.out.println("confirm " + user + " " + token);

    if( user.length() < 5 ||
        user.length() > 30 ||
        user.indexOf(" ") != -1 )
    {
      return false;

    }


    try{

      Statement stmt = conn.createStatement();

      String sSQL = "select uid,active from user where user='" + user + "' and token='" + token + "'";

      System.out.println("querying with sql \n " + sSQL);

      ResultSet rs = stmt.executeQuery(sSQL);

      if( rs.next() && rs.getInt("active") == 0)
      {
        sSQL = "update user set active=1" 
          + " where uid = " + rs.getInt("uid");

        System.out.println("executing sql " + sSQL);

        stmt.execute(sSQL);

        return true;
      }

      return false;

    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();


    }

    return false;


  } // confirmUser





  public synchronized boolean userExists(String user)
  {

    System.out.println("user exists " + user );

    if( user.length() < 5 ||
        user.length() > 30 ||
        user.indexOf(" ") != -1 )
    {
      return false;

    }


    try{

      Statement stmt = conn.createStatement();

      String sSQL = "select uid,active from user where user='" + user +"'";

      System.out.println("querying with sql \n " + sSQL);

      ResultSet rs = stmt.executeQuery(sSQL);

      return rs.next();

    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

      return false;
    }

  } // userExists



  public synchronized int largestTrackID(String token)
  {

    int uid = validateToken(token);


    try{

      Statement stmt = conn.createStatement();

      String sSQL = "select max(trackid) from tempPoints where uid=" + uid;

      System.out.println("querying with sql \n " + sSQL);

      ResultSet rs = stmt.executeQuery(sSQL);

      rs.next();

      int trackID = -1;

      if( rs.getString(1).equals("NULL"))
      {

      }
      else{

        trackID = rs.getInt(1);
      }

      return trackID;

    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

    }


    return -1;
  } // largestTrackID


  public Vector getAllKeys()
  {

    Vector v = new Vector();

    try{

      Statement stmt = conn.createStatement();

      String sSQL = " select j.name, j.user,j.timestamp from (select * from key_meta_table) as h, (select * from osmKeys left join user on user.uid=osmKeys.user_uid) as j  where h.uid=j.uid and h.visible=1 group by h.uid";

      
      System.out.println("querying with sql \n " + sSQL);

      ResultSet rs = stmt.executeQuery(sSQL);

      while ( rs.next() )
      {
        v.add( rs.getString(1) );
        v.add( rs.getString(2) );
        v.add( rs.getString(3) );

      }


    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

    }

    return v;

  } // getAllKeys

} // osmServerSQLHandler

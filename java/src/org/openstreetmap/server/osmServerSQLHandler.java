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

  static final int MIN_USERNAME_LENGTH = 5;
  static final int MAX_USERNAME_LENGTH = 50;
  static final int MIN_PASSWORD_LENGTH = 5;
  static final int MAX_PASSWORD_LENGTH = 35;
  static final int MAX_TOKEN_LENGTH = 30;
  
  boolean bSQLSuccess = false;

  boolean bConnectSuccess = false;

  public void closeDatabase()
  {
    try{
      conn.close();
    }
    catch(Exception e)
    {
      System.out.println("try as we might, the f'ing thing wont close");
      System.out.println(e);
      e.printStackTrace();

    }

  } // closeDatabase

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
        user.length() < MIN_USERNAME_LENGTH ||
        user.length() > MAX_USERNAME_LENGTH ||
        pass.length() < MIN_PASSWORD_LENGTH ||
        pass.length() > MAX_PASSWORD_LENGTH ||
        user.indexOf(" ") != -1 )
    {
      return "ERROR";

    }


    try{

      Statement stmt = conn.createStatement();

      String sSQL = "select uid,active from user where user='" + user + "' and pass_crypt=md5('" + pass + "')";

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


    if(token.length() > MAX_TOKEN_LENGTH || token.indexOf(" ") != -1)
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


  public synchronized int getGPXID(int nUID, String sFilename)
  {
    try{

      Statement stmt = conn.createStatement();

      String sSQL = "select uid from points_meta_table where "
        + "user_uid = " + nUID
        + " and name='" + sFilename + "'";

      System.out.println("querying with sql \n " + sSQL);

      ResultSet rs = stmt.executeQuery(sSQL);

      if( rs.next() )
      {
        return rs.getInt("uid");

      }
      else
      {
        return -1;
      }

    }
    catch(Exception e)
    {

      System.out.println(e);
      e.printStackTrace();

    }

    return -1;


  }


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
      int uid,
      int gpx_id)
  {

    

    try{

      Statement stmt = conn.createStatement();

      String sSQL = "insert into tempPoints (gpx_id,latitude,longitude,altitude,timestamp,uid,hor_dilution,vert_dilution,trackid,quality,satellites,last_time,visible,dropped_by) values ("
        + " " + gpx_id + ", "
        + " " + lat + ", "
        + " " + lon + ", "
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


  public synchronized Vector getPointsWithDate(float p1lat,
      float p1lon,
      float p2lat,
      float p2lon
      )
  {

    System.out.println("getPoints");

    try{

      Statement stmt = conn.createStatement();

      String sSQL = "select longitude,latitude,altitude,timestamp from tempPoints"
        + " where latitude < " + p1lat
        + " and latitude > " + p2lat
        + " and longitude > " + p1lon
        + " and longitude < " + p2lon
        + " and visible=1 limit 50000";

      System.out.println("querying with sql \n " + sSQL);

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



  public synchronized Vector getPoints(float p1lat,
      float p1lon,
      float p2lat,
      float p2lon
      )
  {

    System.out.println("getPoints");

    try{

      Statement stmt = conn.createStatement();

      String sSQL = "select latitude,longitude from tempPoints"
        + " where latitude < " + p1lat
        + " and latitude > " + p2lat
        + " and longitude > " + p1lon
        + " and longitude < " + p2lon
        + " and visible=1 limit 50000";

      System.out.println("querying with sql \n " + sSQL);

      ResultSet rs = stmt.executeQuery(sSQL);

      boolean bFirst = true;

      gpspoint gpFirst = new gpspoint(0,0,0,0);

      gpspoint gpLastPoint = new gpspoint(0,0,0,0);

      Vector v = new Vector();

      while(rs.next())
      {
        v.add(new Float(rs.getFloat(1)));
        v.add(new Float(rs.getFloat(2)));

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

      String sSQL = "select latitude,"
        + " longitude,"
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

        + " where latitude < " + p1lat
        + " and latitude > " + p2lat
        + " and longitude > " + p1lon
        + " and longitude < " + p2lon
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

        v.add( new Double(rs.getDouble("latitude")) ); // lat
        v.add( new Double(rs.getDouble("longitude")) ); // lon
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
        + " latitude = " + lat 
        + " and longitude = " + lon;

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
        + " latitude <= " + lat1
        + " and latitude >= " + lat2
        + " and longitude >= " + lon1
        + " and longitude <= " + lon2;

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

    if( user.length() < MIN_USERNAME_LENGTH ||
        user.length() > MAX_USERNAME_LENGTH ||
        pass.length() < MIN_PASSWORD_LENGTH ||
        pass.length() > MAX_PASSWORD_LENGTH ||
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

      String sSQL = "insert into user (user, pass_crypt, timeout, token) values (" +
        "'" + user + "', " +
        "md5('" + pass + "'), " +
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

    if( user.length() < MIN_USERNAME_LENGTH ||
        user.length() > MAX_USERNAME_LENGTH ||
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

    if( user.length() < MIN_USERNAME_LENGTH ||
        user.length() > MAX_USERNAME_LENGTH ||
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


  public synchronized Vector getAllKeys(boolean bVisibleOrNot)
  {

    Vector v = new Vector();

    try{

      Statement stmt = conn.createStatement();

      String sSQL = "select h.uid, j.name, j.user,j.timestamp from (select * from key_meta_table) as h, (select * from osmKeys left join user on user.uid=osmKeys.user_uid order by timestamp desc) as j  where h.uid=j.uid and h.visible=" + bVisibleOrNot + " group by h.uid";

      System.out.println("querying with sql \n " + sSQL);

      ResultSet rs = stmt.executeQuery(sSQL);

      while ( rs.next() )
      {
        v.add( rs.getString(1) );
        v.add( rs.getString(2) );
        v.add( rs.getString(3) );
        v.add( rs.getString(4) );
      }


    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

    }

    return v;

  } // getAllKeys


  public synchronized Vector getKeyHistory(int nKey)
  {
    Vector v = new Vector();

    try{

      Statement stmt = conn.createStatement();

      String sSQL = "select osmKeys.name, osmKeys.timestamp, osmKeys.visible, user.user from osmKeys left join user on user_uid=user.uid where osmKeys.uid=" + nKey + " order by timestamp desc";

      System.out.println("querying with sql \n " + sSQL);

      ResultSet rs = stmt.executeQuery(sSQL);

      while ( rs.next() )
      {
        v.add( rs.getString(1) );
        v.add( rs.getString(2) );
        v.add( rs.getString(3) );
        v.add( rs.getString(4) );
      }


    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

    }

    return v;

  } // getKeyHistory



  public synchronized int newKey(String sNewKeyName, int nUserUID)
  {
    if(!isStringSQLClean(sNewKeyName) || sNewKeyName.length() == 0)
    {
      return -1;

    }

    try{

      Statement stmt = conn.createStatement();

      String sSQL = "start transaction;";
      System.out.println("querying with sql \n " + sSQL);
      stmt.execute(sSQL);


      sSQL = "insert into key_meta_table (timestamp, user_uid,visible) values ("
        + System.currentTimeMillis() 
        + ", " + nUserUID
        + ", 1)";
      System.out.println("querying with sql \n " + sSQL);
      stmt.execute(sSQL);

      sSQL = "set @id = last_insert_id(); ";
      System.out.println("querying with sql \n " + sSQL);
      stmt.execute(sSQL);


      sSQL = "insert into osmKeys (uid,name,timestamp,user_uid,visible) values ("
        + " last_insert_id(), "
        + "'" + sNewKeyName + "', "
        + System.currentTimeMillis() + ", "
        + nUserUID + ", "
        + "1)";

      stmt.execute(sSQL);


      sSQL = "commit;";
      System.out.println("querying with sql \n " + sSQL);
      stmt.execute(sSQL);


      sSQL = "select @id;";
      System.out.println("querying with sql \n " + sSQL);
      ResultSet rs = stmt.executeQuery(sSQL);

      rs.next();

      return rs.getInt(1);

    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

    }

    return -1;

  } // newKey


  public synchronized boolean newKeyName(String sNewKeyName, int nKeyNum, int nUserUID)
  {
    if( !isStringSQLClean(sNewKeyName) || sNewKeyName.length() ==0)
    {
      return false;
    }

    try{

      Statement stmt = conn.createStatement();

      String sSQL = "select uid from key_meta_table where uid=" + nKeyNum;

      System.out.println("querying with sql \n " + sSQL);

      ResultSet rs = stmt.executeQuery(sSQL);

      if( rs.next() )
      {

        // that key does exist

        sSQL = "insert into osmKeys (uid,name,timestamp,user_uid,visible) values ("
          + " " + nKeyNum + ", "
          + "'" + sNewKeyName + "', "
          + System.currentTimeMillis() + ", "
          + nUserUID + ", "
          + "1)";

        stmt.execute(sSQL);

        return true;

      }

    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

    }

    return false;

  } // newKeyName



  public synchronized boolean deleteKey(int nKeyNum, int nUserUID)
  {
    try{

      Statement stmt = conn.createStatement();

      String sSQL = "select name, visible from osmKeys where uid=" + nKeyNum + " order by timestamp desc limit 1";

      System.out.println("querying with sql \n " + sSQL);

      ResultSet rs = stmt.executeQuery(sSQL);

      if( rs.next() )
      {

        // that key does exist

        if( rs.getString("visible").equals("0") )
        {
          // the key is already deleted
          return false;
        }

        return updateKeyVisibility(nKeyNum, false, rs.getString("name"), nUserUID);


      }

    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

    }

    return false;



  } // deleteKey


  public synchronized boolean undeleteKey(int nKeyNum, int nUserUID)
  {
    try{

      Statement stmt = conn.createStatement();

      String sSQL = "select name, visible from osmKeys where uid=" + nKeyNum + " order by timestamp desc limit 1";

      System.out.println("querying with sql \n " + sSQL);

      ResultSet rs = stmt.executeQuery(sSQL);

      if( rs.next() )
      {

        // that key does exist

        if( rs.getString("visible").equals("1") )
        {

          // the key is already undeleted
          return false;
        }

        return updateKeyVisibility(nKeyNum, true, rs.getString("name"), nUserUID);


      }

    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

    }

    return false;

  } // undeleteKey


  private synchronized boolean updateKeyVisibility(int nKeyNum, boolean bVisible, String sKeyName, int nUserUID)
  {
    try
    {
      String sVisible = "0";
      if(bVisible)
      {
        sVisible = "1";
      }

      Statement stmt = conn.createStatement();

      String sSQL = "start transaction; ";

      System.out.println("querying with sql \n " + sSQL);

      stmt.execute(sSQL);

      sSQL = "insert into osmKeys (uid,name,timestamp,user_uid,visible) values ("
        + " " + nKeyNum + ", "
        + "'" + sKeyName + "', "
        + System.currentTimeMillis() + ", "
        + nUserUID + ", "
        + sVisible + ");";

      System.out.println("querying with sql \n " + sSQL);
      stmt.execute(sSQL);

      sSQL = "update key_meta_table set visible=" + sVisible + " where uid=" +nKeyNum;

      System.out.println("querying with sql \n " + sSQL);
      stmt.execute(sSQL);

      sSQL = "commit;";

      System.out.println("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      return true;



    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

    }

    return false;




  } // updateKeyVisibility


  public synchronized boolean getKeyVisible(int nKeyNum)
  {

    try{

      Statement stmt = conn.createStatement();

      String sSQL = "select visible from key_meta_table where uid=" + nKeyNum;

      System.out.println("querying with sql \n " + sSQL);

      ResultSet rs = stmt.executeQuery(sSQL);

      if( rs.next() )
      {
        return rs.getString("visible").equals("1");
      }


    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

    }

    return false;

  } // getKeyVisible


  private boolean isStringSQLClean(String s)
  {
    int nSpace = s.indexOf(' ');

    if(nSpace == -1)
    {
      return true;
    }

    return false;

  } // isStringSQLClean


  public synchronized int newGPX(String sNewGPXName, int nUserUID)
  {
    if(!isStringSQLClean(sNewGPXName) || sNewGPXName.length() == 0)
    {
      return -1;

    }

    try{

      Statement stmt = conn.createStatement();


      String sSQL = "insert into points_meta_table (timestamp, user_uid, visible, name) values ("
        + System.currentTimeMillis() 
        + ", " + nUserUID
        + ", 1"
        + ", '"  + sNewGPXName + "')";

      System.out.println("querying with sql \n " + sSQL);
      stmt.execute(sSQL);

      sSQL = "select last_insert_id(); ";

      System.out.println("querying with sql \n " + sSQL);
      ResultSet rs = stmt.executeQuery(sSQL);

      rs.next();

      System.out.println("new gpx returning " + rs.getInt(1));

      return rs.getInt(1);

    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

    }

    return -1;

  } // newKey


  public synchronized Vector getGPXFileInfo(int nUID)
  {

    Vector v = new Vector();


    try{

      Statement stmt = conn.createStatement();


      String sSQL = "select name,timestamp,uid from points_meta_table where user_uid=" + nUID;

      System.out.println("querying with sql \n " + sSQL);

      ResultSet rs = stmt.executeQuery(sSQL);

      while(rs.next())
      {

        v.addElement(rs.getString("name"));
        v.addElement(new java.util.Date(Long.parseLong(rs.getString("timestamp"))));
        v.addElement(new Integer(rs.getInt("uid")));

      }


    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

    }

    return v;

  } // getGPXFileInfo


  public synchronized boolean dropGPX(
      int nUID,
      int nGPXUID
      )
  {

    try{

      Statement stmt = conn.createStatement();

      String sSQL = "start transaction; ";

      System.out.println("querying with sql \n " + sSQL);
      stmt.execute(sSQL);

      sSQL = "delete from points_meta_table where user_uid = "
        + nUID 
        + " and uid = " + nGPXUID;

      System.out.println("querying with sql \n " + sSQL);
      stmt.execute(sSQL);

      sSQL = "delete from tempPoints where uid="+ nUID
        + " and gpx_id=" + nGPXUID;

      System.out.println("querying with sql \n " + sSQL);
      stmt.execute(sSQL);

      sSQL = "commit;";

      System.out.println("querying with sql \n " + sSQL);
      stmt.execute(sSQL);

      return true;

    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

    }

    return false;

  } // dropGPX




  public synchronized int newNode(double latitude, double longitude, int nUserUID)
  {

    try{

      Statement stmt = conn.createStatement();

      String sSQL = "start transaction;";
      System.out.println("querying with sql \n " + sSQL);
      stmt.execute(sSQL);

      sSQL = "insert into node_meta_table (timestamp, user_uid, visible) values ("
        + System.currentTimeMillis() 
        + ", " + nUserUID
        + ", 1)";

      System.out.println("querying with sql \n " + sSQL);
      stmt.execute(sSQL);

      sSQL = "set @id = last_insert_id(); ";
      System.out.println("querying with sql \n " + sSQL);
      stmt.execute(sSQL);


      sSQL = "insert into nodes (uid, latitude, longitude, timestamp, user_uid, visible) values ("
        + " last_insert_id(), "
        + "" + latitude + ", "
        + "" + longitude + ", "
        + System.currentTimeMillis() + ", "
        + nUserUID + ", "
        + "1)";

      stmt.execute(sSQL);

      sSQL = "commit;";
      System.out.println("querying with sql \n " + sSQL);
      stmt.execute(sSQL);


      sSQL = "select @id;";
      System.out.println("querying with sql \n " + sSQL);
      ResultSet rs = stmt.executeQuery(sSQL);

      rs.next();

      return rs.getInt(1);

    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

    }

    return -1;

  } // newNode


  public synchronized boolean moveNode(int nNodeNum, double latitude, double longitude, int nUserUID)
  {

    try{

      Statement stmt = conn.createStatement();

      String sSQL = "select uid from node_meta_table where uid=" + nNodeNum;

      System.out.println("querying with sql \n " + sSQL);

      ResultSet rs = stmt.executeQuery(sSQL);

      if( rs.next() )
      {

        // that key does exist

        sSQL = "insert into nodes (uid,latitude,longitude,timestamp,user_uid,visible) values ("
          + " " + nNodeNum + ", "
          + " " + latitude + ", "
          + " " + longitude + ", "
          + System.currentTimeMillis() + ", "
          + nUserUID + ", "
          + "1)";

        stmt.execute(sSQL);

        return true;

      }

    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

    }

    return false;

  } // moveNode


  public synchronized int newStreetSegment(int node_a, int node_b, int nUserUID)
  {

    try{

      Statement stmt = conn.createStatement();

      String sSQL = "start transaction;";
      System.out.println("querying with sql \n " + sSQL);
      stmt.execute(sSQL);

      sSQL = "insert into street_segment_meta_table (timestamp, user_uid, visible) values ("
        + System.currentTimeMillis() 
        + ", " + nUserUID
        + ", 1)";

      System.out.println("querying with sql \n " + sSQL);
      stmt.execute(sSQL);

      sSQL = "set @id = last_insert_id(); ";
      System.out.println("querying with sql \n " + sSQL);
      stmt.execute(sSQL);


      sSQL = "insert into street_segments (uid, node_a, node_b, timestamp, user_uid, visible) values ("
        + " last_insert_id(), "
        + "" + node_a + ", "
        + "" + node_b + ", "
        + System.currentTimeMillis() + ", "
        + nUserUID + ", "
        + "1)";

      stmt.execute(sSQL);

      sSQL = "commit;";
      System.out.println("querying with sql \n " + sSQL);
      stmt.execute(sSQL);


      sSQL = "select @id;";
      System.out.println("querying with sql \n " + sSQL);
      ResultSet rs = stmt.executeQuery(sSQL);

      rs.next();

      return rs.getInt(1);

    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

    }

    return -1;

  } // newNode


  public synchronized Vector getNodes(double lat1, double lon1, double lat2, double lon2)
  {

    Vector v = new Vector();

    try{

      Statement stmt = conn.createStatement();

      String sSQL = "select * from (select uid,latitude,longitude,timestamp,visible from nodes where "
        + "latitude < " + lat1 + " and "
        + "latitude > " + lat2 + " and "
        + "longitude > " + lon1 + " and "
        + "longitude < " + lon2 
        +" and visible = true) as f, (select uid,visible,max(timestamp) as mtime from nodes group by uid) as g where g.uid = f.uid and f.timestamp = g.mtime and f.visible = g.visible";


      System.out.println("querying with sql \n " + sSQL);

      ResultSet rs = stmt.executeQuery(sSQL);

      while ( rs.next() )
      {
        v.add( new Integer(rs.getInt("uid")) );
        v.add( new Double(rs.getDouble("latitude")) );
        v.add( new Double(rs.getDouble("longitude")) );

      }


    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

    }

    return v;

  } // getNodes


  public synchronized boolean deleteNode(int nNodeNum, int nUserUID)
  {

    try
    {

      Statement stmt = conn.createStatement();

      String sSQL = "select latitude,longitude,max(timestamp) from nodes where uid=" + nNodeNum + " group by uid";

      System.out.println("querying with sql \n " + sSQL);

      ResultSet rs = stmt.executeQuery(sSQL);

      if( rs.next() )
      {
        System.out.println("found uid ok");

        // that key does exist

        sSQL = "insert into nodes (uid,latitude,longitude,timestamp,user_uid,visible) values ("
          + " " + nNodeNum + ", "
          + " " + rs.getString("latitude") + ", "
          + " " + rs.getString("longitude") + ", "
          + System.currentTimeMillis() + ", "
          + nUserUID + ", "
          + "0)";

        stmt.execute(sSQL);

        return true;

      }
      else
      {
        System.out.println("didnt find that uid guvnor!");

      }

    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

    }

    return false;

  } // deleteNode


  private synchronized boolean nodeExists(int nNodeNum)
  {

    try
    {

      Statement stmt = conn.createStatement();

      String sSQL = "select uid from node_meta_table where uid=" + nNodeNum;

      System.out.println("querying with sql \n " + sSQL);

      ResultSet rs = stmt.executeQuery(sSQL);

      return rs.next();

    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

    }

    return false;


  } // nodeExists



  private synchronized boolean checkNewLine(int node_a, int node_b)
  {
    if(nodeExists(node_a) && nodeExists(node_b))
    {
      // do some more checks like if the link is 100 miles long and if they're visible

      return true;

    }

    return false;

  } // checkNewLine


  public synchronized int newLine(int node_a, int node_b, int nUserUID)
  {
    if( checkNewLine(node_a, node_b) )
    {

      try{

        Statement stmt = conn.createStatement();

        String sSQL = "start transaction;";
        System.out.println("querying with sql \n " + sSQL);
        stmt.execute(sSQL);

        sSQL = "insert into street_segment_meta_table (timestamp, user_uid, visible) values ("
          + System.currentTimeMillis() 
          + ", " + nUserUID
          + ", 1)";

        System.out.println("querying with sql \n " + sSQL);
        stmt.execute(sSQL);

        sSQL = "set @id = last_insert_id(); ";
        System.out.println("querying with sql \n " + sSQL);
        stmt.execute(sSQL);


        sSQL = "insert into street_segments (uid, node_a, node_b, timestamp, user_uid, visible) values ("
          + " last_insert_id(), "
          + "" + node_a + ", "
          + "" + node_b + ", "
          + System.currentTimeMillis() + ", "
          + nUserUID + ", "
          + "1)";

        System.out.println("querying with sql \n " + sSQL);
        stmt.execute(sSQL);

        sSQL = "commit;";
        System.out.println("querying with sql \n " + sSQL);
        stmt.execute(sSQL);


        sSQL = "select @id;";
        System.out.println("querying with sql \n " + sSQL);
        ResultSet rs = stmt.executeQuery(sSQL);

        rs.next();

        return rs.getInt(1);

      }
      catch(Exception e)
      {
        System.out.println(e);
        e.printStackTrace();

      }
    }

    return -1;

  } // newLine

  
  public synchronized Vector getLines(int nnUID[])
  {

    Vector v = new Vector();

    try{

      Statement stmt = conn.createStatement();

      String sSQL = "select node_a, node_b from (select uid,node_a,node_b,timestamp,visible from street_segments where visible = true and (";
      
      for(int i = 0; i < nnUID.length; i++)
      {
        sSQL = sSQL + " node_a = " + nnUID[i] + " or node_b=" + nnUID[i];

        if(i != nnUID.length -1 )
        {
          sSQL = sSQL + " or ";

        }

      }
      
      sSQL = sSQL + ") ) as f, (select uid,visible,max(timestamp) as mtime from street_segments group by uid) as h where h.mtime = f.timestamp and h.uid = f.uid" ;
      
      System.out.println("querying with sql \n " + sSQL);

      ResultSet rs = stmt.executeQuery(sSQL);

      while ( rs.next() )
      {
        v.add( new Integer(rs.getInt("node_a")) );
        v.add( new Integer(rs.getInt("node_b")) );

      }


    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

    }

    return v;

  } // getLines



} // osmServerSQLHandler

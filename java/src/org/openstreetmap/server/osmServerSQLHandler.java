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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import java.util.Date;

import org.openstreetmap.util.gpspoint;

// TODO confirm that read-only and transaction-ed methods are not synchronized 
public class osmServerSQLHandler extends Thread {

  public static final int TYPE_STREET_SEGMENT = 0;
  public static final int TYPE_STREET = 1;
  public static final int TYPE_POI = 2;
  public static final int TYPE_AREA = 3;
  static final int MIN_USERNAME_LENGTH = 5;
  static final int MAX_USERNAME_LENGTH = 50;
  static final int MIN_PASSWORD_LENGTH = 5;
  static final int MAX_PASSWORD_LENGTH = 35;
  static final int MAX_TOKEN_LENGTH = 30;
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

  private static void LOG(String s) {
    System.err.println(new Date() + " " + s);
  }

  private static void LOG(Throwable t) {
    LOG(t.getMessage());
    t.printStackTrace(System.err);
  }

  public void closeDatabase() {
    try {
      conn.close();
    }
    catch (Exception ex) {
      LOG("try as we might, the f'ing thing wont close");
      LOG(ex);
    }
  }

  public osmServerSQLHandler() {
    sSQLConnection = "jdbc:mysql://128.40.59.181/openstreetmap?useUnicode=true&characterEncoding=latin1";
    sUser = "openstreetmap";
    sPass = "openstreetmap";
    connect();
  }

  public osmServerSQLHandler(String sTSQLConnection, String sTUser, String sTPass) {
    sSQLConnection = sTSQLConnection;
    sUser = sTUser;
    sPass = sTPass;
    connect();
  }

  private void connect() {
    try {
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      conn = DriverManager.getConnection(sSQLConnection, sUser, sPass);
      bConnectSuccess = true;
      LOG("sql connect apparently successful in sql handler");
    }
    catch (Exception ex) {
      LOG("sql connect failure");
      LOG(ex);
    }
  }

  public boolean SQLConnectSuccess() {
    return bConnectSuccess;
  }

  public boolean SQLSuccessful() {
    return bSQLSuccess;
  }

  private String createToken() {
    final String tokenLetters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    Random rand = new Random();
    StringBuffer tokenStringBuffer = new StringBuffer();
    for (int i = 0; i < 30; i++) {
      tokenStringBuffer.append(tokenLetters.charAt(rand.nextInt(tokenLetters.length())));
    }
    return tokenStringBuffer.toString();
  }

  public synchronized String login(String user, String pass) {
    LOG("login " + user + " " + pass);
    if (user.length() < MIN_USERNAME_LENGTH || user.length() > MAX_USERNAME_LENGTH || pass.length() < MIN_PASSWORD_LENGTH || pass.length() > MAX_PASSWORD_LENGTH || user.indexOf(" ") != -1) {
      return "ERROR";
    }
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "select uid, active from user where user='" + user + "' and pass_crypt=md5('" + pass + "')";
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = stmt.executeQuery(sSQL);
      try {
        if (rs.next() && rs.getInt("active") == 1) {
          String token = createToken();
          int uid = rs.getInt(1);
          sSQL = "update user set timeout=" + (System.currentTimeMillis() + lTimeout) + " where uid = " + uid;
          stmt.execute(sSQL);
          sSQL = "update user set token='" + token + "' where uid = " + uid;
          stmt.execute(sSQL);
          return token;
        }
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return "ERROR";
  }

  public synchronized int validateToken(String token) {
    if (bTokenValidated && System.currentTimeMillis() < lValidationTimeout) {
      return nLastUID;
    }
    if (token.length() > MAX_TOKEN_LENGTH || token.indexOf(" ") != -1) {
      LOG("didnt validate " + token);
      bTokenValidated = false;
      return -1;
    }
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "select uid from user where token='" + token + "' and timeout > " + System.currentTimeMillis();
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = stmt.executeQuery(sSQL);
      try {
        if (rs.next()) {
          int uid = rs.getInt(1);
          sSQL = "update user set timeout=" + (System.currentTimeMillis() + lTimeout) + " where uid = " + uid;
          stmt.execute(sSQL);
          LOG("validated token " + token);
          lValidationTimeout = System.currentTimeMillis() + (1000 * 60 * 1); // timeout
          // in 1 minute
          bTokenValidated = true;
          nLastUID = uid;
          return uid;
        }
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    bTokenValidated = false;
    return -1;
  }

  public synchronized int getGPXID(int nUID, String sFilename) {
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "select uid from points_meta_table where " + "user_uid = " + nUID + " and name='" + sFilename + "'";
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sSQL);
        if (rs.next()) {
          return rs.getInt("uid");
        }
        else {
          return -1;
        }
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return -1;
  }

  public synchronized boolean addPoint(float lat, float lon, float alt, long timestamp, float hor_dilution, float vert_dilution, int trackid, int quality, int satellites, int uid, int gpx_id) {
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "insert into tempPoints (gpx_id,latitude,longitude,altitude,timestamp,uid,hor_dilution,vert_dilution,trackid,quality,satellites,last_time,visible,dropped_by) values (" + " " + gpx_id + ", " + " " + lat + ", " + " " + lon + ", " + " " + alt + ", " + " " + timestamp + ", " + " " + uid + ", " + " " + hor_dilution + ", " + " " + vert_dilution + ", " + " " + trackid + ", " + " " + quality + ", " + " " + satellites + ", " + " " + System.currentTimeMillis() + ", 1,0);";
      stmt.execute(sSQL);
    }
    catch (Exception ex) {
      LOG(ex);
      LOG("uh-oh!");
      return false;
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return true;
  }

  public synchronized Vector getPointsWithDate(float p1lat, float p1lon, float p2lat, float p2lon) {
    LOG("getPoints");
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "select longitude,latitude,altitude,timestamp from tempPoints" + " where latitude < " + p1lat + " and latitude > " + p2lat + " and longitude > " + p1lon + " and longitude < " + p2lon + " and visible=1 limit 50000";
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sSQL);
        boolean bFirst = true;
        gpspoint gpFirst = new gpspoint(0, 0, 0, 0);
        gpspoint gpLastPoint = new gpspoint(0, 0, 0, 0);
        Vector v = new Vector();
        while (rs.next()) {
          v.add(new gpspoint(rs.getFloat(2), rs.getFloat(1), rs.getFloat(3), rs.getLong(4)));
        }
        bSQLSuccess = true;
        return v;
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return null;
  }

  public synchronized Vector getPoints(float p1lat, float p1lon, float p2lat, float p2lon) {
    LOG("getPoints");
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "select latitude,longitude from tempPoints" + " where latitude < " + p1lat + " and latitude > " + p2lat + " and longitude > " + p1lon + " and longitude < " + p2lon + " and visible=1 limit 50000";
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sSQL);
        boolean bFirst = true;
        gpspoint gpFirst = new gpspoint(0, 0, 0, 0);
        gpspoint gpLastPoint = new gpspoint(0, 0, 0, 0);
        Vector v = new Vector();
        while (rs.next()) {
          v.add(new Float(rs.getFloat(1)));
          v.add(new Float(rs.getFloat(2)));
        }
        bSQLSuccess = true;
        return v;
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return null;
  }

  public synchronized Vector getFullPoints(float p1lat, float p1lon, float p2lat, float p2lon) {
    LOG("getPoints");
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "select latitude," + " longitude," + " altitude," + " timestamp, " + " hor_dilution, " + " vert_dilution, " + " trackid, " + " quality, " + " satellites, " + " user, " + " last_time " + " from tempPoints, user" + " where latitude < " + p1lat + " and latitude > " + p2lat + " and longitude > " + p1lon + " and longitude < " + p2lon + " and tempPoints.uid = user.uid" + " and visible = 1" + " limit 10000";
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sSQL);
        boolean bFirst = true;
        gpspoint gpFirst = new gpspoint(0, 0, 0, 0);
        gpspoint gpLastPoint = new gpspoint(0, 0, 0, 0);
        Vector v = new Vector();
        while (rs.next()) {
          v.add(new Double(rs.getDouble("latitude"))); // lat
          v.add(new Double(rs.getDouble("longitude"))); // lon
          v.add(new Double(rs.getDouble("altitude"))); // alt
          v.add(new java.util.Date(rs.getLong("timestamp"))); // time
          // point was taken
          v.add(new Double(rs.getDouble("hor_dilution")));
          v.add(new Double(rs.getDouble("vert_dilution")));
          v.add(new Integer(rs.getInt("trackid")));
          v.add(new Integer(rs.getInt("quality")));
          v.add(new Integer(rs.getInt("satellites")));
          v.add(rs.getString("user"));
          v.add(new java.util.Date(rs.getLong("last_time")));
        }
        bSQLSuccess = true;
        return v;
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
      System.exit(-1);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return null;
  }

  public synchronized boolean dropPoint(float lon, float lat, int uid) {
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "update tempPoints set visible=0, dropped_by=" + uid + "  where " + " latitude = " + lat + " and longitude = " + lon;
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
    }
    catch (Exception ex) {
      LOG(ex);
      return false;
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return true;
  }

  public synchronized boolean dropPointsInArea(float lon1, float lat1, float lon2, float lat2, int uid) {
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "update tempPoints set visible=0, dropped_by=" + uid + "  where " + " latitude <= " + lat1 + " and latitude >= " + lat2 + " and longitude >= " + lon1 + " and longitude <= " + lon2;
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
    }
    catch (Exception ex) {
      LOG(ex);
      return false;
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return true;
  }

  public synchronized String addUser(String user, String pass) {
    LOG("addUser " + user + " " + pass);
    if (user.length() < MIN_USERNAME_LENGTH || user.length() > MAX_USERNAME_LENGTH || pass.length() < MIN_PASSWORD_LENGTH || pass.length() > MAX_PASSWORD_LENGTH || user.indexOf(" ") != -1) {
      LOG("returning error");
      return "ERROR";
    }
    String token = createToken();
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "insert into user (user, pass_crypt, timeout, token) values (" + "'" + user + "', " + "md5('" + pass + "'), " + " " + System.currentTimeMillis() + ", " + " '" + token + "')";
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    LOG("returning token " + token);
    return token;
  }

  public synchronized boolean confirmUser(String user, String token) {
    LOG("confirm " + user + " " + token);
    if (user.length() < MIN_USERNAME_LENGTH || user.length() > MAX_USERNAME_LENGTH || user.indexOf(" ") != -1) {
      return false;
    }
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "select uid,active from user where user='" + user + "' and token='" + token + "'";
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sSQL);
        if (rs.next() && rs.getInt("active") == 0) {
          sSQL = "update user set active=1" + " where uid = " + rs.getInt("uid");
          LOG("executing sql " + sSQL);
          stmt.execute(sSQL);
          return true;
        }
        return false;
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return false;
  }

  public synchronized boolean userExists(String user) {
    LOG("user exists " + user);
    if (user.length() < MIN_USERNAME_LENGTH || user.length() > MAX_USERNAME_LENGTH || user.indexOf(" ") != -1) {
      return false;
    }
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "select uid,active from user where user='" + user + "'";
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sSQL);
        return rs.next();
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
      return false;
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
  }

  public synchronized Vector getStreets(float p1lat, float p1lon, float p2lat, float p2lon) {
    LOG("getStreets");
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "select uid_of_street, lon1, lat1, lon2, lat2 from streetSegments" + " where ( " + "     lat1 < " + p1lat + " and lat1 > " + p2lat + " and lon1 > " + p1lon + " and lon1 < " + p2lon + " ) or ( " + "     lat2 < " + p1lat + " and lat2 > " + p2lat + " and lon2 > " + p1lon + " and lon2 < " + p2lon + " ) " + " and visible=1 limit 10000";
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sSQL);
        Vector v = new Vector();
        while (rs.next()) {
          v.add(new Integer(rs.getInt(1)));
          v.add(new Float(rs.getDouble(2)));
          v.add(new Float(rs.getDouble(3)));
          v.add(new Float(rs.getDouble(4)));
          v.add(new Float(rs.getDouble(5)));
        }
        bSQLSuccess = true;
        return v;
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return null;
  }

  public synchronized int largestTrackID(String token) {
    int uid = validateToken(token);
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "select max(trackid) from tempPoints where uid=" + uid;
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sSQL);
        rs.next();
        int trackID = -1;
        if (!rs.getString(1).equals("NULL")) {
          trackID = rs.getInt(1);
        }
        return trackID;
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return -1;
  }

  private boolean doesKeyExist(int nKeyUID) {
    Vector v = new Vector();
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "select * from osmKeys where " + " uid = " + nKeyUID + " order by timestamp desc limit 1";
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sSQL);
        if (rs.next() && rs.getInt("visible") == 1) {
          return true;
        }
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return false;
  }

  public synchronized Vector getAllKeys(boolean bVisibleOrNot) {
    Vector v = new Vector();
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "select h.uid, j.name, j.user,j.timestamp from (select * from key_meta_table) as h, (select * from osmKeys left join user on user.uid=osmKeys.user_uid order by timestamp desc) as j  where h.uid=j.uid and h.visible=" + bVisibleOrNot + " group by h.uid";
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sSQL);
        while (rs.next()) {
          v.add(rs.getString(1));
          v.add(rs.getString(2));
          v.add(rs.getString(3));
          v.add(rs.getString(4));
        }
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return v;
  }

  public synchronized Vector getKeyHistory(int nKey) {
    Vector v = new Vector();
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "select osmKeys.name, osmKeys.timestamp, osmKeys.visible, user.user from osmKeys left join user on user_uid=user.uid where osmKeys.uid=" + nKey + " order by timestamp desc";
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sSQL);
        while (rs.next()) {
          v.add(rs.getString(1));
          v.add(rs.getString(2));
          v.add(rs.getString(3));
          v.add(rs.getString(4));
        }
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return v;
  }

  public synchronized int newKey(String sNewKeyName, int nUserUID) {
    if (!isStringSQLClean(sNewKeyName) || sNewKeyName.length() == 0) {
      return -1;
    }
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "start transaction;";
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      sSQL = "insert into key_meta_table (timestamp, user_uid,visible) values (" + System.currentTimeMillis() + ", " + nUserUID + ", 1)";
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      sSQL = "set @id = last_insert_id(); ";
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      sSQL = "insert into osmKeys (uid,name,timestamp,user_uid,visible) values (" + " last_insert_id(), " + "'" + sNewKeyName + "', " + System.currentTimeMillis() + ", " + nUserUID + ", " + "1)";
      stmt.execute(sSQL);
      sSQL = "commit;";
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      sSQL = "select @id;";
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sSQL);
        rs.next();
        return rs.getInt(1);
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return -1;
  }

  public synchronized boolean newKeyName(String sNewKeyName, int nKeyNum, int nUserUID) {
    if (!isStringSQLClean(sNewKeyName) || sNewKeyName.length() == 0) {
      return false;
    }
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "select uid from key_meta_table where uid=" + nKeyNum;
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sSQL);
        if (rs.next()) {
          // that key does exist
          sSQL = "insert into osmKeys (uid,name,timestamp,user_uid,visible) values (" + " " + nKeyNum + ", " + "'" + sNewKeyName + "', " + System.currentTimeMillis() + ", " + nUserUID + ", " + "1)";
          stmt.execute(sSQL);
          return true;
        }
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return false;
  }

  public synchronized boolean deleteKey(int nKeyNum, int nUserUID) {
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "select name, visible from osmKeys where uid=" + nKeyNum + " order by timestamp desc limit 1";
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sSQL);
        if (rs.next()) {
          // that key does exist
          if (rs.getString("visible").equals("0")) {
            // the key is already deleted
            return false;
          }
          return updateKeyVisibility(nKeyNum, false, rs.getString("name"), nUserUID);
        }
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return false;
  }

  public synchronized boolean undeleteKey(int nKeyNum, int nUserUID) {
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "select name, visible from osmKeys where uid=" + nKeyNum + " order by timestamp desc limit 1";
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sSQL);
        if (rs.next()) {
          // that key does exist
          if (rs.getString("visible").equals("1")) {
            // the key is already undeleted
            return false;
          }
          return updateKeyVisibility(nKeyNum, true, rs.getString("name"), nUserUID);
        }
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return false;
  }

  private synchronized boolean updateKeyVisibility(int nKeyNum, boolean bVisible, String sKeyName, int nUserUID) {
    Statement stmt = null;
    try {
      String sVisible = "0";
      if (bVisible) {
        sVisible = "1";
      }
      stmt = conn.createStatement();
      String sSQL = "start transaction; ";
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      sSQL = "insert into osmKeys (uid,name,timestamp,user_uid,visible) values (" + " " + nKeyNum + ", " + "'" + sKeyName + "', " + System.currentTimeMillis() + ", " + nUserUID + ", " + sVisible + ");";
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      sSQL = "update key_meta_table set visible=" + sVisible + " where uid=" + nKeyNum;
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      sSQL = "commit;";
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      return true;
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return false;
  }

  public synchronized boolean getKeyVisible(int nKeyNum) {
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "select visible from key_meta_table where uid=" + nKeyNum;
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sSQL);
        if (rs.next()) {
          return rs.getString("visible").equals("1");
        }
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return false;
  }

  private boolean isStringSQLClean(String s) {
    int nSpace = s.indexOf(' ');
    if (nSpace == -1) {
      return true;
    }
    return false;
  }

  public synchronized int newGPX(String sNewGPXName, int nUserUID) {
    if (!isStringSQLClean(sNewGPXName) || sNewGPXName.length() == 0) {
      return -1;
    }
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "insert into points_meta_table (timestamp, user_uid, visible, name) values (" + System.currentTimeMillis() + ", " + nUserUID + ", 1" + ", '" + sNewGPXName + "')";
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      sSQL = "select last_insert_id(); ";
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sSQL);
        rs.next();
        LOG("new gpx returning " + rs.getInt(1));
        return rs.getInt(1);
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return -1;
  }

  public synchronized Vector getGPXFileInfo(int nUID) {
    Vector v = new Vector();
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "select name,timestamp,uid from points_meta_table where user_uid=" + nUID;
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sSQL);
        while (rs.next()) {
          v.addElement(rs.getString("name"));
          v.addElement(new java.util.Date(Long.parseLong(rs.getString("timestamp"))));
          v.addElement(new Integer(rs.getInt("uid")));
        }
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return v;
  }

  public synchronized boolean dropGPX(int nUID, int nGPXUID) {
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "start transaction; ";
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      sSQL = "delete from points_meta_table where user_uid = " + nUID + " and uid = " + nGPXUID;
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      sSQL = "delete from tempPoints where uid=" + nUID + " and gpx_id=" + nGPXUID;
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      sSQL = "commit;";
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      return true;
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return false;
  }

  public synchronized int newNode(double latitude, double longitude, int nUserUID) {
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "start transaction;";
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      sSQL = "insert into node_meta_table (timestamp, user_uid, visible) values (" + System.currentTimeMillis() + ", " + nUserUID + ", 1)";
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      sSQL = "set @id = last_insert_id(); ";
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      sSQL = "insert into nodes (uid, latitude, longitude, timestamp, user_uid, visible) values (" + " last_insert_id(), " + "" + latitude + ", " + "" + longitude + ", " + System.currentTimeMillis() + ", " + nUserUID + ", " + "1)";
      stmt.execute(sSQL);
      sSQL = "commit;";
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      sSQL = "select @id;";
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sSQL);
        rs.next();
        return rs.getInt(1);
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return -1;
  }

  public synchronized boolean moveNode(int nNodeNum, double latitude, double longitude, int nUserUID) {
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "select uid from node_meta_table where uid=" + nNodeNum;
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sSQL);
        if (rs.next()) {
          // that key does exist
          sSQL = "insert into nodes (uid,latitude,longitude,timestamp,user_uid,visible) values (" + " " + nNodeNum + ", " + " " + latitude + ", " + " " + longitude + ", " + System.currentTimeMillis() + ", " + nUserUID + ", " + "1)";
          stmt.execute(sSQL);
          return true;
        }
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return false;
  }

  public synchronized int newStreetSegment(int node_a, int node_b, int nUserUID) {
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "start transaction;";
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      sSQL = "insert into street_segment_meta_table (timestamp, user_uid, visible) values (" + System.currentTimeMillis() + ", " + nUserUID + ", 1)";
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      sSQL = "set @id = last_insert_id(); ";
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      sSQL = "insert into street_segments (uid, node_a, node_b, timestamp, user_uid, visible) values (" + " last_insert_id(), " + "" + node_a + ", " + "" + node_b + ", " + System.currentTimeMillis() + ", " + nUserUID + ", " + "1)";
      stmt.execute(sSQL);
      sSQL = "commit;";
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      sSQL = "select @id;";
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sSQL);
        rs.next();
        return rs.getInt(1);
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return -1;
  }

  public synchronized Vector getNode(long lNodeUID)
  {  

    Statement stmt = null;

    try {
      stmt = conn.createStatement();
      String sSQL = "select latitude,longitude from nodes where uid=" + lNodeUID + " order by timestamp desc limit 1";

      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      rs = stmt.executeQuery(sSQL);
      rs.next();

      Vector v = new Vector();

      v.add( new Double( rs.getDouble("latitude")) );
      v.add( new Double( rs.getDouble("longitude")) );

      return v;

    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }

    return new Vector();

  } // getNode




  public synchronized Vector getNodes(double lat1, double lon1, double lat2, double lon2) {
    Vector v = new Vector();
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "select uid, latitude, longitude from (select uid,latitude,longitude,timestamp,visible from nodes where latitude < " + lat1 + " and latitude > " + lat2 + " and longitude > " + lon1 + " and longitude < " + lon2 + " and visible = true order by timestamp desc) as a group by uid";
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sSQL);
        while (rs.next()) {
          Vector vNode = new Vector();
          vNode.add(new Integer(rs.getInt("uid")));
          vNode.add(new Double(rs.getDouble("latitude")));
          vNode.add(new Double(rs.getDouble("longitude")));
          v.add(vNode);
        }
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return v;
  }

  public synchronized boolean deleteLine(int nLineNum, int nUserUID) {
    // TODO
    return false;
  }

  public synchronized boolean deleteStreet(int nStreetNum, int nUserUID) {
    // TODO
    return false;
  }

  public synchronized boolean deleteNode(int nNodeNum, int nUserUID) {

    LOG( "deleteNode called for node " + nNodeNum);
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "select latitude,longitude,max(timestamp) from nodes where uid=" + nNodeNum + " group by uid";
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sSQL);
        if (rs.next()) {
          LOG("found uid ok");
          // that key does exist
          sSQL = "insert into nodes (uid,latitude,longitude,timestamp,user_uid,visible) values (" + " " + nNodeNum + ", " + " " + rs.getString("latitude") + ", " + " " + rs.getString("longitude") + ", " + System.currentTimeMillis() + ", " + nUserUID + ", " + "0)";


          LOG("querying with sql \n " + sSQL);
          stmt.execute(sSQL);
          return true;
        }
        else {
          LOG("didnt find that uid guvnor!");
        }
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return false;
  }

  private synchronized boolean nodeExists(int nNodeNum) {
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "select uid from node_meta_table where uid=" + nNodeNum;
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sSQL);
        return rs.next();
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return false;
  }

  private synchronized boolean checkNewLine(int node_a, int node_b) {
    if (nodeExists(node_a) && nodeExists(node_b)) {
      // FIXME do some more checks like if the link is 100 miles long and
      // if they're visible
      return true;
    }
    return false;
  }

  public synchronized int newLine(int node_a, int node_b, int nUserUID) {
    if (checkNewLine(node_a, node_b)) {
      Statement stmt = null;
      try {
        stmt = conn.createStatement();
        String sSQL = "start transaction;";
        LOG("querying with sql \n " + sSQL);
        stmt.execute(sSQL);
        sSQL = "insert into street_segment_meta_table (timestamp, user_uid, visible) values (" + System.currentTimeMillis() + ", " + nUserUID + ", 1)";
        LOG("querying with sql \n " + sSQL);
        stmt.execute(sSQL);
        sSQL = "set @id = last_insert_id(); ";
        LOG("querying with sql \n " + sSQL);
        stmt.execute(sSQL);
        sSQL = "insert into street_segments (uid, node_a, node_b, timestamp, user_uid, visible) values (" + " last_insert_id(), " + "" + node_a + ", " + "" + node_b + ", " + System.currentTimeMillis() + ", " + nUserUID + ", " + "1)";
        LOG("querying with sql \n " + sSQL);
        stmt.execute(sSQL);
        sSQL = "commit;";
        LOG("querying with sql \n " + sSQL);
        stmt.execute(sSQL);
        sSQL = "select @id;";
        LOG("querying with sql \n " + sSQL);
        ResultSet rs = null;
        try {
          rs = stmt.executeQuery(sSQL);
          rs.next();
          return rs.getInt(1);
        }
        finally {
          if (rs != null) try { rs.close(); } catch (Exception ex) { }
        }
      }
      catch (Exception ex) {
        LOG(ex);
      }
      finally {
        if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
      }
    }
    return -1;
  }

  /**
   * Gets the visible lines associated with the list of nodes you give
   * 
   * @param nnUID
   *            a list of UIDs...
   */
  public synchronized Vector getLines(int nnUID[]) {
    Vector v = new Vector();
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      StringBuffer inClauseBuffer = new StringBuffer();
      inClauseBuffer.append("(");
      for (int i=0; i < nnUID.length; i++) {
        inClauseBuffer.append(nnUID[i]);
        if (i < (nnUID.length - 1)) {
          inClauseBuffer.append(", ");
        }
      }
      inClauseBuffer.append(")");
      String sSQL = "select uid, node_a, node_b from (select uid, node_a, node_b, timestamp, visible from street_segments where visible = true and (node_a in " + inClauseBuffer + " or node_b in " + inClauseBuffer + ") order by timestamp desc) as a group by uid";
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try
      {

        rs = stmt.executeQuery(sSQL);
        while (rs.next())
        {
          Vector vSegment = new Vector();
          vSegment.add(new Integer(rs.getInt("uid")));
          vSegment.add(new Integer(rs.getInt("node_a")));
          vSegment.add(new Integer(rs.getInt("node_b")));
          v.add(vSegment);
        }
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return v;
  }

  public synchronized int newStreet(int nUserUID, int street_segment) {
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "start transaction;";
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      sSQL = "insert into street_meta_table (timestamp, user_uid, visible) values (" + System.currentTimeMillis() + ", " + nUserUID + ", 1)";
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      sSQL = "set @id = last_insert_id(); ";
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      sSQL = "insert into street_table (uid, segment_uid, timestamp, user_uid, visible) values (" + " last_insert_id(), " + "" + street_segment + ", " + System.currentTimeMillis() + ", " + nUserUID + ", " + "1)";
      stmt.execute(sSQL);
      sSQL = "commit;";
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      sSQL = "select @id;";
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sSQL);
        rs.next();
        return rs.getInt(1);
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return -1;
  }

  private boolean doesStreetExist(int nStreetUID) {
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "select * from street_meta_table where uid=" + nStreetUID;
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sSQL);
        if (rs.next()) {
          return true;
        }
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return false;
  }

  private boolean doesAreaExist(int nAreaUID) {
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "select * from area_meta_table where uid=" + nAreaUID;
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sSQL);
        if (rs.next()) {
          return true;
        }
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return false;
  }

  private boolean doesNodeExist(int nNodeUID) {
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "select uid from nodes where uid=" + nNodeUID;
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sSQL);
        if (rs.next()) {
          return true;
        }
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return false;
  }

  private boolean doesStreetSegmentExist(int nStreetSegmentUID) {
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "select * from street_segment_meta_table where uid=" + nStreetSegmentUID;
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sSQL);
        if (rs.next()) {
          return true;
        }
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return false;
  }

  private boolean doesStreetHaveSegmentVisible(int nStreetUID, int nStreetSegmentUID) {
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "select visible from street_table where" + " uid=" + nStreetUID + " and segment_uid=" + nStreetSegmentUID + " order by timestamp desc limit 1";
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sSQL);
        if (rs.next()) {
          int nVisible = rs.getInt("visible");
          if (nVisible == 1) {
            return true;
          }
        }
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return false;
  }

  public synchronized boolean addSegmentToStreet(int nUserUID, int nStreetUID, int nStreetSegmentUID) {
    if (doesStreetExist(nStreetUID) && doesStreetSegmentExist(nStreetSegmentUID) && !doesStreetHaveSegmentVisible(nStreetUID, nStreetSegmentUID)) {
      Statement stmt = null;
      try {
        stmt = conn.createStatement();
        String sSQL = "start transaction;";
        LOG("querying with sql \n " + sSQL);
        stmt.execute(sSQL);
        sSQL = "insert into street_table (uid, segment_uid, timestamp, user_uid, visible) values (" + "" + nStreetUID + ", " + "" + nStreetSegmentUID + ", " + System.currentTimeMillis() + ", " + nUserUID + ", " + "1)";
        LOG("querying with sql \n " + sSQL);
        stmt.execute(sSQL);
        sSQL = "commit;";
        LOG("querying with sql \n " + sSQL);
        stmt.execute(sSQL);
        return true;
      }
      catch (Exception ex) {
        LOG(ex);
      }
      finally {
        if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
      }
    }
    return false;
  }

  public synchronized boolean dropSegmentFromStreet(int nUserUID, int nStreetUID, int nStreetSegmentUID) {
    if (doesStreetExist(nStreetUID) && doesStreetSegmentExist(nStreetSegmentUID) && doesStreetHaveSegmentVisible(nStreetUID, nStreetSegmentUID)) {
      Statement stmt = null;
      try {
        stmt = conn.createStatement();
        String sSQL = "start transaction;";
        LOG("querying with sql \n " + sSQL);
        stmt.execute(sSQL);
        sSQL = "insert into street_table (uid, segment_uid, timestamp, user_uid, visible) values (" + "" + nStreetUID + ", " + "" + nStreetSegmentUID + ", " + System.currentTimeMillis() + ", " + nUserUID + ", " + "0)";
        LOG("querying with sql \n " + sSQL);
        stmt.execute(sSQL);
        sSQL = "commit;";
        LOG("querying with sql \n " + sSQL);
        stmt.execute(sSQL);
        return true;
      }
      catch (Exception ex) {
        LOG(ex);
      }
      finally {
        if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
      }
    }
    return false;
  }

  public synchronized boolean updateStreetKeyValue(int nUID, int nStreetUID, int nKeyUID, String sValue) {
    if (doesStreetExist(nStreetUID) && doesKeyExist(nKeyUID)) {
      Statement stmt = null;
      try {
        stmt = conn.createStatement();
        String sSQL = "insert into street_values (user_uid, street_uid, key_uid, val, timestamp) values (" + "" + nUID + ", " + "" + nStreetUID + ", " + "" + nKeyUID + ", " + "'" + sValue + "', " + System.currentTimeMillis() + ")";
        LOG("querying with sql \n " + sSQL);
        stmt.execute(sSQL);
        return true;
      }
      catch (Exception ex) {
        LOG(ex);
      }
      finally {
        if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
      }
    }
    return false;
  }

  public synchronized boolean updateAreaKeyValue(int nUID, int nAreaUID, int nKeyUID, String sValue) {
    if (doesAreaExist(nAreaUID) && doesKeyExist(nKeyUID)) {
      Statement stmt = null;
      try {
        stmt = conn.createStatement();
        String sSQL = "insert into area_values (user_uid, area_uid, key_uid, val, timestamp) values (" + "" + nUID + ", " + "" + nAreaUID + ", " + "" + nKeyUID + ", " + "'" + sValue + "', " + System.currentTimeMillis() + ")";
        LOG("querying with sql \n " + sSQL);
        stmt.execute(sSQL);
        return true;
      }
      catch (Exception ex) {
        LOG(ex);
      }
      finally {
        if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
      }
    }
    return false;
  }

  public synchronized boolean updateStreetSegmentKeyValue(int nUID, int nStreetSegmentUID, int nKeyUID, String sValue) {
    if (doesStreetSegmentExist(nStreetSegmentUID) && doesKeyExist(nKeyUID)) {
      Statement stmt = null;
      try {
        stmt = conn.createStatement();
        String sSQL = "insert into street_segment_values (user_uid, street_segment_uid, key_uid, val, timestamp) values (" + "" + nUID + ", " + "" + nStreetSegmentUID + ", " + "" + nKeyUID + ", " + "'" + sValue + "', " + System.currentTimeMillis() + ")";
        LOG("querying with sql \n " + sSQL);
        stmt.execute(sSQL);
        return true;
      }
      catch (Exception ex) {
        LOG(ex);
      }
      finally {
        if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
      }
    }
    return false;
  }

  public synchronized int newPointOfInterest(double latitude, double longitude, int nUserUID) {
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "start transaction;";
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      sSQL = "insert into points_of_interest_meta_table (timestamp, user_uid) values (" + System.currentTimeMillis() + ", " + nUserUID + ")";
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      sSQL = "set @id = last_insert_id(); ";
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      sSQL = "insert into point_of_interest (uid, latitude, longitude, timestamp, user_uid, visible) values (" + " last_insert_id(), " + "" + latitude + ", " + "" + longitude + ", " + System.currentTimeMillis() + ", " + nUserUID + ", " + "1)";
      stmt.execute(sSQL);
      sSQL = "commit;";
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      sSQL = "select @id;";
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sSQL);
        rs.next();
        return rs.getInt(1);
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return -1;
  }

  public synchronized boolean updatePoIKeyValue(int nUID, int nPoIUID, int nKeyUID, String sValue) {



    if (doesPoIExist(nPoIUID) && doesKeyExist(nKeyUID)) {
      Statement stmt = null;
      try {
        stmt = conn.createStatement();
        String sSQL = "insert into poi_values (user_uid, poi_uid, key_uid, val, timestamp) values (" + "" + nUID + ", " + "" + nPoIUID + ", " + "" + nKeyUID + ", " + "'" + sValue + "', " + System.currentTimeMillis() + ")";
        LOG("querying with sql \n " + sSQL);
        stmt.execute(sSQL);
        return true;
      }
      catch (Exception ex) {
        LOG(ex);
      }
      finally {
        if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
      }
    }
    return false;
  }



  private boolean doesPoIExist(int nPoIUID) {
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "select * from points_of_interest_meta_table where uid=" + nPoIUID;
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sSQL);
        if (rs.next()) {
          return true;
        }
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return false;
  }

  public synchronized int newArea(int nUserUID, Vector nodes) {
    if (nodes.size() < 3) {
      return -1;
    }
    try {
      Enumeration e = nodes.elements();
      Hashtable ht = new Hashtable();
      while (e.hasMoreElements()) {
        Integer i = (Integer) e.nextElement();
        int n = i.intValue();
        if (n < 1) {
          return -1;
        }
        if (ht.contains("" + n)) {
          return -1; // we have seen this one before
        }
        else {
          ht.put("" + n, "" + n); // put it in the list so we can
          // check if we see it again later
        }
        if (!doesNodeExist(n)) {
          return -1;
        }
      }
    }
    catch (Exception ex) {
      // fuck that input, I'm dieing...
      return -1;
    }
    // vector appears to be a well-formed list of nodes that exist!
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      String sSQL = "start transaction;";
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      sSQL = "insert into area_meta_table (timestamp, user_uid) values (" + System.currentTimeMillis() + ", " + nUserUID + ")";
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      sSQL = "set @id = last_insert_id(); ";
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      sSQL = "insert into area (uid, user_uid, timestamp, node_a, node_b, visible) values ";
      boolean bFirst = true;
      Enumeration e = nodes.elements();
      int lastNode = -1;
      Integer i = (Integer) e.nextElement();
      while (e.hasMoreElements()) {
        if (!bFirst) {
          sSQL = sSQL + ",";
        }
        else {
          bFirst = false;
        }
        Integer p = (Integer) e.nextElement();
        sSQL = sSQL + "(" + " last_insert_id(), " + nUserUID + ", " + System.currentTimeMillis() + ", " + "" + i + ", " + "" + p + ", " + "1)";
        i = new Integer(p.intValue()); // make sure we dereference..
        // TODO might be a good idea to test this one day
      }
      stmt.execute(sSQL);
      sSQL = "commit;";
      LOG("querying with sql \n " + sSQL);
      stmt.execute(sSQL);
      sSQL = "select @id;";
      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sSQL);
        rs.next();
        return rs.getInt(1);
      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }
    return -1;
  }


  public Vector getFeatureValues(int nFeatureType, long lFeatureUID, long lUserUID)
  {
    //FIXME: put all the feature types in the same database table?

    switch(nFeatureType)
    {
      case TYPE_STREET_SEGMENT:
        return getValues("street_segment_values", lFeatureUID, lUserUID);
      case TYPE_STREET:
        return getValues("street_values", lFeatureUID, lUserUID);

      case TYPE_POI:
        return getValues("poi_values", lFeatureUID, lUserUID);

      case TYPE_AREA:
        return getValues("area_values", lFeatureUID, lUserUID);

    }

    return new Vector();

  } // getFeatureValues



  private Vector getValues(String sTableName, long lFeatureUID, long lUserUID)
  {
    Vector v = new Vector();


    Statement stmt = null;
    try
    {
      stmt = conn.createStatement();
      String sSQL = "select * from (select * from "
        + sTableName + " where "
        + " street_uid = " + lFeatureUID
        + " order by timestamp desc) as h group by key_uid;";

      LOG("querying with sql \n " + sSQL);
      ResultSet rs = null;

      try {
        rs = stmt.executeQuery(sSQL);

        while(rs.next())
        {
          String sKeyNum = rs.getString("key_uid");
          String sValue = rs.getString("val");
          java.util.Date dDate = new java.util.Date(
              rs.getLong( "timestamp" ) );

          Vector vKey = new Vector();

          vKey.add( sKeyNum );
          vKey.add( sValue );
          vKey.add( dDate );

          v.add(vKey);

        }

        return v;


      }
      finally {
        if (rs != null) try { rs.close(); } catch (Exception ex) { }
      }
    }
    catch (Exception ex) {
      LOG(ex);
    }
    finally {
      if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
    }

    return new Vector();

  } // getValues

} // osmServerSQLHandler

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
import java.net.*;
import java.io.*;
import org.apache.xmlrpc.*;

import org.openstreetmap.util.gpspoint;


/** 
 * osmServerHandler is whats exposed over XMLRPC to client apps. This documentation is incomplete. FIXME!
 * @author Steve Coast
 * @version .1
 */
public class osmServerHandler
{
  
  private String sJDBC = "jdbc:mysql://128.40.59.181/openstreetmap?useUnicode=true&characterEncoding=latin1";

  
  private osmServerSQLHandler osmSQLH;

  
  private String safeSQLString(String s)
  {
    // makes a string SQL safe. hopefully.

    return s.replace('\'', '`');


  } // safeSQLString

  /**
   * Instantiates the handler
   */
  public osmServerHandler()
  {

  
    osmSQLH = new osmServerSQLHandler(sJDBC, "openstreetmap","openstreetmap");

    
  } // osmServerSQLHandler
  

  /**
   * Checks to see if we connected to the database OK
   * @return whether we connected ok
   */
  public boolean SQLConnectSuccess()
  {
    return osmSQLH.SQLConnectSuccess();

  } // SQLConnectSuccess
  
 
  /**
   * Finds the largest gpx upload for user associated with the given token
   * @param token the login token returned by login()
   * @return the largest GPX UID uploaded by you so far
   */
  public int largestTrackID(String token)
  {
    return osmSQLH.largestTrackID(token);

  } // largestTrackID


  /**
   * Logs you in to OpenStreetMap and gives you a token to use with methods in future
   * @param sUsername your username
   * @param sPassword your password
   * @return a token to be used when manipulating data. Returns "ERROR" if there was something wrong with your user/pass.
   */
  public String login(String sUsername, String sPassword)
  {
    return( osmSQLH.login(sUsername,sPassword) );

     
  } // login
 

  /**
   * Checks to see if a given user exists
   * @param user the username
   * @return whether they exist or not
   */
  public boolean userExists(String user)
  {
    
    return  osmSQLH.userExists(user);

     
  } // addUser

  /**
   * makes sure that token is currently valid and renews it for another 10 minutes
   * @param sToken the token
   * @return true if that token exists and is valid
   */
  public boolean validateToken(String sToken)
  {
    if( osmSQLH.validateToken(sToken) == -1)
    { 
      return false;
    }

    return true;

  } // validateToken
   

  /**
   * Get the uploaded gpx points from the database in the given area range, where the first two points specify the north-west and the latter two the south-east points of a box.
   * @param token the token
   * @param p1lat the north-west latitude
   * @param p1lon the north-west longitude
   * @param p2lat the south-east latitude
   * @param p2lon the south-east longitude
   * @return A one-dimensional list of lats and lons. Should really be a list of lists, FIXME.
   */
  public Vector getPoints(
      String token,
      double p1lat,
      double p1lon,
      double p2lat,
      double p2lon)
  {
    try
    {

      if( !token.equals("applet") && osmSQLH.validateToken(token) == -1 )
      {
        return null;
      }


      return osmSQLH.getPoints((float)p1lat, (float)p1lon, (float)p2lat, (float)p2lon);

    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();
      System.exit(-1);

    }

    return null;
  } // getPoints


  /**
   * Same as getPoints but gets all the data like satellite fix accuracy and stuff. FIXME more documentation
   * @param token the token
   * @param p1lat the north-west latitude
   * @param p1lon the north-west longitude
   * @param p2lat the south-east latitude
   * @param p2lon the south-east longitude
   * @return flattened list of point attributees, like point_1_latitude (double), point_1_longitude (double), point_1_altitude (Date),
   * point_1_timestamp (double), point_1_horizontal_dilution (double), point_1_vertical_dilution (double), point_1_track_ID (int),
   * point_1_quality (int), point_1_satellites (int), point_1_user (String), point_1_last_time (Date), point_2_latitude (double),
   * point_2_longitude (double), point_2_altitude (Date)... Should be a list of lists, FIXME.
   */
  public Vector getFullPoints(
      String token,
      double p1lat,
      double p1lon,
      double p2lat,
      double p2lon)
  {
    try{

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


  /**
   * Gets the list of keys. FIXME: more docs
   */
  public Vector getAllKeys(String token, boolean bVisibleOrNot)
  {

    int uid = osmSQLH.validateToken(token);

    if( uid == -1)
    {
      return new Vector();

    }

    return osmSQLH.getAllKeys(bVisibleOrNot);

  } // getAllKeys


  public Vector getKeyHistory(String token, int nKeyNum)
  {

    int uid = osmSQLH.validateToken(token);

    if( uid == -1)
    {
      return new Vector();

    }

    return osmSQLH.getKeyHistory(nKeyNum);

  } // getKeyHistory



  public boolean deleteKey(String token, int nKeyNum)
  {

    int uid = osmSQLH.validateToken(token);

    if( uid == -1)
    {
      return false;

    }

    return osmSQLH.deleteKey(nKeyNum, uid);

  } // deleteKey



  public boolean undeleteKey(String token, int nKeyNum)
  {

    int uid = osmSQLH.validateToken(token);

    if( uid == -1)
    {
      return false;

    }

    return osmSQLH.undeleteKey(nKeyNum, uid);

  } // undeleteKey


  public boolean setNewKeyName(String token, String sNewKeyName, int nKeyNum)
  {
    int uid = osmSQLH.validateToken(token);

    if( uid == -1)
    {
      return false;

    }


    return osmSQLH.newKeyName(sNewKeyName, nKeyNum, uid);

  } // setNewKeyName



  public int newKey(String sToken, String sNewKeyName)
  {
    int uid = osmSQLH.validateToken(sToken);

    if( uid == -1)
    {
      return -1;

    }

    return osmSQLH.newKey(sNewKeyName, uid);

  } // newKey


  public boolean getKeyVisible(String sToken, int nKeyNum)
  {
    int uid = osmSQLH.validateToken(sToken);

    if( uid == -1)
    {
      return false;

    }

    return osmSQLH.getKeyVisible(nKeyNum);

  } // getKeyVisible
  
  
  public Vector getGPXFileInfo(String sToken)
  {
    int uid = osmSQLH.validateToken(sToken);

    if( uid == -1)
    {
      return new Vector();

    }

    return osmSQLH.getGPXFileInfo(uid);

  } // getGPXFileInfo

  public boolean dropGPX(String sToken, int nGPXUID)
  {
    int nUID = osmSQLH.validateToken(sToken);

    if( nUID == -1)
    {
      return false;

    }
     
    return osmSQLH.dropGPX(nUID, nGPXUID);

  }


  /**
   * Creates a new node given a lat/lon position. The node can then be linked to others to form street segments.
   * @param sToken the login token from login()
   * @param latitude the latitude of the point
   * @param longitude the longitude of the point
   * @return the uid of the node, if added. If not then you get -1
   */
  public int newNode(String sToken, double latitude, double longitude)
  {
    int nUID = osmSQLH.validateToken(sToken);

    if( nUID == -1)
    {
      return -1;

    }
     
    return osmSQLH.newNode(latitude, longitude, nUID);

  } // newNode
  

  /**
   * move a node
   * @param sToken your login token from login()
   * @param nNodeID the uid of the node
   * @param latitude the lat to move the node to
   * @param longitude the lon to move the node to
   */
  public boolean moveNode(String sToken, int nNodeID, double latitude, double longitude)
  {
    int nUID = osmSQLH.validateToken(sToken);

    if( nUID == -1)
    {
      return false;

    }
     
    return osmSQLH.moveNode(nNodeID, latitude, longitude, nUID);

  } // moveNode


  public boolean deleteNode(String sToken, int nNodeID)
  {
    int nUID = osmSQLH.validateToken(sToken);

    if( nUID == -1)
    {
      return false;

    }
     
    return osmSQLH.deleteNode(nNodeID, nUID);
   

  } // deleteNode

  
  public boolean deleteLine(String sToken, int nLineID)
  {
    int nUID = osmSQLH.validateToken(sToken);

    if( nUID == -1)
    {
      return false;

    }
     
    return osmSQLH.deleteLine(nLineID, nUID);
   

  } // deleteNode

  
  /**
   * create a new street segment between the two given nodes
   * @param node_a the first node
   * @param node_b the second node
   * @return the uid of the created segment. -1 if something went wrong.
   */
  public int newLine(String sToken, int node_a, int node_b)
  {
    int nUID = osmSQLH.validateToken(sToken);

    if( nUID == -1)
    {
      return -1;

    }
     
    return osmSQLH.newLine(node_a, node_b, nUID);

  } // newLine
 

  /**
   * Get the street segments associated with a list of nodes.
   * @param sToken the login token returned by login()
   * @param vNodes a one-dimensional list of int's which are node UID's
   * @return a list of lists with the street segment uid, the first node and then the second. Eg [[1,2,3],[4,5,6]] shows street segments 1 and 4. 1 links nodes 2 and 3, and street segment 4 links 5 and 6
   */
  public Vector getLines(String sToken, Vector vNodes)
  {
    int nUID = osmSQLH.validateToken(sToken);

    if( !sToken.equals("applet"))
    {
      if(  nUID == -1)
      {
        return new Vector();

      }
    }

    if( vNodes.size() <1 )
    {
      return new Vector();
    }

    int nnUID[] = new int[vNodes.size()];

    Enumeration e = vNodes.elements();

    for(int i = 0; i < vNodes.size(); i++)
    {
      try
      {
        Integer num = (Integer)e.nextElement();
        int nNum = num.intValue();
        
        if(nNum < 1)
        {
          return new Vector();

        }

        nnUID[i] = nNum;


      }
      catch(Exception ex)
      {
        //something went wrong casting so its dodgy input
        return new Vector();

      }
        

    }
    

    return osmSQLH.getLines(nnUID);

  } // getLines


  /**
   * Get the nodes for a specified area.
   * @param sToken your login token from login()
   * @param lat1 the north-west latitude
   * @param lon1 the north-west longitude
   * @param lat2 the south-east latitude
   * @param lon2 the south-east longitude
   * @return a list of lists with the nude UID, the latitude and longitude, something like: [[1, 10.342, 13.343],[2, 5.423, 3.234]] where in this example 2 nodes are returned.
   */
  public Vector getNodes(String sToken, double lat1, double lon1, double lat2, double lon2)
  {
    int nUID = osmSQLH.validateToken(sToken);

    if( !sToken.equals("applet"))
    {
      if(  nUID == -1)
      {
        return new Vector();

      }
    }

    return osmSQLH.getNodes(lat1, lon1, lat2, lon2);

  } // getNodes

  /**
   * Get the details on a single node
   * @param sToken your login token from login()
   * @param sNodeUID
   * @return a one-dimensional list with the node's latitude and longitude
   */
   public Vector getNode(String sToken, String sNodeUID)
  {
    int nUID = osmSQLH.validateToken(sToken);

    if( !sToken.equals("applet"))
    {
      if(  nUID == -1)
      {
        return new Vector();

      }
    }

    long lVal = 0;
    try
    {

      lVal = turnStringToLong(sNodeUID);
    }
    catch( Exception e)
    {
      //number format fucked
      return new Vector();
    }
 
    return osmSQLH.getNode(lVal);

  } // getNode
 


  /**
   * Attempts to close the db connection, although it happens automagically when you close the XMLRPC connection. This is required if you're doing server side stuff and instantiating this directly.
   */
  public void closeDatabase()
  {
    osmSQLH.closeDatabase();

  } // closeDatabase



  /**
   * Create a new street with an initial street segment in it
   * @param sToken your login token from login()
   * @param line_segment_uid the uid of the first segment you want in the street
   * @return the uid of the street
   */
  public int newStreet(String sToken, int line_segment_uid)
  {
    int nUID = osmSQLH.validateToken(sToken);

    if( nUID == -1)
    {
      return -1;

    }
     
    return osmSQLH.newStreet(nUID, line_segment_uid);

  } // newLine


  /**
   * Add a segment to a street that already exists
   * @param sToken your login token from login()
   * @param nStreetUID the uid of the street
   * @param nStreetSegmentUID the uid of the segment
   * @return true if successful, eg if that street and segment exist and that segment isn't already a visible part of that street
   */
  public boolean addSegmentToStreet(
      String sToken,
      int nStreetUID,
      int nStreetSegmentUID)
  {

    int nUID = osmSQLH.validateToken(sToken);

    if( nUID == -1)
    {
      return false;

    }
     
    return osmSQLH.addSegmentToStreet(nUID, nStreetUID, nStreetSegmentUID);

  } // addSegmentToStreet

  
  /**
   * Drop a segment from a street
   * @param sToken your login token from login()
   * @param nStreetUID the uid of the street
   * @param nStreetSegmentUID the uid of the street segment
   * @return true if we managed to drop that segment, eg the street-segment pair exists, is visible, and we managed to convince the databse to add another row
   */
  public boolean dropSegmentFromStreet(
      String sToken,
      int nStreetUID,
      int nStreetSegmentUID)
  {

    int nUID = osmSQLH.validateToken(sToken);

    if( nUID == -1)
    {
      return false;

    }
     
    return osmSQLH.dropSegmentFromStreet(nUID, nStreetUID, nStreetSegmentUID);


  } // dropSegmentFromStreet


  /**
   * Sets the value for a key associated with a street (not a street segment). This will update the value whether there was a value for this key associated with this street before or not. so find the uid for the key and the street, say 'name' is 42 and your street has uid 23 then you might do something like updateStreetKeyValue(sToken, 23,42, "baker street"). An empty string means the key wont show up (you're deleting it).
   * @param sToken the login token from login()
   * @param nStreetUID the street UID
   * @param nKeyUID the UID of the key
   * @param sValue the value
   * @return true if the key was successfully associated
   */
  public boolean updateStreetKeyValue(
      String sToken,
      int nStreetUID,
      int nKeyUID,
      String sValue)
  {

    int nUID = osmSQLH.validateToken(sToken);

    if( nUID == -1 || sValue.length() > 255)
    {
      return false;

    }
    

    return osmSQLH.updateStreetKeyValue(
        nUID,
        nStreetUID,
        nKeyUID,
        safeSQLString(sValue)
        );

  } // updateStreetKeyValue


  /**
   * Sets the value for a key associated with a street segment (not a street). This will update the value whether there was a value for this key associated with this street before or not. so find the uid for the key and the street segment, say 'name' is 42 and your street segment has uid 50 then you might do something like updateStreetSegmentKeyValue(sToken, 50,42, "baker street"). An empty string means the key wont show up (you're deleting it).
   * @param sToken the login token from login()
   * @param nStreetSegmentUID the street segment UID
   * @param nKeyUID the UID of the key
   * @param sValue the value
   * @return true if the key was successfully associated
   */
  public boolean updateStreetSegmentKeyValue(
      String sToken,
      int nStreetSegmentUID,
      int nKeyUID,
      String sValue)
  {

    int nUID = osmSQLH.validateToken(sToken);

    if( nUID == -1 || sValue.length() > 255)
    {
      return false;

    }
    

    return osmSQLH.updateStreetSegmentKeyValue(
        nUID,
        nStreetSegmentUID,
        nKeyUID,
        safeSQLString(sValue)
        );

  } // updateStreetSegmentKeyValue


  /**
   * Creates a new point of interest. This is like a node, in that its just a lat/lon point but it can't be linked to other points of interest like nodes are. This is for things like train stations, churches or other points.
   * @param sToken the login token from login()
   * @param latitude the latitude for the point
   * @param longitude the longitude for the point
   * @return the uid of the point of interest if added ok, -1 otherwise
   */
  public int newPointOfInterest(String sToken, double latitude, double longitude)
  {
    int nUID = osmSQLH.validateToken(sToken);

    if( nUID == -1)
    {
      return -1;

    }
     
    return osmSQLH.newPointOfInterest(latitude, longitude, nUID);

  } // newPointOfInterest

   /**
   * Sets the value for a key associated with a point of interest. Set an empty string if you dont want a key/value to show up with it anymore..
   * @param sToken the login token from login()
   * @param nPoIUID the point of interest UID
   * @param nKeyUID the UID of the key
   * @param sValue the value
   * @return true if the key was successfully associated
   */
  public boolean updatePoIKeyValue(
      String sToken,
      int nPoIUID,
      int nKeyUID,
      String sValue)
  {

    int nUID = osmSQLH.validateToken(sToken);

    if( nUID == -1 || sValue.length() > 255)
    {
      return false;

    }
    

    return osmSQLH.updatePoIKeyValue(
        nUID,
        nPoIUID,
        nKeyUID,
        safeSQLString(sValue)
        );

  } // updatePoIKeyValue


  /**
   * Create an area
   * @param sToken your login token from login()
   * @param nodes a list of node uid's. Must all exist, be different from each other and the length of the list must be bigger than 2.
   * @return the uid of the area if successful, -1 otherwise.
   */
  public int newArea(String sToken, Vector nodes)
  {
 
    int nUID = osmSQLH.validateToken(sToken);

    if( nUID == -1)
    {
      return -1;

    }
    
    return osmSQLH.newArea(nUID, nodes);

  } // newArea
 
  
  /**
   * Sets the value for a key associated with an area. Set an empty string if you dont want a key/value to show up with it anymore..
   * @param sToken the login token from login()
   * @param nAreaUID the area uid
   * @param nKeyUID the UID of the key
   * @param sValue the value
   * @return true if the key was successfully associated
   */
  public boolean updateAreaKeyValue(
      String sToken,
      int nAreaUID,
      int nKeyUID,
      String sValue)
  {

    int nUID = osmSQLH.validateToken(sToken);

    if( nUID == -1 || sValue.length() > 255)
    {
      return false;

    }
    

    return osmSQLH.updateAreaKeyValue(
        nUID,
        nAreaUID,
        nKeyUID,
        safeSQLString(sValue)
        );

  } // updatePoIKeyValue


  private long turnStringToLong(String sVal) throws Exception
  {
    long lVal = Long.parseLong(sVal);

    if( lVal < 0 )
    {
      throw new Exception("all longs (eg UIDs) must be positive");

    }

    return lVal;
  } // turnStringToLong


  /**
   * Gets the currently active keys and values associated with a feature, given its type and UID.
   * @param nFeatureType the type of feature, see org.openstreetmap.server.osmServerSQLHandler's static types like TYPE_STREET_SEGMENT
   * @param sFeatureUID the feature uid
   * @param sToken the login token returned by login()
   * @return a Vector of Vectors which each has the key uid, the value for that key and the timestamp that this pair was last set/edited. So you might get back something like [[14, 3, Tue May 17 12:44:26 BST 2005], [30, 1, Tue May 17 12:44:26 BST 2005]]
   */
  public Vector getFeatureValues(int nFeatureType, String sAreaUID, String sToken)
  {
    int nUID = osmSQLH.validateToken(sToken);

    if( nUID == -1 )
    {
      return new Vector();
    }

    long lVal = 0;
    try
    {

      lVal = turnStringToLong(sAreaUID);
    }
    catch( Exception e)
    {
      //number format fucked
      return new Vector();
    }
    
    return osmSQLH.getFeatureValues(
        nFeatureType,
        lVal,
        (long)nUID
        );

  } // getFeatureValues

} // osmServerHandler

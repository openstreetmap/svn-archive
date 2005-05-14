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
   * instantiates the handler
   */
  public osmServerHandler()
  {

  
    osmSQLH = new osmServerSQLHandler(sJDBC, "openstreetmap","openstreetmap");

    
  } // osmServerSQLHandler
  

  /**
   * checks to see if we connected to the databse ok
   * @return whether we connected ok
   */
  public boolean SQLConnectSuccess()
  {
    return osmSQLH.SQLConnectSuccess();

  } // SQLConnectSuccess
  
 
  /**
   * gets the largest gpx upload for user associate with given token
   * @param token the token
   * @return the largest GPX number uploaded by you so far
   */
  public int largestTrackID(String token)
  {
    return osmSQLH.largestTrackID(token);

  } // largestTrackID


  /**
   * Logs you in
   * @param user your username
   * @param pass your password
   * @return a token to be used when manipulating data. Returns "ERROR" if there was something wrong with your user/pass.
   */
  public String login(String user, String pass)
  {
    return( osmSQLH.login(user,pass) );

     
  } // login
 

  /**
   * checks to see if given user exists
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
   * get the uploaded gpx points from the database given in range where the first two points specify the north-east and the latter two the south-west points of a box.
   * @param token the token
   * @param p1lat the north-west latitude
   * @param p1lon the north-west longitude
   * @param p2lat the south-east latitude
   * @param p2lon the south-east longitude
   * @return a list of lats and lons. should be a list of lists, FIXME.
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
   * same as getPoints but gets all the data like satellite fix accuracy and stuff. FIXME more documentation
   * @param token the token
   * @param p1lat the north-west latitude
   * @param p1lon the north-west longitude
   * @param p2lat the south-east latitude
   * @param p2lon the south-east longitude
   * @return a list of lats and lons and properties. should be a list of lists, FIXME.
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
 

  public Vector getLines(String sToken, Vector v)
  {
    int nUID = osmSQLH.validateToken(sToken);

    if( !sToken.equals("applet"))
    {
      if(  nUID == -1)
      {
        return new Vector();

      }
    }

    if( v.size() <1 )
    {
      return new Vector();
    }

    int nnUID[] = new int[v.size()];

    Enumeration e = v.elements();

    for(int i = 0; i < v.size(); i++)
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

  } // getNodes

  
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
   * FIXME need to call this when you're done talking to the XMLRPC to make sure the database gets closed. This should be replaced with a timer to close it...
   */
  public void closeDatabase()
  {
    osmSQLH.closeDatabase();

  } // closeDatabase


  /**
   * deprecated! this is the old method that the static png generator uses, which needs to be rewritten to use the Line methods
   */
  public Vector getStreets(
      String token,
      double p1lat,
      double p1lon,
      double p2lat,
      double p2lon)
  {
    try{

      if( !token.equals("applet") && osmSQLH.validateToken(token) == -1 )
      {
        return null;
      }

      Vector v = osmSQLH.getStreets((float)p1lat, (float)p1lon, (float)p2lat, (float)p2lon);

      if( osmSQLH.SQLSuccessful() )
      {

        return v;

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
  } // getStreets


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


} // osmServerHandler

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
 * osmServerHandler is whats exposed over XMLRPC to client apps
 * @author Steve Coast
 * @version .1
 */
public class osmServerHandler
{
  private String sJDBC = "jdbc:mysql://128.40.59.181/openstreetmap?useUnicode=true&characterEncoding=latin1";

  
  private osmServerSQLHandler osmSQLH;

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


  public int newNode(String sToken, double latitude, double longitude)
  {
    int nUID = osmSQLH.validateToken(sToken);

    if( nUID == -1)
    {
      return -1;

    }
     
    return osmSQLH.newNode(latitude, longitude, nUID);

  } // newNode
  

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



  public void closeDatabase()
  {
    osmSQLH.closeDatabase();

  } // closeDatabase


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



} // osmServerHandler


package org.openstreetmap.util;

import java.util.*;
import java.io.*;
import javax.xml.parsers.*;

import org.openstreetmap.server.*;

import org.xml.sax.helpers.*;
import org.xml.sax.*;

//import org.apache.xmlrpc.*;
//import org.apache.xerces.parsers.SAXParser;

public class osmGPXImporter extends DefaultHandler{

  private PrintWriter out = null;
 
  private double lat = 0;
  private double lon = 0;
  private double ele = 0;
  private int fix = 0;
  
  private long timestamp = 0;
  long lPointsAdded = 0;
  int nPointTempCount = 0;
  
  private static final int MODE_GET_TIME = 1;
  private static final int MODE_GET_ELE = 2;

  private int nMode = -1; 

  private boolean bLatLonValid = false;
  private boolean bEleValid = false;
  private boolean bTimeValid = false;

  String sCurrent = "";
  boolean bDoneGetting = false;

  //XmlRpcClientLite xmlrpc;
  String sToken = "";

  String sUser;
  String sPass;
  int nLastTrackID = 0;
  private String sFilename;
  private boolean bGotGPXOK = false;
  private int nGPXUID = -1;
  private int nUID = -1;

  osmServerSQLHandler osmh;
 
  public osmGPXImporter()
  {

  } // osmGPXImporter

  public void closeDatabase()
  {
    osmh.closeDatabase();

  } // closeDatabase
  

  public osmGPXImporter(PrintWriter o, String sToken, String sFilenameIn)
  {
    out = o;

    sFilename = sFilenameIn;

    osmh = new osmServerSQLHandler("jdbc:mysql://128.40.59.181/openstreetmap?useUnicode=true&characterEncoding=latin1","openstreetmap","openstreetmap");

    if( osmh.SQLConnectSuccess() )
    {

      nUID = osmh.validateToken(sToken);

      if( nUID != -1)
      {
        // didnt log in

        nGPXUID = osmh.newGPX(sFilename, nUID);

        if(nGPXUID != -1)
        {
          bGotGPXOK = true;

        }
      }
      System.out.println("got uid and gpx uid: " + nUID + ", "+  nGPXUID);

      o.print("Starting at trackid " + nLastTrackID + "<br>");

    }

  } // osmGPXImporter


  public void startElement(
      String namespaceURI,
      String localName,
      String qName,
      Attributes atts)
  {


    if( qName.equals("trkpt"))    
    {

      lat = Double.parseDouble(atts.getValue("lat"));
      lon = Double.parseDouble(atts.getValue("lon"));

      bLatLonValid = true;

    }   

    if( qName.equals("time"))
    {

      sCurrent = "";

    }

    if( qName.equals("ele"))
    {

      sCurrent = "";

    }

    if( qName.equals("trkseg"))
    {
      nLastTrackID++;
      out.print("Found a new track, id incremented to " + nLastTrackID + "<br>");
    }



  } // startElement



  public void characters(char[] ch,
      int start,
      int length)
  {

    String s = new String(ch, start, length);

    sCurrent += s;

  } // characters


  private void setDate(String s)
  {

    StringTokenizer t = new StringTokenizer(s.trim(), " -:TZ");


    int year = Integer.parseInt(t.nextToken()) - 1900;

    int month = Integer.parseInt(t.nextToken()) - 1;
    int day = Integer.parseInt(t.nextToken()) - 1;
    int hour = Integer.parseInt(t.nextToken());
    int min = Integer.parseInt(t.nextToken());

    String seconds = t.nextToken();

    int sec = 0;
    
    if( seconds.indexOf(".") != -1)
    {
      t = new StringTokenizer(seconds,". ");
      sec = Integer.parseInt(t.nextToken());

    }
    else
    {
      sec = Integer.parseInt(seconds);
    }

    timestamp = new Date(year,month,day,hour,min,sec).getTime();
  } // setDate


  private void setEle(String s)
  {
    ele = Double.parseDouble(s.trim());

  } // setEle


  private void setFix(String s)
  {
    s = s.trim();


    if( s.equals("2d"))
    {
      fix = 3;

    }


    if( s.equals("3d"))
    {
      fix = 4;

    }


    if( s.equals("none"))
    {
      fix = 0;

    }



  } // setFix


  public void endElement(
      String uri,
      String localName,
      String qName
      )
  {

    if( qName.equals("trkpt"))
    {
      addPoint();

    }



    if( qName.equals("ele"))
    {
      setEle(sCurrent);

    }
    if( qName.equals("time"))
    {
      setDate(sCurrent);

    }

    if( qName.equals("gpx"))
    {
      out.print("Total added points: " + (nPointTempCount + lPointsAdded) + ", all done! :-)<br>");

    }

  } // endElement

  /*

     private void connectToServer()
     {

     try
     {
     xmlrpc = new XmlRpcClientLite("http://www.openstreetmap.org/api/xml.jsp");

     }
     catch(Exception e)
     {
     out.println("problem in connecting to server: " + e);
     }

     } // connectToServer
   */


  private void addPoint()
  {

    boolean b = osmh.addPoint(
        (float)lat,
        (float)lon,
        (float)ele,
        timestamp,
        (float)-1,
        (float)-1,
        (int)nLastTrackID,
        (int)255,
        (int)fix,
        nUID,
        nGPXUID
        );

    if( b )
    {
      nPointTempCount++;

      if( nPointTempCount == 500 )
      {
        lPointsAdded += 500;
        nPointTempCount = 0;

        out.print("Added " + lPointsAdded + " points so far...<br>");

        out.flush();
      } 

    }
    else
    {

      out.println("failed to add point " + lat + "," + lon + "," + ele + ": " + fix + " @" + new Date(timestamp) + "<br>");

      out.println("this is bad, quiting <br>");

    }

  } // addPoint


  private boolean SQLConnectSuccess()
  {

    return osmh.SQLConnectSuccess() && bGotGPXOK;

  }



  public void upload(InputStream is, Writer out, String token, String sFilename)
  {
    PrintWriter o = new PrintWriter(out);

    System.out.println("asked to upload");


    try{

      osmGPXImporter handler = new osmGPXImporter(o, token, sFilename);

      if( handler.SQLConnectSuccess() )
      {

        o.print("Success connecting to database<br>");
        SAXParserFactory factory = SAXParserFactory.newInstance();

        SAXParser saxParser = factory.newSAXParser();

        saxParser.parse( is, handler );

        handler.closeDatabase();
      }
      else
      {

        o.print("Something went wrong connecting to the database. Sorry.<br>");
      }
    }
    catch(Exception e)
    {
      o.print("problem in upload!: " + e + "<br>");
    }

    o.print("upload done.<br>");


  } // upload

} // osmGPXImporter


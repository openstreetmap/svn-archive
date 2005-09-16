package org.openstreetmap.client;

import java.io.IOException;

import org.xml.sax.*;

import uk.co.wilson.xml.MinML2;

import java.util.Vector;
import java.util.Date;
import java.util.Enumeration;

import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.InputStream;

import org.openstreetmap.util.Node;
import org.openstreetmap.util.Line;

public class GPXParser extends MinML2 {
  private Vector nodes = new Vector();
  private Vector lines = new Vector();
  
  double lat = 0.0;
  double lon = 0.0;
  long uid = 0;
  long line_uid = 0;
  boolean latfound = false;
  boolean lonfound = false;
  boolean uidfound = false;
  
  long uid_a = 0;
  long uid_b = 0;

  boolean looking_for_uid = false;
  boolean looking_for_node_a = false;
  boolean looking_for_node_b = false;

  boolean looking_for_line_uid = false;

  boolean line_uid_found = false;


  public GPXParser(InputStream i) {
    System.out.println("GPX parser started...");
    try {
      parse(new InputStreamReader(new BufferedInputStream(i, 1024)));
    }
    catch (final IOException e) {
      System.out.println("IOException: " + e);
      e.printStackTrace();
    }
    catch (final SAXException e) {
      System.out.println("SAXException: " + e);
      e.printStackTrace();
    }
    catch (final Throwable e) {
      System.out.println("Other Exception: " + e);
      e.printStackTrace();
    }
    //*/
  }

  public void startDocument() {
    System.out.println("Start of Document");
  }

  public void endDocument() {
    System.out.println("End of Document");
  }

  //  public void startPrefixMapping(final String prefix, final String uri) throws SAXException {
  //    System.out.println("Start prefix mapping: prefix = \"" + prefix + "\" uri = \"" + uri + "\"");
  //  }

  //  public void endPrefixMapping(final String prefix) throws SAXException {
  //    System.out.println("End prefix mapping: prefix = \"" + prefix + "\"");
  //  }

  public void startElement(final String namespaceURI,
      final String localName,
      final String qName,
      final Attributes atts)
    throws SAXException
  {
    /*
       System.out.println("Start of Element: qName = \"" + qName + "\"");
       System.out.println("Start of Element: localName = \"" + localName + "\"");
       System.out.println("Start of Element: namespaceURI = \"" + namespaceURI + "\"");
       System.out.println("Start of Attributes");

       for (int i = 0; i < atts.getLength(); i++)
       System.out.println("qName: \"" + atts.getQName(i)
       + "\" localName: \"" + atts.getLocalName(i)
       + "\" uri: \"" + atts.getURI(i)
       + "\" Type: " + atts.getType(i)
       + " Value: \"" + atts.getValue(i) + "\"");

       System.out.println("End of Attributes");
     */
    if(qName != null)
    {
      if(qName.equals("trkpt"))
      {
        String val = atts.getValue("lat");

        if(val != null)
        {
          lat = Double.parseDouble(val);
          latfound = true;      
        }

        val = atts.getValue("lon");

        if(val != null)
        {
          lon = Double.parseDouble(val);
          lonfound = true;
        }
      }

      if(qName.equals("name"))
      {
        looking_for_uid = true;
      }

      if(qName.equals("trkseg"))
      {
        looking_for_node_a = true;
        looking_for_node_b = false;
      }

      if(qName.equals("wpt"))
      {
        looking_for_uid = true;
        
        String val = atts.getValue("lat");

        if(val != null)
        {
          lat = Double.parseDouble(val);
          latfound = true;      
        }

        val = atts.getValue("lon");

        if(val != null)
        {
          lon = Double.parseDouble(val);
          lonfound = true;
          
        }

      }

      if( qName.equals("trk"))
      {
        looking_for_line_uid = true;

      }

    }

   

  } // startElement

  public void endElement(final String namespaceURI,
      final String localName,
      final String qName)
    throws SAXException
  {
    /*
       System.out.println("End of Element: qName = \"" + qName + "\"");
       System.out.println("End of Element: localName = \"" + localName + "\"");
       System.out.println("End of Element: namespaceURI = \"" + namespaceURI + "\"");
     */

    if(qName != null)
    {



      if(qName.equals("trkpt") &&  latfound && lonfound && uidfound )
      {
//        System.out.println("got node: " + uid + ": " + lon + "," + lat);
        nodes.addElement(new Node(lat, lon, uid));
        lonfound = false;
        latfound = false;
        uidfound = false;

        if( looking_for_node_a )
        {
          uid_a = uid;
          looking_for_node_a = false;
          looking_for_node_b = true;
        }
        else
        {

          if( looking_for_node_b )
          {
            uid_b = uid;
            looking_for_node_a = false;
            looking_for_node_b = false;
          }
        }

      }

      if(qName.equals("trkseg") && line_uid_found)
      {
//        System.out.println("got line: " + uid_a + " -> " + uid_b);
    
        lines.addElement(new Line(getNode(uid_a), getNode(uid_b), line_uid));
      }

      if(qName.equals("wpt"))
      {
//        System.out.println("got hanging node: " + uid + ": " + lon + "," + lat);
        nodes.addElement(new Node(lat, lon, uid));
      }


    }
  }

  public void characters (char ch[], int start, int length) {
    String in = new String(ch, start, length);

    if( looking_for_uid )
    {
      looking_for_uid = false;

      uid = Long.parseLong(in);        

      uidfound = true;

    }

    if( looking_for_line_uid )
    {
      looking_for_line_uid = false;

      line_uid = Long.parseLong(in);

      line_uid_found = true; 

    }
  }

  public void fatalError (SAXParseException e) throws SAXException {
    System.out.println("Error: " + e);
    throw e;
  }

  public Node getNode(long node_uid)
  {
    Enumeration e = nodes.elements();

    while(e.hasMoreElements() )
    {
      Node n = (Node)(e.nextElement());
      if( n.getUID() == node_uid)
      {
        return n;
      }
      

    }

    return null;
      
  } // getNode

  public Vector getNodes()
  {
    return nodes;

  } // getNodes

  public Vector getLines()
  {
    return lines;

  }

}

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
  
  double node_lat = 0.0;
  double node_lon = 0.0;
  long node_uid = 0;

  long line_uid = 0;
  long line_from_uid = 0;
  long line_to_uid = 0;

  String buffered_string = "";

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

    if( qName.equals("node") )
    {
      node_lat = Double.parseDouble( atts.getValue("lat") );
      node_lon = Double.parseDouble( atts.getValue("lon") );
      node_uid = Long.parseLong( atts.getValue("uid") );
    }

    if( qName.equals("segment") )
    {
      line_uid = Long.parseLong( atts.getValue("uid") );
      line_from_uid = Long.parseLong( atts.getValue("from") );
      line_to_uid = Long.parseLong( atts.getValue("to") );
    }
  
  } // startElement

  
  public void endElement(final String namespaceURI,
      final String localName,
      final String qName)
    throws SAXException
  {

    if(qName != null)
    {

      if(qName.equals("node"))
      {
        //System.out.println("adding node " + node_uid + " at " + node_lat + "," + node_lon);
        nodes.addElement(new Node(node_lat, node_lon, node_uid));
      }

      if(qName.equals("segment"))
      {

        //System.out.println("adding seg " + line_uid + " from " + line_from_uid + " -> " + line_to_uid);
        lines.addElement(new Line(getNode(line_from_uid), getNode(line_to_uid), line_uid));
      }
    }
  } // endElement


  public void characters (char ch[], int start, int length) {
    String in = new String(ch, start, length);

    buffered_string += in;

  } // characters

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
      if( n.uid == node_uid)
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

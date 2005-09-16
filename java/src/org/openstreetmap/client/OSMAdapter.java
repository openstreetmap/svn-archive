
/*
 * Copyright (C) 2005 Tom Carden (tom@somethingmodern.com)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 *  
 */

package org.openstreetmap.client;

import java.util.Vector;
import java.util.Iterator;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcClientLite;

import org.openstreetmap.util.*;

public class OSMAdapter {

  XmlRpcClient xmlrpc;
  String token = "";
  Vector lines,nodes;

  public OSMAdapter(Vector lines, Vector nodes, String token, String username, String password) {

    this.lines = lines;
    this.nodes = nodes;

    this.token = token;    

    // initialise Apache XML-RPC client
    try {
      xmlrpc = new XmlRpcClientLite("http://www.openstreetmap.org/api/xml.jsp");
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    // if we don't have a useful token, try to log in
    if (token == null || token.equals("") || token.equals("ERROR")) {
      if (username != null && password != null) {
        login(username, password);
      }
      else {
        System.err.println("no username or password and invalid token given");
      }
    }

    /* uncomment this to print current key/value details... 
       try {
       Vector params = new Vector();
       params.addElement(token);
       params.addElement(new Boolean(true));
       Vector keys = (Vector)xmlrpc.execute ("openstreetmap.getAllKeys", params);
       System.err.println(keys); // uid, keyname, user, timestamp -> "name" uid = 14, "zipCode" uid = 30
       }
       catch (Exception e) {
       e.printStackTrace();
       }*/


  } // OSMAdapter

  public void login(String username, String password) {
    try {
      Vector params = new Vector();
      params.addElement(username);
      params.addElement(password);
      token = (String)xmlrpc.execute("openstreetmap.login", params);
      System.err.println(token);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }


  /** top left and bottom right corners of bounding box */
  public Vector getNodes(Point topLeft, Point bottomRight) {
    return getNodes(topLeft.lat,topLeft.lon,bottomRight.lat,bottomRight.lon);
  }

  /** top left and bottom right corners of bounding box */
  public Vector getNodes(double lat1, double lon1, double lat2, double lon2) {

    Vector osmNodes = new Vector();

    try {

      Vector params = new Vector();
      params.addElement (token);
      params.addElement (new Double(lat1));
      params.addElement (new Double(lon1));
      params.addElement (new Double(lat2));
      params.addElement (new Double(lon2));
      //System.err.println(params);

      Vector result = (Vector)xmlrpc.execute ("openstreetmap.getNodes", params);
      System.err.println(result.size() + " results from getNodes");

      if (result.size() > 0) {
        Iterator iter = result.iterator();
        while(iter.hasNext()) {
          Vector result2 = (Vector)iter.next();
          Iterator iter2 = result2.iterator();
          int uid = ((Integer)iter2.next()).intValue();
          double lat = ((Double)iter2.next()).doubleValue();
          double lon = ((Double)iter2.next()).doubleValue();
          Node node = new Node(lat,lon,uid);
          osmNodes.addElement(node);
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    return osmNodes; 
  }

  /** top left and bottom right corners of bounding box */
  public Vector getPoints(Point topLeft, Point bottomRight) {
    return getPoints(topLeft.lat,topLeft.lon,bottomRight.lat,bottomRight.lon);
  }

  /** top left and bottom right corners of bounding box */
  public Vector getPoints(double lat1, double lon1, double lat2, double lon2) {

    Vector osmPoints = new Vector();

    try {

      Vector params = new Vector();
      params.addElement (token);
      params.addElement (new Double(lat1));
      params.addElement (new Double(lon1));
      params.addElement (new Double(lat2));
      params.addElement (new Double(lon2));
      //System.err.println(params);

      Vector result = (Vector)xmlrpc.execute ("openstreetmap.getPoints", params);
      System.err.println(result.size() + " results from getPoints");

      if (result.size() > 0) {
        Iterator iter = result.iterator();
        while (iter.hasNext()) {
          double lat = ((Double)iter.next()).doubleValue();
          double lon = ((Double)iter.next()).doubleValue();
          Point pt = new Point(lat,lon);
          osmPoints.addElement(pt);
        }
      }

    }
    catch (Exception e) {
      e.printStackTrace();
    }

    return osmPoints; 
  }


  /** return an OSMNode for the given id, can return null */
  public Node getNode(Integer id) {

    Node node = null;

    if (id == null) {
      System.err.println("null id sent to getNode");
      return null;
    }

    try {

      Vector params = new Vector();
      params.addElement (token);
      params.addElement (id.toString());

      Vector result = (Vector)xmlrpc.execute ("openstreetmap.getNode", params);
      if (result.size() > 1) {
        Iterator iter = result.iterator();
        double lat = ((Double)iter.next()).doubleValue();
        double lon = ((Double)iter.next()).doubleValue();
        node = new Node(lat,lon,id.intValue());
      }
      else {
        System.err.println(result.size() + " results from getNode");
        return null;
      }

    }
    catch (Exception e) {
      e.printStackTrace();
    }

    return node; 
  }
/*
  // utility method
  public Vector getIDs(Vector points) {
    Vector ids = new Vector();
    Iterator iter = points.iterator();
    while(iter.hasNext()) {
      Node node = (Node)iter.next();
      ids.addElement(new Integer(node.uid));
    }
    return ids;
  }
*/
  public void deleteNode(Node node) {
    new Thread(new NodeDeleter(node)).start();
  }
  public void deleteLine(Line line) {
    new Thread(new LineDeleter(line)).start();
  }
  public void createNode(Node node) {
    new Thread(new NodeCreator(node)).start();
  }
  public void moveNode(Node node) {
    new Thread(new NodeMover(node)).start();
  }
  public void createLine(Line line) {
    new Thread(new LineCreator(line)).start();
  }
  public void updateLineName(Line line) {
    new Thread(new LineUpdater(line)).start();
  }

  public Vector getLines(Vector pointIDs) {
    Vector osmlines = new Vector();
    try {
      if (pointIDs.size() > 0) {
        Vector params = new Vector();
        params.addElement (token);
        params.addElement (pointIDs);
        Vector result = (Vector)xmlrpc.execute ("openstreetmap.getLines", params);
        System.err.println(result.size() + " results from getLines");
        if (result.size() > 0) {
          osmlines = result; 
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return osmlines;
  }

  public boolean revalidateToken() {
    try {
      Vector params = new Vector();
      params.addElement (token);
      Boolean result = (Boolean)xmlrpc.execute ("openstreetmap.validateToken", params);
      return result.booleanValue();
    }
    catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }


  class NodeDeleter implements Runnable {

    Node node;

    NodeDeleter(Node node) {
      this.node = node;
    }

    public void run() {
      /*
      try {
        nodes.remove(node);
        for (int i = 0; i < node.lines.size(); i++) {
          Line line = (Line)node.lines.elementAt(i);
          lines.remove(line);
          // TODO - does the database do this automagically?
          // deleteLine(line);
        }

        Vector params = new Vector();
        params.addElement (token);
        params.addElement (new Integer(node.uid));

        Boolean result = (Boolean)xmlrpc.execute ("openstreetmap.deleteNode", params);
        System.err.println(result + " result from deleteNode");

        if (result.booleanValue()) {
          System.err.println("node removed successfully: " + node);
        }
        else {
          System.err.println("error removing node: " + node);
          nodes.add(node);
          for (int i = 0; i < node.lines.size(); i++) {
            Line line = (Line)node.lines.elementAt(i);
            lines.add(line);
          }
        }
      }
      catch (Exception e) {
        System.err.println("error removing node: " + node);
        e.printStackTrace();
        nodes.add(node);
        for (int i = 0; i < node.lines.size(); i++) {
          Line line = (Line)node.lines.elementAt(i);
          lines.add(line);
        }
      } 
      */
    }

  }


  class LineDeleter implements Runnable {

    Line line;

    LineDeleter(Line line) {
      this.line = line;
    }

    public void run() {
      /*
      try {
        
        lines.remove(line);

        Vector params = new Vector();
        params.addElement (token);
        params.addElement (new Integer(line.uid));

        Boolean result = (Boolean)xmlrpc.execute ("openstreetmap.deleteLine", params);
        System.err.println(result + " result from deleteLine");

        if (result.booleanValue()) {
          System.err.println("line removed successfully: " + line);
        }
        else {
          System.err.println("error removing line: " + line);
          lines.add(line);
        }
      }
      catch (Exception e) {
        System.err.println("error removing line: " + line);
        e.printStackTrace();
      }
      */
    }

  }

  class NodeCreator implements Runnable {

    Node node;

    NodeCreator(Node node) {
      this.node = node;
    }

    public void run() {
      /*
      try {
        Vector params = new Vector();
        params.addElement (token);
        params.addElement (new Double(node.lat));
        params.addElement (new Double(node.lon));

        Integer result = (Integer)xmlrpc.execute ("openstreetmap.newNode", params);
        System.err.println(result + " result from newNode");

        int id = result.intValue();
        if (id != -1) {
          node.uid = id;
          System.err.println("node created successfully: " + node);
        }
        else {
          System.err.println("error creating node: " + node);
          nodes.remove(node);
        }
      }
      catch (Exception e) {
        System.err.println("error creating node: " + node);
        e.printStackTrace();
        nodes.remove(node);
      }
      */
    }

  }

  class NodeMover implements Runnable {

    Node node;

    NodeMover(Node node) {
      this.node = node;
    }

    public void run() {
      /*
      try{
        Vector params = new Vector();
        params.addElement (token);
        params.addElement (new Integer(node.uid));
        params.addElement (new Double(node.lat));
        params.addElement (new Double(node.lon));

        Boolean result = (Boolean)xmlrpc.execute ("openstreetmap.moveNode", params);
        System.err.println(result + " result from moveNode");

        if (!result.booleanValue()) {
          System.err.println("error moving node: " + node);
          // TODO: error handling... (restore the old node position?)
        }
      }
      catch (Exception e) {
        System.err.println("error moving node: " + node);
        e.printStackTrace();
      }
      */
    }

  }

  class LineCreator implements Runnable {

    Line line;

    LineCreator(Line line) {
      this.line = line;
    }

    public void run() {
      /*
      try{
        Vector params = new Vector();
        params.addElement (token);
        params.addElement (new Integer(line.a.uid));
        params.addElement (new Integer(line.b.uid));

        Integer result = (Integer)xmlrpc.execute ("openstreetmap.newLine", params);
        System.err.println(result + " results from newLine");

        int id = result.intValue();
        if (id != -1) {
          line.uid = id;
          System.err.println("line created successfully: " + line);
        }
        else {
          System.err.println("error creating line: " + line);
          lines.remove(line);
          // TODO: error handling...
        }
      }
      catch(Exception e) {
        System.err.println("error creating line: " + line);
        e.printStackTrace();
        lines.remove(line);
      }
      */
    }

  }


  class LineUpdater implements Runnable {

    Line line;

    LineUpdater(Line line) {
      this.line = line;
    }

    public void run() {
      /*
      try{
        System.err.println("updating line: " + line + " to: " + line.name);
        line.nameChanged = false;

        Vector params = new Vector();
        params.addElement (token);
        params.addElement (new Integer(line.uid));
        params.addElement (new Integer(14)); // name, believe me
        params.addElement (new String(line.name));

        Boolean result = (Boolean)xmlrpc.execute ("openstreetmap.updateStreetSegmentKeyValue", params);

        if (result.booleanValue()) {
          System.err.println("line renamed successfully: " + line + ": " + line.name);
        }
        else {
          System.err.println("error renaming line: " + line + ": " + line.name);
          line.nameChanged = true;
        }
      }
      catch(Exception e) {
        System.err.println("error renaming line: " + line + ": " + line.name);
        e.printStackTrace();
        line.nameChanged = true;
      }
      */
    }

  }


}



package org.openstreetmap.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;

import org.openstreetmap.util.Node;
import org.openstreetmap.util.Line;
import org.openstreetmap.util.Point;

public class Adapter
{

  private String URLBASE = "http://www.openstreetmap.org/api/0.1/";
  String user, pass;

  Vector lines = new Vector();
  Vector nodes = new Vector();
  
  Credentials creds = null;


  public Adapter(String username, String password)
  {
//    System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
  //  System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
    //System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");
//    System.out.println("started OSM Adapter");
    this.user = username;
    this.pass = password;

//    String url = "http://www.openstreetmap.org/api/0.1/map?bbox=-0.149512178200823,51.5255366704934,-0.145415241799177,51.5273573295066";

    creds = new UsernamePasswordCredentials(user, pass);
    System.out.println("Adapter started");

  }

  public void getNodesAndLines(Point topLeft, Point bottomRight)
  {
    System.out.println("getting nodes and lines");
    getNodesAndLines(topLeft.lon,bottomRight.lat,bottomRight.lon,topLeft.lat);
  }

  public Vector getNodes()
  {
    return nodes;
  } // getNodes

  public Vector getLines()
  {
    return lines;

  } // getLines


  public void getNodesAndLines(double bllon, double bllat, double trlon, double trlat)
  {

    String url = URLBASE + "map?bbox=" + bllon + "," + bllat + "," + trlon + "," + trlat;

    System.out.println("trying url: " + url);
    //create a singular HttpClient object
    HttpClient client = new HttpClient();

    //establish a connection within 5 seconds
    client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);

    client.getState().setCredentials(AuthScope.ANY, creds);

    HttpMethod method = null;

    //create a method object
    method = new GetMethod(url);
    method.setFollowRedirects(true);

    //execute the method
    String responseBody = null;
    InputStream responseStream =  null;
    try{
      client.executeMethod(method);
      //            responseBody = method.getResponseBodyAsString();
      responseStream = method.getResponseBodyAsStream();
    } catch (HttpException he) {
      System.err.println("Http error connecting to '" + url + "'");
      System.err.println(he.getMessage());
      System.exit(-4);
    } catch (IOException ioe){
      System.err.println("Unable to connect to '" + url + "'");
      System.exit(-3);
    }

    GPXParser gpxp = new GPXParser(responseStream);

    nodes = gpxp.getNodes();
    lines = gpxp.getLines();    

    //clean up the connection resources
    method.releaseConnection();

  } // getMap

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



  
} // Adapter

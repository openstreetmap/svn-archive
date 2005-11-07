package org.openstreetmap.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.DeleteMethod;

import org.openstreetmap.util.Node;
import org.openstreetmap.util.Line;
import org.openstreetmap.util.Point;
import org.openstreetmap.util.Mercator;

public class Adapter
{

  private String URLBASE = "http://www.openstreetmap.org/api/0.2/";
  String user, pass;

  Vector lines;
  Vector nodes;
  
  Credentials creds = null;

  public Adapter(String username, String password, Vector l, Vector n)
  {
    
    this.user = username;
    this.pass = password;

    this.lines = l;
    this.nodes = n;

    creds = new UsernamePasswordCredentials(user, pass);
    System.out.println("Adapter started");

  } // Adapter
  

  public void getNodesAndLines(Point topLeft, Point bottomRight, Mercator projection)
  {
    System.out.println("getting nodes and lines");
    getNodesAndLines(topLeft.lon,bottomRight.lat,bottomRight.lon,topLeft.lat, projection);
    
  } // getNodesAndLines
  

  public Vector getNodes()
  {
    return nodes;
  } // getNodes

  public Vector getLines()
  {
    return lines;

  } // getLines


  public void getNodesAndLines(double bllon, double bllat, double trlon, double trlat, Mercator projection)
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

    //nodes = gpxp.getNodes();
    
    Vector vnodes = gpxp.getNodes();
    Enumeration e = vnodes.elements();
    
    while(e.hasMoreElements())
    {
       Node n = (Node)e.nextElement();
       n.project(projection);
       nodes.addElement(n);
    }

    e = gpxp.getLines().elements();

    while(e.hasMoreElements())
    {
      lines.addElement(e.nextElement());
      
    }

    

//    System.out.println("nabbed " + lines.size() + " lines"); 

    //clean up the connection resources
    method.releaseConnection();

  } // getNodesAndLines


  public void deleteNode(Node node) {
    new Thread(new NodeDeleter(node)).start();
  }
  public void deleteLine(Line line) {
    System.out.println("Deleting line " + line.uid);
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
      System.out.println("tyring to delete node with " + node.lines.size() + " lines");
            
      try {
        nodes.remove(node);
        for (int i = 0; i < node.lines.size(); i++) {
          Line line = (Line)node.lines.elementAt(i);
          lines.remove(line);
          // TODO - does the database do this automagically?
          // deleteLine(line);
        }

        String url = URLBASE + "node/" + node.uid;
        System.out.println("trying to delete node by throwing HTTP DELETE at " + url);
  
        HttpClient client = new HttpClient();

        client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
        client.getState().setCredentials(AuthScope.ANY, creds);


        DeleteMethod del = new DeleteMethod(url);
          
        client.executeMethod(del);
        int rCode = del.getStatusCode();
        
        del.releaseConnection();

        if (rCode == 200) {
          System.err.println("node removed successfully: " + node);
        }
        else {
          System.err.println("error removing node: " + node);
          System.err.println("HTTP DELETE got response " + rCode + " back from the abyss");
          
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
      
    }

  } // NodeDeleter


  class LineDeleter implements Runnable {

    Line line;

    LineDeleter(Line line) {
      this.line = line;
    }

    public void run() {
      
      System.out.println("Trying to delete line " + line);
      
      try {
        lines.remove(line);

        String url = URLBASE + "segment/" + line.uid;
        System.out.println("trying to delete line by throwing HTTP DELETE at " + url);
  
        HttpClient client = new HttpClient();

        client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
        client.getState().setCredentials(AuthScope.ANY, creds);

        DeleteMethod del = new DeleteMethod(url);
          
        client.executeMethod(del);
        int rCode = del.getStatusCode();
        
        del.releaseConnection();

        if (rCode == 200) {
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
            
    }

  }

  class NodeCreator implements Runnable {

    Node node;

    NodeCreator(Node node) {
      this.node = node;
    }

    public void run() {
      
      try {
     /*   Vector params = new Vector();
        params.addElement (token);
        params.addElement (new Double(node.lat));
        params.addElement (new Double(node.lon));

        Integer result = (Integer)xmlrpc.execute ("openstreetmap.newNode", params);
        System.err.println(result + " result from newNode");

        int id = result.intValue();
        */
 
        String xml = "<osm><node tags='' lon='" + node.lon +  "' lat='" + node.lat + "' /></osm>";

        String url = URLBASE + "newnode";

        System.out.println("Trying to PUT xml '" + xml + "' to URL " + url );
        
        HttpClient client = new HttpClient();

        client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
        client.getState().setCredentials(AuthScope.ANY, creds);

        PutMethod put = new PutMethod(url);
        put.setRequestBody(xml);
          
        client.executeMethod(put);

        int rCode = put.getStatusCode();
        long id = -1;

        System.out.println("Got response code " + rCode);
        if( rCode == 200 )
        {
          String response = put.getResponseBodyAsString();
          System.out.println("got reponse " + response);
          id = Long.parseLong(response);
        }
        
        put.releaseConnection();

       
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
    }

  } // NodeCreator


  class NodeMover implements Runnable {

    Node node;

    NodeMover(Node node) {
      this.node = node;
    }

    public void run() {
  
      try{
        String xml = "<osm><node tags='" + node.tags + "' lon='" + node.lon +  "' lat='" + node.lat + "' uid='" + node.uid + "' /></osm>";

        String url = URLBASE + "node/" + node.uid;

        System.out.println("Trying to PUT xml '" + xml + "' to URL " + url );
        
        HttpClient client = new HttpClient();

        client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
        client.getState().setCredentials(AuthScope.ANY, creds);

        PutMethod put = new PutMethod(url);
        put.setRequestBody(xml);
          
        client.executeMethod(put);
        int rCode = put.getStatusCode();
        
        put.releaseConnection();

        if (rCode != 200) { 
          System.err.println("error moving node: " + node + ", got response " + rCode + " from the abyss");
          // TODO: error handling... (restore the old node position?)
        }
        
      }
      catch (Exception e) {
        System.err.println("error moving node: " + node);
        e.printStackTrace();
      }
      
    }

  } // NodeMover

  class LineCreator implements Runnable {

    Line line;

    LineCreator(Line line) {
      this.line = line;
    }

    public void run() {
      
      try{
        /*
        Vector params = new Vector();
        params.addElement (token);
        params.addElement (new Integer(line.a.uid));
        params.addElement (new Integer(line.b.uid));

        Integer result = (Integer)xmlrpc.execute ("openstreetmap.newLine", params);
        System.err.println(result + " results from newLine");

        int id = result.intValue();
        */
 
        String xml = "<osm><segment tags='' from='" + line.a.uid + "' to='" + line.b.uid + "' /></osm>";

        String url = URLBASE + "newsegment";

        System.out.println("Trying to PUT xml '" + xml + "' to URL " + url );
        
        HttpClient client = new HttpClient();

        client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
        client.getState().setCredentials(AuthScope.ANY, creds);

        PutMethod put = new PutMethod(url);
        put.setRequestBody(xml);
          
        client.executeMethod(put);

        int rCode = put.getStatusCode();
        long id = -1;

        System.out.println("Got response code " + rCode);
        if( rCode == 200 )
        {
          String response = put.getResponseBodyAsString();
          System.out.println("got reponse " + response);
          id = Long.parseLong(response);
        }
        
        put.releaseConnection();

 

        
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
      
    }

  } // LineCreator


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

  } // LineUpdater

  
} // Adapter

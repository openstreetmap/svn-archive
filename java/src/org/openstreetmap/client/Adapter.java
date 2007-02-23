package org.openstreetmap.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.SocketException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.openstreetmap.gui.MsgBox;
import org.openstreetmap.processing.OsmApplet;
import org.openstreetmap.util.GzipAwareGetMethod;
import org.openstreetmap.util.Line;
import org.openstreetmap.util.Node;
import org.openstreetmap.util.OsmPrimitive;
import org.openstreetmap.util.OsmWriter;
import org.openstreetmap.util.Point;
import org.openstreetmap.util.Releaseable;
import org.openstreetmap.util.Way;

public class Adapter implements Releaseable {

    /**
     * Base url string to connect to the osm server api.
     * Default value not used.
     */
    private String apiUrl = "http://www.openstreetmap.org/api/0.3/";

    /**
     * The server command manager to deploy server commands.
     */
    public final CommandManager commandManager;

    /**
     * Are we currently downloading OSM data?
     */
    volatile private boolean downloadingOSMData = false;
    
    /**
     * Back reference to the main applet.
     */
    private OsmApplet applet;
    
    /**
     * Back reference to the applet's map data.
     */
    private MapData map;
    
    /**
     * The apache http credentials for the username and password.
     */
    private Credentials creds = null;
    
    /**
     * The method used to download data.
     */
    volatile private HttpMethod abortableMethod = null;

    /**
     * Flags that user has requested abort of full map download.
     */
    volatile private boolean abortingMapGet = false;

    /**
     * Create the adapter.
     * @param username	The username used to connect to the osm server
     * @param password	The password used to connect to the osm server
     * @param applet	The applet back reference
     * @param apiUrl	The base url of the osm server api.
     */
    public Adapter(String username, String password, OsmApplet applet, String apiUrl) {
        this.applet = applet;
        this.map = applet.getMapData();
        this.apiUrl = apiUrl;
        creds = new UsernamePasswordCredentials(username, password);
        commandManager = new CommandManager();
        commandManager.start();
        debug("Adapter started");
    }
    
    /**
     * Is the adapter currently downloading data from the OSM server?
     */
    public boolean getDownloadingOSMData() { return downloadingOSMData; }

    private void getCopyrights() { 
      
      
      String url =
      "http://us.maps3.yimg.com/aerial.maps.yimg.com/copyright?t=xml&rows="
        + -(this.applet.tiles.y_y_max - this.applet.tiles.y_y) + "&cols="
        + (this.applet.tiles.y_x_max - this.applet.tiles.y_x) + "&row1="
        + this.applet.tiles.y_y + "&col1=" + this.applet.tiles.y_x +
        "&zoom1=1&version1=1.6";

      debug("getCopyrights url is " + url);

      HttpClient client = getClient();

      // create a method object
      HttpMethod method = new GzipAwareGetMethod(url);
      method.setFollowRedirects(true);

      // execute the method
      InputStream responseStream = null;
      try {
        client.executeMethod(method);
        responseStream = method.getResponseBodyAsStream();
      } catch (HttpException he) {
        he.printStackTrace();
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
      if (responseStream == null) {
        MsgBox.msg("Could not download copyright data!");
        method.releaseConnection();
        return;
      }

      YahooCopyrightParser ycp = new YahooCopyrightParser(responseStream);

      this.applet.setCopyright(ycp.copyright);
    } // getCopyrights

    /**
     * Retrieve all nodes and lines in the specified boundaries from the server.
     * Will make two attempts to do the download, in case the first fails
     * @param tl The top left point of the rectangle to fetch.
     * @param br The bottom right point of the rectangle to fetch.
     * @param projection The projection algorithm to use.
     * @return Map if succeeded, <code>null</code> otherwise.
     */
    public MapData getNodesLinesWays(Point tl, Point br, Projection projection) {
      getCopyrights();

      MapData newMap = new MapData();
      debug("Getting OSM vector data...");
      String url = apiUrl + "map?bbox=" + tl.lon+","+br.lat+","+br.lon+","+tl.lat;

      downloadingOSMData = true; applet.redraw();
      
      InputStream responseStream = null;
      abortableMethod = null;
      int retries = applet.retries;
      int attempt = 0;
      
      while( (responseStream == null) && (attempt <= retries) ) {
    	  attempt++;
    	  
    	  if (attempt > 1) {
    	    debug("retrying attempt " + attempt + " of url: " + url);
        }
    	  HttpClient client = getClient();

    	  // create a method object
    	  abortableMethod = new GzipAwareGetMethod(url);
    	  abortableMethod.setFollowRedirects(true);

	      // execute the method
	      try {
	        client.executeMethod(abortableMethod);
          debug("Connected to '" + url + "'.");
	        responseStream = abortableMethod.getResponseBodyAsStream();
	      } catch (HttpException he) {
	        he.printStackTrace();
        } catch (SocketException se) {
          if (abortingMapGet) {
            debug("SocketException caught: Broken out of connection."); // assume OK
          }
          else {
            se.printStackTrace();
          }
	      } catch (IOException ioe) {
	        ioe.printStackTrace();
	      }
        finally {
          if (abortingMapGet) {
            debug("Connection aborted.");
            abortableMethod.releaseConnection();
            abortableMethod = null;
            abortingMapGet = false;
            downloadingOSMData = false;
            applet.redraw();
            return null;
          }
        }
	      
	      if (responseStream == null) {
	          debug("Could not download the main data. The server may be busy?");
	          abortableMethod.releaseConnection();
            abortableMethod = null;
	      }
      }
      
      if (responseStream == null) {
          downloadingOSMData = false; applet.redraw();
          MsgBox.msg("Could not download the main data. The server may be busy. Try again later.");
          return null;
      }

      // Process what we got back
      // (In future, this may wish to be inside the retry block)
      OxParser gpxp = new OxParser(responseStream);


      Collection vnodes = gpxp.getNodes();
      Iterator it = vnodes.iterator();
      while (it.hasNext()) {
        Node n = (Node)it.next();
        n.coor.project(projection);
        newMap.putNode(n);
      }

      it = gpxp.getLines().iterator();
      while (it.hasNext()) {
        Line l = (Line) it.next();
        newMap.putLine(l);
      }

      it = gpxp.getWays().iterator();
      while (it.hasNext()) {
        Way w = (Way)it.next();
        newMap.putWay(w);
      }

      // clean up the connection resources
      abortableMethod.releaseConnection();
      abortableMethod = null;
      downloadingOSMData = false;
      debug("OSM data download completed.  Got " + newMap.getWays().size() + " ways and " 
          + newMap.getNodes().size() + " nodes and " + newMap.getLines().size() + " lines.");
      return newMap;
    }


    /**
     * Queue the deletion of the node. Return immediatly.
     */
    public void deleteNode(Node node) {
      commandManager.add(new NodeDeleter(node));
    }

    /**
     * Queue the creation of the node. Return immediatly.
     */
    public void createNode(Node node, String tempKey) {
      commandManager.add(new NodeCreator(node, tempKey));
    }

    /**
     * Queue the movement of the node. Return immediatly.
     */
    public void moveNode(Node node, double newLat, double newLon, float newX, float newY) {
      commandManager.add(new NodeMover(node, newLat, newLon, newX, newY));
    }

    /**
     * Queue the creation of the line segment. Return immediatly.
     */
    public void createLine(Line line, String tempKey) {
      commandManager.add(new LineCreator(line, tempKey));
    }
    
    /**
     * Queue the update of the line segment. Return immediately.
     */
    public void updateLine(OsmPrimitive line) {
        // Cheat. Cheat as in use property updater to update line to/from
        // only problem being rollback - passing same line as new and old,
        // so if fails, it will appear as if line updated, but that will
        // not appear so on server.
        updateProperty(line, line);
    }

    /**
     * Queue the update of the line segment. Return immediately.
     */
    public void updateWay(Way way) {
        // Cheat (see updateLine() comment above)
        updateProperty(way, way);
    }

    /**
     * Queue the change of the node, segment or ways name. Return immediatly.
     */
    public void updateProperty(OsmPrimitive oldPrimitive, OsmPrimitive newPrimitive) {
      commandManager.add(new PropertyUpdater(oldPrimitive, newPrimitive));
    }

    /**
     * Queue the removal of the primitive.
     */
    public void removePrimitive(OsmPrimitive osm) {
      commandManager.add(new Remover(osm));
    }

    /**
     * Queue the creation of a new way. Return immediatly.
     */
    public void createWay(Way way) {
      commandManager.add(new WayCreator(way));
    }

    /**
     * Helper method used by the inner classes. Takes XML, and the URL to upload it to and uploads it with a PUT.
     * If the URL returns 200, it returns the body of the response as a string, otherwise it returns null.
     *
     *
     * @param xml The XML to put.
     * @param url The URL to put it to.
     * @return The response sent by the server (if it had a code of 200), null otherwise.
     * @throws IOException If there is some sort of network error.
     */
    private String putXMLtoURL(final String xml, final String url) throws IOException {
      debug("Trying to PUT xml \"" + xml + "\" to URL " + url);

      int retries = 0;
      int maxRetries = applet.retries;
      String body = null;
      PutMethod put = null;

      // Retry loop
      while((put == null) && (retries <= maxRetries)) {
          try {
              HttpClient client = getClient();
        
              put = new PutMethod(url);
              put.setRequestEntity(new StringRequestEntity(xml, null, "UTF-8"));
        
              client.executeMethod(put);
        
              int rCode = put.getStatusCode();
              //debug("Got response code " + rCode);
              if (rCode == 200) {
                body = put.getResponseBodyAsString();
              }
              put.releaseConnection();
          } catch(IOException e) {
              // Hit an error - report, close, and retry
              System.err.println("Error uploading - " + e);
              retries++;
              if(put != null) {
                  put.releaseConnection();
              }
          }
          catch (Exception e) {
            System.err.println("Error uploading - " + e);
          }
      }
      return body;
    }

    /**
     * Create the HttpClient with our credentials and default timeout.
     * @return The client object.
     */
    private HttpClient getClient() {
      // create a singular HttpClient object
      HttpClient client = new HttpClient();

      // establish a connection within (default 15) seconds 
      client.getHttpConnectionManager().getParams().setConnectionTimeout(applet.timeout);
      
      // wait up to 120 seconds for a response
      client.getHttpConnectionManager().getParams().setSoTimeout(120 * 1000);
      // use our credentials with the request
      client.getState().setCredentials(AuthScope.ANY, creds);
      return client;
    }

    /**
     * Abort HTTP get of map data.
     */
    public void abortMapGet() {
      if (abortableMethod != null && downloadingOSMData) {
        debug("attempting to abort HTTP get...");
        abortableMethod.abort();
        abortingMapGet = true;
      }
    }

    /**
     * Delete a specific node in the intern node list.
     */
    private class NodeDeleter implements ServerCommand {
      private Node node;
      public NodeDeleter(Node node) {this.node = node;}
      public void preConnectionModifyData() {
        synchronized (map) {
          debug("trying to delete node with " + node.lines.size() + " lines");
          map.removeNode(node.key());
          for (Iterator it = node.lines.iterator(); it.hasNext();) {
            Line line = (Line)it.next();
            map.removeLine(line);
          }
        }
      }
      public boolean connectToServer() throws IOException {
        String url = apiUrl + "node/" + node.id;
        debug("trying to delete node by throwing HTTP DELETE at " + url);

        HttpClient client = getClient();

        DeleteMethod del = new DeleteMethod(url);
        client.executeMethod(del);
        int rCode = del.getStatusCode();
        del.releaseConnection();

        if (rCode == 200) {
          debug("node removed successfully: " + node);
          return true;
        }
        System.err.println("error removing node: " + node);
        System.err.println("HTTP DELETE got response " + rCode + " back from the abyss");
        return false;
      }
      public void undoModifyData() {
        synchronized (map) {
          map.putNode(node);
          for (Iterator it = node.lines.iterator(); it.hasNext();) {
            Line line = (Line) it.next();
            map.putLine(line);
          }
        }
      }
      public void postConnectionModifyData() {}
    }

    /**
     * Create a node in the intern node list.
     */
    private class NodeCreator implements ServerCommand {
      private Node node;
      private String tempKey;
      private long id = -1;

      public NodeCreator(Node node, String t) {
        this.node = node;
        this.tempKey = t;
      }

      public void preConnectionModifyData() {
        map.putNewNode(tempKey, node);
      }

      public boolean connectToServer() throws IOException {
        String xml;
        String url = apiUrl + "node/0";
        synchronized (map) {
          StringWriter s = new StringWriter();
          OsmWriter.output(s, node);
          xml = s.getBuffer().toString();
        }

        String response = putXMLtoURL(xml, url);
        if (response != null) {
          if (response != "") debug("got response " + response);
          response = response.trim(); // get rid of leading and trailing whitespace
          id = Long.parseLong(response);
        }

        if (id != -1) {
          debug("node created successfully: " + node);
          return true;
        }
        System.err.println("error creating node: " + node);
        return false;
      }

      public void undoModifyData() {
        map.removeNode(tempKey);
      }

      public void postConnectionModifyData() {
        synchronized (map) {
          node.id = id;
          map.removeNode(tempKey);
          map.putNode(node);
        }
      }
    }


    /**
     * Move a node.
     */
    private class NodeMover implements ServerCommand {
      private final Node node;
      private final double newLat;
      private final double newLon;
      private final float newX;
      private final float newY;
      private final double oldLat;
      private final double oldLon;
      private final float oldX;
      private final float oldY;

      public NodeMover(Node node, double newLat, double newLon, float newX, float newY) {
        synchronized (map) { // just keeping consistent
          this.node = node;
          this.newLat = newLat;
          this.newLon = newLon;
          this.newX = newX;
          this.newY = newY;
          oldLat = node.coor.lat;
          oldLon = node.coor.lon;
          oldX = node.coor.x;
          oldY = node.coor.y;
        }
      }

      public void preConnectionModifyData() {
        synchronized (map) { // just keeping updates atomic
          node.coor.lat = newLat;
          node.coor.lon = newLon;
          node.coor.x = newX;
          node.coor.y = newY;
        }
      }
      public boolean connectToServer() throws IOException {
        String xml;
        String url;
        synchronized (map) {
          StringWriter s = new StringWriter();
          OsmWriter.output(s, node);
          xml = s.getBuffer().toString();
          url = apiUrl + "node/" + node.id;
        }

        String response = putXMLtoURL(xml, url);
        if (response == null) {
          System.err.println("error moving node: " + node + ", got response '" + response + "' from the abyss");
          return false;
        }
        debug("node moved successfully: " + node);
        return true;
      }
      public void undoModifyData() {
        synchronized (map) {
          node.coor.lat = oldLat;
          node.coor.lon = oldLon;
          node.coor.x = oldX;
          node.coor.y = oldY;
        }
      }
      public void postConnectionModifyData() {}
    }

    /**
     * Create a line segment in the intern map.
     */
    private class LineCreator implements ServerCommand {
      /** New line, or shallow clone of it once added to map */
      private Line line;  
      private String tempKey;
      private long id = -1;

      public LineCreator(Line line, String t) {
        this.line = line;
        this.tempKey = t;
      }

      public void preConnectionModifyData() {
        synchronized (map) {
          map.putNewLine(tempKey, line); 
          line.register();
        }
      }
      
      // e.g. currently, applet doesn't allow deletion, or property edit of new lines: 
      // that should be allowed, but in queueing deletion it would unregister()
      // the node references of the line - any pending line creator would
      // subsequently fail in connecttoServer() because the underlying data
      // has been modified.
      //
      // obviously preferable to clone for this add then UI can delete fine.
      // next step would be analysing if actions can be performed on queued
      // commands only (i.e. a delete after a queued add can be implemented
      // as undo of add instead).
      // (see CommandManager comments for more on ideal processing of command 
      // queue). 

      public boolean connectToServer() throws IOException {
        String xml;
        String url = apiUrl + "segment/0";
        // NB: have been hanging onto line outside of map - what grim fate awaits?
        synchronized (map) {
          StringWriter s = new StringWriter();
          OsmWriter.output(s, line);
          xml = s.getBuffer().toString();
        }

        String response = putXMLtoURL(xml, url);
        if (response != null) {
          //debug("got response " + response);
          id = Long.parseLong(response.trim());
          debug("line created successfully: " + id);
          return true;
        }
        System.err.println("error creating line: " + line);
        return false;
      }
      public void undoModifyData() {
        // TODO without checking, these could fail - e.g. new map
        // would need to re-acquire references to nodes
        synchronized (map) {
          map.removeLine(tempKey);
          line.unregister();
        }
      }
      public void postConnectionModifyData() {
        synchronized (map) {
          line.id = id;
          map.removeLine(tempKey);
          map.putLine(line); // places at key based on new id
        }
      }
    }

    /**
     * Update (upload) a line segment to the server.
     */
    private class PropertyUpdater implements ServerCommand {
      private OsmPrimitive oldPrimitive;
      private OsmPrimitive newPrimitive;
      public PropertyUpdater(OsmPrimitive oldPrimitive, OsmPrimitive newPrimitive) {
        // ids and classes not editable, so don't worry about sync
        if (oldPrimitive.getClass() != newPrimitive.getClass())
          throw new IllegalArgumentException("Class mismatch");
        if (oldPrimitive.id != newPrimitive.id)
          throw new IllegalArgumentException("Cannot change id");
        this.oldPrimitive = oldPrimitive;
        this.newPrimitive = newPrimitive;
      }

      public void preConnectionModifyData() {
        // already updated by properties editor?
      }

      public boolean connectToServer() throws IOException {
        StringWriter s = new StringWriter();
        OsmWriter.output(s, newPrimitive);
        String xml = s.getBuffer().toString();
        String url = apiUrl + newPrimitive.getTypeName()+"/" + newPrimitive.id;

        String response = putXMLtoURL(xml, url);
        if (response != null) {
          //debug("got response " + response);
          debug("updated primitive successfully: " + newPrimitive);
          return true;
        }
        System.err.println("error updating line: " + newPrimitive );
        return false;
      }
      public void undoModifyData() {
        newPrimitive.copyFrom(oldPrimitive);
      }
      public void postConnectionModifyData() {}
    }

    /**
     * Create a line segment in the intern map.
     */
    private class WayCreator implements ServerCommand {
      private Way way;
      private String tempKey = "temp_"+Math.random();
      private long id = -1;

      public WayCreator(Way way) {
        this.way = way;
      }

      public void preConnectionModifyData() {
        synchronized (map) {
          map.putNewWay(tempKey, way);
          way.register();
        }
      }
      public boolean connectToServer() throws IOException {
        StringWriter s = new StringWriter();
        OsmWriter.output(s, way);
        String xml = s.getBuffer().toString();
        String url = apiUrl + "way/0";

        String response = putXMLtoURL(xml, url);
        if (response != null) {
          try {
            id = Long.parseLong(response.trim());
          } catch (NumberFormatException e) {
            debug("got strange response " + response);
            return false;
          }
          debug("way created successfully: " + way);
          return true;
        }
        System.err.println("error creating way: " + way);
        return false;
      }

      public void undoModifyData() {
        synchronized (map) {
          map.removeWay(tempKey);
          way.unregister();
        }
      }
      public void postConnectionModifyData() {
        synchronized (map) {
          way.id = id;
          map.removeWay(tempKey);
          map.putWay(way);
        }
      }
    }

    /**
     * Remove (delete) any primitive from the server.
     * TODO Overlap with NodeDeleter?
     */
    private class Remover implements ServerCommand {
      private OsmPrimitive osm;

      public Remover(OsmPrimitive osm) {
        this.osm = osm;
      }

      public void preConnectionModifyData() {
        synchronized (map) {
          osm.getMainTable(applet).remove(osm.key());
          osm.unregister();
        }
      }
      public boolean connectToServer() throws IOException {
        String url = apiUrl + osm.getTypeName() + "/" + osm.id;
        debug("trying to delete object HTTP DELETE at " + url);
        HttpClient client = getClient();

        // TODO add retries to delete?
        DeleteMethod del = new DeleteMethod(url);
        client.executeMethod(del);
        int rCode = del.getStatusCode();
        del.releaseConnection();
        if (rCode == 200) {
          debug("removed successfully: " + osm);
          return true;
        }
        System.err.println("error removing: " + osm);
        return false;
      }
      public void undoModifyData() {
        synchronized (map) {
          osm.getMainTable(applet).put(osm.key(), osm);
          osm.register();
        }
      }
      public void postConnectionModifyData() {
      }
    }
    private void debug(String s) {
        applet.debug(s);
    }

    /* (non-Javadoc)
     * @see org.openstreetmap.util.Releaseable#release()
     */
    public void release() {
      commandManager.release();
      applet = null;
    }
}

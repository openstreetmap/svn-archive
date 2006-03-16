package org.openstreetmap.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Map;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.openstreetmap.util.GzipAwareGetMethod;
import org.openstreetmap.util.Line;
import org.openstreetmap.util.Node;
import org.openstreetmap.util.Point;
import org.openstreetmap.util.Tag;

public class Adapter {

	/**
	 * Base url string to connect to the osm server api.
	 */
	private String apiUrl = "http://www.openstreetmap.org/api/0.3/";

	/**
	 * The server command manager to deploy server commands.
	 */
	public final CommandManager commandManager = new CommandManager();
	
	/**
	 * A map from the key identifier for line segments to the real line segments.
	 * Type: String -> Line 
	 */
	Map lines;
	/**
	 * A map from all nodes identifiers to their actual nodes.
	 * Type: String -> Node
	 */
	Map nodes;
	/**
	 * The apache http credentials for the username and password.
	 */
	Credentials creds = null;

	/**
	 * Create the adapter.
	 * @param username	The username used to connect to the osm server
	 * @param password	The password used to connect to the osm server
	 * @param lines		The map of all line segments (String -> Line)
	 * @param nodes		The map of all nodes (String -> Node)
	 * @param apiUrl	The base url of the osm server api.
	 */
	public Adapter(String username, String password, Map lines, Map nodes, String apiUrl) {
		this.lines = lines;
		this.nodes = nodes;
		this.apiUrl = apiUrl;
		creds = new UsernamePasswordCredentials(username, password);
		System.out.println("Adapter started");
	}

	public Map getNodes() {
		return nodes;
	}

	public Map getLines() {
		return lines;
	}

	/**
	 * Retrieve all nodes and lines in the specified boundaries from the server.
	 * @param tl The top left point of the rectangle to fetch.
	 * @param br The bottom right point of the rectangle to fetch.
	 * @param projection The projection algorithm to use.
	 */
	public void getNodesAndLines(Point tl, Point br, Tile projection) {
		System.out.println("getting nodes and lines");
		String url = apiUrl + "map?bbox=" + tl.lon+","+br.lat+","+br.lon+","+tl.lat;
		
		System.out.println("trying url: " + url);
		// create a singular HttpClient object
		HttpClient client = new HttpClient();
		
		// establish a connection within 5 seconds
		client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
		client.getState().setCredentials(AuthScope.ANY, creds);
		
		HttpMethod method = null;
		
		// create a method object
		method = new GzipAwareGetMethod(url);
		method.setFollowRedirects(true);
		
		// execute the method
		InputStream responseStream = null;
		try {
			client.executeMethod(method);
			// responseBody = method.getResponseBodyAsString();
			responseStream = method.getResponseBodyAsStream();
		} catch (HttpException he) {
			System.err.println("Http error connecting to \"" + url + "\"");
			System.err.println(he.getMessage());
			System.exit(-4);
		} catch (IOException ioe) {
			System.err.println("Unable to connect to \"" + url + "\"");
			System.exit(-3);
		}
		
		OxParser gpxp = new OxParser(responseStream);
		Collection vnodes = gpxp.getNodes();
		Iterator it = vnodes.iterator();
		while (it.hasNext()) {
			Node n = (Node)it.next();
			n.project(projection);
			nodes.put(n.key(), n);
		}
		
		it = gpxp.getLines().iterator();
		while (it.hasNext()) {
			Line l = (Line)it.next();
			lines.put(l.key(), l);
		}
		
		// System.out.println("nabbed " + lines.size() + " lines");
		
		// clean up the connection resources
		method.releaseConnection();
	}

	/**
	 * Queue the deletion of the node. Return immediatly.
	 */
	public void deleteNode(Node node) {
		commandManager.add(new NodeDeleter(node));
	}

	/**
	 * Queue the deletion of the line segment. Return immediatly.
	 */
	public void deleteLine(Line line) {
		System.out.println("Deleting line " + line.id);
		commandManager.add(new LineDeleter(line));
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
	 * Queue the change of the line segments name. Return immediatly.
	 */
	public void updateLineName(Line line, String newName) {
		commandManager.add(new LineUpdater(line, newName));
	}

	/**
	 * Delete a specific node in the intern node list.
	 */
	private class NodeDeleter implements ServerCommand {
		private Node node;
		public NodeDeleter(Node node) {this.node = node;}
		public void preConnectionModifyData() {
			System.out.println("tyring to delete node with " + node.lines.size() + " lines");
			nodes.remove(node.key());
			for (Iterator it = node.lines.iterator(); it.hasNext();) {
				Line line = (Line)it.next();
				lines.remove(line.key());
				// TODO - does the database do this automagically?
				// deleteLine(line);
			}
		}
		public boolean connectToServer() throws IOException {
			String url = apiUrl + "node/" + node.id;
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
				return true;
			}
			System.err.println("error removing node: " + node);
			System.err.println("HTTP DELETE got response " + rCode + " back from the abyss");
			return false;
		}
		public void undoModifyData() {
			nodes.put(node.key(), node);
			for (Iterator it = node.lines.iterator(); it.hasNext();) {
				Line line = (Line)it.next();
				lines.put(line.key(), line);
			}
		}
		public void postConnectionModifyData() {}
	}

	
	/**
	 * Delete a specific line segment from the intern map.
	 */
	private class LineDeleter implements ServerCommand {
		private Line line;
		public LineDeleter(Line line) {this.line = line;}
		public void preConnectionModifyData() {
			System.out.println("Trying to delete line " + line);
			line.from.lines.remove(line);
			line.to.lines.remove(line);
			lines.remove(line.key());
		}
		public boolean connectToServer() throws IOException {
			String url = apiUrl + "segment/" + line.id;
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
				return true;
			}
			System.err.println("error removing line: " + line);
			return false;
		}
		public void undoModifyData() {
			lines.put(line.key(), line);
			line.to.lines.add(line);
			line.from.lines.add(line);
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
			nodes.put(tempKey, node);
		}

		public boolean connectToServer() throws IOException {
			String xml = "<osm><node id=\"0\" tags=\"\" lon=\"" + node.lon + "\" lat=\"" + node.lat + "\" /></osm>";
			String url = apiUrl + "node/0";

			System.out.println("Trying to PUT xml \"" + xml + "\" to URL " + url);

			HttpClient client = new HttpClient();

			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
			client.getState().setCredentials(AuthScope.ANY, creds);

			PutMethod put = new PutMethod(url);
			put.setRequestBody(xml);

			client.executeMethod(put);

			int rCode = put.getStatusCode();
			System.out.println("Got response code " + rCode);
			if (rCode == 200) {
				String response = put.getResponseBodyAsString();
				System.out.println("got reponse " + response);
				response = response.trim(); // get rid of leading and trailing whitespace
				id = Long.parseLong(response);
			}

			put.releaseConnection();

			if (id != -1) {
				System.err.println("node created successfully: " + node);
				return true;
			}
			System.err.println("error creating node: " + node);
			return false;
		}

		public void undoModifyData() {
			nodes.remove(tempKey);
		}

		public void postConnectionModifyData() {
			node.id = id;
			nodes.remove(tempKey);
			nodes.put(node.key(), node);
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
			this.node = node;
			this.newLat = newLat;
			this.newLon = newLon;
			this.newX = newX;
			this.newY = newY;
			oldLat = node.lat;
			oldLon = node.lon;
			oldX = node.x;
			oldY = node.y;
		}
		
		public void preConnectionModifyData() {
			node.lat = newLat;
			node.lon = newLon;
			node.x = newX;
			node.y = newY;
		}
		public boolean connectToServer() throws IOException {
			String xml = "<osm><node tags=\"" + node.tags + "\" lon=\""
			+ node.lon + "\" lat=\"" + node.lat + "\" id=\""
			+ node.id + "\" /></osm>";

			String url = apiUrl + "node/" + node.id;

			System.out.println("Trying to PUT xml \"" + xml + "\" to URL " + url);

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
				return false;
			}
			return true;
		}
		public void undoModifyData() {
			node.lat = oldLat;
			node.lon = oldLon;
			node.x = oldX;
			node.y = oldY;
		}
		public void postConnectionModifyData() {}
	}

	/**
	 * Create a line segment in the intern map.
	 */
	private class LineCreator implements ServerCommand {
		private Line line;
		private String tempKey;
		private long id = -1;
		
		public LineCreator(Line line, String t) {
			this.line = line;
			this.tempKey = t;
		}

		public void preConnectionModifyData() {
			lines.put(tempKey, line);
		}
		public boolean connectToServer() throws IOException {
			String xml = "<osm><segment id=\"0\" tags=\"\" from=\"" + line.from.id + "\" to=\"" + line.to.id + "\" /></osm>";
			String url = apiUrl + "segment/0";

			System.out.println("Trying to PUT xml \"" + xml + "\" to URL " + url);

			HttpClient client = new HttpClient();

			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
			client.getState().setCredentials(AuthScope.ANY, creds);

			PutMethod put = new PutMethod(url);
			try {
				put.setRequestBody(xml);
				client.executeMethod(put);
				int rCode = put.getStatusCode();
				String response = put.getResponseBodyAsString();
				System.out.println("Got response code " + rCode);

				if (rCode == 200) {
					System.out.println("got reponse " + response);
					id = Long.parseLong(response.trim());
					System.err.println("line created successfully: " + line);
					return true;
				}
				System.err.println("error creating line: " + line);
				return false;
			} finally {
				put.releaseConnection();
			}
		}
		public void undoModifyData() {
			lines.remove(tempKey);
		}
		public void postConnectionModifyData() {
			line.id = id;
			lines.remove(tempKey);
			lines.put(line.key(), line);
		}
	}

	/**
	 * Update (upload) a line segment to the server.
	 */
	private class LineUpdater implements ServerCommand {
		private Line line;
		private String newName;
		private String oldName;
		public LineUpdater(Line line, String newName) {
			this.line = line;
			this.newName = newName;
			oldName = line.getName();
		}

		public void preConnectionModifyData() {
			line.setName(newName);
		}
		public boolean connectToServer() throws IOException {
			String xml = "<osm><segment id=\"" + line.id + "\" from=\"" + line.from.id
					+ "\" to=\"" + line.to.id + "\">";

      Enumeration e = line.tags.elements();
      while(e.hasMoreElements()) {
        Tag tag = (Tag)e.nextElement();
        xml = xml + "<tag k=\"" + tag.key + "\" v=\"" + tag.value + "\" />";
      }

      xml = xml + "</segment></osm>";

			String url = apiUrl + "segment/" + line.id;

			System.out.println("Trying to PUT xml \"" + xml + "\" to URL " + url);

			HttpClient client = new HttpClient();

			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
			client.getState().setCredentials(AuthScope.ANY, creds);

			PutMethod put = new PutMethod(url);
			try {
				put.setRequestBody(xml);

				client.executeMethod(put);

				int rCode = put.getStatusCode();

				System.out.println("Got response code " + rCode);

				if (rCode == 200) {
					String response = put.getResponseBodyAsString();
					System.out.println("got reponse " + response);
					return true;
				}
				System.err.println("error updating line: " + line + ", got code " + rCode);
				return false;
			} finally {
				put.releaseConnection();
			}
		}
		public void undoModifyData() {
			line.setName(oldName);
		}
		public void postConnectionModifyData() {}
	}
}

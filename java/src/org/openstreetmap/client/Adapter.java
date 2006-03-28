package org.openstreetmap.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
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
import org.openstreetmap.gui.MsgBox;
import org.openstreetmap.processing.OsmApplet;
import org.openstreetmap.util.GzipAwareGetMethod;
import org.openstreetmap.util.Line;
import org.openstreetmap.util.Node;
import org.openstreetmap.util.OsmPrimitive;
import org.openstreetmap.util.OsmWriter;
import org.openstreetmap.util.Point;
import org.openstreetmap.util.Way;

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
	 * Back reference to the main applet.
	 */
	private OsmApplet applet;
	/**
	 * The apache http credentials for the username and password.
	 */
	Credentials creds = null;

	/**
	 * Create the adapter.
	 * @param username	The username used to connect to the osm server
	 * @param password	The password used to connect to the osm server
	 * @param applet	The applet back reference
	 * @param apiUrl	The base url of the osm server api.
	 */
	public Adapter(String username, String password, OsmApplet applet, String apiUrl) {
		this.applet = applet;
		this.apiUrl = apiUrl;
		creds = new UsernamePasswordCredentials(username, password);
		System.out.println("Adapter started");
	}

	/**
	 * Retrieve all nodes and lines in the specified boundaries from the server.
	 * @param tl The top left point of the rectangle to fetch.
	 * @param br The bottom right point of the rectangle to fetch.
	 * @param projection The projection algorithm to use.
	 */
	public void getNodesLinesWays(Point tl, Point br, Tile projection) {
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
			responseStream = method.getResponseBodyAsStream();
		} catch (HttpException he) {
			he.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		if (responseStream == null) {
			MsgBox.msg("Could not download the main data. The server may be busy. Try again later.");
			return;
		}

		OxParser gpxp = new OxParser(responseStream);
		Collection vnodes = gpxp.getNodes();
		Iterator it = vnodes.iterator();
		while (it.hasNext()) {
			Node n = (Node)it.next();
			n.coor.project(projection);
			applet.nodes.put(n.key(), n);
		}

		it = gpxp.getLines().iterator();
		while (it.hasNext()) {
			Line l = (Line)it.next();
			applet.lines.put(l.key(), l);
		}

		it = gpxp.getWays().iterator();
		while (it.hasNext()) {
			Way w = (Way)it.next();
			applet.ways.put(w.key(), w);
		}

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
	 * Delete a specific node in the intern node list.
	 */
	private class NodeDeleter implements ServerCommand {
		private Node node;
		public NodeDeleter(Node node) {this.node = node;}
		public void preConnectionModifyData() {
			System.out.println("tyring to delete node with " + node.lines.size() + " lines");
			applet.nodes.remove(node.key());
			for (Iterator it = node.lines.iterator(); it.hasNext();) {
				Line line = (Line)it.next();
				applet.lines.remove(line.key());
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
			applet.nodes.put(node.key(), node);
			for (Iterator it = node.lines.iterator(); it.hasNext();) {
				Line line = (Line)it.next();
				applet.lines.put(line.key(), line);
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
			applet.nodes.put(tempKey, node);
		}

		public boolean connectToServer() throws IOException {
			String xml = "<osm><node id=\"0\" tags=\"\" lon=\"" + node.coor.lon + "\" lat=\"" + node.coor.lat + "\" /></osm>";
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
			applet.nodes.remove(tempKey);
		}

		public void postConnectionModifyData() {
			node.id = id;
			applet.nodes.remove(tempKey);
			applet.nodes.put(node.key(), node);
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
			oldLat = node.coor.lat;
			oldLon = node.coor.lon;
			oldX = node.coor.x;
			oldY = node.coor.y;
		}
		
		public void preConnectionModifyData() {
			node.coor.lat = newLat;
			node.coor.lon = newLon;
			node.coor.x = newX;
			node.coor.y = newY;
		}
		public boolean connectToServer() throws IOException {
			String xml = "<osm><node tags=\"" + node.tags + "\" lon=\""
			+ node.coor.lon + "\" lat=\"" + node.coor.lat + "\" id=\""
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
			node.coor.lat = oldLat;
			node.coor.lon = oldLon;
			node.coor.x = oldX;
			node.coor.y = oldY;
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
			applet.lines.put(tempKey, line);
			line.register();
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
			applet.lines.remove(tempKey);
			line.unregister();
		}
		public void postConnectionModifyData() {
			line.id = id;
			applet.lines.remove(tempKey);
			applet.lines.put(line.key(), line);
		}
	}

	/**
	 * Update (upload) a line segment to the server.
	 */
	private class PropertyUpdater implements ServerCommand {
		private OsmPrimitive oldPrimitive;
		private OsmPrimitive newPrimitive;
		public PropertyUpdater(OsmPrimitive oldPrimitive, OsmPrimitive newPrimitive) {
			if (oldPrimitive.getClass() != newPrimitive.getClass())
				throw new IllegalArgumentException("Class mismatch");
			if (oldPrimitive.id != newPrimitive.id)
				throw new IllegalArgumentException("Cannot change id");
			this.oldPrimitive = oldPrimitive;
			this.newPrimitive = newPrimitive;
		}

		public void preConnectionModifyData() {
			OsmPrimitive backup = (OsmPrimitive)newPrimitive.clone();
			newPrimitive.copyFrom(oldPrimitive);
			oldPrimitive = backup;
		}
		public boolean connectToServer() throws IOException {
			StringWriter s = new StringWriter();
			OsmWriter.output(s, newPrimitive);
			String xml = s.getBuffer().toString();
			String url = apiUrl + newPrimitive.getTypeName()+"/" + newPrimitive.id;

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
				System.err.println("error updating line: " + newPrimitive + ", got code " + rCode);
				return false;
			} finally {
				put.releaseConnection();
			}
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
			applet.ways.put(tempKey, way);
			way.register();
		}
		public boolean connectToServer() throws IOException {
			StringWriter s = new StringWriter();
			OsmWriter.output(s, way);
			String xml = s.getBuffer().toString();
			String url = apiUrl + "way/0";

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
					System.err.println("way created successfully: " + way);
					return true;
				}
				System.err.println("error creating way: " + way);
				return false;
			} finally {
				put.releaseConnection();
			}
		}
		public void undoModifyData() {
			applet.ways.remove(tempKey);
			way.unregister();
		}
		public void postConnectionModifyData() {
			way.id = id;
			applet.ways.remove(tempKey);
			applet.ways.put(way.key(), way);
		}
	}

	/**
	 * Remove (delete) any primitive from the server.
	 */
	private class Remover implements ServerCommand {
		private OsmPrimitive osm;
		
		public Remover(OsmPrimitive osm) {
			this.osm = osm;
		}
		
		public void preConnectionModifyData() {
			osm.getMainTable(applet).remove(osm.key());
			osm.unregister();
		}
		public boolean connectToServer() throws IOException {
			String url = apiUrl + osm.getTypeName() + "/" + osm.id;
			System.out.println("trying to delete object HTTP DELETE at " + url);
			HttpClient client = new HttpClient();
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
			client.getState().setCredentials(AuthScope.ANY, creds);
			DeleteMethod del = new DeleteMethod(url);
			client.executeMethod(del);
			int rCode = del.getStatusCode();
			del.releaseConnection();
			if (rCode == 200) {
				System.err.println("removed successfully: " + osm);
				return true;
			}
			System.err.println("error removing: " + osm);
			return false;
		}
		public void undoModifyData() {
			osm.getMainTable(applet).put(osm.key(), osm);
			osm.register();
		}
		public void postConnectionModifyData() {
			applet.redraw();
		}
	}
}

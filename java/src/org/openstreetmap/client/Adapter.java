package org.openstreetmap.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
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

public class Adapter {

	/**
	 * Base url string to connect to the osm server api.
	 */
	private String apiUrl = "http://www.openstreetmap.org/api/0.2/";

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
		
		GpxParser gpxp = new GpxParser(responseStream);
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
		new Thread(new NodeDeleter(node)).start();
	}

	/**
	 * Queue the deletion of the line segment. Return immediatly.
	 */
	public void deleteLine(Line line) {
		System.out.println("Deleting line " + line.id);
		new Thread(new LineDeleter(line)).start();
	}

	/**
	 * Queue the creation of the node. Return immediatly.
	 */
	public void createNode(Node node, String tempKey) {
		new Thread(new NodeCreator(node, tempKey)).start();
	}

	/**
	 * Queue the movement of the node. Return immediatly.
	 */
	public void moveNode(Node node) {
		new Thread(new NodeMover(node)).start();
	}

	/**
	 * Queue the creation of the line segment. Return immediatly.
	 */
	public void createLine(Line line, String tempKey) {
		new Thread(new LineCreator(line, tempKey)).start();
	}

	/**
	 * Queue the change of the line segments name. Return immediatly.
	 */
	public void updateLineName(Line line) {
		new Thread(new LineUpdater(line)).start();
	}

	/**
	 * Delete a specific node in the intern node list.
	 */
	private class NodeDeleter implements Runnable {
		private Node node;
		public NodeDeleter(Node node) {this.node = node;}
		public void run() {
			System.out.println("tyring to delete node with " + node.lines.size() + " lines");

			try {
				nodes.remove(node.key());
				for (Iterator it = node.lines.iterator(); it.hasNext();) {
					Line line = (Line)it.next();
					lines.remove(line.key());
					// TODO - does the database do this automagically?
					// deleteLine(line);
				}

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
				} else {
					System.err.println("error removing node: " + node);
					System.err.println("HTTP DELETE got response " + rCode + " back from the abyss");

					nodes.put(node.key(), node);
					for (Iterator it = node.lines.iterator(); it.hasNext();) {
						Line line = (Line)it.next();
						lines.put(line.key(), line);
					}
				}
			} catch (Exception e) {
				System.err.println("error removing node: " + node);
				e.printStackTrace();
				nodes.put(node.key(), node);
				for (Iterator it = node.lines.iterator(); it.hasNext();) {
					Line line = (Line)it.next();
					lines.put(line.key(), line);
				}
			}
		}
	} // NodeDeleter

	
	/**
	 * Delete a specific line segment from the intern map.
	 */
	private class LineDeleter implements Runnable {
		private Line line;
		public LineDeleter(Line line) {this.line = line;}
		public void run() {
			System.out.println("Trying to delete line " + line);

			try {
				line.from.lines.remove(line);
				line.to.lines.remove(line);
				lines.remove(line.key());

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
				} else {
					System.err.println("error removing line: " + line);
					lines.put(line.key(), line);
				}
			} catch (Exception e) {
				System.err.println("error removing line: " + line);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Create a node in the intern node list.
	 */
	private class NodeCreator implements Runnable {
		private Node node;
		private String tempKey;
		
		public NodeCreator(Node node, String t) {
			this.node = node;
			this.tempKey = t;
		}

		public void run() {
			try {
				String xml = "<osm><node tags=\"\" lon=\"" + node.lon + "\" lat=\"" + node.lat + "\" /></osm>";
				String url = apiUrl + "newnode";

				System.out.println("Trying to PUT xml \"" + xml + "\" to URL " + url);

				HttpClient client = new HttpClient();

				client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
				client.getState().setCredentials(AuthScope.ANY, creds);

				PutMethod put = new PutMethod(url);
				put.setRequestBody(xml);

				client.executeMethod(put);

				int rCode = put.getStatusCode();
				long id = -1;

				System.out.println("Got response code " + rCode);
				if (rCode == 200) {
					String response = put.getResponseBodyAsString();
					System.out.println("got reponse " + response);
					response = response.trim(); // get rid of leading and trailing whitespace
					id = Long.parseLong(response);
				}

				put.releaseConnection();

				if (id != -1) {
					node.id = id;
					nodes.remove(tempKey);
					nodes.put(node.key(), node);
					System.err.println("node created successfully: " + node);
				} else {
					System.err.println("error creating node: " + node);
					nodes.remove(tempKey);
				}
			} catch (Exception e) {
				System.err.println("error creating node: " + node);
				e.printStackTrace();
				nodes.remove(tempKey);
			}
		}
	} // NodeCreator

	
	/**
	 * Move a node.
	 */
	private class NodeMover implements Runnable {
		private Node node;
		public NodeMover(Node node) {this.node = node;}
		public void run() {
			try {
				String xml = "<osm><node tags=\"" + node.tags + "\" lon=\""
						+ node.lon + "\" lat=\"" + node.lat + "\" uid=\""
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
					// TODO: error handling... (restore the old node position?)
				}
			} catch (Exception e) {
				System.err.println("error moving node: " + node);
				e.printStackTrace();
			}
		}
	} // NodeMover

	/**
	 * Create a line segment in the intern map.
	 */
	private class LineCreator implements Runnable {
		private Line line;
		private String tempKey;
		
		public LineCreator(Line line, String t) {
			this.line = line;
			this.tempKey = t;
		}

		public void run() {
			try {
				String xml = "<osm><segment tags=\"\" from=\"" + line.from.id
						+ "\" to=\"" + line.to.id + "\" /></osm>";
				String url = apiUrl + "newsegment";

				System.out.println("Trying to PUT xml \"" + xml + "\" to URL " + url);

				HttpClient client = new HttpClient();

				client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
				client.getState().setCredentials(AuthScope.ANY, creds);

				PutMethod put = new PutMethod(url);
				put.setRequestBody(xml);

				client.executeMethod(put);

				int rCode = put.getStatusCode();
				long id = -1;

				System.out.println("Got response code " + rCode);
				if (rCode == 200) {
					String response = put.getResponseBodyAsString();
					System.out.println("got reponse " + response);
					id = Long.parseLong(response);
				} else {
					System.err.println("error creating line: " + line);
					lines.remove(line.key());
				}

				put.releaseConnection();

				if (id != -1) {
					line.id = id;
					lines.remove(tempKey);
					lines.put(line.key(), line);
					System.err.println("line created successfully: " + line);
				} else {
					System.err.println("error creating line: " + line);
					lines.remove(tempKey);
					// TODO: error handling...
				}
			} catch (Exception e) {
				System.err.println("error creating line: " + line);
				e.printStackTrace();
				lines.remove(tempKey);
			}
		}
	} // LineCreator

	/**
	 * Update (upload) a line segment to the server.
	 */
	private class LineUpdater implements Runnable {
		private Line line;
		public LineUpdater(Line line) {this.line = line;}

		public void run() {
			try {
				String xml = "<osm><segment uid=\"" + line.id + "\" tags=\""
						+ line.getTags() + "\" from=\"" + line.from.id
						+ "\" to=\"" + line.to.id + "\" /></osm>";

				String url = apiUrl + "segment/" + line.id;

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
					//TODO: Update the intern id?
				} else {
					System.err.println("error updating line: " + line + ", got code " + rCode);
					lines.remove(line.key());
				}
				put.releaseConnection();
			} catch (Exception e) {
				System.err.println("error updating line: " + line);
				e.printStackTrace();
				lines.remove(line.key());
			}
		}
	} // LineUpdater
} // Adapter

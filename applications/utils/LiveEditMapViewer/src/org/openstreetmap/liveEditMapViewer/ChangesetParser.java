package org.openstreetmap.liveEditMapViewer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.osmosis.core.xml.common.DateParser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ChangesetParser extends DefaultHandler {

	int mode;
	int noNodes;
	int noWoodpeck_Fixbot_nodes;
	int noNodes_outside;
	
	String name = null;
	String highway = null;
	boolean restriction = false;
	String restrictionType = null;

	ArrayList<Double> drawLat = new ArrayList<Double>();
	ArrayList<Double> drawLon = new ArrayList<Double>();
	ArrayList<Long> drawTime = new ArrayList<Long>();
	ArrayList<Long> drawID = new ArrayList<Long>();
	ArrayList<Integer> drawMode = new ArrayList<Integer>();
	HashMap<String, Integer> modifiedHighway = new HashMap<String, Integer>();

	DateParser dp = new DateParser();
	
	private Coordinate bboxll, bboxur;

	public ChangesetParser(InputStream i, Coordinate bboxll, Coordinate bboxur) {
		System.out.println("OSM XML parser started...");
		System.out.println("");
		mode = 0;
		noNodes = 0;
		noWoodpeck_Fixbot_nodes = 0;
		noNodes_outside = 0;
		modifiedHighway.clear();
		this.bboxll = bboxll; this.bboxur = bboxur;
		
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			// Parse the input
			factory.setValidating(false);
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(i, this);
		} catch (IOException e) {
			System.out.println("IOException: " + e);
			e.printStackTrace();
			/*
			 * The planet file is presumably corrupt. So there is no point in
			 * continuing, as it will most likely generate incorrect map data.
			 */
			System.exit(10);
		} catch (SAXException e) {
			System.out.println("SAXException: " + e);
			e.printStackTrace();
			/*
			 * The planet file is presumably corrupt. So there is no point in
			 * continuing, as it will most likely generate incorrect map data.
			 */
			System.exit(10);
		} catch (Exception e) {
			System.out.println("Other Exception: " + e);
			e.printStackTrace();
			/*
			 * The planet file is presumably corrupt. So there is no point in
			 * continuing, as it will most likely generate incorrect map data.
			 */
			System.exit(10);
		}
	}

	public void endDocument() {
		System.out.println("Highways created and modified: ");
		
		for (String n : modifiedHighway.keySet()) {
			Integer m = modifiedHighway.get(n);
			if (m == 0) System.out.println("\tCreated:  " + n);
			if (m == 1) System.out.println("\tModified: " + n);
			if (m == 2) System.out.println("\tDeleted:  " + n);
		}
		System.out.println("");
		System.out.println("Changeset contained "
				+ (noNodes + noWoodpeck_Fixbot_nodes) + " nodes in your bbox, of which "
				+ noWoodpeck_Fixbot_nodes
				+ " were from Woodpeck_Fixbot and therefore ignored. "
				+ noNodes_outside + " nodes edited outside your bounding box");
		
	}

	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) {
		// System.out.println("start " + localName + " " + qName);
		if (qName.equals("create")) {
			mode = 0;
		} else if (qName.equals("modify")) {
			mode = 1;
		} else if (qName.equals("delete")) {
			mode = 2;
		} else if (qName.equals("node")) {
			if (Integer.parseInt(atts.getValue("uid")) == 147510) {
				noWoodpeck_Fixbot_nodes++;
			} else {
				
				double lat = Double.parseDouble(atts.getValue("lat"));
				double lon = Double.parseDouble(atts.getValue("lon"));
				if (lat > bboxll.getLat() && lat < bboxur.getLat() && lon > bboxll.getLon() && lon < bboxur.getLon()) {
					long time = dp.parse(atts.getValue("timestamp")).getTime();
					long id = Long.parseLong(atts.getValue("id"));
					drawLat.add(new Double(lat));
					drawLon.add(new Double(lon));
					drawMode.add(new Integer(mode));
					drawTime.add(new Long(time));
					drawID.add(id);
					noNodes++;
				} else {
					noNodes_outside++;
				}
			}
	
		} else if (qName.equals("tag")) {
			if (atts.getValue("k").equalsIgnoreCase("name")) name = atts.getValue("v");
			if (atts.getValue("k").equalsIgnoreCase("highway")) highway = atts.getValue("v");
			if (atts.getValue("k").equalsIgnoreCase("restriction")) restrictionType = atts.getValue("v");
			if (atts.getValue("k").equalsIgnoreCase("type") && atts.getValue("v").equalsIgnoreCase("restriction")) restriction = true;
		} 
		
	}
	
	public void endElement(String namespaceURI, String localName,
			String qName) {
		
		if (qName.equals("relation")) {
			if (restriction) {
				if (mode == 0) System.out.println("A new restriction relation of type " + restrictionType + " was created");
				if (mode == 1) System.out.println("A restriction relation of type " + restrictionType + " was modified");
				if (mode == 2) System.out.println("A restriction relation of type " + restrictionType + " was deleted");
			}
			name = null;
			highway = null;
			restriction = false;
			restrictionType = null;
		} else if (qName.equals("way")) {
			if (highway != null && name != null) {
				if (mode == 0) {
					Integer m = modifiedHighway.get("highway = " + highway + ": " + name);
					if (m == null || m == 0) {
						modifiedHighway.put("highway = " + highway + ": " + name, 0);
					} else if (m == 2) {
						modifiedHighway.put("highway = " + highway + ": " + name, 1);
					}
				}
				if (mode == 1) modifiedHighway.put("highway = " + highway + ": " + name, 1);
				if (mode == 2) {
					Integer m = modifiedHighway.get("highway = " + highway + ": " + name);
					if (m == null || m == 2) {
						modifiedHighway.put("highway = " + highway + ": " + name, 2);
					} else if (m == 0) {
						modifiedHighway.put("highway = " + highway + ": " + name, 1);
					}
				}
			}
			name = null;
			highway = null;
			restriction = false;
			restrictionType = null;
		} else if (qName.equals("node")) {
			name = null;
			highway = null;
			restriction = false;
			restrictionType = null;
		}
		
	}

	public double[] getLats() {
		double[] lats = new double[drawLat.size()];
		for (int i = 0; i < drawLat.size(); i++) {
			lats[i] = ((Double) drawLat.get(i)).doubleValue();
		}
		return lats;
	}

	public double[] getLons() {
		double[] lons = new double[drawLat.size()];
		for (int i = 0; i < drawLat.size(); i++) {
			lons[i] = ((Double) drawLon.get(i)).doubleValue();
		}
		return lons;
	}

	public int[] getModes() {
		int[] modes = new int[drawLat.size()];
		for (int i = 0; i < drawLat.size(); i++) {
			modes[i] = ((Integer) drawMode.get(i)).intValue();
		}
		return modes;
	}

	public long[] getTimes() {
		long[] times = new long[drawLat.size()];
		for (int i = 0; i < drawLat.size(); i++) {
			times[i] = ((Long) drawTime.get(i)).longValue();
		}
		return times;
	}

	public long[] getIDs() {
		long[] ids = new long[drawLat.size()];
		for (int i = 0; i < drawLat.size(); i++) {
			ids[i] = ((Long) drawID.get(i)).longValue();
		}
		return ids;
	}

}

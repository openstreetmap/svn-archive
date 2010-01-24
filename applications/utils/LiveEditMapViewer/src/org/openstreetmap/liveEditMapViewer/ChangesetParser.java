package org.openstreetmap.liveEditMapViewer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.openstreetmap.osmosis.core.xml.common.DateParser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ChangesetParser extends DefaultHandler {

	int mode;
	int noNodes;
	int noWoodpeck_Fixbot_nodes;

	ArrayList<Double> drawLat = new ArrayList<Double>();
	ArrayList<Double> drawLon = new ArrayList<Double>();
	ArrayList<Long> drawTime = new ArrayList<Long>();
	ArrayList<Long> drawID = new ArrayList<Long>();
	ArrayList<Integer> drawMode = new ArrayList<Integer>();

	DateParser dp = new DateParser();

	public ChangesetParser(InputStream i) {
		System.out.println("OSM XML parser started...");
		mode = 0;
		noNodes = 0;
		noWoodpeck_Fixbot_nodes = 0;
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
		System.out.println("Changeset contained "
				+ (noNodes + noWoodpeck_Fixbot_nodes) + " nodes, of which "
				+ noWoodpeck_Fixbot_nodes
				+ " were from Woodpeck_Fixbot and therefore ignored");
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
				long time = dp.parse(atts.getValue("timestamp")).getTime();
				long id = Long.parseLong(atts.getValue("id"));
				drawLat.add(new Double(lat));
				drawLon.add(new Double(lon));
				drawMode.add(new Integer(mode));
				drawTime.add(new Long(time));
				drawID.add(id);
				noNodes++;
			}

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

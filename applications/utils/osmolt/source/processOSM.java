import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class processOSM extends Thread {

	MapFeatures mapFeatures;

	ArrayList<String[]> tags = new ArrayList<String[]>();

	ArrayList<Integer> points = new ArrayList<Integer>();

	double pointLat;

	double PointLon;

	String osmfilename;

	String folder;

	long length;

	int lastprogress = 0;

	boolean workOnElement = false;

	boolean includeways;

	boolean issorted;

	ProcessbarAccess processbarAccess;

	public processOSM(String osmfilename, MapFeatures mapFeatures,
			ProcessbarAccess processbarAccess, boolean includeways,
			boolean issorted) {
		this.processbarAccess = processbarAccess;
		this.osmfilename = osmfilename;
		this.mapFeatures = mapFeatures;
		this.includeways = includeways;
		this.issorted = issorted;

		folder = new File(osmfilename).getParent();
		mapFeatures.setOutputfolder(new File(osmfilename).getParent());
		ArrayList<String> filenames = mapFeatures.getfilenames();
		for (Iterator iter = filenames.iterator(); iter.hasNext();) {
			String filename = (String) iter.next();
			firstLineInfile(folder + "/" + filename);
			// System.out.println(folder + "/" + filename);
		}
		if (osmfilename.indexOf("/") != -1)
			folder = osmfilename.substring(0, osmfilename.lastIndexOf("/") + 1);
		else
			folder = osmfilename
					.substring(0, osmfilename.lastIndexOf("\\") + 1);

		File f = new File(osmfilename);
		length = f.length();
	}

	public void run() {
		processbarAccess.processStart();
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader parser;
			parser = factory.createXMLStreamReader(new FileInputStream(
					osmfilename));
			boolean isend = false;
			while (parser.hasNext() && !isend) {
				// if(parser.getLocalName()!=null)
				// System.out.println(parser.getLocalName());
				double progress = parser.getLocation().getCharacterOffset();
				double progressGlob = progress / length * 100;
				int progressGlobint = (int) Math.floor(progressGlob);

				if (lastprogress < progressGlobint)
					processbarAccess.processAdd();

				lastprogress = progressGlobint;
				switch (parser.getEventType()) {
				case XMLStreamConstants.START_DOCUMENT:
					// System.out.println("START_DOCUMENT: " +
					// parser.getVersion());
					break;

				case XMLStreamConstants.END_DOCUMENT:
					// System.out.println("END_DOCUMENT: ");
					parser.close();
					break;

				case XMLStreamConstants.NAMESPACE:
					// System.out.println("NAMESPACE: " +
					// parser.getNamespaceURI());
					break;

				case XMLStreamConstants.START_ELEMENT:
					String nodeName = parser.getLocalName();

					if (nodeName.equals("node")) {
						/*workOnElement = true;
						tags = new ArrayList<String[]>();
						for (int i = 0; i < parser.getAttributeCount(); i++) {
							String attName = parser.getAttributeLocalName(i);
							String attVal = parser.getAttributeValue(i);
							if (attName.equals("lat"))
								pointLat = new Double(attVal);
							else if (attName.equals("lon"))
								PointLon = new Double(attVal);
						}*/
					} else if (nodeName.equals("tag") && workOnElement) {
						String[] pair = { "", "" };
						for (int i = 0; i < parser.getAttributeCount(); i++) {
							String attName = parser.getAttributeLocalName(i);
							String attVal = parser.getAttributeValue(i);
							if (attName.equals("k"))
								pair[0] = attVal;
							else if (attName.equals("v"))
								pair[1] = attVal;
						}
						tags.add(pair);

					} else if (nodeName.equals("way")) {
						//System.out.println("way");
						// only if sortet
						if (includeways) {
							workOnElement = true;
							tags = new ArrayList<String[]>();
							points = new ArrayList<Integer>();

							for (int i = 0; i < parser.getAttributeCount(); i++) {
								String attName = parser
										.getAttributeLocalName(i);
								String attVal = parser.getAttributeValue(i);
								if (attName.equals("lat"))
									pointLat = new Double(attVal);
								else if (attName.equals("lon"))
									PointLon = new Double(attVal);
							}
						} else if (issorted)
							isend = true;

					} else if (nodeName.equals("nd")) {
						if (includeways) {
							for (int i = 0; i < parser.getAttributeCount(); i++) {
								String attName = parser
										.getAttributeLocalName(i);
								String attVal = parser.getAttributeValue(i);
								if (attName.equals("ref"))
									points.add(new Integer(attVal));
							}
						} else if (issorted)
							isend = true;
					}

					break;
				case XMLStreamConstants.END_ELEMENT:
					if (parser.getLocalName().equals("node")) {
						workOnElement = false;
						ProcessData processData = new ProcessData(folder,
								mapFeatures, PointLon, pointLat, tags);
						processData.start();

					} else if (parser.getLocalName().equals("way")) {
						workOnElement = false;
						ProcessData processData = new ProcessData(folder,
								mapFeatures, tags,points, osmfilename);
						processData.start();
					}
					break;

				default:
					break;
				}
				parser.next();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		processbarAccess.processAdd();
		processbarAccess.processStop();

	}

	private void firstLineInfile(String dateiName) {
		File f = new File(dateiName);
		f.delete();
		if (!f.isAbsolute())
			dateiName = f + dateiName;
		FileWriter file;
		try {
			file = new FileWriter(dateiName, true);
			file.append("point	title	description	icon	iconSize	iconOffset\n");
			file.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

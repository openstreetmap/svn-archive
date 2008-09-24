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

	double pointLat;

	double PointLon;

	String osmfilename;

	String folder;

	long length;

	int lastprogress = 0;

	boolean isWay = false;

	boolean workOnElement = false;

	ProcessbarAccess processbarAccess;

	public processOSM(String osmfilename, MapFeatures mapFeatures,
			ProcessbarAccess processbarAccess) {
		this.processbarAccess = processbarAccess;
		this.osmfilename = osmfilename;
		this.mapFeatures = mapFeatures;
		ArrayList<String> filenames = mapFeatures.getfilenames();
		for (Iterator iter = filenames.iterator(); iter.hasNext();) {
			String filename = (String) iter.next();
			firstLineInfile(filename);
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

			while (parser.hasNext()) {
				// if(parser.getLocalName()!=null)
				// System.out.println(parser.getLocalName());
				double progress = parser.getLocation().getCharacterOffset();
				double progressGlob = progress / length*100;
				int progressGlobint = (int) Math.floor(progressGlob);

				if (lastprogress < progressGlobint)	processbarAccess.processAdd();
				
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
					if (parser.getLocalName().equals("node")) {
						workOnElement = true;
						isWay = false;
						tags = new ArrayList<String[]>();
						for (int i = 0; i < parser.getAttributeCount(); i++) {
							String attName = parser.getAttributeLocalName(i);
							String attVal = parser.getAttributeValue(i);
							if (attName.equals("lat"))
								pointLat = new Double(attVal);
							else if (attName.equals("lon"))
								PointLon = new Double(attVal);
						}
					} else if (parser.getLocalName().equals("tag")
							&& workOnElement) {
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

					} else if (parser.getLocalName().equals("way")) {

					}

					break;
				case XMLStreamConstants.END_ELEMENT:
					if (parser.getLocalName().equals("node")) {
						workOnElement = false;
						ProcessData processData = new ProcessData(folder,
								mapFeatures, PointLon, pointLat, tags);
						processData.start();

					} else if (parser.getLocalName().equals("way")) {

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

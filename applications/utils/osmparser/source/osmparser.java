
/**
 * OSMPArser wandelt eine osm-xmldatei in eine openlayer text datei um
 * 
 * @author Josias Polchau
 * 
 * Copyright (C) 2008 Josias Polchau
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 * 
 * */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class osmparser extends Thread{
	
	boolean differentFiles;
	Document xmlbaum;
	static final Mercator mercator = new Mercator();

	MapFeatures mapFeatures;
	String inputfile;
	String mapFeaturesFile;
	String Outputfile;

	ArrayList<String[]> knowenTyes;
	ProcessbarAccess processbarAccess;
	
	
	public osmparser(String mapFeaturesFile,String inputfile,String Outputfile,boolean differentFiles,ProcessbarAccess processbarAccess) {
		mapFeatures = new MapFeatures(mapFeaturesFile);
		knowenTyes = mapFeatures.getKnowenTyes();
		
		if ((differentFiles)||(Outputfile==null)){
			for (String[] element : knowenTyes) {	
				String filename = mapFeatures.getfilename(element[0], element[1]);
				resetfile(filename);
				firstLineInfile(filename);
			}
		}
		else{

			resetfile(Outputfile);
			firstLineInfile(Outputfile);
		}
		
		this.mapFeaturesFile=mapFeaturesFile;
		this.inputfile=inputfile;
		this.Outputfile=Outputfile;
		this.differentFiles=differentFiles;
		this.processbarAccess = processbarAccess;
	}
	
	 

	public void run() {
		processbarAccess.processStart();
		processbarAccess.processAdd();
		DocumentBuilderFactory fabrik = DocumentBuilderFactory.newInstance();
		DocumentBuilder aufbau;
		try {
			aufbau = fabrik.newDocumentBuilder();
			xmlbaum = aufbau.parse(inputfile);
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		NodeList tags = xmlbaum.getElementsByTagName("tag");
		int anzahl = tags.getLength();
		int oldpercent=0;

		for (int i = 0; i < anzahl; i++) {
			

			if (oldpercent < (int)((double)i/anzahl*100)){
				oldpercent = (int)((double)i/anzahl*100); 
				processbarAccess.processAdd();
				try {
					wait(10);
				} catch (Exception e) {
				}
			}
			Node tag = tags.item(i);
			double[] latlon = {0.,0.};
			boolean found = false;
			String osmTag = "leisure";
			String osmValue = "playground";
			int k = 0;

			while ((k < knowenTyes.size()) && (!found)) {

				String[] strings = knowenTyes.get(k);
				if ((tag.getAttributes().getNamedItem("k").getNodeValue()
						.equals(strings[0]))
						&& (tag.getAttributes().getNamedItem("v")
								.getNodeValue().equals(strings[1]))) {
					found = true;
					osmTag = strings[0];
					osmValue = strings[1];
					//System.out.println(osmTag + "=" + osmValue);
				}
				k++;
			}

			if (found) {
				String name = "";
				Node node = tag.getParentNode();
				//System.out.println(node.getNodeName());
				latlon = getLatLon(node);

				NodeList kinder = node.getChildNodes();

				// namen suchen
				k = 0;
				found = false;
				while ((k < kinder.getLength()) && (!found)) {
					try {
						if ((kinder.item(k).getAttributes() != null)&&
								(kinder.item(k).getAttributes().getNamedItem("k")!=null)&& 
								(kinder.item(k).getAttributes().getNamedItem("k").getNodeValue().equals("name"))) {
							found = true;
							name = kinder.item(k).getAttributes().getNamedItem("v").getNodeValue();
						}
					} catch (Exception e) {
						System.out.println("k="+k);
						e.printStackTrace();
					}
					
					k++;
				}
				writeFile(Outputfile, latlon[0], latlon[1], osmTag, osmValue, name);
			}
		}
		processbarAccess.processStop();
	}

	private double[] getLatLon(Node knoten){
		double[] latlon = {0.,0.};
		int anzahl=0;
	
		if (knoten.getNodeName().equals("node")) {
			latlon[0] = Double.valueOf(knoten.getAttributes().getNamedItem("lat").getNodeValue());
			latlon[1] = Double.valueOf(knoten.getAttributes().getNamedItem("lon").getNodeValue());
			anzahl=1;
		}
		else if (knoten.getNodeName().equals("way")) 
		{
			NodeList kinder = knoten.getChildNodes();
			for (int j = 0; j < kinder.getLength(); j++) {
				if (kinder.item(j).getNodeName().equals("nd"))
				{
					Node point = getNodePerID(new Integer(kinder.item(j).getAttributes().getNamedItem("ref").getNodeValue()));
					latlon[0] += Double.valueOf(point.getAttributes().getNamedItem("lat").getNodeValue());
					latlon[1] += Double.valueOf(point.getAttributes().getNamedItem("lon").getNodeValue());
					anzahl++;
				}
				
			}
		}
		latlon[0]=latlon[0]/anzahl;
		latlon[1]=latlon[1]/anzahl;		
		return latlon;
	}
	
	private Node getNodePerID(Integer id) {
		boolean found =false;
		Node node=null;
		NodeList nodes = xmlbaum.getElementsByTagName("node");
		int anzahl = nodes.getLength();
		int i=0;
		while ((i<anzahl)&&(!found))
		{
			found = new Integer(nodes.item(i).getAttributes().getNamedItem("id").getNodeValue()).equals(id);
			if (!found) i++;			
		}
		if (found)node= nodes.item(i);
		return node;
		
	}
	
	private void resetfile(String dateiName) {
		File fileVar = new File(dateiName);
		fileVar.delete();
	}

	private void firstLineInfile(String dateiName) {

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

	public boolean writeFile(String dateiName, double lat, double lon,
			String osmKey, String osmVal, String name) {

		boolean erfolgreich = false;
		try {FileWriter file;
			if (differentFiles) 
			{
				String filename = mapFeatures.getfilename(osmKey, osmVal);
				file = new FileWriter(filename, true);
			}
			else file = new FileWriter(dateiName, true);
			Double[] merc = mercator.merc(lon,lat);
			file.append(merc[1] + "," + merc[0] + "	"
					+ mapFeatures.getName(osmKey, osmVal) + "	" + name + "	"
					+ mapFeatures.getImage(osmKey, osmVal)+"	" 
					+ mapFeatures.getImagesize(osmKey, osmVal)+"	" 
					+ mapFeatures.getImageoffset(osmKey, osmVal)+"	" 
					+ "\n");

			erfolgreich = true;
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Datei konnte nicht erstellt werden");
			erfolgreich = false;
		}
		return erfolgreich;
	}
	
	

}

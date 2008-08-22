

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * ist teil des OSMPArser-paketes
 * liest eine xmldatei , die Informationen zum verarbeiten der OSM-XML enthält
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

public class MapFeatures {
	String mapFeaturesfile;
	HashMap<String, HashMap<String, String>> mapFeatures = new HashMap<String, HashMap<String, String>>();

	ArrayList<String[]> knowenTyes = new ArrayList<String[]>();

	public MapFeatures(String mapFeaturesfile) {
		this.mapFeaturesfile=mapFeaturesfile;
		try {
			init();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// mapFeatures.put(key, val);
	}

	private void init() throws ParserConfigurationException, SAXException,
			IOException {
		DocumentBuilderFactory fabrik = DocumentBuilderFactory.newInstance();
		DocumentBuilder aufbau = fabrik.newDocumentBuilder();
		Document xmlbaum = aufbau.parse(mapFeaturesfile);

		NodeList tags = xmlbaum.getElementsByTagName("entry");

		for (int i = 0; i < tags.getLength(); i++) {
			String[] key = { "", "" };
			String[] val = { "", "" };
			HashMap<String, String> elemente = new HashMap<String, String>();
			
			Node tag = tags.item(i);
			key[0] = tag.getAttributes().getNamedItem("osmKey").getNodeValue();
			key[1] = tag.getAttributes().getNamedItem("osmValue").getNodeValue();
			try {
				val[0] = tag.getAttributes().getNamedItem("type").getNodeValue();
			} catch (Exception e) {
				val[0] = "";
			}

			if (tag.getAttributes().getNamedItem("name")!=null)
				elemente.put("name", tag.getAttributes().getNamedItem("name").getNodeValue());	
			else elemente.put("name", "");
			
			if (tag.getAttributes().getNamedItem("image")!=null)
				elemente.put("image", tag.getAttributes().getNamedItem("image").getNodeValue());	
			else elemente.put("image", "");	
			
			if (tag.getAttributes().getNamedItem("filename")!=null)
				elemente.put("filename", tag.getAttributes().getNamedItem("filename").getNodeValue());	
			else elemente.put("filename", key[0]+"-"+key[1]+".txt");	

			if (tag.getAttributes().getNamedItem("type")!=null)
				elemente.put("type", tag.getAttributes().getNamedItem("type").getNodeValue());	
			else elemente.put("type", "");	

			if (tag.getAttributes().getNamedItem("imagesize")!=null)
				elemente.put("imagesize", tag.getAttributes().getNamedItem("imagesize").getNodeValue());	
			else elemente.put("imagesize", "");
			
			if (tag.getAttributes().getNamedItem("imageoffset")!=null)
				elemente.put("imageoffset", tag.getAttributes().getNamedItem("imageoffset").getNodeValue());	
			else elemente.put("imageoffset", "");
			
			
			
			mapFeatures.put(key[0]+"="+key[1], elemente);
			knowenTyes.add(key);
		}
	}

	public HashMap<String, String> getInfo(String osmKey, String osmVal) {
		String[] test = { osmKey, osmVal };
		return mapFeatures.get(test);
	}

	public String getType(String osmKey, String osmVal) {
		String[] test = { "leisure", "playground" };
		return mapFeatures.get(test[0]+"="+test[1]).get("type");

	}

	public String getName(String osmKey, String osmVal) {
		String[] test = { osmKey, osmVal };
		return mapFeatures.get(test[0]+"="+test[1]).get("name");
	}

	public String getImage(String osmKey, String osmVal) {
		String[] test = { osmKey, osmVal };
		return mapFeatures.get(test[0]+"="+test[1]).get("image");
	}
	public String getfilename(String osmKey, String osmVal) {
		String[] test = { osmKey, osmVal };
		return mapFeatures.get(test[0]+"="+test[1]).get("filename");
	}
	
	
	public String getImagesize(String osmKey, String osmVal) {
		String[] test = { osmKey, osmVal };
		return mapFeatures.get(test[0]+"="+test[1]).get("imagesize");
	}
	
	public String getImageoffset(String osmKey, String osmVal) {
		String[] test = { osmKey, osmVal };
		return mapFeatures.get(test[0]+"="+test[1]).get("imageoffset");
	}

	public ArrayList<String[]> getKnowenTyes() {
		return knowenTyes;
	}

}

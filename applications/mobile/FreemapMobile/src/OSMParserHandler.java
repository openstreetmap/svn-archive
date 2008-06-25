// OSM Parser
// based on example at http://developers.sun.com/mobility/apis/articles/wsa/
// returns : a Landmark set
// Only parses nodes - we're not doing anything with ways

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import javax.microedition.location.Landmark;
import javax.microedition.location.LandmarkStore;
import javax.microedition.location.QualifiedCoordinates;


public class OSMParserHandler extends LandmarkSourceParserHandler
{


	boolean inNode;
	double curLon, curLat;
	String curName, curType, curDescription;
	String curID;


	public OSMParserHandler()
	{

	}

	public void startDocument()
	{
		System.out.println("startDocument");
	}

	public void startElement(String uri,String localName,String qName,
				Attributes attributes) throws SAXException
	{
		System.out.println("OSMParserHandler: element="+qName);
		if (qName.equals("node"))
		{
			System.out.println("Found a node");
			inNode=true;
			curType=curDescription=curName=null;
			// parse attributes to get lat/lon

			curLat = Double.parseDouble(attributes.getValue("lat"));
			curLon = Double.parseDouble(attributes.getValue("lon"));
			curID = attributes.getValue("id");

		}	
		else if (qName.equals("tag"))
		{
			String curKey = attributes.getValue("k");
			String curValue = attributes.getValue("v");
			System.out.println("tag: curKey="+curKey+" curValue="+curValue);
			
			if(curKey.equals("name"))
			{
				curName=curValue;
			}
			else if (curKey.equals("description"))
			{
				curDescription=curValue;
			}
			// This is a case for the retention of the "class" tag :-)		
			else if (curKey.equals("amenity") || curKey.equals("natural") ||
					curKey.equals("tourism") || curKey.equals("place"))
			{
				curType = curValue;
			}
		}
	}

	public void endElement(String uri,String localName,String qName)
	{
		if(qName.equals("node"))
		{
			inNode=false;
			try
			{
				curName=(curName==null) ? "OSMID-"+curID : curName;
				System.out.println("Creating a landmark: " +
					curName+","+curDescription+","+curType);
				Landmark landmark = new Landmark(curName,curDescription,
						new QualifiedCoordinates(curLat,curLon,Float.NaN,	
									Float.NaN,Float.NaN),null);
				store.addLandmark(landmark,curType);
			}
			catch(Exception e)
			{
				System.out.println("WARNING - could not create landmark : " + e);
			}
		}
	}

	public void characters(char[] ch, int start, int length)
	{

	}
}

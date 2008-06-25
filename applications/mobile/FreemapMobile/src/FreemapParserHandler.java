// Freemap Parser
// based on example at http://developers.sun.com/mobility/apis/articles/wsa/
// returns : a Landmark set
// Only parses Items - we're not doing anything with ways

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.microedition.location.Landmark;
import javax.microedition.location.LandmarkStore;
import javax.microedition.location.QualifiedCoordinates;


public class FreemapParserHandler extends LandmarkSourceParserHandler
{
	boolean inItem, inDescription, inPoint,inType,inName, inID;
	double curLon, curLat;
	String curName, curType, curDescription,curID;


	public FreemapParserHandler()
	{
	
	}

	public void startDocument()
	{
		System.out.println("FreemapParserHandler:startDocument");
	}

	public void startElement(String uri,String localName,String qName,
					Attributes attributes) throws SAXException
	{
	
		System.out.println("FreemapParserHandler: element="+qName);
		if (qName.equals("item"))
		{
			System.out.println("found an item");
			inItem=true;
			curType=curDescription=curName=null;
		}	
		else if (qName.equals("description"))
		{
			inDescription=true;
		}
		else if (qName.equals("title"))
		{
			inName=true;
		}
		else if (qName.equals("georss:point"))
		{
			inPoint=true;
		}
		else if (qName.equals("georss:featuretypetag"))
		{
			inType=true;
		}
		else if (qName.equals("guid"))
		{
			inID = true;
		}
	}

	public void endElement(String uri,String localName,String qName)
	{
		if(qName.equals("item"))
		{
			inItem=false;
			try
			{
				curName = (curName==null) ? "Annotation-"+curID : curName;
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
		else if (qName.equals("description"))
		{
			inDescription=false;
		}
		else if (qName.equals("title"))
		{
			inName=false;
		}
		else if (qName.equals("georss:point"))
		{
			inPoint=false;
		}
		else if (qName.equals("georss:featuretypetag"))
		{	
			inType=false;
		}	
		else if (qName.equals("guid"))
		{
			inID=false;
		}
	}

	public void characters(char[] ch, int start, int length)
	{
		if(ch!=null)
		{
			System.out.println("Characters="+new String(ch,start,length));
			if(inDescription==true)
			{
			curDescription=new String(ch,start,length);
			}
			else if (inName==true)
			{
			curName=new String(ch,start,length);
			}
			else if (inType==true)
			{
			curType=new String(ch,start,length);
			}
			else if (inID==true)
			{
			curID=new String(ch,start,length);
			}
			else if (inPoint==true)	
			{
				String point=new String(ch,start,length);
				int spaceIdx = point.indexOf(' ');
				System.out.println("Lat="+point.substring(0,spaceIdx-1));
				System.out.println("Lon="+point.substring(spaceIdx+1));
				if(spaceIdx>0 && spaceIdx<ch.length-1)
				{
					curLat=Double.parseDouble(point.substring(0,spaceIdx-1));
					curLon=Double.parseDouble(point.substring(spaceIdx+1));
				}
			}
		}
	}
}

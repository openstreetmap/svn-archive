/*
    Copyright (C) 2005 Nick Whitelegg, Hogweed Software, nick@hogweed.org 

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111 USA

 */

import  org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

class GPXHandler extends DefaultHandler
{
	boolean inDoc, inWpt, inTrk, inName, inTrkpt, inType,
					inTrkseg, inTime;

	GPXComponents components;


	String curName, curTimestamp, curType;

	float curLat, curLong;
	int curSeg;

	public GPXHandler(  )
	{
		inDoc = inWpt = inTrk = inName = inTrkpt = inType = 
		inTrkseg =  false;
		curSeg=0;
	}

	public void setComponents(GPXComponents comp)
	{
		components = comp;
	}

	public void startDocument()
	{
		System.out.println("startDocument()");
		inDoc = true;
	}

	public void endDocument()
	{
		System.out.println("endDocument()");
		inDoc = false;
	}

	public void startElement(String uri,String Name,
						String qName, Attributes atts)	
	{
		System.out.println("startElement():"+qName);
		if(inDoc==true)
		{
			if(qName.equals("wpt"))
		{
			inWpt=true;
		}
		else if (qName.equals("trk"))
		{
			inTrk=true;
		}
		else if (qName.equals("trkseg"))
		{
			inTrkseg=true;
			curType = "track";
			components.newSegment();
		}
		else if (qName.equals("name") && (inWpt||inTrkpt||inTrk))
			inName=true;
		else if (qName.equals("type") && (inWpt||inTrk||inTrkseg))
		{
			inType=true;
		}
		else if (qName.equals("time") && (inWpt||inTrkpt))
			inTime=true;
		else if (qName.equals("trkpt") && inTrk)
		{
			inTrkpt = true;
		}

		if(qName.equals("wpt")||qName.equals("trkpt"))
		{
			for(int count=0; count<atts.getLength(); count++)
			{
				if(atts.getQName(count).equals("lat"))
					curLat = Float.parseFloat(atts.getValue(count));		
				else if(atts.getQName(count).equals("lon"))
					curLong = Float.parseFloat(atts.getValue(count));		
			}
		}
		}
	}


	public void endElement(String uri,String name, String qName)
	{
		System.out.println("endElement():"+qName);
		if(inTrkpt && qName.equals("trkpt"))
		{
			components.addTrackpoint (curSeg,curTimestamp,curLat,curLong);
			inTrkpt = false;
		}

		else if(inTrk && qName.equals("trk"))
		{
			System.out.println("setting track ID to " + curName);
			components.setTrackID (curName);
			inTrk = false;
		}

		else if(inName && qName.equals("name"))
			inName=false;

		else if(inType && qName.equals("type"))
			inType=false;

		else if(inTime && qName.equals("time"))
			inTime = false;
	
		else if(inWpt && qName.equals("wpt"))
		{
			components.addWaypoint(curName,curLat,curLong,curType);
			inWpt = false;
		}

		// If the segment had a type, add the segment to the segment table.
		else if (inTrkseg && qName.equals("trkseg"))
		{
			System.out.println("setting segment " + curSeg + " to " + curType);
			components.setSegType(curSeg,curType);
			curSeg++;
			inTrkseg = false;
		}

	}

	public void characters(char ch[], int start, int length)
	{
		if(ch[0]=='\n') return;
		if(inName)
		{
			curName = new String(ch,start,length);
			System.out.println("name=" + curName);
		}
		else if(inType)
		{
			curType = new String(ch,start,length);
			System.out.println("type=" + curType);
		}
		else if(inTime)
		{
			curTimestamp = new String(ch,start,length); 
			System.out.println("timestamp=" + curTimestamp);
		}
	}
}
////////////////////////////////////////////////////////////////////////////////




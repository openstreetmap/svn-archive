package org.openstreetmap.josm.plugins.JunctionChecker.writing;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.Channel;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.ChannelDiGraph;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.OSMEntity;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.OSMNode;

/**
 * @author  joerg
 */
public class OSMXMLWriter {

	String filename;
	ChannelDiGraph digraph;
	XMLStreamWriter writer;

	public OSMXMLWriter(String filename, ChannelDiGraph digraph) {
		this.filename = filename;
		this.digraph = digraph;
	}

	public void writeXML() throws FileNotFoundException, XMLStreamException {
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		writer = factory.createXMLStreamWriter(
		                                   new FileOutputStream(  filename ) );
		// Der XML-Header wird erzeugt
		writer.writeStartDocument("utf-8", "1.0");
		// Zuerst wird das Wurzelelement mit Attribut geschrieben
		  writer.writeStartElement( "osm" );
		    writer.writeAttribute( "version", "0.6" );
		    writer.writeAttribute("generator", "channelGenerator");

		    writer.writeEmptyElement("bounds");
		    writer.writeAttribute("minlat", Double.toString(digraph.getBbbottom()));
		    writer.writeAttribute("minlon", Double.toString(digraph.getBbleft()));
		    writer.writeAttribute("maxlat", Double.toString(digraph.getBbtop()));
		    writer.writeAttribute("maxlon", Double.toString(digraph.getBbright()));


		    OSMNode[] nodes = digraph.getAllOSMNodes();
		    for (int i = 0; i < nodes.length; i++) {
		    	//writer.writeStartElement("node");
		    	writer.writeEmptyElement("node");
		    	writeAttributes(nodes[i]);
			}

		    ArrayList<Channel> ways = digraph.getChannels();
		    for (int i = 0; i < ways.size(); i++) {
				writer.writeStartElement("way");
				writer.writeAttribute("id", Integer.toString(ways.get(i).getNewid()));
				writeAttributes(ways.get(i).getWay());
				  writer.writeEmptyElement("nd");
				  writer.writeAttribute("ref", Long.toString(ways.get(i).getFromNode().getId()));
				  //writer.writeEndElement();
				  writer.writeEmptyElement("nd");
				  writer.writeAttribute("ref", Long.toString(ways.get(i).getToNode().getId()));
				  //writer.writeEndElement();

				  HashMap<String, String> tags = ways.get(i).getWay().getHashmap();
				  Set<String> keys = tags.keySet();
				  String t;
				  Iterator<String> iterator = keys.iterator();
				  while (iterator.hasNext()) {
						t = iterator.next();
						writer.writeEmptyElement("tag");
						writer.writeAttribute("k", t);
						writer.writeAttribute("v", tags.get(t));
					  }
				  writer.writeEmptyElement("tag");
				  writer.writeAttribute("k", "ID");
				  writer.writeAttribute("v", Integer.toString(ways.get(i).getNewid()));
				  writer.writeEmptyElement("tag");
                  writer.writeAttribute("k", "SCC");
                  if (ways.get(i).isStrongConnected()) {
                      writer.writeAttribute("v", "true");
                  }
                  else {
                      writer.writeAttribute("v", "false");
                  }
                  writer.writeEndElement();
			}

		  writer.writeEndElement();
		writer.writeEndDocument();
		writer.close();
	}

	private void writeAttributes(OSMEntity ent) throws FileNotFoundException, XMLStreamException{
		if (ent instanceof OSMNode) {
			writer.writeAttribute("id", Long.toString(ent.getId()) );
			writer.writeAttribute("lat", Double.toString(((OSMNode) ent).getLatitude()));
			writer.writeAttribute("lon", Double.toString(((OSMNode) ent).getLongitude()));
		}

    	if (ent.getTimestamp()!=null) {
    		writer.writeAttribute("timestamp", ent.getTimestamp());
    	}
    	if (ent.isVisible())
    		writer.writeAttribute("visible", "true");
    	else writer.writeAttribute("visible", "false");
		writer.writeAttribute("version", Integer.toString(ent.getVersion()));
	}
}

package de.altenstein.osm;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class OsmXmlWriter {
	
	XMLStreamWriter writer;
	
	/**
	 * Constructs the XMLStreamWriter pointing onto the given file.
	 * Furthermore it adds the xml tag and osm tag.
	 * @param filename
	 */
	public OsmXmlWriter(String filename){
		OutputStream out;
		try {
			out = new FileOutputStream(filename);
			XMLOutputFactory factory = XMLOutputFactory.newInstance();
			writer = factory.createXMLStreamWriter(out, "UTF-8");
			writer.writeStartDocument("UTF-8","1.0");
			writer.writeStartElement("osm");
			writer.writeAttribute("version", "0.6");
			writer.writeAttribute("generator", "altensteinLicenseTool");
		} catch (FileNotFoundException e) {
			System.err.println("Could not write to specified folder/file: " + filename);
			e.printStackTrace();
		} catch (XMLStreamException e){
			System.out.println("Error while writing XML.");
			e.printStackTrace();
		}		
	}
	
	/**
	 * Adds a node tag to osm output file and writes all attributes, tags and licenseStatus.
	 * @param node
	 */
	public void writeNode(OsmNode node){
		try {
			writer.writeStartElement("node");
			
			Iterator<Entry<String, String>> it = node.attMap.entrySet().iterator();
			while (it.hasNext()){
				Entry<String,String> entry = (Entry<String,String>)it.next();
				writer.writeAttribute((String)entry.getKey(), (String)entry.getValue());
			}
						
			Iterator<Entry<String, String>> it2 = node.tagMap.entrySet().iterator();
			while (it2.hasNext()){
				Entry<String,String> entry = (Entry<String,String>)it2.next();
				writer.writeStartElement("tag");
				writer.writeAttribute("k", (String)entry.getKey());
				writer.writeAttribute("v", (String)entry.getValue());
				writer.writeEndElement();
			}
			
			// write license status tag
			writer.writeStartElement("tag");
			writer.writeAttribute("k", "licenseStatus");
			writer.writeAttribute("v", "" + node.licenseStatus);
			writer.writeEndElement();
			
			writer.writeEndElement();
		} catch (XMLStreamException e) {
			System.err.println("Error while writing node.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds a way tag to osm output file and writes all attributes, tags, node references and licenseStatus.
	 * @param way
	 */
	public void writeWay(OsmWay way){
		try {
			writer.writeStartElement("way");
			
			// write attributes
			Iterator<Entry<String, String>> attIt = way.attMap.entrySet().iterator();
			while (attIt.hasNext()){
				Entry<String,String> entry = (Entry<String,String>)attIt.next();
				writer.writeAttribute(entry.getKey(), entry.getValue());
			}
						
			// write node references
			for (int i = 0; i < way.nodeList.size(); i++){
				writer.writeStartElement("nd");
				writer.writeAttribute("ref", "" + way.nodeList.get(i));
				writer.writeEndElement();
			}
			
			// write tags
			Iterator<Entry<String, String>> tagIt = way.tagMap.entrySet().iterator();
			while (tagIt.hasNext()){
				Entry<String,String> entry = (Entry<String,String>)tagIt.next();
				writer.writeStartElement("tag");
				writer.writeAttribute("k", entry.getKey());
				writer.writeAttribute("v", entry.getValue());
				writer.writeEndElement();
			}
			
			// write license status tag
			writer.writeStartElement("tag");
			writer.writeAttribute("k", "licenseStatus");
			writer.writeAttribute("v", "" + way.licenseStatus);
			writer.writeEndElement();
			
			// write referenced nodes license status tag
			if (way.nodeLicenseStatus != 99){
				writer.writeStartElement("tag");
				writer.writeAttribute("k", "nodeLicenseStatus");
				writer.writeAttribute("v", "" + way.nodeLicenseStatus);
				writer.writeEndElement();
			}
			
			writer.writeEndElement();
		} catch (XMLStreamException e) {
			System.err.println("Error while writing way.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Writes close tag for osm document and closes the file stream.
	 */
	public void closeDoc(){
		try {
			writer.writeEndElement();
			writer.writeEndDocument();
			writer.close();
		} catch (XMLStreamException e) {
			System.err.println("Error while finishing and closing output document.");
			e.printStackTrace();
		}
	}
}

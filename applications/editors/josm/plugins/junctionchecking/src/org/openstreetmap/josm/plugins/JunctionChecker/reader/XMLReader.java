package org.openstreetmap.josm.plugins.JunctionChecker.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public  abstract class XMLReader {
	
	protected String filename;
	protected XMLInputFactory factory = XMLInputFactory.newInstance();
	protected XMLStreamReader parser;
	 
	public XMLReader(String filename) {
		try {
			parser = factory
				.createXMLStreamReader(this.getClass().getResourceAsStream(filename));
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}
	
	public XMLReader(File file) {
		try {
			parser = factory
					.createXMLStreamReader(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}
	
	public abstract void parseXML();
}

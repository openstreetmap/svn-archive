/*
 *	  Loader.java
 *	  
 *	  Copyright 2011 Hind <foxhind@gmail.com>
 *	  
 */
 
package CommandLine;

import java.util.ArrayList;
import java.io.File;
import java.util.List;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class Loader extends DefaultHandler {
	private String dirToScan;
	private String currentFile; // For debug XML-files
	private String currentTag;
	private Command currentCommand;
	private Parameter currentParameter;
	private ArrayList<Command> loadingCommands;

	public Loader (String dir) {
		dirToScan = dir;
		currentTag = "";
		loadingCommands = new ArrayList<Command>();
	}

	public ArrayList<Command> load() {
		try {
			// Creating parser
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			
			// Files loading
			File path = new File(dirToScan + "/");
			String[] list;
			list = path.list();
			for(int i = 0; i < list.length; i++)
				if (list[i].endsWith(".xml")) {
					currentFile = dirToScan + "/" + list[i];
					loadFile(sp, currentFile);
				}
		}
		catch (Exception e) {
			System.err.println(e);
		}
		return loadingCommands;
	}

	private void loadFile(SAXParser parser, String fileName) {
		try {
			String a = new File(fileName).toURI().toString().replace("file:/", "file:///");
			System.out.println(a);
			parser.parse(a, this);
		}
		catch (Exception e) {
			System.err.println(e);
		}
		// TODO: Create links for each argument
	}

	public void startElement(String namespaceURI, String localName, String rawName, Attributes attrs) {
		int len = attrs.getLength();
		String Name, Value;
		currentTag = rawName;

		if (rawName.equals("command")) {
			currentCommand = new Command();
			for (int i = 0; i < len; i++) {
				Name = attrs.getQName(i);
				Value = attrs.getValue(i);
				if (Name.equals("name"))
					currentCommand.name = Value;
				else if (Name.equals("run"))
					currentCommand.run = Value;
				else if (Name.equals("tracks")) {
					if (Value.equals("bbox"))
						currentCommand.tracks = true;
				}
				else if (Name.equals("icon")) {
					currentCommand.icon = Value;
				}
			}
		}
		else if (rawName.equals("parameter")) {
			currentParameter = new Parameter();
			for (int i = 0; i < len; i++) {
				Name = attrs.getQName(i);
				Value = attrs.getValue(i);
				if (Name.equals("required")) {
					currentParameter.required = Value.equals("true") ? true : false;
				}
				else if (Name.equals("type")) {
					if (Value.equals("node")) currentParameter.type = Type.NODE;
					else if (Value.equals("way")) currentParameter.type = Type.WAY;
					else if (Value.equals("relation")) currentParameter.type = Type.RELATION;
					else if (Value.equals("point")) currentParameter.type = Type.POINT;
					else if (Value.equals("length")) currentParameter.type = Type.LENGTH;
					else if (Value.equals("natural")) currentParameter.type = Type.NATURAL;
					else if (Value.equals("any")) currentParameter.type = Type.ANY;
					else if (Value.equals("string")) currentParameter.type = Type.STRING;
					else if (Value.equals("relay")) currentParameter.type = Type.RELAY;
					else if (Value.equals("username")) currentParameter.type = Type.USERNAME;
					else if (Value.equals("imageryurl")) currentParameter.type = Type.IMAGERYURL;
					else if (Value.equals("imageryoffset")) currentParameter.type = Type.IMAGERYOFFSET;
				}
				else if (Name.equals("maxinstances")) {
					currentParameter.maxInstances = Integer.parseInt(Value);
				}
				else if (Name.equals("maxvalue")) {
					currentParameter.maxVal = Float.parseFloat(Value);
				}
				else if (Name.equals("minvalue")) {
					currentParameter.minVal = Float.parseFloat(Value);
				}
			}
		}
	}

	public void characters(char ch[], int start, int length) 
	{
		String text = (new String(ch, start, length)).trim();
		if (currentParameter != null) {
			if (currentTag.equals("name")) {
				currentParameter.name = text;
			}
			else if (currentTag.equals("description")) {
				currentParameter.description = text;
			}
			else if (currentTag.equals("value")) {
				if (currentParameter.type == Type.RELAY) {
					if (!(currentParameter.getRawValue() instanceof Relay))
						currentParameter.setValue(new Relay());
					((Relay)(currentParameter.getRawValue())).addValue(text);
				}
				else {
					currentParameter.setValue(text);
				}
			}
		}
	}

	public void endElement(String namespaceURI, String localName, String rawName) {
		if (rawName.equals("command")) {
			loadingCommands.add(currentCommand);
			currentCommand = null;
		}
		else if (rawName.equals("parameter")) {
			if(currentParameter.required)
				currentCommand.parameters.add(currentParameter);
			else
				currentCommand.optParameters.add(currentParameter);
			currentParameter = null;
		}
		else {
			currentTag = "";
		}
	}

	public void warning(SAXParseException ex) {
	  System.err.println("Warning in command xml file " + currentFile + ": " + ex.getMessage());
	}

	public void error(SAXParseException ex) {
	  System.err.println("Error in command xml file " + currentFile + ": " + ex.getMessage());
	}

	public void fatalError(SAXParseException ex) throws SAXException {
	  System.err.println("Error in command xml file " + currentFile + ": " + ex.getMessage());
	  throw ex;
	}
}

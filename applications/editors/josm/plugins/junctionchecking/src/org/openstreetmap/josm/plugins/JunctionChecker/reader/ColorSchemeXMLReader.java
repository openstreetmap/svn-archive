package org.openstreetmap.josm.plugins.JunctionChecker.reader;

import java.awt.Color;
import java.util.HashMap;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

public class ColorSchemeXMLReader extends XMLReader{

	private HashMap<String, Color> colorScheme;

	public ColorSchemeXMLReader(String filename) {
		super(filename);
		parseXML();
	}

	/**
	 * gibt die zu dieser Objektklasse gespeicherte Farbe zurück
	 * @param s Objektklasse
	 * @return die passende Farbe, existiert keine wird grey zurückgegeben
	 */
	public Color getColor(String s) {
		if (colorScheme.containsKey(s)) {
			return colorScheme.get(s);
		}
		else {
			return Color.GRAY;
		}
	}

	@Override
	public void parseXML() {
		colorScheme = new HashMap<String, Color>();
		String tempValue;
		//String tempKeyValue ="";
		try {
			while (parser.hasNext()) {
				switch (parser.getEventType()) {

				case XMLStreamConstants.START_ELEMENT:
					tempValue = parser.getAttributeValue(null, "color");
					if (tempValue != null) {
						String[] erg = tempValue.split(",");
						Color c = new Color(Integer.parseInt(erg[0]), Integer.parseInt(erg[1]), Integer.parseInt(erg[2]));
						colorScheme.put(parser.getLocalName(),c);
					}
					break;
				}
				parser.next();
			}
		} catch (XMLStreamException e) {
			System.out.println(e);
		}
	}
}
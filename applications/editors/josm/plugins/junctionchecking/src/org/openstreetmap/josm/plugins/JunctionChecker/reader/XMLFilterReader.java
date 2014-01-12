package org.openstreetmap.josm.plugins.JunctionChecker.reader;

import java.util.Vector;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import org.openstreetmap.josm.plugins.JunctionChecker.filter.Filter;

/**
 * @author  joerg
 */
public class XMLFilterReader extends XMLReader{

	Vector<Filter> filters;
	Filter filter;

	public XMLFilterReader(String filename) {
		super(filename);
		filters = new Vector<Filter>();
	}

	@Override
	public void parseXML() {
		String tempValue ="";
		String tempKeyValue ="";
		try {
			while (parser.hasNext()) {

				switch (parser.getEventType()) {
				case XMLStreamConstants.START_ELEMENT:
					tempValue = parser.getAttributeValue(null, "entity");
					if (tempValue.equals("k")) {
						filter = new Filter();
						filter.setKeyValue(parser.getLocalName());
						tempKeyValue = parser.getLocalName();
					}
					if (tempValue.equalsIgnoreCase("v")) {
						filter.setTagValue(parser.getLocalName());
					}
					break;

				case XMLStreamConstants.END_ELEMENT:
					if (tempKeyValue.equalsIgnoreCase(parser.getLocalName())) {
						filters.add(filter);
					}
					break;

				}
				parser.next();
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	public Filter[] getFilters() {
		Filter[] filterarray= new Filter[filters.size()];
		return filters.toArray(filterarray);
	}
}

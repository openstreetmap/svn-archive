/**
 * 
 */
package org.openstreetmap.josm.plugins.imageryxmlbounds;

import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.openstreetmap.josm.io.MirroredInputStream;
import org.openstreetmap.josm.io.imagery.ImageryReader;
import org.xml.sax.SAXException;

/**
 * An extended Imagery Reader able to validate input against the JOSM Maps XSD.
 * @author Don-vip
 *
 */
public class ValidatingImageryReader extends ImageryReader implements XmlBoundsConstants {

	public ValidatingImageryReader(String source) throws SAXException, IOException {
		this(source, true);
	}

	public ValidatingImageryReader(String source, boolean validateFirst) throws SAXException, IOException {
		super(source);
		if (validateFirst) {
			validate(source);
		}
	}

	public static void validate(String source) throws SAXException, IOException {
        SchemaFactory factory =  SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new StreamSource(new MirroredInputStream(XML_SCHEMA)));
        schema.newValidator().validate(new StreamSource(source));
	}
}

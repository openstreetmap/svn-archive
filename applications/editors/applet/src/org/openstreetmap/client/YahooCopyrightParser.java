package org.openstreetmap.client;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import uk.co.wilson.xml.MinML2;

public class YahooCopyrightParser extends MinML2 {
  private String text = "";
  public String copyright = "";

	public YahooCopyrightParser(InputStream i) {
		System.out.println("YahooCopyrightParser...");
		try {
			parse(new InputStreamReader(new BufferedInputStream(i, 1024), "ISO-8859-1"));
		} catch (IOException e) {
			System.out.println("IOException: " + e);
			e.printStackTrace();
		} catch (SAXException e) {
			System.out.println("SAXException: " + e);
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Other Exception: " + e);
			e.printStackTrace();
		}
	}

  public void characters (char ch[], int start, int length) {
    text = new String(ch, start, length);
    System.out.print("Characters: " + text);
  }


  public void startDocument() {
    System.out.println("Start of YahooCopyrightParser Document");
  }

  public void endDocument() {
    System.out.println("End of YahooCopyrightParser Document");
  }

  public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
    if (qName.equals("COPY")) {
      text = "";
    }
  } // startElement

  public void endElement(String namespaceURI, String localName, String qName) {
    if (qName.equals("COPY")) {
      System.out.println("got copy: " + text);
      if(copyright.length() != 0)
      {
        copyright += ",";
      }
      copyright += " " + text;

    }
  } // endElement

  public void fatalError(SAXParseException e) throws SAXException {
    System.out.println("Error: " + e);
    throw e;
  }

}

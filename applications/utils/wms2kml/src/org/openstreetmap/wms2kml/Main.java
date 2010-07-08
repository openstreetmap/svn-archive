package org.openstreetmap.wms2kml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;


public class Main {
  
  private List<Layer> layers;
  private boolean allLayers;
  
  
  public Main() {
    layers = new ArrayList<Layer>();
    allLayers=false;
  }
  

  public static void main(String[] args) {
    
    String inFile="", outFile="";

    Main myMain = new Main();
    
	  for (int i=0 ; i<args.length ; i++) {
	    if (args[i].equals("-all")) {
	      myMain.setallLayers(true);
	    }
	    else if (inFile.equals("")) {
	      inFile=args[i];
	    }
	    else if (outFile.equals("")) {
	      outFile=args[i];
	    }
	    else {
	      System.err.println("Usage: wms2kml <file.xml> <file.kml> [-all]");
	      return;
	    }
	  }
	  
	  if (inFile.equals("") || outFile.equals("")) {
      System.err.println("Usage: wms2kml <file.xml> <file.kml> [-all]");
      return;
	  }
	  
	  
    myMain.readFile(inFile);
    
    myMain.writeFile(outFile);
    
  }
  
  private void setallLayers(boolean b) {
    allLayers=b;
  }


  private void readFile(String inFile) {
    Document dom = null;
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    
    try {
      DocumentBuilder db = dbf.newDocumentBuilder();
      dom = db.parse(inFile);
    } catch(ParserConfigurationException pce) {
      pce.printStackTrace();
    } catch(SAXException se) {
      se.printStackTrace();
    } catch(IOException ioe) {
      ioe.printStackTrace();
    }
    
    Element docEle = dom.getDocumentElement();
    
    NodeList nl = docEle.getElementsByTagName("Layer");
    if (nl!=null&& nl.getLength()>0) {
      for (int i=0 ; i<nl.getLength() ; i++) {
        Element el=(Element) nl.item(i);
        parseLayer(el);
      }
    }
    
    
  }

  private  void parseLayer(Element el) {
    String title="", minX="", minY="", maxX="", maxY="";
    
    String queryable = el.getAttribute("queryable");
    
    if (allLayers || queryable.equals("0")) {
      NodeList nl = el.getElementsByTagName("Title");
      if (nl!=null&& nl.getLength()>0) {
        Element el1=(Element) nl.item(0);
        title = el1.getFirstChild().getNodeValue();
      }    
      
      nl = el.getElementsByTagName("LatLonBoundingBox");
      if (nl!=null&& nl.getLength()>0) {
        Element el1=(Element) nl.item(0);
        minX=el1.getAttribute("minx");
        minY=el1.getAttribute("miny");
        maxX=el1.getAttribute("maxx");
        maxY=el1.getAttribute("maxy");
      }
      
      Layer layer = new Layer(title, minX, minY, maxX, maxY);
      layers.add(layer);
    }
    
  
  }
  
  private void writeFile(String outFile) {
    Document dom=null;
    String style="my-style";
    
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder db = dbf.newDocumentBuilder();
      dom = db.newDocument();
    } catch(ParserConfigurationException pce) {
      pce.printStackTrace();
    }
    
    Element rootEle = dom.createElement("kml");
    dom.appendChild(rootEle);
    
    Element docEle = dom.createElement("Document");
    rootEle.appendChild(docEle);
    
    Element styleEle = dom.createElement("Style");
    styleEle.setAttribute("id", style);
    docEle.appendChild(styleEle);
    
    Element polyStyleEle = dom.createElement("PolyStyle");
    styleEle.appendChild(polyStyleEle);
    
    Element fillEle = dom.createElement("fill");
    polyStyleEle.appendChild(fillEle);
    
    Text fillNode = dom.createTextNode("0");
    fillEle.appendChild(fillNode);
    
    Element colorEle = dom.createElement("colorMode");
    polyStyleEle.appendChild(colorEle);
    
    Text colorNode = dom.createTextNode("random");
    colorEle.appendChild(colorNode);
    
    for (int i=0 ; i<layers.size(); i++) {
      Element pmEle = layers.get(i).asElement(dom, style);
      docEle.appendChild(pmEle);
    }
    
    try {
      OutputFormat format = new OutputFormat(dom);
      format.setIndenting(true);
      
      XMLSerializer serializer = new XMLSerializer(new FileOutputStream(new File(outFile)), format);
      
      serializer.serialize(dom);
      
    } catch(IOException ie) {
      ie.printStackTrace();
    }
    
    
  }


}

package org.openstreetmap.wms2kml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class Layer {
  
  private String title="", minX="", minY="", maxX="", maxY="";

  public Layer(String iTitle, String iMinX, String iMinY, String iMaxX, String iMaxY) {
    title=iTitle;
    minX=iMinX;
    minY=iMinY;
    maxX=iMaxX;
    maxY=iMaxY;
  }

  public Element asElement(Document dom, String style) {
    
    Element placemarkEle = dom.createElement("Placemark");
    
    Element nameEle = dom.createElement("name");
    placemarkEle.appendChild(nameEle);
    
    Text nameNode = dom.createTextNode(title);
    nameEle.appendChild(nameNode);

    Element styleEle = dom.createElement("styleUrl");
    placemarkEle.appendChild(styleEle);
    
    Text styleNode = dom.createTextNode("#"+style);
    styleEle.appendChild(styleNode);

    Element polygonEle = dom.createElement("Polygon");
    placemarkEle.appendChild(polygonEle);

    Element obEle = dom.createElement("outerBoundaryIs");
    polygonEle.appendChild(obEle);
    
    Element lrEle = dom.createElement("LinearRing");
    obEle.appendChild(lrEle);
    
    Element coordEle = dom.createElement("coordinates");
    lrEle.appendChild(coordEle);
    
    String text="";
    
    text+=minX+","+minY+" ";
    text+=minX+","+maxY+" ";
    text+=maxX+","+maxY+" ";
    text+=maxX+","+minY+" ";
    text+=minX+","+minY;
    
    Text textNode = dom.createTextNode(text);
    coordEle.appendChild(textNode);
    
    return placemarkEle;
  }

}

package org.openstreetmap.osmolt.slippymap;

import java.text.ParseException;

public class BBox {
  public double minlat;
  
  public double minlon;
  
  public double maxlat;
  
  public double maxlon;
  
  /**
   * Parsing a
   * 
   * @param boundingBox
   * @throws ParseException
   */
  public BBox(String boundingBox) throws ParseException, NumberFormatException {
    setCoordinates(boundingBox);
    
  }
  
  public BBox() {
    // TODO Automatisch erstellter Konstruktoren-Stub
  }
  
  double[] getvalues() {
    double[] result = { minlat, minlon, maxlat, maxlon };
    return result;
  }
  
  @Override
  public String toString() {
    
    return String.format("[bbox=%.5f;%.5f;%.5f;%.5f]", minlon, minlat, maxlon, maxlat).replace(",", ".").replace(";",
        ",");
    
  }
  
  public String toLink() {
    
    return String.format("%.5f;%.5f;%.5f;%.5f", minlon, minlat, maxlon, maxlat).replace(",", ".").replace(";", ",");
  }
  
  public void setCoordinates(String boundingBox) throws ParseException, NumberFormatException {
    
    if ((boundingBox.charAt(0) == '[') && (boundingBox.endsWith("]")))
      boundingBox = boundingBox.substring(1, boundingBox.length() - 2);
    if (boundingBox.indexOf("bbox=") == 0)
      boundingBox = boundingBox.substring(5);
    String[] elements = boundingBox.split(",");
    if (elements.length == 4) {
      minlon = new Double(elements[0]);
      minlat = new Double(elements[1]);
      maxlon = new Double(elements[2]);
      maxlat = new Double(elements[3]);
    } else
      throw new ParseException("too less or too many BBoxElements -> correct number: 4", 0);
    
  }
  
  public double[] getMiddpoint() {
    double[] d = { (minlon + (maxlon - minlon) / 2), (minlat + (maxlat - minlat) / 2) };
    return d;
  }
  
  public String getBounds() {
    return String.format("%.5f;%.5f;%.5f;%.5f", minlon, minlat, maxlon, maxlat).replace(",", ".").replace(";", ",");
  }
  
}

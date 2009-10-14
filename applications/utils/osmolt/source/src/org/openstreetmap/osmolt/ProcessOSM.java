package org.openstreetmap.osmolt;

import org.openstreetmap.osmolt.Filter;
import org.openstreetmap.osmolt.OutputInterface;
import org.openstreetmap.osmolt.MapElement;
import org.openstreetmap.osmolt.gui.MapFeatures;
import org.openstreetmap.osmolt.slippymap.BBox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;

/**
 * handels the layer-making-process
 * 
 * Get the OSM-data from server, process the filter, write the outputfiles
 * 
 * @author Josias Polchau
 * @since 2009
 * 
 * 
 * @license GPL This program is free software; you can redistribute it and/or
 *          modify it under the terms of the GNU General Public License as
 *          published by the Free Software Foundation; either version 3 of the
 *          License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses/>.
 */
public class ProcessOSM extends Thread {
  
  /**
   * the object to raise e.g. error-messages
   */
  OutputInterface outputInterface;
  
  /**
   * the object to translate text
   */
  TranslationAccess translationAccess;
  
  /**
   * DOM root of the OSM Data
   */
  Element osmRoot = null;
  
  /**
   * discribes witch type a OSM-element is
   */
  enum ElementType {
    t_none, t_node, t_way, t_relation
  };
  
  /**
   * the string representation of ElementType
   */
  HashMap<ElementType, String> elementTypeString = new HashMap<ElementType, String>();
  
  /**
   * the folder to write in
   */
  String folder;
  
  /**
   * the BoundingBox of the area to get the data of
   */
  BBox bbox;
  
  /**
   * the filters
   */
  MapFeatures mapFeatures;
  
  /**
   * current filter
   */
  Element currentFilter;
  
  /**
   * initialisize the process
   * 
   * @param bbox
   *          the BoundingBox of the area to get the data of
   * @param mapFeatures
   *          the filters
   * @param outputInterface
   *          the object to raise e.g. error-messages
   */
  
  public ProcessOSM(BBox bbox, MapFeatures mapFeatures, OutputInterface outputInterface,
      TranslationAccess translationAccess) {
    
    this.outputInterface = outputInterface;
    this.translationAccess = translationAccess;
    this.folder = mapFeatures.data.getAttribute("output").getValue();
    this.bbox = bbox;
    this.mapFeatures = mapFeatures;
    
    // fills elementTypeString
    elementTypeString.put(ElementType.t_none, "none");
    elementTypeString.put(ElementType.t_node, "node");
    elementTypeString.put(ElementType.t_way, "way");
    elementTypeString.put(ElementType.t_relation, "relation");
    
    mapFeatures.setOutputfolder(folder);
    
  }
  
  /*
   * http://www.informationfreeway.org/api/0.5/*[leisure=playground][bbox=9.6688,53.5581,9.7624,53.6045]
   * http://api.openstreetmap.org/api/0.5/map?bbox=9.71402,53.59255,9.7192,53.59475
   * http://www.informationfreeway.org/api/0.6/*[leisure=playground][bbox=9.6688,53.5581,9.7624,53.6045]
   */

  /**
   * start the process
   */
  public void run() {
    // the filternames
    ArrayList<String> filterNameList = mapFeatures.getNames();
    outputInterface.osmoltStart();
    for (int i = 0; (i < filterNameList.size()) && (!isInterrupted()); i++) {
      
      // some output
      outputInterface.processStart();
      outputInterface.processSetStatus("preparing");
      
      String filterName = filterNameList.get(i);
      outputInterface.processSetName(filterName);
      currentFilter = mapFeatures.getElementByName(filterName);
      Element mainRestriction = mapFeatures.getMainRestriction(currentFilter);
      String urlString = "http://www.informationfreeway.org/api/0.6/*[" + mainRestriction.getAttributeValue("osmKey")
          + "=" + mainRestriction.getAttributeValue("osmValue") + "][bbox=" + bbox.toLink() + "]";
      
      // urlString = "http://localhost/playgroundwedel.osm";
      
      prepareFile();
      if (!isInterrupted())
        try {
          Document doc = getDocument(urlString);
          if (doc != null) {
            processDoc(doc);
            if (!Osmolt.update) {
              File file = new File(currentFilter.getAttributeValue("image"));
              Copy.file(file, new File(folder + "/" + file.getName()), 128, true);
            }
          }
        } catch (Exception e) {
          outputInterface.printError(e);
        }
      
      outputInterface.processStop();
    }
    mapFeatures.saveToOpenLayersText(translationAccess, folder, "osmolt Ergebnis", bbox);
    
    // zusätzliche dateien
    
    if (!Osmolt.update) {
      try {
        // deaktiviert, weil OpenLayers nicht mehr direkt ins verzeichnis
        // kopiert wird
        // new ZipArchiveExtractor().extractArchive(new File("OpenLayers.zip"),
        // new File(folder));
        
        Copy.fileAsStream(getClass().getResourceAsStream("/mag_map-120x120.png"), new File(folder
            + "/mag_map-120x120.png"), 128, true);
        Copy.fileAsStream(getClass().getResourceAsStream("/somerights20.png"), new File(folder + "/somerights20.png"),
            128, true);
        
      } catch (java.io.FileNotFoundException e) {
        outputInterface.printError("File Not Found: " + e.getMessage());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    outputInterface.osmoltEnd();
  }
  
  /**
   * process the filter
   */
  private void processDoc(Document doc) {
    
    if (!isInterrupted()) {
      outputInterface.processSetStatus("Making the Map");
      osmRoot = doc.getRootElement();
      // System.out.println(MapFeatures.domString(root, 0));
      List children = osmRoot.getChildren();
      int length = children.size();
      int i = 1;
      Iterator iterator = children.iterator();
      while (iterator.hasNext()) {
        if (!isInterrupted()) {
          Element child = (Element) iterator.next();
          processElement(child);
          
          if ((i * 100 / length) > ((i - 1) * 100 / length)) {
            outputInterface.processAdd();
          }
          i++;
        }
      }
      outputInterface.processSetStatus("finished");
    }
  }
  
  private void processElement(Element element) {
    if (Filter.matches(element, currentFilter.getChild("filter").getChild("logical"))) {
      // System.out.println(Osmolt.xmlToString(element));
      writeToFile(element);
    }
  }
  
  /**
   * gets the XML from the API and build a tree
   * 
   * @param urlString
   *          the url to the api with parameters eg. bbox, mainrestriction
   * @return the Document
   */
  public Document getDocument(String urlString) {
    Document doc = null;
    URLConnection conn = null;
    InputStream in = null;
    try {
      SAXBuilder sxbuild = new SAXBuilder();
      if (Osmolt.debug) {
        
        outputInterface.printDebugMessage("ProcessOSM", "url: " + urlString);
        in = new FileInputStream(new File(System.getProperty("user.home") + "/data.osm"));
        outputInterface.processSetStatus("getting data from " + System.getProperty("user.home") + "/data.osm");
      } else {
        // System.out.println(urlString.getBytes());
        URL url = new URL(urlString);
        outputInterface.printDebugMessage("ProcessOSM", "url: " + urlString);
        
        outputInterface.processSetStatus("connecting Server");
        conn = url.openConnection();
        
        outputInterface.processSetStatus("loading Data");
        // conn.setReadTimeout(Osmolt.connectTimeout);
        
        in = conn.getInputStream();
      }
      doc = sxbuild.build(in);
    } catch (java.net.UnknownHostException e) {
      outputInterface.printError("Unknown Host: " + urlString);
    } catch (java.net.SocketTimeoutException e) {
      outputInterface.printError("Timeout: Server does not response");
    } catch (java.net.ConnectException e) {
      outputInterface.printError("Error Server response: " + e.getMessage());
    } catch (java.net.SocketException e) {
      outputInterface.printError("Error Server response: " + e.getMessage());
    } catch (JDOMException e) {
      outputInterface.printError(e.getMessage());
      e.printStackTrace();
    } catch (IOException e) {
      outputInterface.printError(e.getMessage());
      e.printStackTrace();
      
    } catch (Exception e) {
      outputInterface.printError(e.getMessage());
      e.printStackTrace();
      
    } finally {
      try {
        if (in != null) {
          in.close();
        }
      } catch (IOException ioe) {
      }
    }
    
    return doc;
  }
  
  /**
   * writes a overlay-element to file
   * 
   * @param element
   */
  private void writeToFile(Element element) {
    MapElement mapElement = new MapElement();
    mapElement.coordinate = getPosition(element);
    mapElement.title = performTitel(element, currentFilter.getChild("titel"));
    mapElement.description = performTitel(element, currentFilter.getChild("description"));
    mapElement.icon = new File(currentFilter.getAttributeValue("image")).getName();
    mapElement.size = currentFilter.getAttributeValue("imagesize");
    mapElement.offset = currentFilter.getAttributeValue("imageoffset");
    mapElement.writeToFile(folder + "/" + currentFilter.getAttributeValue("filename"));
    
  }
  
  /**
   * get the position from an element
   * 
   * @param element
   * @return the Position
   */
  private Double[] getPosition(Element element) {
    ElementType type = getType(element.getName());
    
    Double[] coordinate = { 0., 0. };
    switch (type) {
      case t_node:
        coordinate[0] = new Double(element.getAttributeValue("lat"));
        coordinate[1] = new Double(element.getAttributeValue("lon"));
        break;
      case t_way: {
        List members = element.getChildren("nd");
        long count = 0;
        for (Iterator iter = members.iterator(); iter.hasNext();) {
          Element member = (Element) iter.next();
          count++;
          Element node = getElementWithId(member.getAttributeValue("ref"), ElementType.t_node);
          Double[] currCoordinates = getPosition(node);
          coordinate[0] = coordinate[0] * ((count - 1.) / count) + currCoordinates[0] / count;
          coordinate[1] = coordinate[1] * ((count - 1.) / count) + currCoordinates[1] / count;
          
        }
      }
        break;
      case t_relation: {
        List members = element.getChildren("nd");
        long count = 0;
        for (Iterator iter = members.iterator(); iter.hasNext();) {
          Element member = (Element) iter.next();
          
          count++;
          Element node = getElementWithId(member.getAttributeValue("ref"), getType(member.getAttributeValue("type")));
          Double[] currCoordinates = getPosition(node);
          
          coordinate[0] = coordinate[0] * (count - 1) / count + currCoordinates[0] / count;
          coordinate[1] = coordinate[1] * (count - 1) / count + currCoordinates[1] / count;
          
        }
      }
        break;
      
      default:
        break;
    }
    
    /*
     * sumalt / count-1 = avgalt sumalt = avgalt * (count-1)
     * 
     * (sumalt + zahl) / count = avg (avgalt * (count-1) + zahl) / count = avg
     * 
     * 
     * (avgalt * (count-1) + zahl) / count =
     * 
     * sumalt =
     * 
     * 
     * 
     * avgalt * (count-1)/count + zahl/count
     * 
     * 5 7
     * 
     * 0 0 1 0 + 5/1 2 5/2 +7/2 2,5 +3,5 = 6
     */

    return coordinate;
  }
  
  /**
   * get an Element from OSM-XML with a specified ID<br>
   * eg. to get the elements of a way or relation
   * 
   * @param id
   *          the specified ID
   * @param type
   *          type of the element
   * @return if found: the Element <br>
   *         else: null
   */
  Element getElementWithId(String id, ElementType type) {
    Element element = null;
    List elements = osmRoot.getChildren(elementTypeString.get(type));
    for (Iterator iter = elements.iterator(); iter.hasNext();) {
      Element elem = (Element) iter.next();
      if (elem.getAttributeValue("id").equals(id)) {
        element = elem;
        break;
      }
    }
    return element;
  }
  
  /**
   * build the titel of the overlay-element
   * 
   * @param element
   *          the sourceelement
   * @param titelElement
   *          the filter element
   * @return the titlestring
   */
  private String performTitel(Element element, Element titelElement) {
    String titel = "";
    List children = titelElement.getContent();
    for (Object child : children) {
      String s = "";
      if (child.getClass().getName().equals("org.jdom.Element")) {
        Element elm = ((Element) child);
        if (elm.getName().equals("valueof")) {
          List tags = element.getChildren("tag");
          for (Iterator iter = tags.iterator(); iter.hasNext();) {
            Element tag = (Element) iter.next();
            
            if (tag.getAttributeValue("k").equals(elm.getAttributeValue("osmKey"))) {
              s = tag.getAttributeValue("v");
            }
          }
        } else if (elm.getName().equals("br")) {
          s = "<br/>";
        }
        
      } else if (child.getClass().getName().equals("org.jdom.Text")) {
        s = ((Text) child).getText().trim().replaceAll("\n", " ");
      }
      if (!s.equals(""))
        titel += s + " ";
    }
    return titel;
  }
  
  /**
   * write the header to the outputfile
   */
  private void prepareFile() {
    try {
      
      FileWriter file;
      file = new FileWriter(folder + "/" + currentFilter.getAttributeValue("filename"), false);
      file.append("point	title	description	icon	iconSize	iconOffset\n");
      
      file.close();
    } catch (IOException e) {
      outputInterface.printError("cant write in file " + folder + "/" + currentFilter.getAttributeValue("filename"));
      interrupt();
    }
  }
  
  /**
   * transform an element-typename to elementtype
   * 
   * @param s
   *          the name
   * @return the type
   */
  private ElementType getType(String s) {
    if (s.equals("node"))
      return ElementType.t_node;
    if (s.equals("way"))
      return ElementType.t_way;
    if (s.equals("relation"))
      return ElementType.t_relation;
    
    this.interrupt();
    return ElementType.t_none;
  }
  
}
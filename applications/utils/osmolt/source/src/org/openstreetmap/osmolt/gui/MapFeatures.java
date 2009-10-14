package org.openstreetmap.osmolt.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.openstreetmap.osmolt.Copy;
import org.openstreetmap.osmolt.Osmolt;
import org.openstreetmap.osmolt.OutputInterface;
import org.openstreetmap.osmolt.OltEntry;
import org.openstreetmap.osmolt.OpenLayersText;
import org.openstreetmap.osmolt.TranslationAccess;
import org.openstreetmap.osmolt.Version;
import org.openstreetmap.osmolt.slippymap.BBox;

public class MapFeatures {
  public static MapFeatures mapFeatures;
  
  OutputInterface outputInterface;
  
  public Element data = new Element("MapFeatures"); // Wurzelelement erzeugen
  
  String mapFeaturesfile;
  
  ArrayList<String[]> knowenTyes = new ArrayList<String[]>();
  
  ArrayList<String> names = new ArrayList<String>();
  
  public MapFeatures(OutputInterface outputInterface) {
    this.outputInterface = outputInterface;
  }
  
  static private String getIndent(int ebene) {
    String s = "";
    for (int i = 0; i < ebene; i++) {
      s += "    ";
    }
    return s;
  }
  
  /**
   * gibt den DOM-baum als string zurück
   * 
   * @param element
   * @param ebene
   *          die einrückung
   * @return
   */
  public static String domString(Element element, int ebene) {
    String s = "";
    s += getIndent(ebene) + "<" + element.getName();
    
    List atributes = element.getAttributes();
    for (Iterator iter = atributes.iterator(); iter.hasNext();) {
      Attribute atribut = (Attribute) iter.next();
      s += " " + atribut.getName() + "=\"" + atribut.getValue() + "\"";
    }
    
    List children = element.getChildren();
    if (!children.isEmpty()) {
      s += ">\n";
      for (Iterator iter = children.iterator(); iter.hasNext();) {
        Element child = (Element) iter.next();
        s += domString(child, ebene + 1);
      }
      s += getIndent(ebene);
      s += "</" + element.getName() + ">\n";
    } else
      s += "/>\n";
    return s;
  }
  
  @SuppressWarnings("unchecked")
  public void openFile(String mapFeaturesfile) {
    
    Document newdoc = null;
    try {
      // validierenden Parser nutzen
      SAXBuilder b = new SAXBuilder(true);
      newdoc = b.build(new File(mapFeaturesfile));
      data = newdoc.getRootElement();
      
      // version check
      String versionStr = data.getAttributeValue("version");
      if ((versionStr == null) || (versionStr == ""))
        versionStr = "2.0";
      Version version = new Version(versionStr);
      
      if (new Version("2.1").compareTo(version) < 0) {
        // kleiner als 2.1
        convertDataToV_2_1();
      }
      // proof consistence
      // proof if mainrestriction exists
      
      List<Element> entries = data.getChildren();
      
      for (Element entry : entries) {
        Element rootLogical = entry.getChild("filter").getChild("logical");
        Element mainRestriction = getMainRestriction(entry);
        if (!mainRestriction.getName().equals("restriction"))
          throw new NullPointerException("restriction");
        if (mainRestriction.getAttributeValue("type") == null)
          throw new NullPointerException("type");        
        if (!((mainRestriction.getAttributeValue("type").equals("equal")) || (mainRestriction.getAttributeValue("type")
            .equals("anything"))))
          throw new NullPointerException("equal");
        if (!rootLogical.getAttributeValue("type").equals("and"))
          throw new NullPointerException("and");
        
      }
      // daten.addContent();
      updatesknowenTyes();
    } catch (IOException e) {
      outputInterface.printError(e);
      
    } catch (NullPointerException e) {
      outputInterface.printTranslatedMessage("err_MF-File_isnt_correct");
      data = new Element("MapFeatures");
    } catch (JDOMException j) {
      // nur eine Ausnahme für alle Situationen
      outputInterface.printError(j.getMessage());
    }
    updatesknowenTyes();
  }
  
  @SuppressWarnings("unchecked")
  private void convertDataToV_2_1() {
    // convert from 2.0
    // System.out.println(Osmolt.xmlToString(data));
    
    data.setAttribute("version", "2.1");
    List<Element> entries = data.getChildren();
    
    for (Element entry : entries) {
      entry.removeAttribute("osmKey");
      entry.removeAttribute("osmValue");
      Element rootLogical = entry.getChild("filter");
      entry.removeChild("filter");
      
      Element filter = new Element("filter");
      entry.addContent(0, filter);
      filter.addContent(convertFilterToV_2_1(rootLogical));
    }
    // System.out.println(Osmolt.xmlToString(data));
  }
  
  @SuppressWarnings("unchecked")
  private Element convertFilterToV_2_1(Element filter) {
    filter.setName("logical");
    filter.removeAttribute("name");
    filter.setAttribute("type", filter.getAttributeValue("logical"));
    filter.removeAttribute("logical");
    
    List<Element> subRestrictions = filter.getChildren("restriction");
    for (Element element : subRestrictions) {
      element.setAttribute("type", "equal");
    }
    
    List<Element> subfilter = filter.getChildren("filter");
    for (Element element : subfilter) {
      convertFilterToV_2_1(element);
    }
    return filter;
    
  }
  
  public void saveFile(String mapFeaturesfile) {
    FileOutputStream out;
    try {
      
      // System.out.println(daten.getNamespace());
      DocType doct = new DocType("MapFeatures");
      // System.out.println(daten.getParentElement());
      
      doct.setSystemID("grammar.dtd");
      Document doc = new Document((Element) data.detach());
      doc.setDocType(doct);
      out = new FileOutputStream(mapFeaturesfile);
      XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
      serializer.output(doc, out);
      out.flush();
      out.close();
      
      Copy.fileAsStream(getClass().getResourceAsStream("/grammar.dtd"), new File(new File(mapFeaturesfile)
          .getParentFile()
          + "/grammar.dtd"), 128, true);
      
      // Copy.file(getClass().getResource("/grammar.dtd").getFile(), new
      // File(new
      // File(
      // mapFeaturesfile).getParentFile()
      // + "/grammar.dtd"), 128, true);
      // BufferedWriter grammarout = new BufferedWriter(new FileWriter(new
      // File(mapFeaturesfile).getParentFile() + "/grammar.dtd"));
      // grammarout.write(Grammar.getGrammar());
      // grammarout.close();
      
      // getGrammar
    } catch (FileNotFoundException e) {
      outputInterface.printTranslatedError("file not found");
    } catch (IOException e) {
      outputInterface.printError(e);
    }
    
  }
  
  public void updatesknowenTyes() {
    names.clear();
    knowenTyes.clear();
    List children = data.getChildren();
    for (int i = 0; i < children.size(); i++) {
      Element entry = (Element) children.get(i);
      names.add(entry.getAttribute("name").getValue());
      String[] keyPair = { "", "" };
      Element mainRestriction = getMainRestriction(entry);
      keyPair[0] = mainRestriction.getAttributeValue("osmKey");
      keyPair[1] = mainRestriction.getAttributeValue("osmValue");
      knowenTyes.add(keyPair);
    }
    
  }
  
  /**
   * returns the mainrestriction the restriction, wich ar searched on the server
   * 
   * @param entry
   *          an mapentry
   * @return mainrestriction
   */
  public Element getMainRestriction(Element entry) {
    return (Element) entry.getChild("filter").getChild("logical").getChildren().get(0);
  }
  
  @SuppressWarnings("unchecked")
  public Element addEntry(String name) {
    
    Element element = new Element("entry");
    data.getChildren().add(element);
    element.setAttribute("name", name);
    element.setAttribute("filename", name + ".txt");
    element.setAttribute("image", name + ".png");
    element.setAttribute("imagesize", "20,20");
    element.setAttribute("imageoffset", "-10,-10");
    
    Element filter = new Element("filter");
    element.addContent(filter);
    
    Element logical = new Element("logical");
    logical.setAttribute("type", "and");
    filter.addContent(logical);
    
    Element restriction = new Element("restriction");
    restriction.setAttribute("osmKey", "");
    restriction.setAttribute("osmValue", "");
    restriction.setAttribute("type", "");
    logical.addContent(restriction);
    
    Element titel = new Element("titel");
    titel.addContent(name);
    element.addContent(titel);
    
    Element description = new Element("description");
    description.addContent("");
    element.addContent(description);
    updatesknowenTyes();
    return element;
  }
  
  public Element getElementByName(String name) {
    List children = data.getChildren();
    Element result = null;
    for (int i = 0; i < children.size(); i++) {
      Element element = (Element) children.get(i);
      if (element.getAttribute("name").getValue().equals(name))
        result = element;
    }
    return result;
  }
  
  public ArrayList<String> getNames() {
    return names;
  }
  
  public void name(String name) {
    mapFeaturesfile = name;
  }
  
  String[] getTopFilter(String filtername) {
    String[] result = { "", "" };
    List children = data.getChildren();
    for (int i = 0; i < children.size(); i++) {
      Element element = (Element) children.get(i);
      if (element.getAttribute("name").equals(filtername)) {
        result[0] = element.getAttribute("osmKey").getValue();
        result[1] = element.getAttribute("osmValue").getValue();
      }
    }
    return result;
  }
  
  // public HashMap<String, String> getInfo(String osmKey, String osmVal) {
  // String[] test = { osmKey, osmVal };
  // return mapFeatures.get(test);
  // }
  //
  // public String getType(String osmKey, String osmVal) {
  // String[] test = { osmKey, osmVal };
  // return mapFeatures.get(test[0] + "=" + test[1]).get("type");
  // }
  //
  // public String getName(String osmKey, String osmVal) {
  // String[] test = { osmKey, osmVal };
  // return mapFeatures.get(test[0] + "=" + test[1]).get("name");
  // }
  //
  // public String getImage(String osmKey, String osmVal) {
  // String[] test = { osmKey, osmVal };
  // return mapFeatures.get(test[0] + "=" + test[1]).get("image");
  // }
  //
  // public String getfilename(String osmKey, String osmVal) {
  // String[] test = { osmKey, osmVal };
  // return mapFeatures.get(test[0] + "=" + test[1]).get("filename");
  // }
  //
  // public String getImagesize(String osmKey, String osmVal) {
  // String[] test = { osmKey, osmVal };
  // return mapFeatures.get(test[0] + "=" + test[1]).get("imagesize");
  // }
  //
  // public String getImageoffset(String osmKey, String osmVal) {
  // String[] test = { osmKey, osmVal };
  // return mapFeatures.get(test[0] + "=" + test[1]).get("imageoffset");
  // }
  //
  // public ArrayList<String[]> getKnowenTyes() {
  // return knowenTyes;
  // }
  //	
  
  ArrayList<String> getfilenames() {
    ArrayList<String> result = new ArrayList<String>();
    
    List children = data.getChildren();
    for (int i = 0; i < children.size(); i++) {
      Element element = (Element) children.get(i);
      result.add(element.getAttribute("filename").getValue());
    }
    return result;
  }
  
  Element getFilterByName(String Name) {
    List children = data.getChildren();
    for (int i = 0; i < children.size(); i++) {
      Element element = (Element) children.get(i);
      if (element.getAttribute("name").getValue().equals(Name))
        return element.getChild("filter");
      
    }
    System.out.println("fehler");
    return null;
  }
  
  public String getOutputfolder() {
    return data.getAttributeValue("bbox");
  }
  
  public void setOutputfolder(String outputfolder) {
    data.setAttribute("bbox", outputfolder);
  }
  
  public void saveToOpenLayersText(TranslationAccess translationAccess, String outputFolder, String title, BBox bbox) {
    ArrayList<OltEntry> entrys = new ArrayList<OltEntry>();
    List children = data.getChildren();
    for (int i = 0; i < children.size(); i++) {
      Element element = (Element) children.get(i);
      File image;
      image = new File(element.getAttribute("image").getValue());
      entrys.add(new OltEntry(element.getAttribute("name").getValue(), element.getAttribute("filename").getValue(),
          image.getName()));
    }
    OpenLayersText.save(translationAccess, entrys, outputFolder, title, bbox);
  }
  
}
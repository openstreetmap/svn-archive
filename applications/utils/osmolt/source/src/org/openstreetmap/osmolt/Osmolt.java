package org.openstreetmap.osmolt;

import java.io.File;
import java.text.ParseException;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.jdom.Attribute;
import org.jdom.Element;
import org.openstreetmap.osmolt.gui.MapFeatures;
import org.openstreetmap.osmolt.gui.OsmoltGui;
import org.openstreetmap.osmolt.slippymap.BBox;

/**
 * a Programm to make a OpenLayers map-overlay from the OpenStreetMap-Server<br>
 * OSMOLT = OSM to OpenLayers.Text
 * 
 * 
 * @license GPL. Copyright 2009
 * @author Josias Polchau
 */
public class Osmolt implements OutputInterface, TranslationAccess {
  /**
   * Version number
   */
  public static Version version = new Version("2.1.-1");
  
  /**
   * if true: print debugmessages<br>
   * parameter: -d
   */
  static boolean debug = false;
  
  /**
   * if true: print debugmessages<br>
   * parameter: -t
   */
  static int connectTimeout = 180000;

  /**
   * if true: update only the OLT-File<br>
   * parameter: -u
   */
  static boolean update = false;
  /**
   * if true: convert the xml file to the current version<br>
   * parameter: -u
   */
  static boolean convert = false;
  
  /**
   * false if strats without parameters <br>
   * else: true <br>
   * only -d = debug : false
   */
  static boolean runOnComandline = true;
  
  public static final ResourceBundle bundle = ResourceBundle.getBundle("osmolt");
  
  /**
   * the main-methode <br>
   * run this to start osmolt
   * 
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    debug = ((args.length == 1) && (args[0].equals("-d")));
    /* if no arg or only -d */
    if ((args.length == 0) || debug)
      OsmoltGui.startGui();
    else {
      runOnComandline = true;
      comandline(args);
    }
  }
  
  /**
   * processing on comandline
   * 
   * @param args
   * @throws Exception
   */
  private static void comandline(String[] args) throws Exception {
    String[] oldargs = args.clone();
    int i = 0;
    String folder = "";
    String boundingBox = "";
    String mapFeaturesFileName = null;
    
    /* Arguments */
    while (i < args.length) {
      if (args[i].equals("--help") || args[i].equals("-h") || args[i].equals("-?")) {
        printHelp(0);
      } else {
        
        if (args[i].charAt(0) != '-') {
          System.out.println("no '-' at arg" + (i + 1));
          showWrongArgument(oldargs, i);
        }
        
        args[i] = args[i].substring(1);
        
        if (args[i].length() != 1) {
          System.out.println("only one letter needet at arg" + (i + 1));
          showWrongArgument(oldargs, i);
        }
        
        switch (args[i].charAt(0)) {
          
          /* BBox */
          case 'b':
            boundingBox = args[i + 1];
            i++;
            
            break;
          /* debug */
          case 'd':
            debug = true;
            break;
          /* debug */
          case 'c':
            convert = true;
            break;
          
          /* Filterfile */
          case 'f':
            mapFeaturesFileName = args[i + 1];
            i++;
            break;
          
          /* help */
          case 'h':
          case '?':
            printHelp(0);
            break;
          
          /* Folder */
          case 'p':
            folder = args[i + 1];
            i++;
            
            break;
          /* Folder */
          case 't':
            connectTimeout = new Integer(args[i + 1]);
            i++;
            
            break;
          /* update */
          case 'u':
            update = true;
            break;
          default:
            System.out.println("wrong argument at arg" + (i + 1));
            showWrongArgument(oldargs, i);
            break;
        }
      }
      i++;
    }
    
    if ((mapFeaturesFileName == null) || (!(new File(mapFeaturesFileName).exists()))) {
      System.out.println("please specify a filterfile");
      printHelp(1);
    } else {
      Osmolt osmolt = new Osmolt();
      MapFeatures mapFeatures = new MapFeatures(osmolt);
      mapFeatures.openFile(mapFeaturesFileName);
      if (folder != "")
        mapFeatures.data.setAttribute("output", folder);
      
      try {
        
        BBox bbox = null;
        if (boundingBox != "") {
          bbox = new BBox(boundingBox);
          mapFeatures.data.setAttribute("bbox", bbox.toLink());
        } else {
          String bboxStr = mapFeatures.data.getAttributeValue("bbox");
          if (bboxStr != null)
            bbox = new BBox(bboxStr);
        }
        if (bbox != null) {
          System.out.println(bbox + " " + mapFeatures.data.getAttributeValue("output") + " " + mapFeaturesFileName);
          ProcessOSM processOSM = new ProcessOSM(bbox, mapFeatures, osmolt, osmolt);
          processOSM.run();
        }
      } catch (ParseException pe) {
        System.out.println("Error: " + pe.getMessage());
      } catch (NumberFormatException nfe) {
        System.out.println("Error: " + nfe.getMessage());
      }
    }
  }
  
  private static void showWrongArgument(String[] oldargs, int worngArg) {
    
    String output = "\nWrong Call:\n";
    int charPosOfError = 0;
    for (int i = 0; i < oldargs.length; i++) {
      output += " " + oldargs[i] + "";
      if (i < worngArg)
        charPosOfError += oldargs[i].length() + 1;
    }
    output += "\n";
    for (int j = 0; j < charPosOfError + 1; j++) {
      output += " ";
    }
    output += "^";
    System.out.println(output);
    printHelp(1);
  }
  
  /**
   * 
   * @param returncode
   *          0 = no error
   */
  private static void printHelp(int returncode) {
    if (returncode != 0)
      System.out.println("");// empty line
    System.out
        .println("Osmolt Help\n"
            + "systax: osmolt [options][ARGs]\n" //
            + "\n" //
            + "Options\n"
            + "  -d  : print debug-messages\n"//
            + "  -u  : Update: change only the OLT-File\n"
            + "  -?\n" //
            + "  -h  : print debug-messages\n"
            + "\n" //
            + "required ARG:\n"
            + "  -f [mapFeatures]    XML file with requested Tags (see readme)\n" //
            + "optional ARGs:\n"
            + "  -p [Path]           Folder to save the files in\n" //
            + "  -b [boundingBox]    boundingBox thats differs from the mapFeature-File\n"
            + "  -t [int]            Timeout\n"
            + "\n" //
            + "examples:\n"
            + "  osmolt f=bicycle_rental.xml\n" //
            + "  osmolt f=bicycle_rental.xml b=[2.20825,49.40382,7.33887,53.80065]\n" //
            + "  osmolt f=bicycle_rental.xml b=[2.20825,49.40382,7.33887,53.80065] p=outputfolder/\n" //
            + "\n"
            + "\n" //
            + "\n"
            + "Osmolt  Copyright (C) 2008  Josias Polchau\n"//
            + "\n" //
            + "This program comes with ABSOLUTELY NO WARRANTY.\n"
            + "This is free software, and you are welcome to redistribute it under certain conditions; look at the readme-file for details.\n");
    System.exit(returncode);
  }
  
  /**
   * @see org.openstreetmap.osmolt.InputOutputAccess
   */
  public void addvalue() {
    System.out.print("#");
  }
  
  /**
   * @see org.openstreetmap.osmolt.InputOutputAccess
   */
  public void processAdd() {
    System.out.print("#");
  }
  
  /**
   * @see org.openstreetmap.osmolt.InputOutputAccess
   */
  public void processStart() {
    System.out.print("|");
  }
  
  /**
   * @see org.openstreetmap.osmolt.InputOutputAccess
   */
  public void processStop() {
    System.out.println("|");
  }
  
  /**
   * @see org.openstreetmap.osmolt.InputOutputAccess
   */
  public void printError(String error) {
    System.out.println(error);
  }
  
  /**
   * @see org.openstreetmap.osmolt.InputOutputAccess
   */
  public void printMessage(String message) {
    System.out.println(message);
  }
  
  /**
   * @see org.openstreetmap.osmolt.InputOutputAccess
   */
  public void printWarning(String warning) {
    System.out.println(warning);
  }
  
  /**
   * @see org.openstreetmap.osmolt.InputOutputAccess
   */
  public void printTranslatedError(String error) {
    System.out.println(error);
  }
  
  /**
   * @see org.openstreetmap.osmolt.InputOutputAccess
   */
  public void printTranslatedMessage(String message) {
    System.out.println(message);
  }
  
  /**
   * @see org.openstreetmap.osmolt.InputOutputAccess
   */
  public void printTranslatedWarning(String warning) {
    System.out.println(warning);
  }
  
  /**
   * @see org.openstreetmap.osmolt.InputOutputAccess
   */
  public void processSetName(String s) {
    System.out.println(s);
  }
  
  /**
   * @see org.openstreetmap.osmolt.InputOutputAccess
   */
  public void osmoltStart() {
    System.out.println("begin process");
  }
  
  /**
   * @see org.openstreetmap.osmolt.InputOutputAccess
   */
  public void osmoltEnd() {
    System.out.println("end of process");
  }
  
  /**
   * not implemented if run on comandline
   * 
   * @see org.openstreetmap.osmolt.InputOutputAccess
   */
  public void processSetPercent(int percent) {
    // not implemented if run on comandline
  }
  
  /**
   * @see org.openstreetmap.osmolt.InputOutputAccess
   */
  public void processSetStatus(String s) {
    System.out.println(s);
  }
  
  /**
   * @see org.openstreetmap.osmolt.InputOutputAccess
   */
  public void printError(Exception error) {
    error.printStackTrace();
  }
  
  /**
   * @see org.openstreetmap.osmolt.InputOutputAccess
   */
  public void printError(Throwable error) {
    error.printStackTrace();
    
    
    
  }
  
  /**
   * @see org.openstreetmap.osmolt.InputOutputAccess
   */
  public void printDebugMessage(String classname, String message) {
    System.out.println(classname + "\n" + message.replaceAll("\n", "\n\t"));
  }
  
  public String translate(String s) {
    try {
      return OsmoltGui.bundle.getString(s);
    } catch (MissingResourceException e) {
      System.out.println("not translated: " + s);
      return s;
    }
  }
  
  public static String xmlToString(Element e) {
    return xmlToStringHelpfunction(e, 0);
  }
  
  private static String xmlToStringHelpfunction(Element e, int depth) {
    String s = "";
    String indent = getIndent(depth);
    s += indent + "<" + e.getName();
    List attributes = e.getAttributes();
    for (Object object : attributes) {
      Attribute attribute = (Attribute) object;
      s += " " + attribute.getName() + "=\"" + attribute.getValue() + "\"";
    }
    
    List children = e.getChildren();
    if (children.size() == 0) {
      s += "/>\n";
    } else {
      s += ">\n";
      for (Object object : children) {
        Element child = (Element) object;
        s += xmlToStringHelpfunction(child, depth + 1);
      }
      s += indent + "</" + e.getName() + ">\n";
    }
    return s;
  }
  
  private static String getIndent(int indent) {
    String s = "";
    for (int i = 0; i < indent; i++)
      s += "  ";
    
    return s;
  }
  
}

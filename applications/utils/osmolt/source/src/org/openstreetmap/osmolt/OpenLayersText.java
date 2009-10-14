package org.openstreetmap.osmolt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.openstreetmap.osmolt.slippymap.BBox;

/**
 * Making a HTML File to display the OpenLayers.Text Files License: GPL.
 * Copyright 2009
 * 
 * @author Josias Polchau
 * 
 */
public class OpenLayersText {
  
  public static void save(TranslationAccess translation, ArrayList<OltEntry> entrys, String outputFolder, String title,
      BBox bbox) {
    BufferedWriter out;
    InputStream is;
    
    try {
      is = Osmolt.class.getResourceAsStream("/OpenLayersTextTemplate.html");
      InputStreamReader isr = new InputStreamReader(is);
      BufferedReader in = new BufferedReader(isr);
      out = new BufferedWriter(new FileWriter(outputFolder + "/" + "index.html"));
      
      String zeile;
      int replaceStart;
      try {
        while ((zeile = in.readLine()) != null) {
          while ((replaceStart = zeile.indexOf("<osmolt")) >= 0) {
            int replaceEnd;
            String replacement = "";
            String type = zeile.substring(replaceStart);
            replaceEnd = type.indexOf(">") + 1;
            type = type.substring(0, replaceEnd);
            
            type = type.substring(type.indexOf('"') + 1, type.lastIndexOf('"'));
            
            double[] middpoint = bbox.getMiddpoint();
            
            if (type.equals("title"))
              replacement = title;
            else if (type.equals("lon"))
              replacement = middpoint[0] + "";
            else if (type.equals("lat"))
              replacement = middpoint[1] + "";
            else if (type.equals("zoom"))
              if ((middpoint[0] == 0.0) || (middpoint[1] == 0.0))
                replacement = "3";
              else
                replacement = "12";
            else if (type.equals("bbox"))
              replacement = bbox.getBounds();
            else if (type.equals("layers")) {
              replacement = replacement + "\n";
              for (OltEntry entry : entrys) {
                
                replacement = replacement + "         var " + entry.name + " = new OpenLayers.Layer.GML(\""
                    + entry.name + "\",\"" + entry.url
                    + "\",{format: OpenLayers.Format.Text, projection: map.displayProjection });\n"
                    + "         map.addLayer(" + entry.name + ");\n\n";
                replacement = replacement + "         " + "" + "var " + entry.name
                    + "sfc = new OpenLayers.Control.SelectFeature(" + entry.name + ");\n" + "         map.addControl("
                    + entry.name + "sfc);\n" + "         " + entry.name + "sfc.activate();\n" + "         "
                    + entry.name + ".events.on({\n" + "            'featureselected': onFeatureSelect,\n"
                    + "            'featureunselected': onFeatureUnselect\n" + "          });\n\n";
              }
              replacement = replacement + "\n";
            } else if (type.equals("legendtitle")) {
              replacement = replacement + translation.translate("MapKey");
            } else if (type.equals("legendtext")) {
              for (OltEntry entry : entrys) {
                
                replacement = replacement + "<img src=\"" + entry.imgUrl + "\"/> " + entry.name + " <br/>";
                
              }
            }
            zeile = zeile.substring(0, replaceStart) + replacement
                + zeile.substring(replaceStart + replaceEnd, zeile.length());
            // System.out.println("'" + zeile + "' " + replaceStart
            // + " " + replacement);
            
          }
          
          out.write(zeile);
          out.newLine();
          
        }
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        out.close();
        in.close();
      }
      
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}

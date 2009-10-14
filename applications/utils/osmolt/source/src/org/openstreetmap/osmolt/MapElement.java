package org.openstreetmap.osmolt;

import java.io.FileWriter;
import java.io.IOException;

public class MapElement {
  Double[] coordinate = { 0., 0. };
  
  String title = "";
  
  String description = "";
  
  String icon = "";
  
  String size = "";
  
  String offset = "";
    
  Boolean writeToFile(String filepath) {
    try {
      FileWriter file;
      file = new FileWriter(filepath, true);
      file.append(coordinate[0] + "," + coordinate[1] + "	" + title + "	" + description + "	" + icon + "	" + size + "	"
          + offset + "	" + "\n");
      
      file.close();
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }
}

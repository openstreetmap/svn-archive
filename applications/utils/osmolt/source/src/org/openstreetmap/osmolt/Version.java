package org.openstreetmap.osmolt;

import java.util.ArrayList;

public class Version {
  ArrayList<Integer> vers = new ArrayList<Integer>();
  /**
   * example: 2.0.1
   * @param s
   */
  public Version(String s) {
    String[] splitted = s.split("\\.");
    for (int i = 0; i < splitted.length; i++) {
      vers.add(new Integer(splitted[i]));
    }
    
  }
  
  /**
   * 
   * @param v Version
   * @return positiv if v greater than this <br>
   *         negativ if v smaller than this <br>
   *         0 if equal
   */
  public int compareTo(Version v) {
    int num = 0;
    int vSize = v.vers.size();
    int i;
    
    for (i = 0; i < vers.size(); i++) {
      if (vSize == i)
        num = 0 - vers.get(i);
      else
        num = v.vers.get(i) - vers.get(i);
      if (num != 0)
        return num;
      
    }
    return 0;
  }
  
  @Override
  public String toString() {
    String s = "";
    boolean first = true;
    for (Integer el : vers) {
      if (!first)
        s += ".";
      else
        first = false;
      s += el.toString();
    }
    return s;
  }
}

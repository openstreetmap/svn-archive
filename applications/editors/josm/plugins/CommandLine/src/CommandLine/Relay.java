/*
 *      Relay.java
 *      
 *      Copyright 2010 Hind <foxhind@gmail.com>
 *      
 */

package CommandLine;

import java.util.HashMap;

public class Relay {
  static String marker = "\u0332";
  private String optionsString;
  private HashMap<String, String> options;
  private String value;

  public Relay() {
    optionsString = "";
    value = "";
    options = new HashMap<String, String>();
  }

  public void setValue(String value) {
    if (options.containsKey(value))
      this.value = options.get(value);
    else if (options.containsValue(value))
      this.value = value;
  }

  public void addValue(String value) {
    String letter = null;
    if (!(options.containsValue(value))) {
      int i = 0;
      for (; i < value.length() ; i++) {
        letter = value.substring(i, i + 1).toLowerCase();
        if (!options.containsKey(letter))
          break;
      }
      if (i == value.length()) {
        letter = String.valueOf(System.currentTimeMillis());
        optionsString = optionsString + (optionsString.length() == 0 ? "" : ", ") + value;
      }
      else
        optionsString = optionsString + (optionsString.length() == 0 ? "" : ", ") + value.substring(0, i) + marker + value.substring(i);
      options.put(letter, value);
    }
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public boolean isCorrectValue(String value) {
    return options.containsValue(value) || options.containsKey(value.toLowerCase());
  }

  public String getOptionsString() {
    return optionsString;
  }
}

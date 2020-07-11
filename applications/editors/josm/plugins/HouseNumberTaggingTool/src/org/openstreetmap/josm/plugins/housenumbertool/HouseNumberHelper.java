package org.openstreetmap.josm.plugins.housenumbertool;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HouseNumberHelper {

    static public String incrementHouseNumber(String number, int increment) {
        if (number != null) {
            try {
                Matcher m = Pattern.compile("([^\\pN]+)?(\\pN+)([^\\pN]+)?").matcher(number);
                if (m.matches()) {
                    String prefix = m.group(1) != null ? m.group(1) : "";
                    int n = Integer.parseInt(m.group(2)) + increment;
                    String suffix = m.group(3) != null ? m.group(3) : "";
                    return prefix + n + suffix;
                }
            } catch (NumberFormatException e)  {
                // Do nothing
            }
        }
        return null;
    }
}

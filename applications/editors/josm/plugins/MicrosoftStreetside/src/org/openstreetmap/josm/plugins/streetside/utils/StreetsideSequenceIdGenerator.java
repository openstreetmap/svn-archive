// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.utils;

import java.util.UUID;

/**
 * 
 */
public class StreetsideSequenceIdGenerator {
  
  public static String generateId() {
    
    return UUID.randomUUID().toString();
    
  }

}

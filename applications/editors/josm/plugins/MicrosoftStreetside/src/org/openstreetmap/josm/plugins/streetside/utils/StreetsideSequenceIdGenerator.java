// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.utils;

import java.util.UUID;

/**
 * Utility class to generated unique ids for Streetside "sequences".
 * Due to the functionality inherited from Mapillary the plugin is structured to
 * handle sequences of contiguous imagery, but Streetside only has implicit
 * sequences defined by the "pre" and "ne" attributes.
 * <p/>
 * @See StreetsideSequence
 */
public class StreetsideSequenceIdGenerator {

  private StreetsideSequenceIdGenerator() {
    // private constructor to avoid instantiation
  }
  public static String generateId() {

    return UUID.randomUUID().toString();

  }

}

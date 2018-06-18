// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.utils;

import org.openstreetmap.josm.plugins.streetside.StreetsideData;

/**
 * Interface for listeners of the class {@link StreetsideData}.
 */
@FunctionalInterface
public interface StreetsideChangesetListener {

  /**
   * Fired when the an image is added or removed from the changeset.
   */
  void changesetChanged();
}

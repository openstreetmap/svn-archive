// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.openstreetmap.josm.plugins.streetside.utils.StreetsideChangesetListener;

public class StreetsideLocationChangeset extends AbstractSet<StreetsideImage> {
  private final Set<StreetsideChangesetListener> listeners = new HashSet<>();
  private final Set<StreetsideImage> changeset = Collections.newSetFromMap(new ConcurrentHashMap<>());

  public void addChangesetListener(StreetsideChangesetListener listener) {
    listeners.add(listener);
  }

  public void cleanChangeset() {
    changeset.clear();
    fireListeners();
  }

  private void fireListeners() {
    listeners.forEach(StreetsideChangesetListener::changesetChanged);
  }

  @Override
  public boolean add(StreetsideImage image) {
    boolean add = changeset.add(image);
    fireListeners();
    return add;
  }

  @Override
  public Iterator<StreetsideImage> iterator() {
    return changeset.iterator();
  }

  @Override
  public int size() {
    return changeset.size();
  }

  @Override
  public boolean remove(Object image) {
    boolean remove = changeset.remove(image);
    fireListeners();
    return remove;
  }
}

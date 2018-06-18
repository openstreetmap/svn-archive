// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.history.commands;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

import org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideUtils;

/**
 * Command joined when joining two images into the same sequence.
 *
 * @author nokutu
 *
 */
public class CommandUnjoin extends StreetsideExecutableCommand {

  private final StreetsideAbstractImage a;
  private final StreetsideAbstractImage b;

  /**
   * Main constructor.
   *
   * @param images
   *          The two images that are going to be unjoined. Must be of exactly
   *          size 2.
   * @throws IllegalArgumentException
   *           if the List size is different from 2.
   */
  public CommandUnjoin(List<StreetsideAbstractImage> images) {
    super(new ConcurrentSkipListSet<>(images));
    a = images.get(0);
    b = images.get(1);
    if (images.size() != 2)
      throw new IllegalArgumentException();
  }

  @Override
  public void execute() {
    this.redo();
  }

  @Override
  public void undo() {
    StreetsideUtils.join(a, b);
  }

  @Override
  public void redo() {
    StreetsideUtils.unjoin(a, b);
  }

  @Override
  public void sum(StreetsideCommand command) {
  }

  @Override
  public String toString() {
    // TODO: tr( RRH
    return tr("2 images unjoined");
  }
}

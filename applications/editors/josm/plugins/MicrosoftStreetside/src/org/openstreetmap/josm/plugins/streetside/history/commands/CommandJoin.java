// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.history.commands;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Arrays;
import java.util.concurrent.ConcurrentSkipListSet;

import org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideUtils;

/**
 * Command joined when joining two images into the same sequence.
 *
 * @author nokutu
 *
 */
public class CommandJoin extends StreetsideExecutableCommand {

  private final StreetsideAbstractImage a;
  private final StreetsideAbstractImage b;

  /**
   * Main constructor.
   *
   * @param a the first image, that is joined with the second one
   * @param b the second image, that is joined with the first one
   * @throws IllegalArgumentException if the images are already in the same sequence
   * @throws NullPointerException if {@code a} or {@code b} is null
   */
  public CommandJoin(final StreetsideAbstractImage a, final StreetsideAbstractImage b) {
    super(new ConcurrentSkipListSet<>(Arrays.asList(a, b))); // throws NPE if a or b is null
    if (a.getSequence() == b.getSequence()) {
      throw new IllegalArgumentException("Both images must be in different sequences for joining.");
    }
    this.a = a;
    this.b = b;
  }

  @Override
  public void execute() {
    redo();
  }

  @Override
  public void undo() {
    StreetsideUtils.unjoin(a, b);
  }

  @Override
  public void redo() {
    StreetsideUtils.join(a, b);
  }

  @Override
  public void sum(StreetsideCommand command) {
  }

  @Override
  public String toString() {
    return tr("2 images joined");
  }
}

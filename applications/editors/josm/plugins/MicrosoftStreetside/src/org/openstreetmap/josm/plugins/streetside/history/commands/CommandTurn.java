// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.history.commands;

import static org.openstreetmap.josm.tools.I18n.trn;

import java.util.Set;

import org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage;
import org.openstreetmap.josm.plugins.streetside.StreetsideLayer;

/**
 * Command created when an image's direction is changed.
 *
 * @author nokutu
 *
 */
public class CommandTurn extends StreetsideCommand {
  private double ca;

  /**
   * Main constructor.
   *
   * @param images
   *          Set of images that is turned.
   * @param ca
   *          How much the images turn.
   */
  public CommandTurn(Set<StreetsideAbstractImage> images, double ca) {
    super(images);
    this.ca = ca;
  }

  @Override
  public void undo() {
    for (StreetsideAbstractImage image : images) {
      image.turn(-ca);
      image.stopMoving();
    }
    StreetsideLayer.invalidateInstance();
  }

  @Override
  public void redo() {
    for (StreetsideAbstractImage image : images) {
      image.turn(ca);
      image.stopMoving();
    }
    StreetsideLayer.invalidateInstance();
  }

  @Override
  public String toString() {
    return trn("Turned {0} image", "Turned {0} images", images.size(),
        images.size());
  }

  @Override
  public void sum(StreetsideCommand command) {
    if (command instanceof CommandTurn) {
      ca += ((CommandTurn) command).ca;
    }
  }
}

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
  private double he;

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
    he = ca;
  }

  @Override
  public void undo() {
    for (StreetsideAbstractImage image : images) {
      image.turn(-he);
      image.stopMoving();
    }
    StreetsideLayer.invalidateInstance();
  }

  @Override
  public void redo() {
    for (StreetsideAbstractImage image : images) {
      image.turn(he);
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
      he += ((CommandTurn) command).he;
    }
  }
}

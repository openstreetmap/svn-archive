// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.history.commands;

import static org.openstreetmap.josm.tools.I18n.trn;

import java.util.Set;

import org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage;
import org.openstreetmap.josm.plugins.streetside.StreetsideLayer;

/**
 * Command created when an image's position is changed.
 *
 * @author nokutu
 *
 */
public class CommandMove extends StreetsideCommand {
  private double x;
  private double y;

  /**
   * Main constructor.
   *
   * @param images
   *          Set of images that are going to be moved.
   * @param x
   *          How much the x coordinate increases.
   * @param y
   *          How much the y coordinate increases.
   */
  public CommandMove(Set<StreetsideAbstractImage> images, double x,
                     double y) {
    super(images);
    this.x = x;
    this.y = y;
  }

  @Override
  public void undo() {
    for (StreetsideAbstractImage image : this.images) {
      image.move(-this.x, -this.y);
      image.stopMoving();
    }
    StreetsideLayer.invalidateInstance();
  }

  @Override
  public void redo() {
    for (StreetsideAbstractImage image : this.images) {
      image.move(this.x, this.y);
      image.stopMoving();
    }
    StreetsideLayer.invalidateInstance();
  }

  @Override
  public String toString() {
    // TODO: trn( RRH
    return trn("Moved {0} image", "Moved {0} images", this.images.size(),
        this.images.size());
  }

  @Override
  public void sum(StreetsideCommand command) {
    if (command instanceof CommandMove) {
      this.x += ((CommandMove) command).x;
      this.y += ((CommandMove) command).y;
    }
  }
}

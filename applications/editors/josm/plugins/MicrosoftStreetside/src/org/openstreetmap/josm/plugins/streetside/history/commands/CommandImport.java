// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.history.commands;

import static org.openstreetmap.josm.tools.I18n.trn;

import java.util.Set;

import org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage;
import org.openstreetmap.josm.plugins.streetside.StreetsideLayer;


/**
 * Imports a set of images stored locally.
 *
 * @author nokutu
 *
 */
public class CommandImport extends StreetsideExecutableCommand {

  /**
   * Main constructor.
   *
   * @param images
   *          The set of images that are going to be added. Might be in the same
   *          sequence or not.
   */
  public CommandImport(Set<StreetsideAbstractImage> images) {
    super(images);
  }

  @Override
  public void execute() {
    StreetsideLayer.getInstance().getData().addAll(this.images);
  }

  @Override
  public void undo() {
    for (StreetsideAbstractImage img : this.images) {
      StreetsideLayer.getInstance().getData().getImages().remove(img);
    }
    StreetsideLayer.invalidateInstance();
  }

  @Override
  public void redo() {
    this.execute();
  }

  @Override
  public void sum(StreetsideCommand command) {
  }

  @Override
  public String toString() {
    // TODO: trn( RRH
    return trn("Imported {0} image", "Imported {0} images", this.images.size(),
        this.images.size());
  }
}

// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.history.commands;

import java.util.Set;

import org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage;

/**
* Superclass for those commands that must be executed after creation.
*
* @author nokutu
*
*/
public abstract class StreetsideExecutableCommand extends StreetsideCommand {

/**
* Main constructor.
*
* @param images
*          The set of images affected by the command.
*/
public StreetsideExecutableCommand(Set<StreetsideAbstractImage> images) {
 super(images);
}

/**
* Executes the command. It is run when the command is added to the history
* record.
*/
public abstract void execute();
}

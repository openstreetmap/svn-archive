// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.history;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage;

import org.openstreetmap.josm.plugins.streetside.history.commands.StreetsideCommand;
import org.openstreetmap.josm.plugins.streetside.history.commands.StreetsideExecutableCommand;

/**
* History record system in order to let the user undo and redo commands.
*
* @author nokutu
*
*/
public class StreetsideRecord {
/** The unique instance of the class. */
private static StreetsideRecord instance;

private final List<StreetsideRecordListener> listeners = new ArrayList<>();

/** The set of commands that have taken place or that have been undone. */
public List<StreetsideCommand> commandList;
/** Last written command. */
public int position;

/**
* Main constructor.
*/
public StreetsideRecord() {
 this.commandList = new ArrayList<>();
 this.position = -1;
}

/**
* Returns the unique instance of the class.
*
* @return The unique instance of the class.
*/
public static synchronized StreetsideRecord getInstance() {
 if (StreetsideRecord.instance == null)
   StreetsideRecord.instance = new StreetsideRecord();
 return StreetsideRecord.instance;
}

/**
* Adds a listener.
*
* @param lis
*          The listener to be added.
*/
public void addListener(StreetsideRecordListener lis) {
 this.listeners.add(lis);
}

/**
* Removes the given listener.
*
* @param lis
*          The listener to be removed.
*/
public void removeListener(StreetsideRecordListener lis) {
 this.listeners.remove(lis);
}

/**
* Adds a new command to the list.
*
* @param command
*          The command to be added.
*/
public void addCommand(final StreetsideCommand command) {

 if (command instanceof StreetsideExecutableCommand)
   ((StreetsideExecutableCommand) command).execute();
 // Checks if it is a continuation of last command
 if (this.position != -1) {
   boolean equalSets = true;
   for (StreetsideAbstractImage img : this.commandList.get(this.position).images) {
     equalSets = command.images.contains(img) && equalSets;
   }
   for (StreetsideAbstractImage img : command.images) {
     equalSets = this.commandList.get(this.position).images.contains(img) && equalSets;
   }
   if (equalSets && this.commandList.get(this.position).getClass() == command.getClass()) {
     this.commandList.get(this.position).sum(command);
     fireRecordChanged();
     return;
   }
 }
 // Adds the command to the last position of the list.
 this.commandList.add(this.position + 1, command);
 this.position++;
 while (this.commandList.size() > this.position + 1) {
   this.commandList.remove(this.position + 1);
 }
 fireRecordChanged();
}

/**
* Undo latest command.
*/
public void undo() {
 if (this.position <= -1) {
   return;
 }
 this.commandList.get(this.position).undo();
 this.position--;
 fireRecordChanged();
}

/**
* Redoes latest undone action.
*/
public void redo() {
 if (position + 1 >= commandList.size()) {
   return;
 }
 this.position++;
 this.commandList.get(this.position).redo();
 fireRecordChanged();
}

private void fireRecordChanged() {
 this.listeners.stream().filter(Objects::nonNull).forEach(StreetsideRecordListener::recordChanged);
}

/**
* Resets the object to its start state.
*/
public void reset() {
 this.commandList.clear();
 this.position = -1;
}
}

// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses;

import org.openstreetmap.josm.command.Command;

/**
 * Command listener.
 */
public interface ICommandListener {
    /**
     * Called by a node entity if a command has been created. Clients may collect
     * these commands to define a sequence command.
     * @param entity The entity which created/used the command.
     * @param command The command instance to process by the enclosing command listener.
     */
    void commandIssued(IOSMEntity entity, Command command);
}

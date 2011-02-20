/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.openstreetmap.josm.plugins.fixAddresses;

import org.openstreetmap.josm.command.Command;

public interface ICommandListener {
	/**
	 * Called by a node entity if a command has been created. Clients may collect
	 * these commands to define a sequence command.
	 * @param entity The entity which created/used the command.
	 * @param command The command instance to process by the enclosing command listener.
	 */
	public void commandIssued(IOSMEntity entity, Command command);
}

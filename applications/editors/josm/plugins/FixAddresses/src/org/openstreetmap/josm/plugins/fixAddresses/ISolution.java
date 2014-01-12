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

import org.openstreetmap.josm.actions.JosmAction;

public interface ISolution {

	/**
	 * Gets the description of the solution.
	 *
	 * @return the description
	 */
	public String getDescription();

	/**
	 * Gets the action to execute for solving the problem.
	 *
	 * @return the action
	 */
	public JosmAction getAction();

	/**
	 * Gets the solution type.
	 *
	 * @return the type
	 */
	public SolutionType getType();

	/**
	 * Executes one or more actions to solve a problem.
	 */
	public void solve();
}

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

/**
 * The Class AddressSolution provides a basic implementation for a problem solution.
 */
public class AddressSolution implements ISolution {
	private JosmAction action;
	private String description;
	private SolutionType type;

	/**
	 * @param description The solution description.
	 * @param action The action to execute to solve the problem.
	 * @param type The solution type.
	 */
	public AddressSolution(String description, JosmAction action,
			SolutionType type) {
		super();
		this.description = description;
		this.action = action;
		this.type = type;
	}

	@Override
	public JosmAction getAction() {
		return action;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public SolutionType getType() {
		return type;
	}

	@Override
	public void solve() {
		// TODO: Remove??
	}
}

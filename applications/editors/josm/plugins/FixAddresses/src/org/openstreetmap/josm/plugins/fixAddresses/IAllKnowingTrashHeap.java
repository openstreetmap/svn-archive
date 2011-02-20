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

import java.util.List;

public interface IAllKnowingTrashHeap {

	/**
	 * Gets the list containing the best matching (closest) street names.
	 *
	 * @param name the name of the street to find the matches for.
	 * @param maxEntries the maximum number of matches.
	 * @return the closest street names
	 */
	public List<String> getClosestStreetNames(String name, int maxEntries);

	/**
	 * Gets the closest street name to the given name.
	 *
	 * @param name the name of the street to find a match for.
	 * @return the closest street name
	 */
	public String getClosestStreetName(String name);

	/**
	 * Checks if the given street name is valid.
	 *
	 * @param name the name of the street to check.
	 * @return true, if street name is valid; otherwise false.
	 */
	public boolean isValidStreetName(String name);
}

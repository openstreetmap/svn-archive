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

public interface IProblem {

	/**
	 * Gets the OSM entity which causes the problem.
	 *
	 * @return the source
	 */
	public IOSMEntity getSource();

	/**
	 * Gets the problem description.
	 *
	 * @return the description
	 */
	public String getDescription();

	/**
	 * Gets the problem type.
	 *
	 * @return the type
	 */
	public ProblemType getType();

	/**
	 * Gets the available solutions for this problem.
	 *
	 * @return the solutions
	 */
	public List<ISolution> getSolutions();

	/**
	 * Adds a possible solution to the problem.
	 *
	 * @param solution the solution
	 */
	public void addSolution(ISolution solution);

	/**
	 * Removes a solution from this problem.
	 *
	 * @param solution the solution
	 */
	public void removeSolution(ISolution solution);

	/**
	 * Removes all solutions from this problem.
	 */
	public void clearSolutions();

	/**
	 * Applies a {@link ISolution} instance on the problem.
	 *
	 * @param solution the solution
	 */
	public void applySolution(ISolution solution);
}

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

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.tools.CheckParameterUtil;

public class AddressProblem implements IProblem {
	private List<ISolution> solutions = null;
	private String description;
	private ProblemType type;
	private IOSMEntity source;

	/**
	 * Instantiates a new problem.
	 *
	 * @param source the source
	 * @param description The problem description.
	 * @param solutions This list of solutions.
	 * @param type the type
	 */
	public AddressProblem(IOSMEntity source, String description,
			List<ISolution> solutions, ProblemType type) {
		super();
		this.source = source;
		this.description = description;
		this.solutions = solutions;
		this.type = type;
	}

	/**
	 * Instantiates a new problem with type 'warning'.
	 *
	 * @param source the source
	 * @param description The problem description.
	 * @param solutions This list of solutions.
	 */
	public AddressProblem(IOSMEntity source, String description, List<ISolution> solutions) {
		this(source, description, solutions, ProblemType.Warning);
	}

	/**
	 * Instantiates a new problem with type 'warning' and without solutions.
	 *
	 * @param source the source
	 * @param description The problem description.
	 */
	public AddressProblem(IOSMEntity source, String description) {
		this(source, description, null, ProblemType.Warning);
	}

	/**
	 * Creates the solution list, if necessary.
	 */
	private void lazyCreateSolutions() {
		if (solutions == null) {
			solutions = new ArrayList<ISolution>();
		}
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fixAddresses.IProblem#addSolution(org.openstreetmap.josm.plugins.fixAddresses.ISolution)
	 */
	@Override
	public void addSolution(ISolution solution) {
		CheckParameterUtil.ensureParameterNotNull(solution, "solution");

		lazyCreateSolutions();
		solutions.add(solution);
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fixAddresses.IProblem#applySolution(org.openstreetmap.josm.plugins.fixAddresses.ISolution)
	 */
	@Override
	public void applySolution(ISolution solution) {
		CheckParameterUtil.ensureParameterNotNull(solution, "solution");

		solution.solve();
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fixAddresses.IProblem#clearSolutions()
	 */
	@Override
	public void clearSolutions() {
		if (solutions == null) return;

		solutions.clear();
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fixAddresses.IProblem#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fixAddresses.IProblem#getSolutions()
	 */
	@Override
	public List<ISolution> getSolutions() {
		return solutions;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fixAddresses.IProblem#getType()
	 */
	@Override
	public ProblemType getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fixAddresses.IProblem#removeSolution(org.openstreetmap.josm.plugins.fixAddresses.ISolution)
	 */
	@Override
	public void removeSolution(ISolution solution) {
		if (solutions == null ) throw new RuntimeException("Solution list is null");
		if (solutions.size() == 0) throw new RuntimeException("Solution list is empty");

		CheckParameterUtil.ensureParameterNotNull(solution, "solution");
		solutions.remove(solution);
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fixAddresses.IProblem#getSource()
	 */
	@Override
	public IOSMEntity getSource() {
		return source;
	}

}

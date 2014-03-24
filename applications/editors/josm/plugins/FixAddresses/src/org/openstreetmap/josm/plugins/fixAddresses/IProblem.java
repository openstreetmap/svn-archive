// License: GPL. For details, see LICENSE file.
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

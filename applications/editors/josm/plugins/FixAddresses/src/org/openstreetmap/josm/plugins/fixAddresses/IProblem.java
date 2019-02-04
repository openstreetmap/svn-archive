// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses;

import java.util.List;

/**
 * Generic problem.
 */
public interface IProblem {

    /**
     * Gets the OSM entity which causes the problem.
     *
     * @return the source
     */
    IOSMEntity getSource();

    /**
     * Gets the problem description.
     *
     * @return the description
     */
    String getDescription();

    /**
     * Gets the problem type.
     *
     * @return the type
     */
    ProblemType getType();

    /**
     * Gets the available solutions for this problem.
     *
     * @return the solutions
     */
    List<ISolution> getSolutions();

    /**
     * Adds a possible solution to the problem.
     *
     * @param solution the solution
     */
    void addSolution(ISolution solution);

    /**
     * Removes a solution from this problem.
     *
     * @param solution the solution
     */
    void removeSolution(ISolution solution);

    /**
     * Removes all solutions from this problem.
     */
    void clearSolutions();

    /**
     * Applies a {@link ISolution} instance on the problem.
     *
     * @param solution the solution
     */
    void applySolution(ISolution solution);
}

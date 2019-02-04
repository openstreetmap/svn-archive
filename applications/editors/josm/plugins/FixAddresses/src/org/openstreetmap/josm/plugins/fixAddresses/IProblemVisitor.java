// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses;

/**
 * Problem visitor.
 */
public interface IProblemVisitor {
    /**
     * Adds a problem without solution.
     *
     * @param problem the problem to add
     */
    void addProblem(IProblem problem);

    /**
     * Removes the problems of the given source.
     * @param entity OSM entity
     */
    void removeProblemsOfSource(IOSMEntity entity);
}

// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses;

public interface IProblemVisitor {
    /**
     * Adds a problem without solution.
     *
     * @param problem the problem to add
     */
    public void addProblem(IProblem problem);

    /**
     * Removes the problems of the given source.
     */
    public void removeProblemsOfSource(IOSMEntity entity);
}

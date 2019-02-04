// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses;

import org.openstreetmap.josm.actions.JosmAction;

/**
 * Generic solution to a {@link IProblem}.
 */
public interface ISolution {

    /**
     * Gets the description of the solution.
     *
     * @return the description
     */
    String getDescription();

    /**
     * Gets the action to execute for solving the problem.
     *
     * @return the action
     */
    JosmAction getAction();

    /**
     * Gets the solution type.
     *
     * @return the type
     */
    SolutionType getType();

    /**
     * Executes one or more actions to solve a problem.
     */
    void solve();
}

// License: GPL. For details, see LICENSE file.
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

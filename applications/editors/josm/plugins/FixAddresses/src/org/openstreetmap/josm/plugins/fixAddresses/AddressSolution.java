// License: GPL. For details, see LICENSE file.
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

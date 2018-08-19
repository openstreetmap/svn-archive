// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.surveyor.action;

import java.util.List;

import org.openstreetmap.josm.plugins.surveyor.SurveyorAction;

/**
 * @author cdaller
 *
 */
public abstract class AbstractSurveyorAction implements SurveyorAction {
    private List<String> parameters;

    /**
     * Returns the parameters.
     * @return the parameters
     */
    public List<String> getParameters() {
        return parameters;
    }

    @Override
    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }
}

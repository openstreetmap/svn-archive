/**
 * Copyright by Christof Dallermassl
 * This program is free software and licensed under GPL.
 */
package at.dallermassl.josm.plugin.surveyor.action;

import java.util.List;

import at.dallermassl.josm.plugin.surveyor.SurveyorAction;

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
        
    /* (non-Javadoc)
     * @see at.dallermassl.josm.plugin.surveyor.SurveyorAction#setParameters(java.util.List)
     */
    //@Override
    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

}

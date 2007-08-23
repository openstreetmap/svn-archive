/**
 * Copyright by Christof Dallermassl
 * This program is free software and licensed under GPL.
 */
package at.dallermassl.josm.plugin.surveyor;

import java.util.List;

/**
 * @author cdaller
 *
 */
public interface SurveyorAction {

    /**
     * Action callback indicating that the action should do something.
     * @param event the event.
     */
    public void actionPerformed(GpsActionEvent event);
    
    /**
     * Sets the parameters for the action execution.
     * @param parameters the parameters.
     */
    public void setParameters(List<String> parameters);
}

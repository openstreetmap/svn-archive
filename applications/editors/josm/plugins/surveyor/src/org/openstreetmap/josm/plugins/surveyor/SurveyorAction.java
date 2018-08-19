// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.surveyor;

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
    void actionPerformed(GpsActionEvent event);

    /**
     * Sets the parameters for the action execution.
     * @param parameters the parameters.
     */
    void setParameters(List<String> parameters);
}

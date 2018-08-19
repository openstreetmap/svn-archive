// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.surveyor;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple factory that creates a class instance from a classname. It caches the instances, so
 * the action instances are used as singletons!
 * A package name of "at.dallermassl.josm.plugin.surveyor.action" is assumed, if the class could
 * not be found.
 *
 * @author cdaller
 *
 */
public final class SurveyorActionFactory {
    private static Map<String, SurveyorAction> actionCache = new HashMap<>();
    public static final String DEFAULT_PACKAGE = SurveyorActionFactory.class.getPackage().getName() + ".action";

    private SurveyorActionFactory() {
        // Hide default contructir for utilities classes
    }
    
    public static SurveyorAction getInstance(String actionClass) {
        try {
            SurveyorAction action = actionCache.get(actionClass);
            if (action == null) {
                try {
                    action = (SurveyorAction) Class.forName(actionClass).getDeclaredConstructor().newInstance();
                } catch (ClassNotFoundException e) {
                    actionClass = DEFAULT_PACKAGE + "." + actionClass;
                    action = (SurveyorAction) Class.forName(actionClass).getDeclaredConstructor().newInstance();
                }
                actionCache.put(actionClass, action);
            }
            return action;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Could not create action class '" + actionClass + "'", e);
        }
    }

}

/**
 * Copyright by Christof Dallermassl
 * This program is free software and licensed under GPL.
 */
package at.dallermassl.josm.plugin.surveyor;

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
public class SurveyorActionFactory {
    private static Map<String, SurveyorAction>actionCache = new HashMap<String, SurveyorAction>();
    public static final String DEFAULT_PACKAGE = SurveyorActionFactory.class.getPackage().getName() + ".action";

    /**
     * @param actionClass
     * @return
     */
    public static SurveyorAction getInstance(String actionClass) {
        try {
            SurveyorAction action = actionCache.get(actionClass);
            if(action == null) {
                try {
                    action = (SurveyorAction)Class.forName(actionClass).newInstance();
                } catch (ClassNotFoundException e) {
                    actionClass = DEFAULT_PACKAGE + "." + actionClass;
                    action = (SurveyorAction)Class.forName(actionClass).newInstance();
                }
                actionCache.put(actionClass, action);
            }
            return action;
        } catch (InstantiationException e) {
            throw new RuntimeException("Could not create action class '" + actionClass + "'", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not create action class '" + actionClass + "'", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not create action class '" + actionClass + "'", e);
        }
    }

}

// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.surveyor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dinopolis.util.io.Tokenizer;
import org.openstreetmap.josm.tools.Logging;

/**
 * @author cdaller
 *
 */
public class SurveyorActionDescription {
    private String actionClass;
    private List<String> params;
    private SurveyorAction action;

    /**
     * Default Constructor
     */
    public SurveyorActionDescription() {
        super();
    }
    
    public SurveyorActionDescription(String actionClass) {
        super();
        this.actionClass = actionClass;
    }
    
    public SurveyorActionDescription(String actionClass, List<String> params) {
        super();
        this.actionClass = actionClass;
        this.params = params;
    }
    
    public SurveyorActionDescription(String actionClass, String[] params) {
        super();
        this.actionClass = actionClass;
        this.params = new ArrayList<>();
        for (int index = 0; index < params.length; index++) {
            this.params.add(params[index]);
        }
    }
    
    /**
     * @return the actionClass
     */
    public String getActionClass() {
        return this.actionClass;
    }
    
    /**
     * @param actionClass the actionClass to set
     */
    public void setActionClass(String actionClass) {
        this.actionClass = actionClass;
    }
    
    /**
     * @return the params
     */
    public List<String> getParameterList() {
        return this.params;
    }
    
    /**
     * @param params the params to set
     */
    public void setParameterList(List<String> params) {
        this.params = params;
    }

    public void actionPerformed(GpsActionEvent e) {
        if (action == null) {
            action = SurveyorActionFactory.getInstance(actionClass);
            action.setParameters(getParameterList());
        }
        action.actionPerformed(e);
    }

    /**
     * Sets the classname of the action to use. Callback method of xml parser.
     * @param claszName the name of the action class.
     */
    public void setClass_(String claszName) {
        setActionClass(claszName);
    }

    /**
     * Set the params as a comma separated string.
     * @param paramString the comma separated string for the parameters.
     */
    public void setParams(String paramString) {
        Tokenizer tokenizer = new Tokenizer(paramString, ",");
        try {
            params = tokenizer.nextLine();
        } catch (IOException ignore) {
            Logging.debug(ignore);
        }
    }

}

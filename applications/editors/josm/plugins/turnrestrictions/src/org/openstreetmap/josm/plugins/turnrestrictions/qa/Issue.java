package org.openstreetmap.josm.plugins.turnrestrictions.qa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Action;

import org.openstreetmap.josm.tools.CheckParameterUtil;

/**
 * An issue represents a data integrity violation in a turn restriction. 
 * 
 * The issue has a {@see Severity}. It is described to the user with a HTML formatted
 * text (see {@see #getText()}) and it suggests a list of possible actions to fix
 * the issue (see {@see #getActions()}).
 * 
 */
abstract public class Issue {
    /** the parent model for this issue */
    protected IssuesModel parent;
    protected Severity severity;
    protected final ArrayList<Action> actions = new ArrayList<Action>();
    
    /**
     * Creates a new issue associated with a parent model. Severity is
     * initialized to {@see Severity#WARNING}.
     * 
     * @param parent the parent model. Must not be null.
     * @throws IllegalArgumentException thrown if parent is null
     */
    public Issue(IssuesModel parent) throws IllegalArgumentException{
        CheckParameterUtil.ensureParameterNotNull(parent, "parent");
        this.parent = parent;
        this.severity = Severity.WARNING;
    }
    
    /**
     * Creates a new issue of severity {@code severity} associated with
     * the parent model {@code parent}.
     * 
     * @param parent the parent model. Must not be null.
     * @param severity the severity. Must not be null.
     * @throws IllegalArgumentException thrown if parent is null
     * @throws IllegalArgumentException thrown if severity is null 
     */
    public Issue(IssuesModel parent, Severity severity){
        CheckParameterUtil.ensureParameterNotNull(parent, "parent");
        CheckParameterUtil.ensureParameterNotNull(severity, "severity");
        this.parent = parent;
        this.severity = severity;
    }

    /**
     * Replies the parent model this issue is associated with 
     * 
     * @return the parent model 
     */
    public IssuesModel getIssuesModel() {
        return parent;
    }

    /**
     * Replies the severity of this issue 
     * 
     * @return the severity 
     */
    public Severity getSeverity() {
        return severity;
    }

    /**
     * Sets the severity of this issue. 
     * 
     * @param severity the severity. Must not be null.
     * @throws IllegalArgumentException thrown if severity is null
     */
    public void setSeverity(Severity severity) throws IllegalArgumentException {
        CheckParameterUtil.ensureParameterNotNull(severity, "severity");
        this.severity = severity;
    }

    /**
     * Replies the HTML formatted description of the issue. The text should neither include
     * the &lt;html&gt;, nor the &lt;body&gt; tag.  
     * 
     * @return the HTML formatted description of the issue.
     */
    public abstract String getText();
    
    /**
     * Replies a list of actions which can be applied to this issue in order to fix
     * it. The default implementation replies an empty list.
     * 
     * @return a list of action
     */
    public List<Action> getActions() {
        return Collections.unmodifiableList(actions);
    }
}

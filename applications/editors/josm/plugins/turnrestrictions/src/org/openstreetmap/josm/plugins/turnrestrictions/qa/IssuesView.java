package org.openstreetmap.josm.plugins.turnrestrictions.qa;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.gui.widgets.VerticallyScrollablePanel;
import org.openstreetmap.josm.tools.CheckParameterUtil;

/**
 * IssuesView provides a view on a {@see IssuesModel}.
 */
public class IssuesView extends VerticallyScrollablePanel implements Observer{
    //static private final Logger logger = Logger.getLogger(IssuesView.class.getName());
    
    /** the issues model */
    private IssuesModel model;
    
    protected void build(){
        setLayout(new GridBagLayout());
    }
    
    /**
     * Creates the view 
     * 
     * @param model the model. Must not be null.
     * @exception IllegalArgumentException thrown if model is null
     */
    public IssuesView(IssuesModel model) throws IllegalArgumentException{
        CheckParameterUtil.ensureParameterNotNull(model, "model");
        this.model = model;
        model.addObserver(this);
        build();
        HelpUtil.setHelpContext(this, HelpUtil.ht("/Plugin/TurnRestrictions#ErrorsAndWarnings"));
    }
    
    /**
     * Refreshes the view with the current state in the model
     */
    public void refresh() {
        removeAll();
        if (! model.getIssues().isEmpty()){
            GridBagConstraints gc = new GridBagConstraints();
            gc.anchor = GridBagConstraints.NORTHWEST;
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.weightx = 1.0;
            gc.weighty = 0.0;
            gc.gridx = 0;
            gc.gridy = 0;
            for (Issue issue: model.getIssues()){
                add(new IssueView(issue), gc);
                gc.gridy++;
            }
            // filler - grabs remaining space
            gc.weighty = 1.0;           
            add(new JPanel(), gc);
        }
        invalidate();
    }

    /* ------------------------------------------------------------------------------- */
    /* interface Observer                                                              */
    /* ------------------------------------------------------------------------------- */
    public void update(Observable o, Object arg) {
        refresh();      
    }
}

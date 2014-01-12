package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import javax.swing.JComboBox;

import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.plugins.turnrestrictions.preferences.PreferenceKeys;
/**
 * A combo box for selecting a turn restriction type.
 */
public class TurnRestrictionComboBox extends JComboBox{
    
    /**
     * Constructor 
     * 
     * @param model the combo box model. Must not be null.
     */
    public TurnRestrictionComboBox(TurnRestrictionComboBoxModel model){
        super(model);
        setEditable(false);
        setRenderer(new TurnRestrictionTypeRenderer());
    }
    
    /**
     * Replies the turn restriction combo box model 
     * 
     * @return the turn restriction combo box model
     */
    public TurnRestrictionComboBoxModel getTurnRestrictionComboBoxModel() {
        return (TurnRestrictionComboBoxModel)getModel();
    }
    
    /**
     * Initializes the set of icons used from the preference key
     * {@link PreferenceKeys#ROAD_SIGNS}.
     * 
     * @param prefs the JOSM preferences 
     */
    public void initIconSetFromPreferences(Preferences prefs){
        TurnRestrictionTypeRenderer renderer = (TurnRestrictionTypeRenderer)getRenderer();
        renderer.initIconSetFromPreferences(prefs);
        repaint();
    }
}

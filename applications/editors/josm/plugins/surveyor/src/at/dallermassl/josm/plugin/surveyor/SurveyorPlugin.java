/**
 * 
 */
package at.dallermassl.josm.plugin.surveyor;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import livegps.LiveGpsPlugin;

/**
 * Plugin that uses live gps data and a button panel to add nodes/waypoints etc at the current
 * position.
 * 
 * TODO: auto save marker layer and data layer?
 * TODO: in action retrieve buttontype state to set on/off values
 * @author cdaller
 *
 */
public class SurveyorPlugin extends LiveGpsPlugin {

    /**
     * 
     */
    public SurveyorPlugin() {
        super();
        SurveyorShowAction surveyorAction = new SurveyorShowAction("Surveyor", this);
        JMenuItem surveyorMenuItem = new JMenuItem(surveyorAction);
        surveyorAction.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke("alt S"));
//        surveyorMenuItem.addActionListener(new ActionListener() {
        getLgpsMenu().addSeparator();
        getLgpsMenu().add(surveyorMenuItem);
        
        AutoSaveAction autoSaveAction = new AutoSaveAction("AutoSave LiveData");
        JCheckBoxMenuItem autoSaveMenu = new JCheckBoxMenuItem(autoSaveAction);
        getLgpsMenu().add(autoSaveMenu);
        
    }
    
}

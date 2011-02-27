package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.gui.widgets.VerticallyScrollablePanel;
import org.openstreetmap.josm.plugins.turnrestrictions.editor.NavigationControler.BasicEditorFokusTargets;
import org.openstreetmap.josm.plugins.turnrestrictions.preferences.PreferenceKeys;
import org.openstreetmap.josm.tools.CheckParameterUtil;

/**
 * BasicEditorPanel provides a UI for editing the basic elements of a turn restriction,
 * i.e. its restriction type, the from, the to, and the via objects.
 * 
 */
public class BasicEditorPanel extends VerticallyScrollablePanel {

    /** the turn restriction model */
    private TurnRestrictionEditorModel model;
    
    /** the UI widgets */
    private TurnRestrictionLegEditor fromEditor;
    private TurnRestrictionLegEditor toEditor;
    private ViaList lstVias;
    private JLabel lblVias;
    private JScrollPane spVias;
    private TurnRestrictionComboBox cbTurnRestrictions;
    private VehicleExceptionEditor vehicleExceptionsEditor;
    
    /**
     * builds the UI
     */
    protected void build() {
        setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 0.0;
        
        // the editor for selecting the 'from' leg
        gc.insets = new Insets(0,0,5,5);    
        add(new JLabel(tr("Type:")), gc);
        
        gc.gridx = 1;
        gc.weightx = 1.0;
        add(cbTurnRestrictions = new TurnRestrictionComboBox(new TurnRestrictionComboBoxModel(model)), gc);

        // the editor for selecting the 'from' leg
        gc.gridx = 0;
        gc.gridy = 1;   
        gc.weightx = 0.0;
        add(new JLabel(tr("From:")), gc);
        
        gc.gridx = 1;
        gc.weightx = 1.0;
        add(fromEditor = new TurnRestrictionLegEditor(model, TurnRestrictionLegRole.FROM),gc);

        // the editor for selecting the 'to' leg
        gc.gridx = 0;
        gc.gridy = 2;
        gc.weightx = 0.0;
        gc.insets = new Insets(0,0,5,5);    
        add(new JLabel(tr("To:")), gc);
        
        gc.gridx = 1;
        gc.weightx = 1.0;
        add(toEditor = new TurnRestrictionLegEditor(model, TurnRestrictionLegRole.TO),gc);
        
        // the editor for selecting the 'vias' 
        gc.gridx = 0;
        gc.gridy = 3;
        gc.weightx = 0.0;
        gc.insets = new Insets(0,0,5,5);    
        add(lblVias = new JLabel(tr("Vias:")), gc);
        
        gc.gridx = 1;
        gc.weightx = 1.0;
        gc.weighty = 1.0;
        DefaultListSelectionModel selectionModel = new DefaultListSelectionModel();
        spVias = new JScrollPane(lstVias = new ViaList(new ViaListModel(model, selectionModel), selectionModel)) {
        	// fixes #6016 : Scrollbar hides field entry
        	public Dimension getPreferredSize() {
        		return new Dimension(100, 80); // only height is relevant, 80 is just a heuristical value
             }
        };
        add(spVias,gc);
        if (!Main.pref.getBoolean(PreferenceKeys.SHOW_VIAS_IN_BASIC_EDITOR, false)) {
            lblVias.setVisible(false);
            spVias.setVisible(false);
        }
        
        // the editor for vehicle exceptions
        vehicleExceptionsEditor = new VehicleExceptionEditor(model);
        gc.gridx = 0;
        gc.gridy = 4;
        gc.weightx = 1.0;
        gc.weighty = 0.0;
        gc.gridwidth = 2;
        gc.insets = new Insets(0,0,5,5);    
        add(vehicleExceptionsEditor, gc);
        
        // just a filler - grabs remaining space 
        gc.gridx = 0;
        gc.gridy = 5;
        gc.gridwidth = 2;
        gc.weighty = 1.0;
        gc.fill = GridBagConstraints.BOTH;
        add(new JPanel(), gc);
            
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    }
    
    
    /**
     * Creates the panel. 
     * 
     * @param model the editor model. Must not be null.
     * @throws IllegalArgumentException thrown if model is null
     */
    public BasicEditorPanel(TurnRestrictionEditorModel model) {
        CheckParameterUtil.ensureParameterNotNull(model, "model");
        this.model = model;
        build();
        HelpUtil.setHelpContext(this, HelpUtil.ht("/Plugin/TurnRestrictions#BasicEditor"));
    }
    
    /**
     * Requests the focus on one of the input widgets for turn
     * restriction data.
     * 
     * @param focusTarget the target component to request focus for.
     * Ignored if null.
     */
    public void requestFocusFor(BasicEditorFokusTargets focusTarget){
        if (focusTarget == null) return;
        switch(focusTarget){
        case RESTRICION_TYPE:
            cbTurnRestrictions.requestFocusInWindow();
            break;
        case FROM:
            fromEditor.requestFocusInWindow();
            break;
        case TO:
            toEditor.requestFocusInWindow();
            break;
        case VIA:
            lstVias.requestFocusInWindow();
            break;
        }
    }   
    
    /**
     * Initializes the set of icons used from the preference key
     * {@link PreferenceKeys#ROAD_SIGNS}.
     * 
     * @param prefs the JOSM preferences 
     */
    public void initIconSetFromPreferences(Preferences prefs){      
        cbTurnRestrictions.initIconSetFromPreferences(prefs);
    }
    
    /**
     * Initializes the visibility of the list of via-objects depending
     * on values in the JOSM preferences
     * 
     * @param prefs the JOSM preferences
     */
    public void initViasVisibilityFromPreferences(Preferences prefs){
        boolean value = prefs.getBoolean(PreferenceKeys.SHOW_VIAS_IN_BASIC_EDITOR, false);
        if (value != lblVias.isVisible()){
            lblVias.setVisible(value);
            spVias.setVisible(value);
        }
    }
}

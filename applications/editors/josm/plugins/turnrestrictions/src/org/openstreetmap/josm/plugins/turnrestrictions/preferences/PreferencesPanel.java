package org.openstreetmap.josm.plugins.turnrestrictions.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.logging.Logger;


import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;

import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.gui.widgets.HtmlPanel;
import org.openstreetmap.josm.gui.widgets.VerticallyScrollablePanel;
import org.openstreetmap.josm.plugins.turnrestrictions.editor.TurnRestrictionType;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * IconPreferencePanel allows to configure a set of road sign icons to be
 * used in the turnrestrictions plugin.
 * 
 */
public class PreferencesPanel extends VerticallyScrollablePanel {
    private static final Logger logger = Logger.getLogger(PreferencesPanel.class.getName());
    private JRadioButton rbSetA;
    private JRadioButton rbSetB;
    private ButtonGroup bgIconSet;
    private JCheckBox cbShowViaListInBasicEditor;
    
    protected JPanel buildShowViaListInBasicEditorPanel() {
        JPanel pnl = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.gridx = 0;
        gc.gridy = 0;
        
        HtmlPanel msg = new HtmlPanel();
        msg.setText("<html><body>"
                + tr("The Basic Editor can optionally display the list of via-objects "
                     + "of a turn restriction. If enabled, one can edit them "
                     + "in the Basic editor too. If disabled, editing of via-objects is "
                     + "possible in the Advanced Editor only."
                  )
                + "</body></html>"
        );
        pnl.add(msg, gc);
        
        gc.gridy++;
        pnl.add(cbShowViaListInBasicEditor = new JCheckBox(tr("Display and edit list of via-objects in the Basic Editor")), gc);
        return pnl;
    }
    
    /**
     * Builds the panel for the icon set "set-a"
     * 
     * @return
     */
    protected JPanel buildSetAPanel() {
        JPanel pnl = new JPanel(new GridBagLayout());;
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.gridx = 0;
        gc.gridy = 0;
        
        pnl.add(rbSetA = new JRadioButton(tr("Road signs - Set A")),gc);
        
        JPanel icons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (TurnRestrictionType type: TurnRestrictionType.values()){
            JLabel lbl = new JLabel();
            icons.add(lbl);
            lbl.setIcon(ImageProvider.get("types/set-a",type.getTagValue()));
        }
        
        gc.gridy = 1;
        gc.insets = new Insets(0,20,0,0);
        pnl.add(icons, gc);
        return pnl;     
    }
    
    /**
     * Builds the panel for the icon set "set-b"
     * 
     * @return
     */
    protected JPanel buildSetBPanel() {
        JPanel pnl = new JPanel(new GridBagLayout());;
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.gridx = 0;
        gc.gridy = 0;
        
        pnl.add(rbSetB = new JRadioButton(tr("Road signs - Set B")),gc);
        
        JPanel icons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (TurnRestrictionType type: TurnRestrictionType.values()){
            JLabel lbl = new JLabel();
            icons.add(lbl);
            lbl.setIcon(ImageProvider.get("types/set-b",type.getTagValue()));
        }
        
        gc.gridy = 1;
        gc.insets = new Insets(0,20,0,0);
        pnl.add(icons, gc);
        return pnl;     
    }
    
    /**
     * Builds the message panel at the top
     * 
     * @return
     */
    protected JPanel buildMessagePanel() {
        HtmlPanel pnl = new HtmlPanel();
        pnl.setText(
                "<html><body>"
              + tr("Please select the set of road sign icons to be used in the plugin.")
              + "</body></html>"
        );
        return pnl;
    }
    
    /**
     * Builds the UI
     * 
     * @return
     */
    protected void build() {            
        setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.gridx = 0;
        gc.gridy = 0;
        
        add(buildMessagePanel(), gc);
        gc.gridy++;
        add(buildSetAPanel(), gc);
        gc.gridy++;
        add(buildSetBPanel(), gc);
        gc.gridy++;
        add(new JSeparator(), gc);      
        gc.gridy++;
        add(buildShowViaListInBasicEditorPanel(), gc);
        gc.gridy++;
        add(new JSeparator(), gc);
        gc.gridy++;
        
        // filler - just grab remaining space
        gc.gridy++;
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1.0;
        add(new JPanel(), gc);       
        
        bgIconSet = new ButtonGroup();
        bgIconSet.add(rbSetA);
        bgIconSet.add(rbSetB);
        
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    }
    
    /**
     * Initializes the UI from the current settings in the JOSM preferences
     * {@code prefs}
     * 
     * @param prefs the preferences 
     */
    public void initFromPreferences(Preferences prefs){
        String set = prefs.get(PreferenceKeys.ROAD_SIGNS, "set-a");
        if (! set.equals("set-a") && ! set.equals("set-b")) {
            System.out.println(tr("Warning: the preference with key ''{0}'' has an unsupported value ''{1}''. Assuming the default value ''set-a''.", PreferenceKeys.ROAD_SIGNS, set));
            set = "set-a";
        }
        if (set.equals("set-a")){
            rbSetA.setSelected(true);
        } else {
            rbSetB.setSelected(true);
        }
        
        boolean b = prefs.getBoolean(PreferenceKeys.SHOW_VIAS_IN_BASIC_EDITOR, false);
        cbShowViaListInBasicEditor.setSelected(b);
    }
    
    /**
     * Saves the current settings to the JOSM preferences {@code prefs}.
     * 
     * @param prefs the preferences 
     */
    public void saveToPreferences(Preferences prefs){
        prefs.put(PreferenceKeys.ROAD_SIGNS, rbSetA.isSelected() ? "set-a" : "set-b");
        prefs.put(PreferenceKeys.SHOW_VIAS_IN_BASIC_EDITOR, cbShowViaListInBasicEditor.isSelected());
    }
    
    public PreferencesPanel() {
        build();
    }   
}
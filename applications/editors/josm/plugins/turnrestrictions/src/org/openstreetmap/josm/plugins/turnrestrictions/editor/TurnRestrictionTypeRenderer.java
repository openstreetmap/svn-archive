package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.plugins.turnrestrictions.preferences.PreferenceKeys;
import org.openstreetmap.josm.tools.ImageProvider;


public class TurnRestrictionTypeRenderer extends JLabel implements ListCellRenderer{
 
    final private Map<TurnRestrictionType, ImageIcon> icons = new HashMap<TurnRestrictionType, ImageIcon>();
    private String iconSet = "set-a";
    
    /**
     * Loads the image icons for the rendered turn restriction types 
     */
    protected void loadImages() {
        for(TurnRestrictionType type: TurnRestrictionType.values()) {
            try {
                ImageIcon icon = new ImageIcon(ImageProvider.get("types/" + iconSet, type.getTagValue()).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
                icons.put(type,icon);
            } catch(Exception e){
                System.out.println(tr("Warning: failed to load icon for turn restriction type ''{0}''", type.getTagValue()));
                e.printStackTrace();                
            }
        }
    }
    
    public TurnRestrictionTypeRenderer() {
        setOpaque(true);
        loadImages();
    }
    
    protected void renderColors(boolean isSelected){
        if (isSelected){
            setBackground(UIManager.getColor("List.selectionBackground"));
            setForeground(UIManager.getColor("List.selectionForeground"));
        } else {
            setBackground(UIManager.getColor("List.background"));
            setForeground(UIManager.getColor("List.foreground"));           
        }
    }
    
    /**
     * Initializes the set of icons used from the preference key
     * {@link PreferenceKeys#ROAD_SIGNS}.
     * 
     * @param prefs the JOSM preferences 
     */
    public void initIconSetFromPreferences(Preferences prefs){      
        iconSet = prefs.get(PreferenceKeys.ROAD_SIGNS, "set-a");
        iconSet = iconSet.trim().toLowerCase();
        if (!iconSet.equals("set-a") && !iconSet.equals("set-b")) {
            iconSet = "set-a";
        }
        loadImages();
    }
    
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        
        renderColors(isSelected);
        if (value == null) {
            setText(tr("please select a turn restriction type"));
            setIcon(null);
        } else if (value instanceof String){
            setText((String)value);
            setIcon(null); // FIXME: special icon for non-standard types? 
        } else if (value instanceof TurnRestrictionType){
            TurnRestrictionType type = (TurnRestrictionType)value;
            setText(type.getDisplayName());
            setIcon(icons.get(type));
        }
        return this;
    }   
}

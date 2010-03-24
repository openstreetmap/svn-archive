package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import java.awt.Component;
import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import org.openstreetmap.josm.tools.ImageProvider;
import static org.openstreetmap.josm.tools.I18n.tr;


public class TurnRestrictionTypeRenderer extends JLabel implements ListCellRenderer{
 
	final private Map<TurnRestrictionType, ImageIcon> icons = new HashMap<TurnRestrictionType, ImageIcon>();
	
	/**
	 * Loads the image icons for the rendered turn restriction types 
	 */
	protected void loadImages() {
		for(TurnRestrictionType type: TurnRestrictionType.values()) {
			try {
				ImageIcon icon = new ImageIcon(ImageProvider.get("types/set-a", type.getTagValue()).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
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

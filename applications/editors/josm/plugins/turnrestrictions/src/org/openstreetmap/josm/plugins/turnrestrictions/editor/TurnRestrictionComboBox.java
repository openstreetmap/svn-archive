package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import javax.swing.JComboBox;
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
}

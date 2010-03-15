package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.openstreetmap.josm.tools.CheckParameterUtil;

/**
 * BasicEditorPanel provides a UI for editing the basic elements of a turn restriction,
 * i.e. its restriction type, the from, the to, and the via objects.
 * 
 *
 */
public class BasicEditorPanel extends JPanel {

	/** the turn restriction model */
	private TurnRestrictionEditorModel model;
	
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
	    add(new JLabel("Type:"), gc);
	    
	    gc.gridx = 1;
	    gc.weightx = 1.0;
	    add(new TurnRestrictionComboBox(new TurnRestrictionComboBoxModel(model)), gc);

		// the editor for selecting the 'from' leg
	    gc.gridx = 0;
	    gc.gridy = 1;	
	    gc.weightx = 0.0;
	    add(new JLabel("From:"), gc);
	    
	    gc.gridx = 1;
	    gc.weightx = 1.0;
	    add(new TurnRestrictionLegEditor(model, TurnRestrictionLegRole.FROM),gc);

	    // the editor for selecting the 'to' leg
	    gc.gridx = 0;
	    gc.gridy = 2;
		gc.weightx = 0.0;
	    gc.insets = new Insets(0,0,5,5);	
	    add(new JLabel("To:"), gc);
	    
	    gc.gridx = 1;
	    gc.weightx = 1.0;
	    add(new TurnRestrictionLegEditor(model, TurnRestrictionLegRole.TO),gc);
	    
	    // the editor for selecting the 'vias' 
	    gc.gridx = 0;
	    gc.gridy = 3;
		gc.weightx = 0.0;
	    gc.insets = new Insets(0,0,5,5);	
	    add(new JLabel("Vias:"), gc);
	    
	    gc.gridx = 1;
	    gc.weightx = 1.0;
	    DefaultListSelectionModel selectionModel = new DefaultListSelectionModel();
	    add(new JScrollPane(new ViaList(new ViaListModel(model, selectionModel), selectionModel)),gc);
	    
	    // just a filler - grabs remaining space 
	    gc.gridx = 0;
	    gc.gridy = 4;
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
	}
	
	
}

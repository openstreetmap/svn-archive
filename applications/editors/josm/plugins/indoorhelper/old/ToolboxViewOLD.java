package Views;

import static org.openstreetmap.josm.tools.I18n.tr;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.openstreetmap.josm.gui.dialogs.ToggleDialog;


/**
 * 
 * The toolbox view for the UI of the indoor mapping helper.
 * 
 * 
 * @author egru
 *
 */

@SuppressWarnings("serial")
public class ToolboxViewOLD extends ToggleDialog{
	
	/**
	 * 
	 */
	private JPanel toolboxPanel;									// JPanel for the toolbox
	private JToggleButton activatorButton;							// button to activate/deactivate the toolbox
															
	
	/**
	 * Constructor for the indoor helper toolbox 
	 */	
	public ToolboxViewOLD() {
		super(tr("Indoor Mapping Helper"), "indoorhelper", "Toolbox for indoor mapping assistance", null, 150, true);
		
		toolboxPanel = new JPanel(new BorderLayout());
		activatorButton = new JToggleButton("OFF");
		
		activatorButton.addActionListener(new ActionListener() {
			
			// Handles the click events on the ON/OFF Button of the event
			 
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(activatorButton.getText().equals("OFF")){
					activatorButton.setText("ON");
					
					LevelSelectorView levSel = new LevelSelectorView();
					levSel.createFrame();
					
				} else if(activatorButton.getText().equals("ON")){
					activatorButton.setText("OFF");
				}
				
				
			}
		});
		toolboxPanel.add(activatorButton, java.awt.BorderLayout.NORTH);		
		
		this.createLayout(toolboxPanel, false, null);
	}
}

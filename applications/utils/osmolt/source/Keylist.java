// Imports
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import net.miginfocom.swing.MigLayout;

class Keylist extends JPanel implements ActionListener, ListSelectionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// Instance attributes used in this example

	private JList listbox;

	private Vector listData;

	private JButton addButton;

	private JButton removeButton;

	private JScrollPane scrollPane;

	MapFeatures mapFeatures;

	MapFeaturesGUI mapFeaturesGUI;

	// Constructor of main frame
	public Keylist(MapFeatures mapFeatures, MapFeaturesGUI mapFeaturesGUI) {
		this.mapFeatures = mapFeatures;
		this.mapFeaturesGUI = mapFeaturesGUI;

		setLayout(new MigLayout("fill"));
		//setLayout(new MigLayout(( "debug, inset 20"),"[para]0[][100lp, fill][60lp][95lp, fill]", ""));
		

		// Create the data model for this example
		listData = new Vector();

		// Create a new listbox control
		listbox = new JList(listData);
		listbox.addListSelectionListener(this);

		// Add the listbox to a scrolling pane
		scrollPane = new JScrollPane();
		scrollPane.getViewport().add(listbox);
	
		
		add(scrollPane,"grow");

		// Create a panel to hold all other components
		JPanel controlpanel = new JPanel(new MigLayout());
		// Create some function buttons
		addButton = new JButton("Add");
		controlpanel.add(addButton);
		addButton.addActionListener(this);
		
		removeButton = new JButton("Delete");
		controlpanel.add(removeButton);
		removeButton.addActionListener(this);
		add(controlpanel,"south");
	}

	// Handler for list selection changes
	public void valueChanged(ListSelectionEvent event) {
		// See if this is a listbox selection and the
		// event stream has settled
		if (event.getSource() == listbox && !event.getValueIsAdjusting()) {
			// Get the current selection and place it in the
			// edit field
			String stringValue = (String) listbox.getSelectedValue();
			if (stringValue != null)
				mapFeaturesGUI.setWorkElement(mapFeatures.getElementByName(stringValue));
		}
	}

	// Handler for button presses
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == addButton) {
			String name = JOptionPane.showInputDialog(null, new String("Name"));
			if ((name != null) && (!name.equals("")))
				mapFeaturesGUI.setWorkElement(mapFeatures.addEntry(name));
			updateList();
		}

		if (event.getSource() == removeButton) {
			// Get the current selection
			int selection = listbox.getSelectedIndex();
			if (selection >= 0) {
				// Add this item to the list and refresh
				listData.removeElementAt(selection);
				listbox.setListData(listData);
				scrollPane.revalidate();
				scrollPane.repaint();

				// As a nice touch, select the next item
				if (selection >= listData.size())
					selection = listData.size() - 1;
				listbox.setSelectedIndex(selection);
			}
		}
	}

	public void updateList() {

		listData = new Vector();
		ArrayList<String> names = mapFeatures.getNames();
		for (int i = 0; i < names.size(); i++) {
			String stringValue = names.get(i);
			listData.addElement(stringValue);
		}
		listbox.setListData(listData);
		scrollPane.revalidate();
		scrollPane.repaint();

	}
}

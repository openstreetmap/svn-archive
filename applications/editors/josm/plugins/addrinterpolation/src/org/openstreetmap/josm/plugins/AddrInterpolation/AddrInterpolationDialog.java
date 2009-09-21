// License: GPL. Copyright 2009 by Mike Nice and others

// Main plugin logic
package org.openstreetmap.josm.plugins.AddrInterpolation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;
import org.openstreetmap.josm.tools.UrlLabel;

/**
 * 
 */





public class AddrInterpolationDialog extends ToggleDialog implements ActionListener  {

	private Way selectedStreet = null;
	private Way addrInterpolationWay = null;
	private Relation associatedStreetRelation = null;
	private ArrayList<Node> houseNumberNodes = null;  // Additional nodes with addr:housenumber

	private static String lastCity = "";
	private static String lastState = "";
	private static String lastPostCode = "";
	private static String lastCountry = "";
	private static String lastFullAddress = "";

	// Edit controls
	private EscapeDialog dialog=null;
	private JRadioButton streetNameButton = null;
	private JRadioButton streetRelationButton  = null;
	private JTextField startTextField = null;
	private JTextField endTextField = null;
	private JTextField cityTextField = null;
	private JTextField stateTextField = null;
	private JTextField postCodeTextField = null;
	private JTextField countryTextField = null;
	private JTextField fullTextField = null;

	private boolean relationChanged = false; // Whether to re-trigger data changed for relation


	// NOTE: The following 2 arrays must match in number of elements and position
	String[] addrInterpolationTags = { "odd", "even", "all", "alphabetic" };  // Tag values for map
	String[] addrInterpolationStrings = { tr("Odd"), tr("Even"), tr("All"), tr("Alphabetic") }; // Translatable names for display
	private JComboBox addrInterpolationList = null;


	public AddrInterpolationDialog(String name, String iconName,
			String tooltip, Shortcut shortcut, int preferredHeight) {
		super(name, iconName, tooltip, shortcut, preferredHeight);

		if (!FindAndSaveSelections()) {
			return;
		}

		JPanel editControlsPane = CreateEditControls();

		ShowDialog(editControlsPane, name);

	}



	private void ShowDialog(JPanel editControlsPane, String name) {
		dialog = new EscapeDialog((Frame) Main.parent, name, true);

		dialog.add(editControlsPane);
		dialog.setSize(new Dimension(300,450));
		dialog.setLocation(new Point(100,300));

		// Listen for windowOpened event to set focus
		dialog.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowOpened( WindowEvent e )
			{
				if (addrInterpolationWay != null) {
					addrInterpolationList.requestFocus();
				}
				else {
					cityTextField.requestFocus();
				}
			}
		});

		dialog.setVisible(true);

		lastCity = cityTextField.getText();
		lastState = stateTextField.getText();
		lastPostCode = postCodeTextField.getText();
		lastCountry = countryTextField.getText();
		lastFullAddress = fullTextField.getText();

	}



	// Create edit control items and return JPanel on which they reside
	private JPanel CreateEditControls() {

		JPanel editControlsPane = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		editControlsPane.setLayout(gridbag);

		editControlsPane.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));


		String streetName = selectedStreet.get("name");
		String streetRelation = FindRelation();
		if (streetRelation.equals("")) {
			streetRelation = " (Create new)";
		}
		streetNameButton = new JRadioButton(tr("Name: {0}", streetName));
		streetRelationButton = new JRadioButton(tr("Relation: {0}", streetRelation));
		if (associatedStreetRelation == null) {
			streetNameButton.setSelected(true);
		}else {
			streetRelationButton.setSelected(true);
		}

		// Create edit controls for street / relation radio buttons
		ButtonGroup group = new ButtonGroup();
		group.add(streetNameButton);
		group.add(streetRelationButton);
		JPanel radioButtonPanel = new JPanel(new BorderLayout());
		radioButtonPanel.setBorder(BorderFactory.createTitledBorder(tr("Associate with street using:")));
		radioButtonPanel.add(streetNameButton, BorderLayout.NORTH);
		radioButtonPanel.add(streetRelationButton, BorderLayout.SOUTH);

		// Add to edit panel
		c.gridx = 0;
		c.gridwidth = 2; // # of columns to span
		c.fill = GridBagConstraints.HORIZONTAL;      // Full width
		c.gridwidth = GridBagConstraints.REMAINDER;     //end row
		editControlsPane.add(radioButtonPanel, c);

		JLabel numberingLabel = new JLabel(tr("Numbering Scheme:"));
		addrInterpolationList = new JComboBox(addrInterpolationStrings);

		JLabel startLabel = new JLabel(tr("Starting #:"));
		JLabel endLabel = new JLabel(tr("Ending #:"));

		startTextField = new JTextField(10);
		endTextField = new JTextField(10);




		// Preload any values already set in map
		GetExistingMapKeys();


		JLabel[] textLabels = {numberingLabel, startLabel, endLabel};
		Component[] editFields = {addrInterpolationList, startTextField, endTextField};
		AddEditControlRows(textLabels, editFields,	editControlsPane);

		// Address interpolation fields not valid if Way not selected
		if (addrInterpolationWay == null) {
			addrInterpolationList.setEnabled(false);
			startTextField.setEnabled(false);
			endTextField.setEnabled(false);
		}



		JPanel optionPanel = CreateOptionalFields();
		c.gridx = 0;
		c.gridwidth = 2; // # of columns to span
		c.fill = GridBagConstraints.BOTH;      // Full width
		c.gridwidth = GridBagConstraints.REMAINDER;     //end row

		editControlsPane.add(optionPanel, c);


		KeyAdapter enterProcessor = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					if (ValidateAndSave()) {
						dialog.dispose();
					}

				}
			}
		};

		// Make Enter == OK click on fields using this adapter
		endTextField.addKeyListener(enterProcessor);
		cityTextField.addKeyListener(enterProcessor);



		if (houseNumberNodes.size() > 0) {
			JLabel houseNumberNodeNote = new JLabel(tr("Will associate {0} additional house number nodes",
					houseNumberNodes.size() ));
			editControlsPane.add(houseNumberNodeNote, c);
		}

		editControlsPane.add(new UrlLabel("http://wiki.openstreetmap.org/wiki/JOSM/Plugins/AddrInterpolation",
				tr("More information about this feature")), c);


		c.gridx = 0;
		c.gridwidth = 1; //next-to-last
		c.fill = GridBagConstraints.NONE;      //reset to default
		c.weightx = 0.0;
		c.insets = new Insets(15, 0, 0, 0);
		c.anchor = GridBagConstraints.LINE_END;
		JButton okButton = new JButton(tr("OK"), ImageProvider.get("ok"));
		editControlsPane.add(okButton, c);

		c.gridx = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;     //end row
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.LINE_START;

		JButton cancelButton = new JButton(tr("Cancel"), ImageProvider.get("cancel"));
		editControlsPane.add(cancelButton, c);

		okButton.setActionCommand("ok");
		okButton.addActionListener(this);
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);

		return editControlsPane;
	}



	// Create optional control fields in a group box
	private JPanel CreateOptionalFields() {

		JPanel editControlsPane = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();

		editControlsPane.setLayout(gridbag);

		editControlsPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		JLabel[] optionalTextLabels = {new JLabel(tr("City:")),
				new JLabel(tr("State:")),
				new JLabel(tr("Post Code:")),
				new JLabel(tr("Country:")),
				new JLabel(tr("Full Address:"))};
		cityTextField = new JTextField(lastCity, 100);
		stateTextField = new JTextField(lastState, 100);
		postCodeTextField = new JTextField(lastPostCode, 20);
		countryTextField = new JTextField(lastCountry, 2);
		fullTextField = new JTextField(lastFullAddress, 300);

		// Special processing for addr:country code, max length and uppercase
		countryTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				JTextField jtextfield = (JTextField)e.getSource();
				String text = jtextfield.getText();
				int length = text.length();
				if (length == jtextfield.getColumns()) {
					e.consume();
				} else if (length > jtextfield.getColumns()) {
					// show error message ??
					e.consume();
				} else {
					// Accept key; convert to upper case
					if (!e.isActionKey()) {
						e.setKeyChar(Character.toUpperCase(e.getKeyChar()) );
					}
				}
			}
		});

		Component[] optionalEditFields = {cityTextField, stateTextField, postCodeTextField, countryTextField, fullTextField};
		AddEditControlRows(optionalTextLabels, optionalEditFields,	editControlsPane);



		JPanel optionPanel = new JPanel(new BorderLayout());
		Border groupBox = BorderFactory.createEtchedBorder();
		TitledBorder titleBorder = BorderFactory.createTitledBorder(groupBox, tr("Optional Information:"),
				TitledBorder.LEFT, TitledBorder.TOP);

		optionPanel.setBorder(titleBorder);
		optionPanel.add(editControlsPane, BorderLayout.CENTER);

		return optionPanel;
	}



	// Populate dialog for any possible existing settings if editing an existing Address interpolation way
	private void GetExistingMapKeys() {


		// Check all nodes for optional addressing data
		//    Address interpolation nodes will overwrite these value if they contain optional data
		for (Node node : houseNumberNodes) {
			CheckNodeForAddressTags(node);
		}

		if (addrInterpolationWay != null) {
			String currentMethod = addrInterpolationWay.get("addr:interpolation");
			if (currentMethod != null) {

				int currentIndex = 0;
				// Must scan key values because combo box is already loaded with translated strings
				for (int i=0; i<addrInterpolationTags.length; i++) {
					if (addrInterpolationTags[i].equals(currentMethod)) {
						currentIndex = i;
						break;
					}
				}
				addrInterpolationList.setSelectedIndex(currentIndex);
			}

			Node firstNode = addrInterpolationWay.getNode(0);
			Node lastNode = addrInterpolationWay.getNode(addrInterpolationWay.getNodesCount()-1);

			// Get any existing start / end # values
			String value = firstNode.get("addr:housenumber");
			if (value != null) {
				startTextField.setText(value);
			}

			value = lastNode.get("addr:housenumber");
			if (value != null) {
				endTextField.setText(value);
			}
			CheckNodeForAddressTags(firstNode);
			CheckNodeForAddressTags(lastNode);

		}



	}


	// Check for any existing address data.   If found,
	// overwrite any previous data
	private void CheckNodeForAddressTags(Node checkNode) {

		// Interrogate possible existing optional values
		String value = checkNode.get("addr:city");
		if (value != null) {
			lastCity = value;
		}
		value = checkNode.get("addr:state");
		if (value != null) {
			lastState = value;
		}
		value = checkNode.get("addr:postcode");
		if (value != null) {
			lastPostCode = value;
		}
		value = checkNode.get("addr:country");
		if (value != null) {
			lastCountry = value;
		}
		value = checkNode.get("addr:full");
		if (value != null) {
			lastFullAddress = value;
		}

	}



	// Look for a possible 'associatedStreet' type of relation for selected street
	// Returns relation description, or an empty string
	private String FindRelation() {
		String relationDescription = null;
		DataSet currentDataSet = Main.main.getCurrentDataSet();
		if (currentDataSet != null) {
			for (Relation relation : currentDataSet.relations) {

				String relationType = relation.get("type");
				if (relationType != null) {
					if (relationType.equals("associatedStreet")) {
						for (RelationMember relationMember : relation.getMembers()) {
							if (relationMember.isWay()){
								Way way = (Way) relationMember.getMember();
								// System.out.println("Name: " + way.get("name") );
								if (way == selectedStreet) {
									associatedStreetRelation = relation;
									relationDescription = Long.toString(way.getId());

									String streetName = "";
									if (relation.getKeys().containsKey("name")) {
										streetName =  relation.get("name");
									} else {
										// Relation is unnamed - use street name
										streetName =  selectedStreet.get("name");
									}
									relationDescription += " (" + streetName + ")";
									return relationDescription;
								}
							}

						}
					}

				}
			}

		}

		return "";
	}


	// We can proceed only if there is both a named way (the 'street') and
	// one un-named way (the address interpolation way ) selected.
	//    The plugin menu item is enabled after a single way is selected to display a more meaningful
	//    message (a new user may not realize that they need to select both the street and
	//    address interpolation way first).
	// Also, selected street and working address interpolation ways are saved.
	private boolean FindAndSaveSelections() {

		boolean isValid = false;

		int namedWayCount = 0;
		int unNamedWayCount = 0;
		DataSet currentDataSet = Main.main.getCurrentDataSet();
		if (currentDataSet != null) {
			for (OsmPrimitive osm : currentDataSet.getSelectedWays()) {
				Way way = (Way) osm;
				if (way.getKeys().containsKey("name")) {
					namedWayCount++;
					this.selectedStreet = way;
				}
				else {
					unNamedWayCount++;
					this.addrInterpolationWay = way;
				}
			}

			// Get additional nodes with addr:housenumber tags:
			//   Either selected or in the middle of the Address Interpolation way
			//     Do not include end points of Address Interpolation way in this set yet.
			houseNumberNodes  = new ArrayList<Node>();
			// Check selected nodes
			for (OsmPrimitive osm : currentDataSet.getSelectedNodes()) {
				Node node = (Node) osm;
				if (node.getKeys().containsKey("addr:housenumber")) {
					houseNumberNodes.add(node);
				}
			}

			if (addrInterpolationWay != null) {
				// Check nodes in middle of address interpolation way
				if (addrInterpolationWay.getNodesCount() > 2) {
					for (int i=1; i<(addrInterpolationWay.getNodesCount()-2); i++) {
						Node testNode = addrInterpolationWay.getNode(i);
						if (testNode.getKeys().containsKey("addr:housenumber")) {
							houseNumberNodes.add(testNode);
						}
					}
				}
			}

		}

		if (namedWayCount != 1) {
			JOptionPane.showMessageDialog(
					Main.parent,
					tr("Please select a street to associate with address interpolation way"),
					tr("Error"),
					JOptionPane.ERROR_MESSAGE
			);
		} else {
			// Avoid 2 error dialogs if both conditions don't match
			if (unNamedWayCount != 1) {
				// Allow for street + house number nodes only to be selected (no address interpolation way).
				if (houseNumberNodes.size() > 0) {
					isValid = true;
				} else {
					JOptionPane.showMessageDialog(
							Main.parent,
							tr("Please select address interpolation way for this street"),
							tr("Error"),
							JOptionPane.ERROR_MESSAGE
					);
				}
			} else {
				isValid = true;
			}
		}


		return isValid;
	}


	/**
	 * Add rows of edit controls - with labels in the left column, and controls in the right
	 * column on the gridbag of the specified container.
	 */
	private void AddEditControlRows(JLabel[] labels,
			Component[] editFields,
			Container container) {
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.EAST;
		int numLabels = labels.length;

		for (int i = 0; i < numLabels; i++) {
			c.gridx = 0;
			c.gridwidth = 1; //next-to-last
			c.fill = GridBagConstraints.NONE;      //reset to default
			c.weightx = 0.0;                       //reset to default
			container.add(labels[i], c);

			c.gridx = 1;
			c.gridwidth = GridBagConstraints.REMAINDER;     //end row
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			container.add(editFields[i], c);
		}
	}



	public void actionPerformed(ActionEvent e) {
		if ("ok".equals(e.getActionCommand())) {
			if (ValidateAndSave()) {
				dialog.dispose();
			}

		} else  if ("cancel".equals(e.getActionCommand())) {
			dialog.dispose();

		}
	}


	// Test for valid long int
	private boolean isLong( String input )
	{
		try
		{
			Long.parseLong( input );
			return true;
		}
		catch( Exception e)
		{
			return false;
		}
	}

	private boolean isEven( Long input )
	{
		return ((input %2) == 0);
	}

	private static Pattern p = Pattern.compile("^[0-9]+$");
	private static boolean IsNumeric(String s) {
		return p.matcher(s).matches();
	}


	private boolean ValidateAndSave() {

		String startValueString = ReadTextField(startTextField);
		String endValueString = ReadTextField(endTextField);
		String city = ReadTextField(cityTextField);
		String state = ReadTextField(stateTextField);
		String postCode = ReadTextField(postCodeTextField);
		String country = ReadTextField(countryTextField);
		String fullAddress = ReadTextField(fullTextField);

		String selectedMethod = GetInterpolationMethod();
		if (addrInterpolationWay != null) {
			Long startAddr=0L, endAddr=0L;
			if (!selectedMethod.equals("alphabetic")) {
				Long[] addrArray = {startAddr, endAddr};
				if (!ValidAddressNumbers(startValueString, endValueString, addrArray )) {
					return false;
				}
				startAddr = addrArray[0];
				endAddr = addrArray[1];
			}

			String errorMessage = "";
			if (selectedMethod.equals("odd")) {
				if (isEven(startAddr) || isEven(endAddr)) {
					errorMessage = tr("Expected odd numbers for addresses");
				}

			} else if (selectedMethod.equals("even")) {
				if (!isEven(startAddr) || !isEven(endAddr)) {
					errorMessage = tr("Expected even numbers for addresses");
				}
			} else if (selectedMethod.equals("all")) {

			}else if (selectedMethod.equals("alphabetic")) {
				errorMessage = ValidateAlphaAddress(startValueString, endValueString);
			}
			if (!errorMessage.equals("")) {
				JOptionPane.showMessageDialog(Main.parent, errorMessage, tr("Error"), 	JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}

		if (country != null) {
			if (country.length() != 2) {
				JOptionPane.showMessageDialog(Main.parent,
						tr("Country code must be 2 letters"), tr("Error"), 	JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}

		// Entries are valid ... save in map
		String streetName = selectedStreet.get("name");

		if (addrInterpolationWay != null) {
			addrInterpolationWay.setModified(true);

			Node firstNode = addrInterpolationWay.getNode(0);
			Node lastNode = addrInterpolationWay.getNode(addrInterpolationWay.getNodesCount()-1);

			addrInterpolationWay.put("addr:interpolation", selectedMethod);
			firstNode.put("addr:housenumber", startValueString);
			lastNode.put("addr:housenumber", endValueString);
			if (streetNameButton.isSelected()) {

				firstNode.put("addr:street", streetName);
				lastNode.put("addr:street", streetName);

			}
			// Add address interpolation house number nodes to main house number node list for common processing
			houseNumberNodes.add(firstNode);
			houseNumberNodes.add(lastNode);

			// De-select address interpolation way; leave street selected
			addrInterpolationWay.setSelected(false);
		}



		if (streetRelationButton.isSelected()) {

			// Relation button was selected
			if (associatedStreetRelation == null) {
				CreateRelation(streetName);
				relationChanged = true;
			}


			if (addrInterpolationWay != null) {
				AddToRelation(associatedStreetRelation, addrInterpolationWay, "house");
			}
		}


		// For all nodes, add to relation and
		//   Add optional text fields to all nodes if specified
		for (Node node : houseNumberNodes) {

			node.setModified(true); // Trigger re-upload in case there is a change

			if (streetRelationButton.isSelected()) {
				AddToRelation(associatedStreetRelation, node, "house");
			}
			if ((city != null) || (streetNameButton.isSelected()) ) {
				// Include street unconditionally if adding nodes only or city name specified
				node.put("addr:street", streetName);
			}
			// Set or remove remaining optional fields
			node.put("addr:city", city);
			node.put("addr:state", state);
			node.put("addr:postcode", postCode);
			node.put("addr:country", country);
			node.put("addr:full", fullAddress);
		}

		if (relationChanged) {
			associatedStreetRelation.setModified(true);

			// Redraw relation list dialog
			Main.main.getEditLayer().fireDataChange();
		}


		Main.map.mapView.repaint();

		return true;
	}

	// Create Associated Street relation, add street, and add to map
	private void CreateRelation(String streetName) {
		associatedStreetRelation = new Relation();
		associatedStreetRelation.put("name", streetName);
		associatedStreetRelation.put("type", "associatedStreet");
		RelationMember newStreetMember = new RelationMember("street", selectedStreet);
		associatedStreetRelation.addMember(newStreetMember);
		Main.main.getCurrentDataSet().addPrimitive(associatedStreetRelation);
	}



	// Read from dialog text box, removing leading and trailing spaces
	// Return the string, or null for a zero length string
	private String ReadTextField(JTextField field) {
		String value = field.getText();
		if (value != null) {
			value = value.trim();
			if (value.equals("")) {
				value = null;
			}
		}
		return value;
	}


	// Test if relation contains specified member
	//   If not already present, it is added
	private void AddToRelation(Relation relation, 	OsmPrimitive testMember, String role) {
		boolean isFound = false;
		for (RelationMember relationMember : relation.getMembers()) {

			if (testMember == relationMember.getMember()) {
				isFound = true;
				break;
			}
		}

		if (!isFound) {
			RelationMember newMember = new RelationMember(role, testMember);
			associatedStreetRelation.addMember(newMember);
			relationChanged = true;
		}
	}



	// Check alphabetic style address
	//   Last character of both must be alpha
	//   Last character of ending must be greater than starting
	//   Return empty error message if OK
	private String ValidateAlphaAddress(String startValueString,
			String endValueString) {
		String errorMessage="";

		if (startValueString.equals("") || endValueString.equals("")) {
			errorMessage = tr("Please enter valid number for starting and ending address");
		} else {
			char startingChar = startValueString.charAt(startValueString.length()-1);
			char endingChar = endValueString.charAt(endValueString.length()-1);

			if ( (IsNumeric("" + startingChar)) || (IsNumeric("" + endingChar)) ) {
				errorMessage = tr("Alphabetic address must end with a letter");
			}

			// ?? Character collation in all languages ??
			if (startingChar >= endingChar) {
				errorMessage = tr("Starting address letter must be less than ending address letter");
			}

		}

		return errorMessage;
	}



	// Convert string addresses to numeric, with error check
	private boolean ValidAddressNumbers(String startValueString,
			String endValueString, Long[] addrArray) {
		String errorMessage = "";

		if (!isLong(startValueString)) {
			errorMessage = tr("Please enter valid number for starting address");
		}
		if (!isLong(endValueString)) {
			errorMessage = tr("Please enter valid number for ending address");
		}
		if (errorMessage.equals("")) {
			addrArray[0] = Long.parseLong( startValueString );
			addrArray[1] = Long.parseLong( endValueString );

			if (addrArray[1] <= addrArray[0]) {
				errorMessage = tr("Starting address number must be less than ending address number");
			}
		}

		if (errorMessage.equals("")) {
			return true;

		} else {
			JOptionPane.showMessageDialog(Main.parent, errorMessage, tr("Error"), 	JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}



	private String GetInterpolationMethod() {
		int selectedIndex = addrInterpolationList.getSelectedIndex();
		return addrInterpolationTags[selectedIndex];
	}







}



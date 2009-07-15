package org.openstreetmap.josm.plugins.graphview.plugin.dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openstreetmap.josm.gui.preferences.PreferenceDialog;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.graphview.plugin.dialogs.AccessParameterDialog.BookmarkAction;
import org.openstreetmap.josm.plugins.graphview.plugin.preferences.GraphViewPreferenceDefaults;
import org.openstreetmap.josm.plugins.graphview.plugin.preferences.GraphViewPreferences;
import org.openstreetmap.josm.plugins.graphview.plugin.preferences.PreferenceAccessParameters;
import org.openstreetmap.josm.tools.GBC;

public class GraphViewPreferenceEditor implements PreferenceSetting {

	private File rulesetFolder;
	private Map<String, PreferenceAccessParameters> parameterBookmarks;

	private JPanel preferencePanel;

	private JCheckBox internalRulesetCheckBox;
	private JLabel rulesetFolderLabel;
	private JTextField rulesetFolderTextField;
	private JButton selectRulesetFolderButton;

	private JComboBox bookmarkComboBox;
	private JButton editBookmarkButton;
	private JButton deleteBookmarkButton;

	private JCheckBox separateDirectionsCheckBox;

	public void addGui(PreferenceDialog gui) {

		readPreferences();

		preferencePanel = gui.createPreferenceTab("graphview", "Graphview",
		"Settings for the Graphview plugin that visualizes routing graphs.");

		JPanel mainPanel = createMainPanel();

		preferencePanel.add(mainPanel, GBC.eol().fill(GBC.BOTH));

		updateVehiclePanel(GraphViewPreferences.getInstance().getCurrentParameterBookmarkName());

	}

	/**
	 * creates local versions of preference information
	 * that will only be written to real preferences if the OK button is pressed
	 */
	private void readPreferences() {

		GraphViewPreferences preferences = GraphViewPreferences.getInstance();

		rulesetFolder = preferences.getRulesetFolder();

		parameterBookmarks =
			new HashMap<String, PreferenceAccessParameters>(preferences.getParameterBookmarks());

	}

	private JPanel createMainPanel() {

		JPanel mainPanel = new JPanel();

		GridBagLayout mainLayout = new GridBagLayout();
		mainPanel.setLayout(mainLayout);

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		constraints.gridx = 0;

		{
			JPanel rulesetPanel = createRulesetPanel();
			constraints.gridy = 0;
			mainLayout.setConstraints(rulesetPanel, constraints);
			mainPanel.add(rulesetPanel);
		} {
			JPanel vehiclePanel = createVehiclePanel();
			constraints.gridy = 1;
			mainLayout.setConstraints(vehiclePanel, constraints);
			mainPanel.add(vehiclePanel);
		} {
			JPanel visualizationPanel = createVisualizationPanel();
			constraints.gridy = 2;
			mainLayout.setConstraints(visualizationPanel, constraints);
			mainPanel.add(visualizationPanel);
		}

		mainPanel.add(GBC.glue(0, 0));

		return mainPanel;

	}

	private JPanel createRulesetPanel() {

		JPanel rulesetPanel = new JPanel();
		rulesetPanel.setBorder(BorderFactory.createTitledBorder("ruleset"));
		rulesetPanel.setLayout(new BoxLayout(rulesetPanel, BoxLayout.Y_AXIS));

		internalRulesetCheckBox = new JCheckBox("use built-in rulesets");
		internalRulesetCheckBox.setSelected(GraphViewPreferences.getInstance().getUseInternalRulesets());
		internalRulesetCheckBox.addActionListener(internalRulesetActionListener);
		rulesetPanel.add(internalRulesetCheckBox);

		rulesetFolderLabel = new JLabel("external ruleset directory:");
		rulesetPanel.add(rulesetFolderLabel);

		rulesetFolderTextField = new JTextField();
		rulesetFolderTextField.setText(rulesetFolder.getPath());
		rulesetFolderTextField.setEditable(false);
		rulesetPanel.add(rulesetFolderTextField);

		selectRulesetFolderButton = new JButton("select directory");
		selectRulesetFolderButton.addActionListener(selectRulesetFolderActionListener);
		rulesetPanel.add(selectRulesetFolderButton);

		updateRulesetPanel();

		return rulesetPanel;
	}

	private JPanel createVehiclePanel() {

		JPanel vehiclePanel = new JPanel();
		vehiclePanel.setBorder(BorderFactory.createTitledBorder("vehicle"));
		vehiclePanel.setLayout(new BoxLayout(vehiclePanel, BoxLayout.Y_AXIS));

		bookmarkComboBox = new JComboBox();
		vehiclePanel.add(bookmarkComboBox);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

		JButton createButton = new JButton("create");
		createButton.addActionListener(createVehicleActionListener);
		buttonPanel.add(createButton);

		editBookmarkButton = new JButton("edit");
		editBookmarkButton.addActionListener(editVehicleActionListener);
		buttonPanel.add(editBookmarkButton);

		deleteBookmarkButton = new JButton("delete");
		deleteBookmarkButton.addActionListener(deleteVehicleActionListener);
		buttonPanel.add(deleteBookmarkButton);

		JButton restoreDefaultsButton = new JButton("restore defaults");
		restoreDefaultsButton.addActionListener(restoreVehicleDefaultsActionListener);
		buttonPanel.add(restoreDefaultsButton);

		vehiclePanel.add(buttonPanel);

		return vehiclePanel;
	}

	private JPanel createVisualizationPanel() {

		JPanel visualizationPanel = new JPanel();
		visualizationPanel.setBorder(BorderFactory.createTitledBorder("visualization"));
		visualizationPanel.setLayout(new BoxLayout(visualizationPanel, BoxLayout.Y_AXIS));

		separateDirectionsCheckBox = new JCheckBox("draw directions separately");
		separateDirectionsCheckBox.setSelected(GraphViewPreferences.getInstance().getSeparateDirections());
		visualizationPanel.add(separateDirectionsCheckBox);

		return visualizationPanel;
	}

	public boolean ok() {

		GraphViewPreferences preferences = GraphViewPreferences.getInstance();

		preferences.setUseInternalRulesets(internalRulesetCheckBox.isSelected());
		preferences.setRulesetFolder(rulesetFolder);

		preferences.setParameterBookmarks(parameterBookmarks);

		String selectedBookmarkName = (String)bookmarkComboBox.getSelectedItem();
		preferences.setCurrentParameterBookmarkName(selectedBookmarkName);

		preferences.setSeparateDirections(separateDirectionsCheckBox.isSelected());

		preferences.distributeChanges();

		return false;
	}

	private final ActionListener internalRulesetActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			updateRulesetPanel();
		}
	};

	private final ActionListener selectRulesetFolderActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {

			File initialFCDirectory = rulesetFolder;
			if (rulesetFolder.getParentFile() != null) {
				initialFCDirectory = rulesetFolder.getParentFile();
			}

			final JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fc.setCurrentDirectory(initialFCDirectory);

			int returnVal = fc.showOpenDialog(preferencePanel);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				rulesetFolder = fc.getSelectedFile();
				rulesetFolderTextField.setText(rulesetFolder.getPath());
			}

		}
	};

	private final ActionListener createVehicleActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {

			PreferenceAccessParameters defaultBookmarkParameters =
				GraphViewPreferenceDefaults.createDefaultBookmarkAccessParameters();

			AccessParameterDialog apd = new AccessParameterDialog(
					null,
					false,
					"new bookmark",
					parameterBookmarks.keySet(),
					defaultBookmarkParameters,
					new BookmarkAction() {
						public void execute(String name, PreferenceAccessParameters parameters) {
							parameterBookmarks.put(name, parameters);
							updateVehiclePanel(name);
						}
					});

			apd.setVisible(true);
		}
	};

	private final ActionListener editVehicleActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (bookmarkComboBox.getSelectedItem() != null) {

				final String selectedBookmarkName = (String)bookmarkComboBox.getSelectedItem();
				PreferenceAccessParameters parameters =
					parameterBookmarks.get(selectedBookmarkName);

				Collection<String> otherBookmarkNames = new LinkedList<String>();
				for (String bookmarkName : parameterBookmarks.keySet()) {
					if (!bookmarkName.equals(selectedBookmarkName)) {
						otherBookmarkNames.add(bookmarkName);
					}
				}

				AccessParameterDialog apd = new AccessParameterDialog(
						null,
						true,
						selectedBookmarkName,
						otherBookmarkNames,
						parameters,
						new BookmarkAction() {
							public void execute(String name, PreferenceAccessParameters parameters) {
								parameterBookmarks.remove(selectedBookmarkName);
								parameterBookmarks.put(name, parameters);
								updateVehiclePanel(name);
							}
						});

				apd.setVisible(true);
			}

		}
	};

	private final ActionListener deleteVehicleActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (bookmarkComboBox.getSelectedItem() != null) {

				String selectedBookmarkName = (String)bookmarkComboBox.getSelectedItem();

				int userChoice = JOptionPane.showConfirmDialog(
						preferencePanel,
						"Really delete \"" + selectedBookmarkName + "\"?",
						"Bookmark deletion",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

				if (userChoice == JOptionPane.YES_OPTION) {
					parameterBookmarks.remove(selectedBookmarkName);
					updateVehiclePanel(null);
				}

			}
		}
	};

	private final ActionListener restoreVehicleDefaultsActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {

			int userChoice = JOptionPane.showConfirmDialog(
					preferencePanel,
					"Really restore default bookmarks?\n"
					+ "All manually added or edited bookmarks will be lost!",
					"Bookmark reset",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

			if (userChoice == JOptionPane.YES_OPTION) {
				parameterBookmarks.clear();
				parameterBookmarks.putAll(
						GraphViewPreferenceDefaults.createDefaultAccessParameterBookmarks());
				updateVehiclePanel(null);
			}

		}
	};

	private void updateRulesetPanel() {

		rulesetFolderLabel.setEnabled(!internalRulesetCheckBox.isSelected());
		rulesetFolderTextField.setEnabled(!internalRulesetCheckBox.isSelected());
		selectRulesetFolderButton.setEnabled(!internalRulesetCheckBox.isSelected());

	}

	private void updateVehiclePanel(String selectedBookmarkName) {

		bookmarkComboBox.removeAllItems();
		for (String bookmarkName : parameterBookmarks.keySet()) {
			bookmarkComboBox.addItem(bookmarkName);
		}

		if (selectedBookmarkName == null) {
			if (bookmarkComboBox.getItemCount() > 0) {
				bookmarkComboBox.setSelectedIndex(0);
			}
		} else {
			bookmarkComboBox.setSelectedItem(selectedBookmarkName);
		}

		editBookmarkButton.setEnabled(parameterBookmarks.size() > 0);
		deleteBookmarkButton.setEnabled(parameterBookmarks.size() > 0);

	}

}

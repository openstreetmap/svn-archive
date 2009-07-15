package org.openstreetmap.josm.plugins.graphview.plugin.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.plugins.graphview.core.access.AccessRulesetReader;
import org.openstreetmap.josm.plugins.graphview.core.visualisation.ColorScheme;
import org.openstreetmap.josm.plugins.graphview.core.visualisation.EndNodeColorScheme;
import org.openstreetmap.josm.plugins.graphview.core.visualisation.InclineColorScheme;
import org.openstreetmap.josm.plugins.graphview.core.visualisation.MaxheightColorScheme;
import org.openstreetmap.josm.plugins.graphview.core.visualisation.MaxspeedColorScheme;
import org.openstreetmap.josm.plugins.graphview.core.visualisation.MaxweightColorScheme;
import org.openstreetmap.josm.plugins.graphview.plugin.GraphViewPlugin;
import org.openstreetmap.josm.plugins.graphview.plugin.layer.PreferencesColorScheme;
import org.openstreetmap.josm.plugins.graphview.plugin.preferences.GraphViewPreferences;
import org.openstreetmap.josm.plugins.graphview.plugin.preferences.InternalRuleset;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Dialog for graph view configuration.
 */
public class GraphViewDialog extends ToggleDialog implements Observer {

	private static final int HEIGHT = 150;

	/** map from labels to available color schemes */
	private final LinkedHashMap<String, ColorScheme> availableColorSchemes;


	private final GraphViewPreferences preferences;
	private final GraphViewPlugin plugin;

	private final JComboBox rulesetComboBox;
	private final JComboBox bookmarkComboBox;
	private final JComboBox colorSchemeComboBox;

	/**
	 * list of ruleset files in the order currently used by rulesetComboBox;
	 * null if internal rulesets are used
	 */
	private List<File> rulesetFiles;

	public GraphViewDialog(final GraphViewPlugin plugin) {

		super("Graph View Dialog", "graphview",
				"Open the dialog for graph view configuration.", (Shortcut)null, HEIGHT);

		this.preferences = GraphViewPreferences.getInstance();
		this.plugin = plugin;

		availableColorSchemes = new LinkedHashMap<String, ColorScheme>();

		availableColorSchemes.put("default",
				new PreferencesColorScheme(preferences));
		availableColorSchemes.put("end nodes",
				new EndNodeColorScheme(Color.GRAY, Color.RED, Color.GRAY));
		availableColorSchemes.put("maxspeed",
				new MaxspeedColorScheme());
		availableColorSchemes.put("maxweight",
				new MaxweightColorScheme());
		availableColorSchemes.put("maxheight",
				new MaxheightColorScheme());
		availableColorSchemes.put("incline",
				new InclineColorScheme());

		JPanel selectionPanel = new JPanel();
		GridBagLayout selectionLayout = new GridBagLayout();
		selectionPanel.setLayout(selectionLayout);

		GridBagConstraints gbcLabel = new GridBagConstraints();
		gbcLabel.gridx = 0;
		gbcLabel.anchor = GridBagConstraints.WEST;
		gbcLabel.insets = new Insets(0, 5, 0, 5);

		GridBagConstraints gbcComboBox = new GridBagConstraints();
		gbcComboBox.gridx = 1;
		gbcComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbcComboBox.weightx = 1;


		/* create ruleset label and combo box */
		{
			JLabel rulesetLabel = new JLabel("ruleset:");
			gbcLabel.gridy = 0;
			selectionLayout.setConstraints(rulesetLabel, gbcLabel);
			selectionPanel.add(rulesetLabel);

			rulesetComboBox = new JComboBox();
			rulesetComboBox.addActionListener(rulesetActionListener);
			gbcComboBox.gridy = 0;
			selectionLayout.setConstraints(rulesetComboBox, gbcComboBox);
			selectionPanel.add(rulesetComboBox);
		}

		/* create bookmark label and combo box */
		{
			JLabel bookmarkLabel = new JLabel("parameters:");
			gbcLabel.gridy = 1;
			selectionLayout.setConstraints(bookmarkLabel, gbcLabel);
			selectionPanel.add(bookmarkLabel);

			bookmarkComboBox = new JComboBox();
			bookmarkComboBox.addActionListener(bookmarkActionListener);
			gbcComboBox.gridy = 1;
			selectionLayout.setConstraints(bookmarkComboBox, gbcComboBox);
			selectionPanel.add(bookmarkComboBox);
		}

		/* create color scheme label and combo box */
		{
			JLabel colorSchemeLabel = new JLabel("coloring:");
			gbcLabel.gridy = 2;
			selectionLayout.setConstraints(colorSchemeLabel, gbcLabel);
			selectionPanel.add(colorSchemeLabel);

			colorSchemeComboBox = new JComboBox();
			for (String colorSchemeName : availableColorSchemes.keySet()) {
				colorSchemeComboBox.addItem(colorSchemeName);
				ColorScheme colorScheme = availableColorSchemes.get(colorSchemeName);
				if (colorScheme.getClass().equals(preferences.getCurrentColorScheme().getClass())) {
					colorSchemeComboBox.setSelectedItem(colorSchemeName);
				}
			}
			colorSchemeComboBox.addActionListener(colorSchemeActionListener);
			gbcComboBox.gridy = 2;
			selectionLayout.setConstraints(colorSchemeComboBox, gbcComboBox);
			selectionPanel.add(colorSchemeComboBox);
		}

		this.add(BorderLayout.CENTER, selectionPanel);


		JPanel buttonPanel = new JPanel();
		JButton showLayerButton = new JButton("create/update graph");
		showLayerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				plugin.createGraphViewLayer();
			}
		});
		buttonPanel.add(showLayerButton);

		this.add(BorderLayout.SOUTH, buttonPanel);

		updateSelections();
		this.preferences.addObserver(this);

	}

	private final ActionListener rulesetActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (rulesetComboBox.getSelectedItem() != null) {
				int selectedRulesetIndex = rulesetComboBox.getSelectedIndex();
				if (rulesetFiles != null) {
					File selectedRulesetFile = rulesetFiles.get(selectedRulesetIndex);
					preferences.setCurrentRulesetFile(selectedRulesetFile);
					preferences.distributeChanges();
					plugin.updateGraphViewLayer();
				} else {
					if (selectedRulesetIndex < InternalRuleset.values().length) {
						InternalRuleset selectedIRR = InternalRuleset.values()[selectedRulesetIndex];
						preferences.setCurrentInternalRuleset(selectedIRR);
						preferences.distributeChanges();
						plugin.updateGraphViewLayer();
					}
				}
			}
		}
	};

	private final ActionListener bookmarkActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			String selectedBookmarkName = (String)bookmarkComboBox.getSelectedItem();
			if (selectedBookmarkName != null) {
				preferences.setCurrentParameterBookmarkName(selectedBookmarkName);
				preferences.distributeChanges();
				plugin.updateGraphViewLayer();
			}
		}
	};

	private final ActionListener colorSchemeActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			assert availableColorSchemes.containsKey(colorSchemeComboBox.getSelectedItem());
			String colorSchemeLabel = (String)colorSchemeComboBox.getSelectedItem();
			preferences.setCurrentColorScheme(availableColorSchemes.get(colorSchemeLabel));
			preferences.distributeChanges();
			plugin.repaintGraphViewLayer();
		}
	};

	public void update(Observable observable, Object param) {
		if (observable == preferences) {
			updateSelections();
		}
	}

	protected void updateSelections() {

		/* update rulesets */

		rulesetComboBox.removeActionListener(rulesetActionListener);

		if (preferences.getUseInternalRulesets()) {

			rulesetFiles = null;

			rulesetComboBox.removeAllItems();
			for (int i=0; i < InternalRuleset.values().length; i++) {
				InternalRuleset ruleset = InternalRuleset.values()[i];
				rulesetComboBox.addItem(ruleset.toString());
				if (ruleset == preferences.getCurrentInternalRuleset()) {
					rulesetComboBox.setSelectedIndex(i);
				}
			}

			if (preferences.getCurrentInternalRuleset() == null) {
				rulesetComboBox.addItem("");
				rulesetComboBox.setSelectedIndex(InternalRuleset.values().length);
			}

		} else {

			rulesetFiles = new LinkedList<File>();

			File[] filesInRulesetFolder = preferences.getRulesetFolder().listFiles();

			if (filesInRulesetFolder != null) {
				for (File possibleRulesetFile : filesInRulesetFolder) {
					try {
						AccessRulesetReader.readAccessRuleset(new FileInputStream(possibleRulesetFile));
						rulesetFiles.add(possibleRulesetFile);
					} catch (IOException ioe) {
						//don't add to rulesetFiles
					}
				}
			}

			Collections.sort(rulesetFiles);

			rulesetComboBox.removeAllItems();
			for (int i=0; i < rulesetFiles.size(); i++) {
				File rulesetFile = rulesetFiles.get(i);
				rulesetComboBox.addItem(rulesetFile.getName());
				if (rulesetFile.equals(preferences.getCurrentRulesetFile())) {
					rulesetComboBox.setSelectedIndex(i);
				}
			}

		}

		rulesetComboBox.addActionListener(rulesetActionListener);

		/* update bookmarks */

		bookmarkComboBox.removeActionListener(bookmarkActionListener);

		String activeBookmarkName = preferences.getCurrentParameterBookmarkName();
		Set<String> bookmarkNames = new HashSet<String>(preferences.getParameterBookmarks().keySet());

		bookmarkComboBox.removeAllItems();
		for (String bookmarkName : bookmarkNames) {
			bookmarkComboBox.addItem(bookmarkName);
			if (bookmarkName.equals(activeBookmarkName)) {
				bookmarkComboBox.setSelectedItem(bookmarkName);
			}
		}

		bookmarkComboBox.addActionListener(bookmarkActionListener);

	}

}

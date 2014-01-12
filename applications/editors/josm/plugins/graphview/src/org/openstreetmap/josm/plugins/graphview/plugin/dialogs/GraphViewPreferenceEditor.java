package org.openstreetmap.josm.plugins.graphview.plugin.dialogs;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.plugins.graphview.plugin.dialogs.AccessParameterDialog.BookmarkAction;
import org.openstreetmap.josm.plugins.graphview.plugin.layer.GraphViewLayer;
import org.openstreetmap.josm.plugins.graphview.plugin.preferences.GraphViewPreferenceDefaults;
import org.openstreetmap.josm.plugins.graphview.plugin.preferences.GraphViewPreferences;
import org.openstreetmap.josm.plugins.graphview.plugin.preferences.PreferenceAccessParameters;
import org.openstreetmap.josm.tools.GBC;

public class GraphViewPreferenceEditor extends DefaultTabPreferenceSetting {

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
    private JButton segmentColorButton;
    private JPanel segmentColorField;
    private JButton nodeColorButton;
    private JPanel nodeColorField;
    private JButton arrowheadFillColorButton;
    private JPanel arrowheadFillColorField;
    private JSlider arrowheadPlacementSlider;
    private JPanel arrowPreviewPanel;

    public GraphViewPreferenceEditor() {
        super("graphview", tr("Graphview"),
                tr("Settings for the Graphview plugin that visualizes routing graphs."));
    }

    public void addGui(PreferenceTabbedPane gui) {

        readPreferences();

        preferencePanel = gui.createPreferenceTab(this);

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
        rulesetPanel.setBorder(BorderFactory.createTitledBorder(tr("Ruleset")));
        rulesetPanel.setLayout(new BoxLayout(rulesetPanel, BoxLayout.Y_AXIS));

        internalRulesetCheckBox = new JCheckBox(tr("Use built-in rulesets"));
        internalRulesetCheckBox.setSelected(GraphViewPreferences.getInstance().getUseInternalRulesets());
        internalRulesetCheckBox.addActionListener(internalRulesetActionListener);
        rulesetPanel.add(internalRulesetCheckBox);

        rulesetFolderLabel = new JLabel(tr("External ruleset directory:"));
        rulesetPanel.add(rulesetFolderLabel);

        rulesetFolderTextField = new JTextField();
        rulesetFolderTextField.setText(rulesetFolder.getPath());
        rulesetFolderTextField.setEditable(false);
        rulesetPanel.add(rulesetFolderTextField);

        selectRulesetFolderButton = new JButton(tr("Select directory"));
        selectRulesetFolderButton.addActionListener(selectRulesetFolderActionListener);
        rulesetPanel.add(selectRulesetFolderButton);

        updateRulesetPanel();

        return rulesetPanel;
    }

    private JPanel createVehiclePanel() {

        JPanel vehiclePanel = new JPanel();
        vehiclePanel.setBorder(BorderFactory.createTitledBorder(tr("Vehicle")));
        vehiclePanel.setLayout(new BoxLayout(vehiclePanel, BoxLayout.Y_AXIS));

        bookmarkComboBox = new JComboBox();
        vehiclePanel.add(bookmarkComboBox);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        JButton createButton = new JButton(tr("Create"));
        createButton.addActionListener(createVehicleActionListener);
        buttonPanel.add(createButton);

        editBookmarkButton = new JButton(tr("Edit"));
        editBookmarkButton.addActionListener(editVehicleActionListener);
        buttonPanel.add(editBookmarkButton);

        deleteBookmarkButton = new JButton(tr("Delete"));
        deleteBookmarkButton.addActionListener(deleteVehicleActionListener);
        buttonPanel.add(deleteBookmarkButton);

        JButton restoreDefaultsButton = new JButton(tr("Restore defaults"));
        restoreDefaultsButton.addActionListener(restoreVehicleDefaultsActionListener);
        buttonPanel.add(restoreDefaultsButton);

        vehiclePanel.add(buttonPanel);

        return vehiclePanel;
    }

    private JPanel createVisualizationPanel() {

    	JPanel visualizationPanel = new JPanel();
    	visualizationPanel.setBorder(BorderFactory.createTitledBorder(tr("Visualization")));
    	visualizationPanel.setLayout(new BoxLayout(visualizationPanel, BoxLayout.Y_AXIS));

    	separateDirectionsCheckBox = new JCheckBox(tr("Draw directions separately"));
    	separateDirectionsCheckBox.setSelected(GraphViewPreferences.getInstance().getSeparateDirections());
    	visualizationPanel.add(separateDirectionsCheckBox);

    	{ // create color chooser panel

    		JPanel colorPanel = new JPanel();
    		colorPanel.setLayout(new GridLayout(3, 2));

    		Color nodeColor = GraphViewPreferences.getInstance().getNodeColor();

    		nodeColorButton = new JButton(tr("Node color"));
    		nodeColorButton.addActionListener(chooseNodeColorActionListener);
    		colorPanel.add(nodeColorButton);
    		nodeColorField = new JPanel();
    		nodeColorField.setBackground(nodeColor);
    		colorPanel.add(nodeColorField);

    		Color segmentColor = GraphViewPreferences.getInstance().getSegmentColor();

    		segmentColorButton = new JButton(tr("Arrow color"));
    		segmentColorButton.addActionListener(chooseSegmentColorActionListener);
    		colorPanel.add(segmentColorButton);
    		segmentColorField = new JPanel();
    		segmentColorField.setBackground(segmentColor);
    		colorPanel.add(segmentColorField);

    		Color arrowheadFillColor = GraphViewPreferences.getInstance().getArrowheadFillColor();

    		arrowheadFillColorButton = new JButton(tr("Arrowhead fill color"));
    		arrowheadFillColorButton.addActionListener(chooseArrowheadFillColorActionListener);
    		colorPanel.add(arrowheadFillColorButton);
    		arrowheadFillColorField = new JPanel();
    		arrowheadFillColorField.setBackground(arrowheadFillColor);
    		colorPanel.add(arrowheadFillColorField);

    		visualizationPanel.add(colorPanel);

    	}

    	arrowheadPlacementSlider = new JSlider(0, 100);
    	arrowheadPlacementSlider.setToolTipText(tr("Arrowhead placement"));
    	arrowheadPlacementSlider.setMajorTickSpacing(10);
    	arrowheadPlacementSlider.setPaintTicks(true);
    	arrowheadPlacementSlider.setName("name");
    	arrowheadPlacementSlider.setLabelTable(null);
    	arrowheadPlacementSlider.setValue((int)Math.round(
    			100 * GraphViewPreferences.getInstance().getArrowheadPlacement()));
    	arrowheadPlacementSlider.addChangeListener(arrowheadPlacementChangeListener);
    	visualizationPanel.add(arrowheadPlacementSlider);

    	arrowPreviewPanel = new ArrowPreviewPanel();
    	visualizationPanel.add(arrowPreviewPanel);

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

        preferences.setNodeColor(nodeColorField.getBackground());
        preferences.setSegmentColor(segmentColorField.getBackground());
        preferences.setArrowheadFillColor(arrowheadFillColorField.getBackground());

        preferences.setArrowheadPlacement(
        		arrowheadPlacementSlider.getValue() / 100f);

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
                    tr("New bookmark"),
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
                        tr("Really delete \"{0}\"?", selectedBookmarkName),
                        tr("Bookmark deletion"),
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
                    tr("Really restore default bookmarks?\n"
                    + "All manually added or edited bookmarks will be lost!"),
                    tr("Bookmark reset"),
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (userChoice == JOptionPane.YES_OPTION) {
                parameterBookmarks.clear();
                parameterBookmarks.putAll(
                        GraphViewPreferenceDefaults.createDefaultAccessParameterBookmarks());
                updateVehiclePanel(null);
            }

        }
    };

    private final ActionListener chooseNodeColorActionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {

        	Color selectedColor = JColorChooser.showDialog(
        			preferencePanel, tr("Choose node color"), nodeColorField.getBackground());

        	if (selectedColor != null) {
        		nodeColorField.setBackground(selectedColor);
        	}

        	arrowPreviewPanel.repaint();

        }
    };

    private final ActionListener chooseSegmentColorActionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {

        	Color selectedColor = JColorChooser.showDialog(
        			preferencePanel, tr("Choose arrow color"), segmentColorField.getBackground());

        	if (selectedColor != null) {
        		segmentColorField.setBackground(selectedColor);
        	}

        	arrowPreviewPanel.repaint();

        }
    };

    private final ActionListener chooseArrowheadFillColorActionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {

        	Color selectedColor = JColorChooser.showDialog(
        			preferencePanel, tr("Choose arrowhead fill color"), segmentColorField.getBackground());

        	if (selectedColor != null) {
        		arrowheadFillColorField.setBackground(selectedColor);
        	}

        	arrowPreviewPanel.repaint();

        }
    };

    private final ChangeListener arrowheadPlacementChangeListener = new ChangeListener() {
    	public void stateChanged(ChangeEvent e) {
        	arrowPreviewPanel.repaint();
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

    private class ArrowPreviewPanel extends JPanel {

    	public ArrowPreviewPanel() {
    		setPreferredSize(new Dimension(100, 50));
			setBackground(Color.DARK_GRAY);
		}

    	@Override
    	public void paint(Graphics g) {

    		super.paint(g);

    		Graphics2D g2D = (Graphics2D)g;

    		Point p1 = new Point(15, this.getHeight() / 2);
    		Point p2 = new Point(this.getWidth()-15, this.getHeight() / 2);

    		g2D.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    		g2D.setColor(segmentColorField.getBackground());
    		g2D.draw(new Line2D.Float(p1.x, p1.y, p2.x, p2.y));

    		GraphViewLayer.paintNode(g, p1, nodeColorField.getBackground());
    		GraphViewLayer.paintNode(g, p2, nodeColorField.getBackground());

    		GraphViewLayer.paintArrowhead(g2D, p1, p2,
    				arrowheadPlacementSlider.getValue() / 100.0,
    				segmentColorField.getBackground(),
    				arrowheadFillColorField.getBackground());

    	}

    }

}

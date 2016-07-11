/*
 * Indoorhelper is a JOSM plug-in to support users when creating their own indoor maps.
 *  Copyright (C) 2016  Erik Gruschka
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package views;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.TextField;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.widgets.DisableShortcutsOnFocusGainedTextField;
import org.openstreetmap.josm.gui.widgets.JosmComboBox;

import model.IndoorLevel;
import model.TagCatalog;
import model.TagCatalog.IndoorObject;

/**
 *
 * This is the main toolbox of the indoorhelper plug-in.
 *
 * @author egru
 *
 */
@SuppressWarnings("serial")
public class ToolBoxView extends ToggleDialog {
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JToggleButton powerButton;
    private JLabel levelLabel;
    private JosmComboBox<String> levelBox;
    private JLabel levelTagLabel;
    private DisableShortcutsOnFocusGainedTextField levelTagField;
    private JLabel objectLabel;
    private JosmComboBox<TagCatalog.IndoorObject> objectBox;
    private JLabel nameLabel;
    private DisableShortcutsOnFocusGainedTextField nameField;
    private JLabel refLabel;
    private DisableShortcutsOnFocusGainedTextField refField;
    private JPanel buttonBar;
    private JButton applyButton;
    private JSeparator separator1;
    private JSeparator separator2;
    private PresetButton preset1;
    private PresetButton preset2;
    private PresetButton preset3;
    private PresetButton preset4;

    public ToolBoxView() {
        super(tr("Indoor Mapping Helper"), "indoorhelper",
                tr("Toolbox for indoor mapping assistance"), null, 300, true);

        initComponents();
    }

    /**
     * Creates the layout of the plug-in.
     */
    private void initComponents() {
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        powerButton = new JToggleButton();
        levelLabel = new JLabel();
        levelBox = new JosmComboBox<String>();
        levelTagLabel = new JLabel();
        levelTagField = new DisableShortcutsOnFocusGainedTextField();
        objectLabel = new JLabel();
        objectBox = new JosmComboBox<>(TagCatalog.IndoorObject.values());
        nameLabel = new JLabel();
        nameField = new DisableShortcutsOnFocusGainedTextField();
        refLabel = new JLabel();
        refField = new DisableShortcutsOnFocusGainedTextField();
        buttonBar = new JPanel();
        applyButton = new JButton();
        separator1 = new JSeparator();
        separator2 = new JSeparator();
        preset1 = new PresetButton(IndoorObject.ROOM);
        preset2 = new PresetButton(IndoorObject.SHELL);
        preset3 = new PresetButton(IndoorObject.CONCRETE_WALL);
        preset4 = new PresetButton(IndoorObject.GLASS_WALL);

        //======== this ========
        //Container contentPane = this.get;
        //contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setLayout(new GridBagLayout());
                ((GridBagLayout) contentPanel.getLayout()).columnWidths = new int[] {
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                ((GridBagLayout) contentPanel.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
                ((GridBagLayout) contentPanel.getLayout()).columnWeights = new double[] {
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
                ((GridBagLayout) contentPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

                //---- powerButton ----
                powerButton.setText(tr("POWER"));
                powerButton.setToolTipText(tr("Activates the plug-in"));
                contentPanel.add(powerButton, new GridBagConstraints(8, 0, 4, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));
                contentPanel.add(separator1, new GridBagConstraints(1, 1, 12, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- levelLabel ----
                levelLabel.setText(tr("Working Level"));
                contentPanel.add(levelLabel, new GridBagConstraints(1, 2, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- levelBox ----
                levelBox.setEnabled(false);
                levelBox.setEditable(false);
                levelBox.setToolTipText(tr("Selects the working level."));
                contentPanel.add(levelBox, new GridBagConstraints(3, 2, 3, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- levelTagLabel ----
                levelTagLabel.setText(tr("Level Name"));
                contentPanel.add(levelTagLabel, new GridBagConstraints(7, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- levelTagField ----
                levelTagField.setEnabled(false);
                levelTagField.setColumns(6);
                levelTagField.setToolTipText(tr("Optional name-tag for a level."));
                contentPanel.add(levelTagField, new GridBagConstraints(8, 2, 5, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));
                contentPanel.add(separator2, new GridBagConstraints(1, 3, 12, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- objectLabel ----
                objectLabel.setText(tr("Object"));
                contentPanel.add(objectLabel, new GridBagConstraints(0, 4, 3, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- objectBox ----
                objectBox.setEnabled(false);
                objectBox.setPrototypeDisplayValue(IndoorObject.CONCRETE_WALL);
                objectBox.setToolTipText(tr("The object preset you want to tag."));
                contentPanel.add(objectBox, new GridBagConstraints(3, 4, 3, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- nameLabel ----
                nameLabel.setText(tr("Name"));
                contentPanel.add(nameLabel, new GridBagConstraints(0, 5, 3, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- nameField ----
                nameField.setEnabled(false);
                nameField.addFocusListener(new FocusListener() {

                    @Override
                    public void focusLost(FocusEvent e) {}

                    @Override
                    public void focusGained(FocusEvent e) {
                        nameField.selectAll();
                    }
                });
                nameField.setToolTipText(tr("Sets the name tag when the room-object is selected."));
                contentPanel.add(nameField, new GridBagConstraints(3, 5, 3, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- refLabel ----
                refLabel.setText(tr("Reference"));
                contentPanel.add(refLabel, new GridBagConstraints(0, 6, 3, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- refField ----
                refField.setEnabled(false);
                refField.addFocusListener(new FocusListener() {

                    @Override
                    public void focusLost(FocusEvent e) {}

                    @Override
                    public void focusGained(FocusEvent e) {
                        refField.selectAll();
                    }
                });
                refField.setToolTipText(tr("Sets the ref tag when the room-object is selected."));
                contentPanel.add(refField, new GridBagConstraints(3, 6, 3, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- preset1 ----
                preset1.setEnabled(false);
                contentPanel.add(preset1, new GridBagConstraints(16, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));
                contentPanel.add(separator2, new GridBagConstraints(1, 3, 13, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- preset2 ----
                preset2.setEnabled(false);
                contentPanel.add(preset2, new GridBagConstraints(16, 3, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- preset3 ----
                preset3.setEnabled(false);
                contentPanel.add(preset3, new GridBagConstraints(16, 4, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- preset4 ----
                preset4.setEnabled(false);
                contentPanel.add(preset4, new GridBagConstraints(16, 5, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                buttonBar.setLayout(new GridBagLayout());
                ((GridBagLayout) buttonBar.getLayout()).columnWidths = new int[] {0, 80};
                ((GridBagLayout) buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0};

                //---- applyButton ----
                applyButton.setText(tr("Apply Tags"));
                applyButton.setEnabled(false);
                buttonBar.add(applyButton, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);
        }
        //contentPane.add(dialogPane, BorderLayout.CENTER);


        this.createLayout(dialogPane, false, null);
    }

    /**
     * Returns the state of the power button.
     *
     * @return boolean which is true when the button is selected
     */
    public boolean getPowerButtonState() {
        return this.powerButton.isSelected();
    }

    /**
     * Enables or disables the interactive UI elements of the toolbox.
     *
     * @param enabled set this true for enabled elements
     */
    public void setAllUiElementsEnabled(boolean enabled) {
        this.applyButton.setEnabled(enabled);
        this.levelBox.setEnabled(enabled);
        this.objectBox.setEnabled(enabled);
        this.nameField.setEnabled(enabled);
        this.refField.setEnabled(enabled);
        this.levelTagField.setEnabled(enabled);
        this.preset1.setEnabled(enabled);
        this.preset2.setEnabled(enabled);
        this.preset3.setEnabled(enabled);
        this.preset4.setEnabled(enabled);


        if (enabled == false) {
            resetUiElements();
            this.levelTagField.setText("");
        }
    }

    /**
     * Enables or disables the interactive text box elements name and ref.
     *
     * @param enabled set this true for enabled elements
     */
    public void setTagUiElementsEnabled(boolean enabled) {
        this.nameField.setEnabled(enabled);
        this.refField.setEnabled(enabled);

        if (enabled == false) resetUiElements();
    }

    /**
     * Disables the power-button of the plug-in.
     */
    public void setPowerButtonDisabled() {
        this.powerButton.setSelected(false);
    }

    /**
     * Getter for the selected {@link IndoorObject} in the objectBox.
     *
     * @return the selected indoor object in the object ComboBox.
     */
    public IndoorObject getSelectedObject() {
        return (IndoorObject) this.objectBox.getSelectedItem();
    }


    /**
     * Sets the level list for the level selection comboBox.
     *
     * @param levelList the list of levels which you want to set
     */
    public void setLevelList(List<IndoorLevel> levelList) {
        this.levelBox.removeAllItems();

        ListIterator<IndoorLevel> listIterator = levelList.listIterator();

        while (listIterator.hasNext()) {
            IndoorLevel level = listIterator.next();
            if (level.hasEmptyName()) {
                this.levelBox.addItem(Integer.toString(level.getLevelNumber()));
            } else {
                this.levelBox.addItem(level.getName());
            }
        }
    }

    /**
     * Getter for the selected working level.
     *
     * @return the index of the selected item in the level-box
     */
    public int getSelectedLevelIndex() {
        return this.levelBox.getSelectedIndex();
    }

    /**
     * Checks if the level list is empty.
     *
     * @return boolean which is true if the level-list is empty
     */
    public boolean levelListIsEmpty() {
        if (this.levelBox.getItemCount() == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Getter for the level-name-field.
     *
     * @return the {@link String} of the levelTagField
     */
    public String getLevelName() {
        return this.levelTagField.getText();
    }

    /**
     * Setter for the level name field.
     *
     * @param name the String for the levelTagField
     */
    public void setLevelName(String name) {
        this.levelTagField.setText(name);
    }

    /**
     * Getter for the name {@link TextField}.
     *
     * @return {@link String} of the name text field
     */
    public String getNameText() {
        return this.nameField.getText();
    }

    /**
     * Getter for the ref {@link TextField}.
     *
     * @return {@link String} of the ref text field
     */
    public String getRefText() {
        return this.refField.getText();
    }

    /**
     * Resets the view by making the UI elements disabled and deleting the level list.
     */
    public void reset() {
        this.setAllUiElementsEnabled(false);
        this.levelBox.removeAllItems();
    }

    /**
     * Clears the text boxes and sets an empty {@link String}.
     */
    public void resetUiElements() {
        this.nameField.setText("");
        this.refField.setText("");
    }

    /*
     * ********************************
     * SETTERS FOR THE BUTTON LISTENERS
     * ********************************
     */

    /**
     * Set the listener for the power button.
     *
     * @param l the listener to set
     */
    public void setPowerButtonListener(ActionListener l) {
        this.powerButton.addActionListener(l);
    }

    /**
     * Set the listener for the apply button.
     *
     * @param l the listener to set
     */
    public void setApplyButtonListener(ActionListener l) {
        this.applyButton.addActionListener(l);
    }

    /**
     * Set the listener which is called when a new item in the level list is selected.
     *
     * @param l the listener to set
     */
    public void setLevelItemListener(ItemListener l) {
        this.levelBox.addItemListener(l);
    }


    /**
     * Set the listener which is called when a new item in the object list is selected.
     *
     * @param l the listener to set
     */
    public void setObjectItemListener(ItemListener l) {
        this.objectBox.addItemListener(l);
    }

    // Preset Button Functions

    public void setPresetButtons(List<IndoorObject> objects) {
        this.preset1.setIndoorObject(objects.get(0));
        this.preset2.setIndoorObject(objects.get(1));
        this.preset3.setIndoorObject(objects.get(2));
        this.preset4.setIndoorObject(objects.get(3));
    }

    public void setPreset1Listener(ActionListener l) {
        this.preset1.addActionListener(l);
    }

    public void setPreset2Listener(ActionListener l) {
        this.preset2.addActionListener(l);
    }

    public void setPreset3Listener(ActionListener l) {
        this.preset3.addActionListener(l);
    }

    public void setPreset4Listener(ActionListener l) {
        this.preset4.addActionListener(l);
    }

    public IndoorObject getPreset1() {
        return preset1.getIndoorObject();
    }

    public IndoorObject getPreset2() {
        return preset2.getIndoorObject();
    }

    public IndoorObject getPreset3() {
        return preset3.getIndoorObject();
    }

    public IndoorObject getPreset4() {
        return preset4.getIndoorObject();
    }
}

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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

import org.openstreetmap.josm.gui.widgets.DisableShortcutsOnFocusGainedTextField;
import org.openstreetmap.josm.gui.widgets.JosmComboBox;

import model.TagCatalog;
import model.TagCatalog.IndoorObject;

@SuppressWarnings("serial")
public class DialogPanel extends JPanel {
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

    /**
     * Create the panel.
     */
    public DialogPanel() {
        contentPanel = new JPanel();
        powerButton = new JToggleButton();
        levelLabel = new JLabel();
        levelBox = new JosmComboBox<String>();
        levelTagLabel = new JLabel();
        levelTagField = new DisableShortcutsOnFocusGainedTextField();
        objectLabel = new JLabel();
        objectBox = new JosmComboBox<>();
        objectBox.setModel(new DefaultComboBoxModel<>(TagCatalog.IndoorObject.values()));
        nameLabel = new JLabel();
        nameField = new DisableShortcutsOnFocusGainedTextField();
        refLabel = new JLabel();
        refField = new DisableShortcutsOnFocusGainedTextField();
        buttonBar = new JPanel();
        applyButton = new JButton();
        separator1 = new JSeparator();
        separator2 = new JSeparator();

        //======== this ========
        //Container contentPane = this.get;
        //contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            this.setBorder(new EmptyBorder(12, 12, 12, 12));
            this.setLayout(new BorderLayout());

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
            }
            this.add(contentPanel, BorderLayout.CENTER);

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
            this.add(buttonBar, BorderLayout.SOUTH);
        }
    }

}

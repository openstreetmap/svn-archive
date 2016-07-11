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
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.border.EmptyBorder;

/**
 * Class for the pop-up window which provides an level selector to get a user input.
 * In this window the user declares the lowest and the highest level of the building he wants to map.
 *
 * @author egru
 *
 */

@SuppressWarnings("serial")
public class LevelSelectorView extends JFrame {

    private JPanel dialogPane;
    private JPanel contentPanel;
    private JLabel minLabel;
    private JSpinner minSpinner;
    private JLabel maxLabel;
    private JSpinner maxSpinner;
    private JPanel buttonBar;
    private JButton okButton;
    private JButton cancelButton;

    public LevelSelectorView() {
        initComponents();
    }

    private void initComponents() {
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        minLabel = new JLabel();
        minSpinner = new JSpinner();
        maxLabel = new JLabel();
        maxSpinner = new JSpinner();
        buttonBar = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();

        //======== this ========
        setTitle(tr("Level Selection"));
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setLayout(new GridBagLayout());
                ((GridBagLayout) contentPanel.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0};
                ((GridBagLayout) contentPanel.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0};
                ((GridBagLayout) contentPanel.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};
                ((GridBagLayout) contentPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

                //---- minLabel ----
                minLabel.setText(tr("Lowest Level"));
                contentPanel.add(maxLabel, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));
                JSpinner.DefaultEditor minEditor = (DefaultEditor) maxSpinner.getEditor();
                minEditor.getTextField().setColumns(2);
                maxSpinner.setToolTipText(tr("The lowest level of your building."));
                contentPanel.add(maxSpinner, new GridBagConstraints(2, 0, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //---- maxLabel ----
                maxLabel.setText(tr("Highest Level"));
                contentPanel.add(minLabel, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));
                JSpinner.DefaultEditor maxEditor = (DefaultEditor) minSpinner.getEditor();
                maxEditor.getTextField().setColumns(2);
                minSpinner.setToolTipText(tr("The highest level of your building."));
                contentPanel.add(minSpinner, new GridBagConstraints(2, 2, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                buttonBar.setLayout(new GridBagLayout());
                ((GridBagLayout) buttonBar.getLayout()).columnWidths = new int[] {0, 85, 80};
                ((GridBagLayout) buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0};

                //---- okButton ----
                okButton.setText(tr("OK"));
                buttonBar.add(okButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- cancelButton ----
                cancelButton.setText(tr("Cancel"));
                buttonBar.add(cancelButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                buttonBar.setLayout(new GridBagLayout());
                ((GridBagLayout) buttonBar.getLayout()).columnWidths = new int[] {0, 85, 80};
                ((GridBagLayout) buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0};

                //---- okButton ----
                okButton.setText(tr("OK"));
                buttonBar.add(okButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- cancelButton ----
                cancelButton.setText(tr("Cancel"));
                buttonBar.add(cancelButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());

    }

    /**
     * Set the listener for the OK button.
     *
     * @param l the listener to set
     */
    public void setOkButtonListener(ActionListener l) {
        this.okButton.addActionListener(l);
    }

    /**
     * Set the listener for the cancel button.
     *
     * @param l the listener to set
     */
    public void setCancelButtonListener(ActionListener l) {
        this.cancelButton.addActionListener(l);
    }

    /**
     * Getter for the lowest level.
     *
     * @return Integer which represents the lowest level of the building.
     */
    public int getMin() {
        return (int) this.minSpinner.getValue();
    }

    /**
     * Getter for the highest level.
     *
     * @return Integer which represents the highest level of the building.
     */
    public int getMax() {
        return (int) this.maxSpinner.getValue();
    }
}

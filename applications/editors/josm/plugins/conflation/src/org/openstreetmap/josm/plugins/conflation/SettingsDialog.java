package org.openstreetmap.josm.plugins.conflation;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.*;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Dialog for selecting objects and configuring conflation settings
 */
public class SettingsDialog extends ExtendedDialog {

    private JPanel costsPanel;
    private JCheckBox distanceCheckBox;
    private JButton freezeReferenceButton;
    private JButton freezeSubjectButton;
    private JPanel jPanel3;
    private JPanel jPanel5;
    private JButton restoreReferenceButton;
    private JButton restoreSubjectButton;
    private JLabel referenceLayerLabel;
    private JPanel referencePanel;
    private JLabel referenceSelectionLabel;
    private JLabel subjectLayerLabel;
    private JPanel subjectPanel;
    private JLabel subjectSelectionLabel;
    ArrayList<OsmPrimitive> subjectSelection = null;
    ArrayList<OsmPrimitive> referenceSelection = null;
    OsmDataLayer referenceLayer;
    DataSet subjectDataSet;
    OsmDataLayer subjectLayer;
    DataSet referenceDataSet;

    public SettingsDialog() {
        super(Main.parent,
                tr("Configure conflation settings"),
                new String[]{tr("Conflate"), tr("Cancel")},
                false);
        initComponents();
    }

    /**
     * Build GUI components
     */
    private void initComponents() {
        referencePanel = new JPanel();
        referenceLayerLabel = new JLabel();
        referenceSelectionLabel = new JLabel();
        jPanel3 = new JPanel();
        restoreReferenceButton = new JButton(new RestoreReferenceAction());
        freezeReferenceButton = new JButton(new FreezeReferenceAction());
        subjectPanel = new JPanel();
        subjectLayerLabel = new JLabel();
        subjectSelectionLabel = new JLabel();
        jPanel5 = new JPanel();
        restoreSubjectButton = new JButton(new RestoreSubjectAction());
        freezeSubjectButton = new JButton(new FreezeSubjectAction());
        costsPanel = new JPanel();
        distanceCheckBox = new JCheckBox();
        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.PAGE_AXIS));
        referencePanel.setBorder(BorderFactory.createTitledBorder(tr("Reference")));
        referencePanel.setLayout(new BoxLayout(referencePanel, BoxLayout.PAGE_AXIS));
        referenceLayerLabel.setText("(none)");
        referencePanel.add(referenceLayerLabel);
        referenceSelectionLabel.setText("Rel.:0 / Ways:0 / Nodes: 0");
        referencePanel.add(referenceSelectionLabel);
        jPanel3.setLayout(new BoxLayout(jPanel3, BoxLayout.LINE_AXIS));
        restoreReferenceButton.setText(tr("Restore"));
        jPanel3.add(restoreReferenceButton);
        jPanel3.add(freezeReferenceButton);
        referencePanel.add(jPanel3);
        pnl.add(referencePanel);
        subjectPanel.setBorder(BorderFactory.createTitledBorder(tr("Subject")));
        subjectPanel.setLayout(new BoxLayout(subjectPanel, BoxLayout.PAGE_AXIS));
        subjectLayerLabel.setText("(none)");
        subjectPanel.add(subjectLayerLabel);
        subjectSelectionLabel.setText("Rel.:0 / Ways:0 / Nodes: 0");
        subjectPanel.add(subjectSelectionLabel);
        jPanel5.setLayout(new BoxLayout(jPanel5, BoxLayout.LINE_AXIS));
        restoreSubjectButton.setText(tr("Restore"));
        jPanel5.add(restoreSubjectButton);
        freezeSubjectButton.setText(tr("Freeze"));
        jPanel5.add(freezeSubjectButton);
        subjectPanel.add(jPanel5);
        pnl.add(subjectPanel);
        costsPanel.setBorder(BorderFactory.createTitledBorder(tr("Costs")));
        costsPanel.setLayout(new BoxLayout(costsPanel, BoxLayout.LINE_AXIS));
        distanceCheckBox.setSelected(true);
        distanceCheckBox.setText(tr("Distance"));
        distanceCheckBox.setEnabled(false);
        costsPanel.add(distanceCheckBox);
        pnl.add(costsPanel);
        setContent(pnl);
        setupDialog();
    }

    @Override
    protected void buttonAction(int buttonIndex, ActionEvent evt) {
        super.buttonAction(buttonIndex, evt);
        if (buttonIndex == 0) {
        }
    }

    /**
     * @return the settings
     */
    public ConflationSettings getSettings() {
        ConflationSettings settings = new ConflationSettings();
        settings.setReferenceDataSet(referenceDataSet);
        settings.setReferenceLayer(referenceLayer);
        settings.setReferenceSelection(referenceSelection);
        settings.setSubjectDataSet(subjectDataSet);
        settings.setSubjectLayer(subjectLayer);
        settings.setSubjectSelection(subjectSelection);
        return settings;
    }

    /**
     * @param settings the settings to set
     */
    public void setSettings(ConflationSettings settings) {
        referenceDataSet = settings.getReferenceDataSet();
        referenceLayer = settings.getReferenceLayer();
        referenceSelection = settings.getReferenceSelection();
        subjectDataSet = settings.getSubjectDataSet();
        subjectLayer = settings.getSubjectLayer();
        subjectSelection = settings.getSubjectSelection();
        update();
    }

    class RestoreSubjectAction extends JosmAction {

        public RestoreSubjectAction() {
            super(tr("Restore"), null, tr("Restore subject selection"), null, false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (subjectLayer != null && subjectDataSet != null && subjectSelection != null && !subjectSelection.isEmpty()) {
                Main.map.mapView.setActiveLayer(subjectLayer);
                subjectLayer.setVisible(true);
                subjectDataSet.setSelected(subjectSelection);
            }
        }
    }

    class RestoreReferenceAction extends JosmAction {

        public RestoreReferenceAction() {
            super(tr("Restore"), null, tr("Restore reference selection"), null, false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (referenceLayer != null && referenceDataSet != null && referenceSelection != null && !referenceSelection.isEmpty()) {
                Main.map.mapView.setActiveLayer(referenceLayer);
                referenceLayer.setVisible(true);
                referenceDataSet.setSelected(referenceSelection);
            }
        }
    }

    class FreezeSubjectAction extends JosmAction {

        public FreezeSubjectAction() {
            super(tr("Freeze"), null, tr("Freeze subject selection"), null, false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (subjectDataSet != null && subjectDataSet == Main.main.getCurrentDataSet()) {
                //                subjectDataSet.removeDataSetListener(this); FIXME:
                //                subjectDataSet.removeDataSetListener(this); FIXME:
            }
            subjectDataSet = Main.main.getCurrentDataSet();
            //            subjectDataSet.addDataSetListener(tableModel); FIXME:
            //            subjectDataSet.addDataSetListener(tableModel); FIXME:
            subjectLayer = Main.main.getEditLayer();
            if (subjectDataSet == null || subjectLayer == null) {
                JOptionPane.showMessageDialog(Main.parent, tr("No valid OSM data layer present."), tr("Error freezing selection"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            subjectSelection = new ArrayList<OsmPrimitive>(subjectDataSet.getSelected());
            if (subjectSelection.isEmpty()) {
                JOptionPane.showMessageDialog(Main.parent, tr("Nothing is selected, please try again."), tr("Empty selection"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            update();
        }
    }

    class FreezeReferenceAction extends JosmAction {

        public FreezeReferenceAction() {
            super(tr("Freeze"), null, tr("Freeze subject selection"), null, false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (referenceDataSet != null && referenceDataSet == Main.main.getCurrentDataSet()) {
                //                referenceDataSet.removeDataSetListener(this); FIXME:
                //                referenceDataSet.removeDataSetListener(this); FIXME:
            }
            referenceDataSet = Main.main.getCurrentDataSet();
            //            referenceDataSet.addDataSetListener(this); FIXME:
            //            referenceDataSet.addDataSetListener(this); FIXME:
            referenceLayer = Main.main.getEditLayer();
            if (referenceDataSet == null || referenceLayer == null) {
                JOptionPane.showMessageDialog(Main.parent, tr("No valid OSM data layer present."), tr("Error freezing selection"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            referenceSelection = new ArrayList<OsmPrimitive>(referenceDataSet.getSelected());
            if (referenceSelection.isEmpty()) {
                JOptionPane.showMessageDialog(Main.parent, tr("Nothing is selected, please try again."), tr("Empty selection"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            update();
        }
    }

    /**
     * Update GUI elements
     */
    void update() {
        int numNodes = 0;
        int numWays = 0;
        int numRelations = 0;

        if (subjectSelection != null) {
            for (OsmPrimitive p : subjectSelection) {
                if (p instanceof Node) {
                    numNodes++;
                } else if (p instanceof Way) {
                    numWays++;
                } else if (p instanceof Relation) {
                    numRelations++;
                }
            }
            // FIXME: translate correctly
            subjectLayerLabel.setText(subjectLayer.getName());
            subjectSelectionLabel.setText(String.format("Rel.: %d / Ways: %d / Nodes: %d", numRelations, numWays, numNodes));
        }
        numNodes = 0;
        numWays = 0;
        numRelations = 0;
        if (referenceSelection != null) {
            for (OsmPrimitive p : referenceSelection) {
                if (p instanceof Node) {
                    numNodes++;
                } else if (p instanceof Way) {
                    numWays++;
                } else if (p instanceof Relation) {
                    numRelations++;
                }
            }

            // FIXME: translate correctly
            referenceLayerLabel.setText(referenceLayer.getName());
            referenceSelectionLabel.setText(String.format("Rel.: %d / Ways: %d / Nodes: %d", numRelations, numWays, numNodes));
        }
    }
}

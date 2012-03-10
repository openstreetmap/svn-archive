package org.openstreetmap.josm.plugins.conflation;

import utilsplugin2.dumbutils.HungarianAlgorithm;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.event.*;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.MapView.EditLayerChangeListener;
import org.openstreetmap.josm.gui.OsmPrimitivRenderer;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.tools.GBC;
import static org.openstreetmap.josm.tools.I18n.tr;
import org.openstreetmap.josm.tools.Shortcut;
import utilsplugin2.dumbutils.ReplaceGeometryUtils;

public class ConflationToggleDialog extends ToggleDialog
        implements EditLayerChangeListener, SelectionChangedListener, DataSetListener,
        ConflationListChangedListener {

    public final static String PREF_PREFIX = "conflation";
    JTable resultsTable;
    ConflationLayer conflationLayer;
    MatchTableModel tableModel;
    ConflationCandidateList candidates;
    ConflationOptionsDialog optionsDialog;

    public ConflationToggleDialog(String name, String iconName, String tooltip,
            Shortcut shortcut, int preferredHeight, ConflationPlugin conflationPlugin) {
        super(tr(name), iconName, tr(tooltip), shortcut, preferredHeight);

        candidates = new ConflationCandidateList();
//        candidates.addConflationListChangedListener(this);

        optionsDialog = new ConflationOptionsDialog();
        optionsDialog.setModalityType(Dialog.ModalityType.MODELESS);

        tableModel = new MatchTableModel();
        tableModel.setCandidates(candidates);
        candidates.addConflationListChangedListener(tableModel);

        resultsTable = new JTable(tableModel);

        // add selection handler, to center/zoom view
        resultsTable.getSelectionModel().addListSelectionListener(
                new MatchListSelectionHandler());
        resultsTable.getColumnModel().getSelectionModel().addListSelectionListener(
                new MatchListSelectionHandler());

        // FIXME: doesn't work right now
        resultsTable.getColumnModel().getColumn(0).setCellRenderer(new OsmPrimitivRenderer());
        resultsTable.getColumnModel().getColumn(1).setCellRenderer(new OsmPrimitivRenderer());
        resultsTable.getColumnModel().getColumn(4).setCellRenderer(new ColorTableCellRenderer("Tags"));

        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        createLayout(resultsTable, true, Arrays.asList(new SideButton[]{
                    new SideButton(new ConfigureAction(), true),
                    new SideButton(new ConflationAction(), true)
//                    new SideButton("Replace Geometry", false),
//                    new SideButton("Merge Tags", false),
//                    new SideButton("Remove", false)
                }));
    }

    @Override
    public void conflationListChanged(ConflationCandidateList list) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public class ConfigureAction extends JosmAction {

        public ConfigureAction() {
            super(tr("Configure"), null, tr("Configure conflation"),
                    Shortcut.registerShortcut("conflation:configure", tr("Conflation: {0}", tr("Conflation")),
                    KeyEvent.VK_F, Shortcut.ALT_CTRL), false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            optionsDialog.setVisible(true);
        }
    }

    @Override
    public void editLayerChanged(OsmDataLayer oldLayer, OsmDataLayer newLayer) {
        // TODO
    }

    @Override
    public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
        // TODO
    }

    class MatchListSelectionHandler implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();

            int firstIndex = lsm.getMinSelectionIndex();
            int lastIndex = lsm.getMaxSelectionIndex();
            boolean isAdjusting = e.getValueIsAdjusting();
            if (isAdjusting) {
                return;
            }

            // only one item selected, show tags and zoom/center map
            if (!lsm.isSelectionEmpty() && firstIndex == lastIndex && firstIndex < candidates.size()) {
                ConflationCandidate c = candidates.get(firstIndex);
                OsmPrimitive reference = c.getReferenceObject();
                OsmPrimitive subject = c.getSubjectObject();

                conflationLayer.setSelectedCandidate(c);

                reference.getDataSet().clearSelection();
                subject.getDataSet().clearSelection();
                reference.getDataSet().addSelected(reference);
                subject.getDataSet().addSelected(subject);

                // zoom/center on pair
                BoundingXYVisitor box = new BoundingXYVisitor();
                box.computeBoundingBox(Arrays.asList(reference, subject));
                if (box.getBounds() == null) {
                    return;
                }
                box.enlargeBoundingBox();
                Main.map.mapView.recalculateCenterScale(box);
            }

        }
    }

    class ColorTableCellRenderer extends JLabel implements TableCellRenderer {

        private String columnName;

        public ColorTableCellRenderer(String column) {
            this.columnName = column;
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Object columnValue = table.getValueAt(row, table.getColumnModel().getColumnIndex(columnName));

            if (value != null) {
                setText(value.toString());
            }
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
                if (columnValue.equals("Conflicts!")) {
                    setBackground(java.awt.Color.red);
                } else {
                    setBackground(java.awt.Color.green);
                }
            }
            return this;
        }
    }

    static public class LayerListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            Layer layer = (Layer) value;
            JLabel label = (JLabel) super.getListCellRendererComponent(list, layer.getName(), index, isSelected,
                    cellHasFocus);
            Icon icon = layer.getIcon();
            label.setIcon(icon);
            label.setToolTipText(layer.getToolTipText());
            return label;
        }
    }
    
    class ConflationAction extends JosmAction {
        public ConflationAction() {
            super(tr("Replace Geometry"), null, tr("Replace geometry"),
                    Shortcut.registerShortcut("conflation:replace", tr("Conflation: {0}", tr("Replace")),
                    KeyEvent.VK_F, Shortcut.ALT_CTRL), false);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            //FIXME: should layer listen for selection change?
            ConflationCandidate c = conflationLayer.getSelectedCandidate();
            if (c.getReferenceLayer() != c.getSubjectLayer()) {
                JOptionPane.showMessageDialog(Main.parent, tr("Conflation between layers isn't supported yet."),
                        tr("Cannot conflate between layes"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (ReplaceGeometryUtils.replace(c.getReferenceObject(), c.getSubjectObject())) {
                candidates.remove(c);
            }
        }
    }
    
    public class ConflationOptionsDialog extends ExtendedDialog {

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

        public ConflationOptionsDialog() {
            super(Main.parent,
                    tr("Configure conflation options"),
                    new String[]{tr("Conflate"), tr("Cancel")},
                    false);
            initComponents();
        }

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
                performConflation();
            }
        }

        private void performConflation() {

            // some initialization
            int n = subjectSelection.size();
            int m = referenceSelection.size();
            double cost[][] = new double[n][m];

            // calculate cost matrix
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    cost[i][j] = ConflationUtils.calcCost(subjectSelection.get(i), referenceSelection.get(j));
                }
            }

            // perform assignment using Hungarian algorithm
            int[][] assignment = HungarianAlgorithm.hgAlgorithm(cost, "min");
            OsmPrimitive subObject, refObject;
            candidates.clear();
            for (int i = 0; i < n; i++) {
                int subIdx = assignment[i][0];
                int refIdx = assignment[i][1];
                if (subIdx < n) {
                    subObject = subjectSelection.get(subIdx);
                } else {
                    subObject = null;
                }
                if (refIdx < m) {
                    refObject = referenceSelection.get(refIdx);
                } else {
                    refObject = null;
                }

                if (subObject != null && refObject != null) {
                    // TODO: do something!
                    if (!(candidates.hasCandidate(refObject, subObject) || candidates.hasCandidate(subObject, refObject))) {
                        candidates.add(new ConflationCandidate(refObject, referenceLayer, subObject, subjectLayer, cost[subIdx][refIdx]));
                    }
                }
            }

            // add conflation layer
            try {
                conflationLayer = new ConflationLayer(subjectLayer.data, candidates);
                Main.main.addLayer(conflationLayer);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(Main.parent, ex.toString(),
                        "Error adding conflation layer", JOptionPane.ERROR_MESSAGE);
            }

            // print list of matched pairsalong with distance
            // upon selection of one pair, highlight them and draw arrow

//            if (resultsPanel != null) {
//                resultsTabPanel.setSelectedComponent(resultsPanel);
//            }
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
                }
                subjectDataSet = Main.main.getCurrentDataSet();
//            subjectDataSet.addDataSetListener(tableModel); FIXME:
                subjectLayer = Main.main.getEditLayer();
                if (subjectDataSet == null || subjectLayer == null) {
                    JOptionPane.showMessageDialog(Main.parent, tr("No valid OSM data layer present."),
                            tr("Error freezing selection"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
                subjectSelection = new ArrayList<OsmPrimitive>(subjectDataSet.getSelected());
                if (subjectSelection.isEmpty()) {
                    JOptionPane.showMessageDialog(Main.parent, tr("Nothing is selected, please try again."),
                            tr("Empty selection"), JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int numNodes = 0;
                int numWays = 0;
                int numRelations = 0;
                for (OsmPrimitive p : subjectSelection) {
                    switch (p.getType()) {
                        case NODE:
                            numNodes++;
                            break;
                        case WAY:
                            numWays++;
                            break;
                        case RELATION:
                            numRelations++;
                            break;
                    }
                }

                // FIXME: translate correctly
                subjectLayerLabel.setText(subjectLayer.getName());
                subjectSelectionLabel.setText(String.format("Rel.: %d / Ways: %d / Nodes: %d", numRelations, numWays, numNodes));
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
                }
                referenceDataSet = Main.main.getCurrentDataSet();
//            referenceDataSet.addDataSetListener(this); FIXME:
                referenceLayer = Main.main.getEditLayer();
                if (referenceDataSet == null || referenceLayer == null) {
                    JOptionPane.showMessageDialog(Main.parent, tr("No valid OSM data layer present."),
                            tr("Error freezing selection"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
                referenceSelection = new ArrayList<OsmPrimitive>(referenceDataSet.getSelected());
                if (referenceSelection.isEmpty()) {
                    JOptionPane.showMessageDialog(Main.parent, tr("Nothing is selected, please try again."),
                            tr("Empty selection"), JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int numNodes = 0;
                int numWays = 0;
                int numRelations = 0;
                for (OsmPrimitive p : referenceSelection) {
                    switch (p.getType()) {
                        case NODE:
                            numNodes++;
                            break;
                        case WAY:
                            numWays++;
                            break;
                        case RELATION:
                            numRelations++;
                            break;
                    }
                }

                // FIXME: translate correctly
                referenceLayerLabel.setText(referenceLayer.getName());
                referenceSelectionLabel.setText(String.format("Rel.: %d / Ways: %d / Nodes: %d", numRelations, numWays, numNodes));
            }
        }
    }

    @Override
    public void primitivesAdded(PrimitivesAddedEvent event) {
    }

    @Override
    public void primitivesRemoved(PrimitivesRemovedEvent event) {
        List<? extends OsmPrimitive> prims = event.getPrimitives();
        for (OsmPrimitive p : prims) {
            for (ConflationCandidate c : candidates) {
                if (c.getReferenceObject().equals(p) || c.getSubjectObject().equals(p)) {
                    candidates.remove(c);
                    break;
                }
            }
        }
        tableModel.fireTableDataChanged();
    }

    @Override
    public void tagsChanged(TagsChangedEvent event) {
    }

    @Override
    public void nodeMoved(NodeMovedEvent event) {
    }

    @Override
    public void wayNodesChanged(WayNodesChangedEvent event) {
    }

    @Override
    public void relationMembersChanged(RelationMembersChangedEvent event) {
    }

    @Override
    public void otherDatasetChange(AbstractDatasetChangedEvent event) {
    }

    @Override
    public void dataChanged(DataChangedEvent event) {
    }
}

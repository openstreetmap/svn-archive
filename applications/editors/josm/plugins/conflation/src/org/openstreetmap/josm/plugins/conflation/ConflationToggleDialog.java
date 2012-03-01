/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openstreetmap.josm.plugins.conflation;

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
import utilsplugin2.dumbutils.ReplaceGeometryAction;

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
                OsmPrimitive src = c.getSourcePrimitive();
                OsmPrimitive tgt = c.getTargetPrimitive();

                conflationLayer.setSelectedCandidate(c);

                src.getDataSet().clearSelection();
                tgt.getDataSet().clearSelection();
                src.getDataSet().addSelected(src);
                tgt.getDataSet().addSelected(tgt);

                // zoom/center on pair
                BoundingXYVisitor box = new BoundingXYVisitor();
                box.computeBoundingBox(Arrays.asList(src, tgt));
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
            ReplaceGeometryAction rg = new ReplaceGeometryAction();
            ConflationCandidate c = conflationLayer.getSelectedCandidate();
            if (rg.replace(c.getSourcePrimitive(), c.getTargetPrimitive())) {
                candidates.remove(c);
            }
        }
    }
    
    public class ConflationOptionsDialog extends ExtendedDialog {

        private JPanel costsPanel;
        private JCheckBox distanceCheckBox;
        private JButton freezeSourceButton;
        private JButton freezeTargetButton;
        private JPanel jPanel3;
        private JPanel jPanel5;
        private JButton restoreSourceButton;
        private JButton restoreTargetButton;
        private JLabel sourceLayerLabel;
        private JPanel sourcePanel;
        private JLabel sourceSelectionLabel;
        private JLabel targetLayerLabel;
        private JPanel targetPanel;
        private JLabel targetSelectionLabel;
        ArrayList<OsmPrimitive> tgtSelection = null;
        ArrayList<OsmPrimitive> srcSelection = null;
        OsmDataLayer srcLayer;
        DataSet tgtDataSet;
        OsmDataLayer tgtLayer;
        DataSet srcDataSet;
        private boolean canceled = false;

        public ConflationOptionsDialog() {
            super(Main.parent,
                    tr("Configure conflation options"),
                    new String[]{tr("Conflate"), tr("Cancel")},
                    false);
            initComponents();
        }

        private void initComponents() {
            sourcePanel = new JPanel();
            sourceLayerLabel = new JLabel();
            sourceSelectionLabel = new JLabel();
            jPanel3 = new JPanel();
            restoreSourceButton = new JButton(new RestoreSourceAction());
            freezeSourceButton = new JButton(new FreezeSourceAction());
            targetPanel = new JPanel();
            targetLayerLabel = new JLabel();
            targetSelectionLabel = new JLabel();
            jPanel5 = new JPanel();
            restoreTargetButton = new JButton(new RestoreTargetAction());
            freezeTargetButton = new JButton(new FreezeTargetAction());
            costsPanel = new JPanel();
            distanceCheckBox = new JCheckBox();

            JPanel pnl = new JPanel();
            pnl.setLayout(new BoxLayout(pnl, BoxLayout.PAGE_AXIS));

            sourcePanel.setBorder(BorderFactory.createTitledBorder("Source"));
            sourcePanel.setLayout(new BoxLayout(sourcePanel, BoxLayout.PAGE_AXIS));

            sourceLayerLabel.setText("layer");
            sourcePanel.add(sourceLayerLabel);

            sourceSelectionLabel.setText("Rel.:0 / Ways:0 / Nodes: 0");
            sourcePanel.add(sourceSelectionLabel);

            jPanel3.setLayout(new BoxLayout(jPanel3, BoxLayout.LINE_AXIS));

            restoreSourceButton.setText("Restore");
            jPanel3.add(restoreSourceButton);

            jPanel3.add(freezeSourceButton);

            sourcePanel.add(jPanel3);

            pnl.add(sourcePanel);

            targetPanel.setBorder(BorderFactory.createTitledBorder("Target"));
            targetPanel.setLayout(new BoxLayout(targetPanel, BoxLayout.PAGE_AXIS));

            targetLayerLabel.setText("layer");
            targetPanel.add(targetLayerLabel);

            targetSelectionLabel.setText("Rel.:0 / Ways:0 / Nodes: 0");
            targetPanel.add(targetSelectionLabel);

            jPanel5.setLayout(new BoxLayout(jPanel5, BoxLayout.LINE_AXIS));

            restoreTargetButton.setText("Restore");
            jPanel5.add(restoreTargetButton);

            freezeTargetButton.setText("Freeze");
            jPanel5.add(freezeTargetButton);

            targetPanel.add(jPanel5);

            pnl.add(targetPanel);

            costsPanel.setBorder(BorderFactory.createTitledBorder("Costs"));
            costsPanel.setLayout(new BoxLayout(costsPanel, BoxLayout.LINE_AXIS));

            distanceCheckBox.setSelected(true);
            distanceCheckBox.setText("Distance");
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
                criteriaTabConflateButtonActionPerformed();
            }
        }

        private void criteriaTabConflateButtonActionPerformed() {

            // some initialization
            int n = tgtSelection.size();
            int m = srcSelection.size();
            int maxLen = Math.max(n, m);
            double cost[][] = new double[maxLen][maxLen];

            // calculate cost matrix
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    cost[i][j] = ConflationUtils.calcCost(tgtSelection.get(i), srcSelection.get(j));
                }
            }

            // perform assignment using Hungarian algorithm
            int[][] assignment = HungarianAlgorithm.hgAlgorithm(cost, "min");
            OsmPrimitive tgt, src;
            candidates.clear();
            for (int i = 0; i < maxLen; i++) {
                int tgtIdx = assignment[i][0];
                int srcIdx = assignment[i][1];
                if (tgtIdx < n) {
                    tgt = tgtSelection.get(tgtIdx);
                } else {
                    tgt = null;
                }
                if (srcIdx < m) {
                    src = srcSelection.get(srcIdx);
                } else {
                    src = null;
                }

                if (tgt != null && src != null) {
                    // TODO: do something!
                    if (!(candidates.hasCandidate(src, tgt) || candidates.hasCandidate(tgt, src))) {
                        candidates.add(new ConflationCandidate(src, srcLayer, tgt, tgtLayer, cost[tgtIdx][srcIdx]));
                    }
                }
            }

            // add conflation layer
            try {
                conflationLayer = new ConflationLayer(tgtLayer.data, candidates);
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

        class RestoreTargetAction extends JosmAction {

            public RestoreTargetAction() {
                super(tr("Restore"), null, tr("Restore target selection"), null, false);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                if (tgtLayer != null && tgtDataSet != null && tgtSelection != null && !tgtSelection.isEmpty()) {
                    Main.map.mapView.setActiveLayer(tgtLayer);
                    tgtLayer.setVisible(true);
                    tgtDataSet.setSelected(tgtSelection);
                }
            }
        }

        class RestoreSourceAction extends JosmAction {

            public RestoreSourceAction() {
                super(tr("Restore"), null, tr("Restore source selection"), null, false);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                if (srcLayer != null && srcDataSet != null && srcSelection != null && !srcSelection.isEmpty()) {
                    Main.map.mapView.setActiveLayer(srcLayer);
                    srcLayer.setVisible(true);
                    srcDataSet.setSelected(srcSelection);
                }
            }
        }

        class FreezeTargetAction extends JosmAction {

            public FreezeTargetAction() {
                super(tr("Freeze"), null, tr("Freeze target selection"), null, false);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                if (tgtDataSet != null && tgtDataSet == Main.main.getCurrentDataSet()) {
//                targetDataSet.removeDataSetListener(this); FIXME:
                }
                tgtDataSet = Main.main.getCurrentDataSet();
//            targetDataSet.addDataSetListener(tableModel); FIXME:
                tgtLayer = Main.main.getEditLayer();
                if (tgtDataSet == null || tgtLayer == null) {
                    JOptionPane.showMessageDialog(Main.parent, tr("No valid OSM data layer present."),
                            tr("Error freezing selection"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
                tgtSelection = new ArrayList<OsmPrimitive>(tgtDataSet.getSelected());
                if (tgtSelection.isEmpty()) {
                    JOptionPane.showMessageDialog(Main.parent, tr("Nothing is selected, please try again."),
                            tr("Empty selection"), JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int numNodes = 0;
                int numWays = 0;
                int numRelations = 0;
                for (OsmPrimitive p : tgtSelection) {
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
                targetLayerLabel.setText(tgtLayer.getName());
                targetSelectionLabel.setText(String.format("Rel.: %d / Ways: %d / Nodes: %d", numRelations, numWays, numNodes));
            }
        }

        class FreezeSourceAction extends JosmAction {

            public FreezeSourceAction() {
                super(tr("Freeze"), null, tr("Freeze target selection"), null, false);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                if (srcDataSet != null && srcDataSet == Main.main.getCurrentDataSet()) {
//                sourceDataSet.removeDataSetListener(this); FIXME:
                }
                srcDataSet = Main.main.getCurrentDataSet();
//            sourceDataSet.addDataSetListener(this); FIXME:
                srcLayer = Main.main.getEditLayer();
                if (srcDataSet == null || srcLayer == null) {
                    JOptionPane.showMessageDialog(Main.parent, tr("No valid OSM data layer present."),
                            tr("Error freezing selection"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
                srcSelection = new ArrayList<OsmPrimitive>(srcDataSet.getSelected());
                if (srcSelection.isEmpty()) {
                    JOptionPane.showMessageDialog(Main.parent, tr("Nothing is selected, please try again."),
                            tr("Empty selection"), JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int numNodes = 0;
                int numWays = 0;
                int numRelations = 0;
                for (OsmPrimitive p : srcSelection) {
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
                sourceLayerLabel.setText(srcLayer.getName());
                sourceSelectionLabel.setText(String.format("Rel.: %d / Ways: %d / Nodes: %d", numRelations, numWays, numNodes));
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
                if (c.getSourcePrimitive().equals(p) || c.getTargetPrimitive().equals(p)) {
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

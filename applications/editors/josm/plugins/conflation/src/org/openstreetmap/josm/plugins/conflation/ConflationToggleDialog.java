package org.openstreetmap.josm.plugins.conflation;

import com.vividsolutions.jcs.conflate.polygonmatch.*;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.task.TaskMonitor;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.event.*;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView.EditLayerChangeListener;
import org.openstreetmap.josm.gui.OsmPrimitivRenderer;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.progress.PleaseWaitProgressMonitor;
import org.openstreetmap.josm.plugins.utilsplugin2.replacegeometry.ReplaceGeometryUtils;
import static org.openstreetmap.josm.tools.I18n.tr;
import org.openstreetmap.josm.tools.Shortcut;

public class ConflationToggleDialog extends ToggleDialog
        implements EditLayerChangeListener, SelectionChangedListener, DataSetListener,
        ConflationListChangedListener {

    public final static String PREF_PREFIX = "conflation";
    JTable resultsTable;
    ConflationLayer conflationLayer;
    MatchTableModel tableModel;
    ConflationCandidateList candidates;
    ConflationSettings settings;
    SettingsDialog settingsDialog;

    public ConflationToggleDialog(String name, String iconName, String tooltip,
            Shortcut shortcut, int preferredHeight, ConflationPlugin conflationPlugin) {
        super(tr(name), iconName, tr(tooltip), shortcut, preferredHeight);

        candidates = new ConflationCandidateList();
//        candidates.addConflationListChangedListener(this);

        settingsDialog = new SettingsDialog();
        settingsDialog.setModalityType(Dialog.ModalityType.MODELESS);
        settingsDialog.addWindowListener(new WindowAdapter() {
        public void windowClosed(WindowEvent e) {
                if (settingsDialog.getValue() == 1) {
                    settings = settingsDialog.getSettings();
                    performConflation();
                }
        }});

        tableModel = new MatchTableModel();

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
            settingsDialog.setVisible(true);
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
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
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
            if (settings.getReferenceLayer() != settings.getSubjectLayer()) {
                JOptionPane.showMessageDialog(Main.parent, tr("Conflation between layers isn't supported yet."),
                        tr("Cannot conflate between layes"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (ReplaceGeometryUtils.replace(c.getReferenceObject(), c.getSubjectObject())) {
                candidates.remove(c);
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

    /**
     * Create FeatureSchema using union of all keys from all selected primitives
     * @param prims
     * @return 
     */
    private FeatureSchema createSchema(Collection<OsmPrimitive> prims) {
        Set<String> keys = new HashSet<String>();
        for (OsmPrimitive prim : prims) {
            keys.addAll(prim.getKeys().keySet());
        }
        FeatureSchema schema = new FeatureSchema();
        schema.addAttribute("__GEOMETRY__", AttributeType.GEOMETRY);
        for (String key : keys) {
            schema.addAttribute(key, AttributeType.STRING);
        }
        return schema;   
    }
    
    private FeatureCollection createFeatureCollection(Collection<OsmPrimitive> prims) {
        FeatureDataset dataset = new FeatureDataset(createSchema(prims));
        for (OsmPrimitive prim : prims) {
            dataset.add(new OsmFeature(prim));
        }
        return dataset;
    }
    
    /**
     * Progress monitor for use with JCS
     */
    private class JosmTaskMonitor extends PleaseWaitProgressMonitor implements TaskMonitor {
        
        @Override
        public void report(String description) {
            subTask(description);
        }

        @Override
        public void report(int itemsDone, int totalItems, String itemDescription) {
            subTask(String.format("Processing %d of %d %s", itemsDone, totalItems, itemDescription));
        }

        @Override
        public void report(Exception exception) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void allowCancellationRequests() {
            setCancelable(true);
        }

        @Override
        public boolean isCancelRequested() {
            return isCanceled();
        }
        
    }
    
    private ConflationCandidateList generateCandidates(ConflationSettings settings) {
        JosmTaskMonitor monitor = new JosmTaskMonitor();
        monitor.beginTask("Generating conflation candidates");
        
        // create Features and collections from primitive selections
        Set<OsmPrimitive> allPrimitives = new HashSet<OsmPrimitive>();
        allPrimitives.addAll(settings.getReferenceSelection());
        allPrimitives.addAll(settings.getSubjectSelection());
        FeatureCollection allFeatures = createFeatureCollection(allPrimitives);
        FeatureCollection refColl = new FeatureDataset(allFeatures.getFeatureSchema());
        FeatureCollection subColl = new FeatureDataset(allFeatures.getFeatureSchema());
        for (Feature f : allFeatures.getFeatures()) {
            OsmFeature osmFeature = (OsmFeature)f;
            if (settings.getReferenceSelection().contains(osmFeature.getPrimitive()))
                refColl.add(osmFeature);
            if (settings.getSubjectSelection().contains(osmFeature.getPrimitive()))
                subColl.add(osmFeature);
        }
        
        // get maximum possible distance so scores can be scaled (FIXME: not quite accurate)
        Envelope envelope = refColl.getEnvelope();
        envelope.expandToInclude(subColl.getEnvelope());
        double maxDistance = Point2D.distance(
            envelope.getMinX(),
            envelope.getMinY(),
            envelope.getMaxX(),
            envelope.getMaxY());
        
        // build matcher
        CentroidDistanceMatcher centroid = new CentroidDistanceMatcher();
        centroid.setMaxDistance(maxDistance);
        IdenticalFeatureFilter identical = new IdenticalFeatureFilter();
        FeatureMatcher[] matchers = {centroid, identical};
        ChainMatcher chain = new ChainMatcher(matchers);
        BasicFCMatchFinder basicFinder = new BasicFCMatchFinder(chain);
        OneToOneFCMatchFinder finder = new OneToOneFCMatchFinder(basicFinder);

        // FIXME: ignore/filter duplicate objects (i.e. same object in both sets)
        // FIXME: fix match functions to work on point/linestring features as well
        // find matches
        Map<OsmFeature, Matches> map = finder.match(refColl, subColl, monitor);
        
        monitor.subTask("Finishing conflation candidate list");
        
        // convert to simple one-to-one match
        ConflationCandidateList list = new ConflationCandidateList();
        for (Map.Entry<OsmFeature, Matches> entry: map.entrySet()) {
            OsmFeature target = entry.getKey();
            OsmFeature subject = (OsmFeature)entry.getValue().getTopMatch();
            list.add(new ConflationCandidate(target.getPrimitive(), subject.getPrimitive(),
                    entry.getValue().getTopScore()));
        }
        
        monitor.finishTask();
        monitor.close();
        return list;
    }

    private void performConflation() {
        candidates = generateCandidates(settings);
        tableModel.setCandidates(candidates);
        candidates.addConflationListChangedListener(tableModel);
        settings.getSubjectDataSet().addDataSetListener(this);
        settings.getReferenceDataSet().addDataSetListener(this);
        // add conflation layer
        try {
            if (conflationLayer == null) {
                conflationLayer = new ConflationLayer();
                Main.main.addLayer(conflationLayer);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(Main.parent, ex.toString(), "Error adding conflation layer", JOptionPane.ERROR_MESSAGE);
        }
        conflationLayer.setCandidates(candidates);
//        candidates.addConflationListChangedListener(conflationLayer);

                
    }
}

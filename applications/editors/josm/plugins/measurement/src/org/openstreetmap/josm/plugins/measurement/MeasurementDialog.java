// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.measurement;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.josm.data.SystemOfMeasurement;
import org.openstreetmap.josm.data.SystemOfMeasurement.SoMChangeListener;
import org.openstreetmap.josm.data.osm.DataSelectionListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.event.AbstractDatasetChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataSetListener;
import org.openstreetmap.josm.data.osm.event.NodeMovedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesAddedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesRemovedEvent;
import org.openstreetmap.josm.data.osm.event.RelationMembersChangedEvent;
import org.openstreetmap.josm.data.osm.event.SelectionEventManager;
import org.openstreetmap.josm.data.osm.event.TagsChangedEvent;
import org.openstreetmap.josm.data.osm.event.WayNodesChangedEvent;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerAddEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerOrderChangeEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerRemoveEvent;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.tools.Geometry;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;
import org.openstreetmap.josm.tools.SubclassFilteredCollection;

/**
 * A small tool dialog for displaying the current measurement data.
 *
 * @author ramack
 */
public class MeasurementDialog extends ToggleDialog implements DataSelectionListener, DataSetListener, SoMChangeListener, LayerChangeListener {
    private static final long serialVersionUID = 4708541586297950021L;

    /**
     * The reset button
     */
    private SideButton resetButton;

    /**
     * The measurement label for the path length
     */
    protected JLabel pathLengthLabel;

    /**
     * The measurement label for the currently selected segments
     */
    protected JLabel selectLengthLabel;

    /**
     * The measurement label for area of the currently selected loop
     */
    protected JLabel selectAreaLabel;

    /**
     * The measurement label for radius if the currently selected loop is a circle.
     */
    protected JLabel selectRadiusLabel;

    /**
     * The measurement label for the segment angle, actually updated, if 2 nodes are selected
     */
    protected JLabel segAngleLabel;

    private DataSet ds;

    private Collection<Relation> relations;
    private Collection<Way> ways;
    private Collection<Node> nodes;

    /**
     * Constructor
     */
    public MeasurementDialog()
    {
        super(tr("Measured values"), "measure", tr("Open the measurement window."),
                Shortcut.registerShortcut("subwindow:measurement", tr("Toggle: {0}", tr("Measured values")),
                        KeyEvent.VK_U, Shortcut.CTRL_SHIFT), 150);

        resetButton = new SideButton(new AbstractAction() {
            {
                putValue(NAME, tr("Reset"));
                new ImageProvider("dialogs", "select").getResource().attachImageIcon(this, true);
                putValue(SHORT_DESCRIPTION, tr("Reset current measurement results and delete measurement path."));
                putValue("help", HelpUtil.ht("/Dialog/Measurement#Reset"));
            }
            @Override
            public void actionPerformed(ActionEvent e)
            {
                resetValues();
            }
        });

        JPanel valuePanel = new JPanel(new GridLayout(0,2));

        valuePanel.add(new JLabel(tr("Path Length")));

        pathLengthLabel = new JLabel(getDistText(0));
        valuePanel.add(pathLengthLabel);

        valuePanel.add(new JLabel(tr("Selection Length")));

        selectLengthLabel = new JLabel(getDistText(0));
        valuePanel.add(selectLengthLabel);

        valuePanel.add(new JLabel(tr("Selection Area")));

        selectAreaLabel = new JLabel(getAreaText(0));
        valuePanel.add(selectAreaLabel);

        valuePanel.add(new JLabel(tr("Selection Radius")));

        selectRadiusLabel = new JLabel(getRadiusText(0));
        valuePanel.add(selectRadiusLabel);

        JLabel angle = new JLabel(tr("Angle"));
        angle.setToolTipText(tr("Angle between two selected Nodes"));
        valuePanel.add(angle);

        segAngleLabel = new JLabel("- \u00b0");
        valuePanel.add(segAngleLabel);

        this.setPreferredSize(new Dimension(0, 92));

        createLayout(valuePanel, false, Arrays.asList(new SideButton[] {
                resetButton
        }));

        MainApplication.getLayerManager().addLayerChangeListener(this);
        SelectionEventManager.getInstance().addSelectionListener(this);
        SystemOfMeasurement.addSoMChangeListener(this);
    }

    protected String getDistText(double v) {
        return SystemOfMeasurement.getSystemOfMeasurement().getDistText(v, new DecimalFormat("#0.000"), 1e-3);
    }

    protected String getAreaText(double v) {
        return SystemOfMeasurement.getSystemOfMeasurement().getAreaText(v, new DecimalFormat("#0.000"), 1e-3);
    }

    protected String getRadiusText(double v) {
        return SystemOfMeasurement.getSystemOfMeasurement().getDistText(v, new DecimalFormat("#0.000"), 1e-3);
    }

    protected String getAngleText(double v) {
        return new DecimalFormat("#0.0").format(v) + " \u00b0";
    }

    /**
     * Cleans the active Measurement Layer
     */
    public void resetValues(){
        MeasurementPlugin.getCurrentLayer().reset();
    }

    @Override
    public void selectionChanged(SelectionChangeEvent event) {
        refresh(event.getSelection());
    }

    private void refresh(Collection<? extends OsmPrimitive> selection) {
        double length = 0.0;
        double segAngle = 0.0;
        double area = 0.0;
        double radius = 0.0;
        Node lastNode = null;
        // Don't mix up way and nodes computation (fix #6872). Priority given to ways
        ways = new SubclassFilteredCollection<>(selection, Way.class::isInstance);
        if (ways.isEmpty()) {
            nodes = new SubclassFilteredCollection<>(selection, Node.class::isInstance);
            if (nodes.isEmpty()) {
                relations = new SubclassFilteredCollection<>(selection, Relation.class::isInstance);
                for (Relation r : relations) {
                    if (r.isMultipolygon()) {
                        area += Geometry.multipolygonArea(r);
                    }
                }
            } else {
                for (Node n : nodes) {
                    if (n.getCoor() != null) {
                        if (lastNode == null) {
                            lastNode = n;
                        } else {
                            length += lastNode.getCoor().greatCircleDistance(n.getCoor());
                            segAngle = MeasurementLayer.angleBetween(lastNode.getCoor(), n.getCoor());
                            lastNode = n;
                        }
                    }
                }
            }
        } else {
            nodes = null;
            for (Way w : ways) {
                Node lastN = null;
                double wayArea = 0.0;
                Double firstSegLength = null;
                boolean isCircle = true;
                for (Node n: w.getNodes()) {
                    if (lastN != null && lastN.getCoor() != null && n.getCoor() != null) {
                        final double segLength = lastN.getCoor().greatCircleDistance(n.getCoor());
                        if (firstSegLength == null) {
                            firstSegLength = segLength;
                        }
                        if (isCircle && Math.abs(firstSegLength - segLength) > 0.000001) {
                            isCircle = false;
                        }
                        length += segLength;
                        //http://local.wasp.uwa.edu.au/~pbourke/geometry/polyarea/
                        wayArea += (MeasurementLayer.calcX(n.getCoor()) * MeasurementLayer.calcY(lastN.getCoor()))
                                - (MeasurementLayer.calcY(n.getCoor()) * MeasurementLayer.calcX(lastN.getCoor()));
                        segAngle = MeasurementLayer.angleBetween(lastN.getCoor(), n.getCoor());
                    }
                    lastN = n;
                }
                if (lastN != null && lastN.equals(w.getNodes().iterator().next()))
                    wayArea = Math.abs(wayArea / 2);
                else
                    wayArea = 0;
                area += wayArea;
            }
            if (ways.size() == 1 && area > 0.0) {
                radius = length / (2 * Math.PI);
            }
        }

        final String lengthLabel = getDistText(length);
        final String angleLabel = getAngleText(segAngle);
        final String areaLabel = getAreaText(area);
        final String radiusLabel = getRadiusText(radius);

        GuiHelper.runInEDT(new Runnable() {
            @Override
            public void run() {
                selectLengthLabel.setText(lengthLabel);
                segAngleLabel.setText(angleLabel);
                selectAreaLabel.setText(areaLabel);
                selectRadiusLabel.setText(radiusLabel);
            }
        });

        DataSet currentDs = MainApplication.getLayerManager().getEditDataSet();

        if (ds != currentDs) {
            if (ds != null) {
                ds.removeDataSetListener(this);
            }
            if (currentDs != null) {
                currentDs.addDataSetListener(this);
            }
            ds = currentDs;
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        SystemOfMeasurement.removeSoMChangeListener(this);
        SelectionEventManager.getInstance().removeSelectionListener(this);
        MainApplication.getLayerManager().removeLayerChangeListener(this);
        clear();
    }

    private void clear() {
        if (ds != null) {
            ds.removeDataSetListener(this);
            ds = null;
        }
        clear(relations);
        clear(ways);
        clear(nodes);
    }

    private static void clear(Collection<?> collection) {
        if (collection != null) {
            collection.clear();
        }
    }

    private boolean parentsContain(Way w) {
        return w.getReferrers(true).stream()
                .anyMatch(ref -> ref instanceof Relation && relations != null && relations.contains(ref));
    }

    private boolean parentsContain(Node n) {
        return n.getReferrers(true).stream()
                .anyMatch(ref
                        -> (ref instanceof Way && ((ways != null && ways.contains(ref)) || parentsContain((Way) ref)))
                        || (ref instanceof Relation && relations != null && relations.contains(ref)));
    }

    @Override public void nodeMoved(NodeMovedEvent event) {
        Node n = event.getNode();
        // Refresh selection if a node belonging to a selected member has moved (example: scale action)
        if ((nodes != null && nodes.contains(n)) || parentsContain(n)) {
            refresh(event.getDataset().getSelected());
        }
    }

    @Override public void wayNodesChanged(WayNodesChangedEvent event) {
        Way w = event.getChangedWay();
        if ((ways != null && ways.contains(w)) || parentsContain(w)) {
            refresh(event.getDataset().getSelected());
        }
    }

    @Override public void relationMembersChanged(RelationMembersChangedEvent event) {
        if (relations != null && relations.contains(event.getRelation())) {
            refresh(event.getDataset().getSelected());
        }
    }

    @Override public void primitivesAdded(PrimitivesAddedEvent event) {}
    @Override public void primitivesRemoved(PrimitivesRemovedEvent event) {}
    @Override public void tagsChanged(TagsChangedEvent event) {}
    @Override public void otherDatasetChange(AbstractDatasetChangedEvent event) {}
    @Override public void dataChanged(DataChangedEvent event) {}

    @Override
    public void systemOfMeasurementChanged(String oldSoM, String newSoM) {
        // Refresh selection to take into account new system of measurement
        DataSet currentDs = MainApplication.getLayerManager().getEditDataSet();
        if (currentDs != null) {
            refresh(currentDs.getSelected());
        }
    }

    @Override public void layerOrderChanged(LayerOrderChangeEvent e) {}
    @Override public void layerAdded(LayerAddEvent e) {}

    @Override
    public void layerRemoving(LayerRemoveEvent e) {
        if (e.getRemovedLayer() instanceof OsmDataLayer && Objects.equals(ds, ((OsmDataLayer) e.getRemovedLayer()).getDataSet())) {
            clear();
        }
    }
}

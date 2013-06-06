package org.openstreetmap.josm.plugins.measurement;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.event.AbstractDatasetChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataSetListener;
import org.openstreetmap.josm.data.osm.event.NodeMovedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesAddedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesRemovedEvent;
import org.openstreetmap.josm.data.osm.event.RelationMembersChangedEvent;
import org.openstreetmap.josm.data.osm.event.TagsChangedEvent;
import org.openstreetmap.josm.data.osm.event.WayNodesChangedEvent;
import org.openstreetmap.josm.gui.NavigatableComponent;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;
import org.openstreetmap.josm.tools.SubclassFilteredCollection;

/**
 * A small tool dialog for displaying the current measurement data.
 *
 * @author ramack
 */
public class MeasurementDialog extends ToggleDialog implements SelectionChangedListener, DataSetListener {
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
     * The measurement label for the segment angle, actually updated, if 2 nodes are selected
     */
    protected JLabel segAngleLabel;
    
    private DataSet ds;

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
        		putValue(SMALL_ICON,ImageProvider.get("dialogs", "select"));
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

        pathLengthLabel = new JLabel(NavigatableComponent.getDistText(0));
        valuePanel.add(pathLengthLabel);

        valuePanel.add(new JLabel(tr("Selection Length")));

        selectLengthLabel = new JLabel(NavigatableComponent.getDistText(0));
        valuePanel.add(selectLengthLabel);

        valuePanel.add(new JLabel(tr("Selection Area")));

        selectAreaLabel = new JLabel(NavigatableComponent.getAreaText(0));
        valuePanel.add(selectAreaLabel);

        JLabel angle = new JLabel(tr("Angle"));
        angle.setToolTipText(tr("Angle between two selected Nodes"));
        valuePanel.add(angle);

        segAngleLabel = new JLabel("- \u00b0");
        valuePanel.add(segAngleLabel);

        this.setPreferredSize(new Dimension(0, 92));

        createLayout(valuePanel, false, Arrays.asList(new SideButton[] {
            resetButton
        }));
        
        DataSet.addSelectionListener(this);
    }

    /**
     * Cleans the active Measurement Layer
     */
    public void resetValues(){
        MeasurementPlugin.getCurrentLayer().reset();
    }

	@Override
	public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
        double length = 0.0;
        double segAngle = 0.0;
        double area = 0.0;
        Node lastNode = null;
        // Don't mix up way and nodes computation (fix #6872). Priority given to ways
        ways = new SubclassFilteredCollection<OsmPrimitive, Way>(newSelection, OsmPrimitive.wayPredicate);
        if (ways.isEmpty()) {
            nodes = new SubclassFilteredCollection<OsmPrimitive, Node>(newSelection, OsmPrimitive.nodePredicate);
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
        } else {
            nodes = null;
            for (Way w : ways) {
                Node lastN = null;
                double wayArea = 0.0;
                for (Node n: w.getNodes()) {
                    if (lastN != null && lastN.getCoor() != null && n.getCoor() != null) {
                        length += lastN.getCoor().greatCircleDistance(n.getCoor());
                        //http://local.wasp.uwa.edu.au/~pbourke/geometry/polyarea/
                        wayArea += (MeasurementLayer.calcX(n.getCoor()) * MeasurementLayer.calcY(lastN.getCoor()))
                                 - (MeasurementLayer.calcY(n.getCoor()) * MeasurementLayer.calcX(lastN.getCoor()));
                        segAngle = MeasurementLayer.angleBetween(lastN.getCoor(), n.getCoor());
                    }
                    lastN = n;
                }
                if (lastN != null && lastN == w.getNodes().iterator().next())
                    wayArea = Math.abs(wayArea / 2);
                else
                    wayArea = 0;
                area += wayArea;
            }
        }
        
        final String lengthLabel = NavigatableComponent.getDistText(length);
        final String angleLabel = new DecimalFormat("#0.0").format(segAngle) + " \u00b0";
        final String areaLabel = NavigatableComponent.getAreaText(area);
        
        GuiHelper.runInEDT(new Runnable() {
            @Override
            public void run() {
                selectLengthLabel.setText(lengthLabel);
                segAngleLabel.setText(angleLabel);
                selectAreaLabel.setText(areaLabel);
            }
        });
        
        DataSet currentDs = Main.main.getCurrentDataSet();
    
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

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.dialogs.ToggleDialog#destroy()
	 */
	@Override
	public void destroy() {
		super.destroy();
		DataSet.removeSelectionListener(this);
		if (ds != null) {
		    ds.removeDataSetListener(this);
		    ds = null;
		}
	}

	private boolean waysContain(Node n) {
	    if (ways != null) {
	        for (Way w : ways) {
	            if (w.containsNode(n)) {
	                return true;
	            }
	        }
	    }
	    return false;
	}
	
    @Override public void nodeMoved(NodeMovedEvent event) {
        Node n = event.getNode();
        // Refresh selection if a node belonging to a selected member has moved (example: scale action)
        if ((nodes != null && nodes.contains(n)) || waysContain(n)) {
            selectionChanged(Main.main.getCurrentDataSet().getSelected());
        }
    }

    @Override public void primitivesAdded(PrimitivesAddedEvent event) {}
    @Override public void primitivesRemoved(PrimitivesRemovedEvent event) {}
    @Override public void tagsChanged(TagsChangedEvent event) {}
    @Override public void wayNodesChanged(WayNodesChangedEvent event) { }
    @Override public void relationMembersChanged(RelationMembersChangedEvent event) {}
    @Override public void otherDatasetChange(AbstractDatasetChangedEvent event) {}
    @Override public void dataChanged(DataChangedEvent event) {}
}

package org.openstreetmap.josm.plugins.measurement;

import static org.openstreetmap.josm.tools.I18n.marktr;
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

import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * A small tool dialog for displaying the current measurement data.
 *
 * @author ramack
 */
public class MeasurementDialog extends ToggleDialog {
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
        		putValue(NAME, marktr("Reset"));
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

        pathLengthLabel = new JLabel("0 m");
        valuePanel.add(pathLengthLabel);

        valuePanel.add(new JLabel(tr("Selection Length")));

        selectLengthLabel = new JLabel("0 m");
        valuePanel.add(selectLengthLabel);

        valuePanel.add(new JLabel(tr("Selection Area")));

        selectAreaLabel = new JLabel("0 m\u00b2");
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

        final MeasurementDialog dlg = this;

        DataSet.addSelectionListener(new SelectionChangedListener() {
            @Override
            public void selectionChanged(Collection<? extends OsmPrimitive> arg0) {
                double length = 0.0;
                double segAngle = 0.0;
                double area = 0.0;
                Node lastNode = null;
                for(OsmPrimitive p:arg0) {
                    // ignore incomplete nodes
                    if(p instanceof Node && !((Node)p).isIncomplete()) {
                        Node n =(Node)p;
                        if(lastNode == null) {
                            lastNode = n;
                        } else {
                            length += lastNode.getCoor().greatCircleDistance(n.getCoor());
                            segAngle = MeasurementLayer.angleBetween(lastNode.getCoor(), n.getCoor());
                            lastNode = n;
                        }
                    } else if(p instanceof Way) {
                        Way w = (Way)p;
                        Node lastN = null;
                        for(Node n: w.getNodes()) {
                            if(lastN != null) {
                                length += lastN.getCoor().greatCircleDistance(n.getCoor());
                                //http://local.wasp.uwa.edu.au/~pbourke/geometry/polyarea/
                                area += (MeasurementLayer.calcX(n.getCoor()) * MeasurementLayer.calcY(lastN.getCoor()))
                                - (MeasurementLayer.calcY(n.getCoor()) * MeasurementLayer.calcX(lastN.getCoor()));
                                segAngle = MeasurementLayer.angleBetween(lastN.getCoor(), n.getCoor());
                            }
                            lastN = n;
                        }
                        if (lastN != null && lastN == w.getNodes().iterator().next())
                            area = Math.abs(area / 2);
                        else
                            area = 0;
                    }
                }
                dlg.selectLengthLabel.setText(new DecimalFormat("#0.00").format(length) + " m");

                dlg.segAngleLabel.setText(new DecimalFormat("#0.0").format(segAngle) + " \u00b0");
                dlg.selectAreaLabel.setText(new DecimalFormat("#0.00").format(area) + " m\u00b2");
            }
        });
    }

    /**
     * Cleans the active Meausurement Layer
     */
    public void resetValues(){
        MeasurementPlugin.getCurrentLayer().reset();
    }
}

/**
 *
 */
package com.tilusnet.josm.plugins.alignways;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.event.AbstractDatasetChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataSetListener;
import org.openstreetmap.josm.data.osm.event.NodeMovedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesAddedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesRemovedEvent;
import org.openstreetmap.josm.data.osm.event.RelationMembersChangedEvent;
import org.openstreetmap.josm.data.osm.event.TagsChangedEvent;
import org.openstreetmap.josm.data.osm.event.WayNodesChangedEvent;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * @author tilusnet <tilusnet@gmail.com>
 * Handles the state machine and user interaction (mouse clicks).
 *
 */
public class AlignWaysMode extends MapMode implements DataSetListener {

    private static final long serialVersionUID = -1090955708412011141L;
    private final AlignWaysState noneSelected;
    private final AlignWaysState referenceSelected;
    private final AlignWaysState aligneeSelected;
    private final AlignWaysState bothSelected;
    private AlignWaysState currentState;
    private AlignWaysSegmentMgr awSegs;
    boolean tipShown;

    public AlignWaysMode(String name, String desc) {
        super(tr(name), "alignways.png", tr(desc),
                Shortcut.registerShortcut("mapmode:alignways",
                        tr("Mode: {0}", tr("Align Ways")),
                        KeyEvent.VK_N, Shortcut.SHIFT),
                        Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        noneSelected = new AlignWaysSelNoneState();
        referenceSelected = new AlignWaysSelRefState();
        aligneeSelected = new AlignWaysSelAlgnState();
        bothSelected = new AlignWaysSelBothState();
        tipShown = false;
    }

    @Override
    public void enterMode() {
        super.enterMode();

        AlignWaysPlugin.getAwDialog().activate(true);
        IconToggleButton optBtn = AlignWaysPlugin.getOptBtn();
        if (!optBtn.isSelected()) {
            // Make sure the option panel is visible when align mode starts
            optBtn.doClick();
        }

        boolean showTips = Boolean.parseBoolean(Config.getPref().get("alignways.showtips", "true"));
        if ((showTips) && (!tipShown)) {
            showTips();
        }
        int majorVer = Integer.parseInt(Config.getPref().get("alignways.majorver", "-1"));
        if (majorVer != AlignWaysPlugin.AlignWaysMajorVersion) {
            showWhatsNew();
        }

        awSegs = AlignWaysSegmentMgr.getInstance(MainApplication.getMap().mapView);
        MainApplication.getMap().mapView.addMouseListener(this);
        DataSet ds = MainApplication.getLayerManager().getEditDataSet();
        if (ds != null)
            ds.addDataSetListener(this);
        setCurrentState(noneSelected);
    }


    @Override
    public void exitMode() {
        super.exitMode();

        AlignWaysPlugin.getAwDialog().activate(false);
        IconToggleButton optBtn = AlignWaysPlugin.getOptBtn();
        if (optBtn.isSelected()) {
            // The option panel will be switched off
            optBtn.doClick();
        }

        setCurrentState(noneSelected);
        MainApplication.getMap().mapView.removeMouseListener(this);
        DataSet ds = MainApplication.getLayerManager().getEditDataSet();
        if (ds != null)
            ds.removeDataSetListener(this);
        AlignWaysPlugin.getAwAction().setEnabled(false);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        boolean ctrlPressed = (e.getModifiers() & ActionEvent.CTRL_MASK) != 0;
        boolean altPressed = (e.getModifiers() & (ActionEvent.ALT_MASK | InputEvent.ALT_GRAPH_MASK)) != 0;

        if (e.getButton() == MouseEvent.BUTTON1) {

            if (altPressed) {
                currentState.altLClick(this);

            } else {
                Point clickedPoint = new Point(e.getX(), e.getY());

                if (!ctrlPressed) {
                    // Alignee could change

                    if (awSegs.algnUpdate(clickedPoint)) {
                        currentState.leftClick(this);
                    }

                } else {
                    // Reference could change
                    if (awSegs.refUpdate(clickedPoint)) {
                        currentState.ctrlLClick(this);
                    }
                }
            }
        }

        MainApplication.getMap().mapView.repaint();
    }

    /**
     * Sets the current state based on the selected segments.
     * @param mgr AlignWays segment manager singleton
     */
    public void setCurrentState(AlignWaysSegmentMgr mgr) {

        boolean algnSelected = mgr.getAlgnSeg() != null;
        boolean refSelected = mgr.getRefSeg() != null;

        if (algnSelected && refSelected)
            setCurrentState(getBothSelected());
        else if (algnSelected)
            setCurrentState(getAligneeSelected());
        else if (refSelected)
            setCurrentState(getReferenceSelected());
        else
            setCurrentState(getNoneSelected());
    }

    /**
     * Sets the current state.
     * @param currentState One of the AlignWays states
     */
    public void setCurrentState(AlignWaysState currentState) {
        this.currentState = currentState;
        currentState.setHelpText();

        // Activate the Align Ways button if we have enough selections
        AlignWaysPlugin.getAwAction().setEnabled(currentState == bothSelected);

        if (currentState == noneSelected) {
            awSegs.cleanupWays();

            // getEditDataSet() may return null when the editable layer had
            // already been removed by JOSM. This happens e.g. when the user closes
            // JOSM while AlignWays mode is still active.
            DataSet ds = getLayerManager().getEditDataSet();
            if (ds != null) {
                ds.clearSelection();
            }
        }
    }

    /**
     * @return the noneSelected
     */
    public AlignWaysState getNoneSelected() {
        return noneSelected;
    }

    /**
     * @return the referenceSelected
     */
    public AlignWaysState getReferenceSelected() {
        return referenceSelected;
    }

    /**
     * @return the aligneeSelected
     */
    public AlignWaysState getAligneeSelected() {
        return aligneeSelected;
    }

    /**
     * @return the bothSelected
     */
    public AlignWaysState getBothSelected() {
        return bothSelected;
    }

    private void showTips() {

        AlignWaysTipsPanel atp = new AlignWaysTipsPanel();
        Object[] okButton = {tr("I''m ready!")};
        JOptionPane tipPane = new JOptionPane(atp, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION,
                null, okButton, okButton[0]);
        tipPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 30, 10));
        JDialog tipDialog = tipPane.createDialog(MainApplication.getMainFrame(), tr("AlignWays Tips"));
        tipDialog.setIconImage(new ImageIcon(getClass().getResource("/images/alignways.png")).getImage());

        tipDialog.setResizable(true);
        tipDialog.setVisible(true);
        tipShown = true;

        tipDialog.dispose();

        Config.getPref().putBoolean("alignways.showtips", !atp.isChkBoxSelected());
    }

    private void showWhatsNew() {

        AlignWaysWhatsNewPanel awnp = new AlignWaysWhatsNewPanel();
        JOptionPane wnPane = new JOptionPane(awnp, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null);
        wnPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JDialog wnDialog = wnPane.createDialog(MainApplication.getMainFrame(), tr("AlignWays: What''s New..."));
        wnDialog.setIconImage(new ImageIcon(getClass().getResource("/images/alignways.png")).getImage());

        wnDialog.setResizable(true);
        wnDialog.setVisible(true);

        wnDialog.dispose();

        Config.getPref().put("alignways.majorver", Integer.toString(AlignWaysPlugin.AlignWaysMajorVersion));
    }

    @Override
    public boolean layerIsSupported(Layer l) {
        return l instanceof OsmDataLayer;
    }

    @Override
    protected void updateEnabledState() {
        setEnabled(getLayerManager().getEditLayer() != null);
    }

    /* --------------- *
     * DataSetListener *
     * --------------- */

    @Override
    public void primitivesAdded(PrimitivesAddedEvent event) {
    }

    @Override
    public void primitivesRemoved(PrimitivesRemovedEvent event) {
        awSegs = AlignWaysSegmentMgr.getInstance(MainApplication.getMap().mapView);

        // Check whether any of the removed primitives were part of a highlighted alignee or reference segment.
        // If so: remove the affected segment and update the state accordingly.
        if (awSegs.primitivesRemoved(event.getPrimitives()))
            setCurrentState(awSegs);
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

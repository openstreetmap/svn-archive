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

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * @author tilusnet <tilusnet@gmail.com>
 * Handles the state machine and user interaction (mouse clicks).
 * 
 */
public class AlignWaysMode extends MapMode /* implements MapViewPaintable */{

    private static final long serialVersionUID = -1090955708412011141L;
    private final AlignWaysState noneSelected;
    private final AlignWaysState referenceSelected;
    private final AlignWaysState aligneeSelected;
    private final AlignWaysState bothSelected;
    private AlignWaysState currentState;
    private AlignWaysSegmentMgr awSegs;
    boolean tipShown;

    public AlignWaysMode(MapFrame mapFrame, String name, String desc) {
        super(tr(name), "alignways.png", tr(desc),
                Shortcut.registerShortcut("mapmode:alignways",
                        tr("Mode: {0}", tr("Align Ways")),
                        KeyEvent.VK_N, Shortcut.GROUP_EDIT, Shortcut.SHIFT_DEFAULT),
                        mapFrame, Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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

        boolean showTips = Boolean.parseBoolean(Main.pref.get("alignways.showtips", "true"));
        if ((showTips) && (!tipShown)) {
            showTips();
        }
        int majorVer = Integer.parseInt(Main.pref.get("alignways.majorver", "-1"));
        if (majorVer != AlignWaysPlugin.AlignWaysMajorVersion) {
            showWhatsNew();
        }

        awSegs = AlignWaysSegmentMgr.getInstance(Main.map.mapView);
        Main.map.mapView.addMouseListener(this);
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
        Main.map.mapView.removeMouseListener(this);
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

        // Activate the Align Ways button if we have enough selections
        if (currentState == bothSelected) {
            AlignWaysPlugin.getAwAction().setEnabled(true);
        } else {
            AlignWaysPlugin.getAwAction().setEnabled(false);
        }
        Main.map.mapView.repaint();
    }

    /**
     * @param currentState
     *            One of the AlignWays states
     */
    public void setCurrentState(AlignWaysState currentState) {
        this.currentState = currentState;
        currentState.setHelpText();

        if (currentState == noneSelected) {
            awSegs.cleanupWays();
            // TODO getCurrentDataSet may return null when the editable layer had
            // already been removed by JOSM. This happens e.g. when the user closes
            // JOSM while AlignWays mode is still active.
            if (getCurrentDataSet() != null) {
                getCurrentDataSet().clearSelection();
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

    /**
     * @return the current state
     */
    public AlignWaysState getCurrentState() {
        return currentState;
    }

    private void showTips() {

        AlignWaysTipsPanel atp = new AlignWaysTipsPanel();
        Object[] okButton = {tr("I''m ready!")};
        JOptionPane tipPane = new JOptionPane(atp, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION,
                null, okButton, okButton[0]);
        tipPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 30, 10));
        JDialog tipDialog = tipPane.createDialog(Main.parent, tr("AlignWays Tips"));
        tipDialog.setIconImage(new ImageIcon(getClass().getResource("/images/alignways.png")).getImage());

        tipDialog.setResizable(true);
        tipDialog.setVisible(true);
        tipShown = true;

        tipDialog.dispose();

        Main.pref.put("alignways.showtips", !atp.isChkBoxSelected());

    }


    private void showWhatsNew() {

        AlignWaysWhatsNewPanel awnp = new AlignWaysWhatsNewPanel();
        JOptionPane wnPane = new JOptionPane(awnp, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null);
        wnPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JDialog wnDialog = wnPane.createDialog(Main.parent, tr("AlignWays: What''s New..."));
        wnDialog.setIconImage(new ImageIcon(getClass().getResource("/images/alignways.png")).getImage());

        wnDialog.setResizable(true);
        wnDialog.setVisible(true);

        wnDialog.dispose();

        Main.pref.put("alignways.majorver", new Integer(AlignWaysPlugin.AlignWaysMajorVersion).toString());

    }

    /* (non-Javadoc)
     * @see org.openstreetmap.josm.actions.mapmode.MapMode#layerIsSupported(org.openstreetmap.josm.gui.layer.Layer)
     */
    @Override
    public boolean layerIsSupported(Layer l) {
        if (l == null)
            return false;
        else
            return true;
    }
}

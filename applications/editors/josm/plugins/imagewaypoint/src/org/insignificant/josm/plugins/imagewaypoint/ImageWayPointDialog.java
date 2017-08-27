// License: GPL. For details, see LICENSE file.
package org.insignificant.josm.plugins.imagewaypoint;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.insignificant.josm.plugins.imagewaypoint.actions.NextAction;
import org.insignificant.josm.plugins.imagewaypoint.actions.PreviousAction;
import org.insignificant.josm.plugins.imagewaypoint.actions.RotateLeftAction;
import org.insignificant.josm.plugins.imagewaypoint.actions.RotateRightAction;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.tools.Shortcut;

public final class ImageWayPointDialog extends ToggleDialog {
    final ImageComponent imageDisplay;
    private final Action previousAction;
    private final Action nextAction;
    private final Action rotateLeftAction;
    private final Action rotateRightAction;

    private final IImageChangeListener listener;

    public ImageWayPointDialog() {
        super(tr("WayPoint Image"), "imagewaypoint", tr("Display non-geotagged photos"),
            Shortcut.registerShortcut("subwindow:imagewaypoint", tr("Toggle: {0}", tr("WayPoint Image")),
            KeyEvent.VK_Y, Shortcut.ALT_SHIFT), 200);

        this.previousAction = new PreviousAction();
        this.nextAction = new NextAction();
        this.rotateLeftAction = new RotateLeftAction();
        this.rotateRightAction = new RotateRightAction();

        final JButton previousButton = new JButton(this.previousAction);
        final JButton nextButton = new JButton(this.nextAction);
        final JButton rotateLeftButton = new JButton(this.rotateLeftAction);
        final JButton rotateRightButton = new JButton(this.rotateRightAction);

        // default layout, FlowLayout, is fine
        final JPanel buttonPanel = new JPanel();
        buttonPanel.add(previousButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(rotateLeftButton);
        buttonPanel.add(rotateRightButton);

        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        this.imageDisplay = new ImageComponent();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.add(this.imageDisplay, BorderLayout.CENTER);

        this.listener = new ImageChangeListener(this);
        ImageEntries.getInstance().addListener(this.listener);

        this.updateGUI();
        add(mainPanel);
    }

    void updateGUI() {
        this.previousAction.setEnabled(ImageEntries.getInstance().hasPrevious());
        this.nextAction.setEnabled(ImageEntries.getInstance().hasNext());
        this.rotateLeftAction.setEnabled(null != ImageEntries.getInstance().getCurrentImageEntry());
        this.rotateRightAction.setEnabled(null != ImageEntries.getInstance().getCurrentImageEntry());

        if (null != MainApplication.getMap()) {
            MainApplication.getMap().repaint();
        }
    }
}

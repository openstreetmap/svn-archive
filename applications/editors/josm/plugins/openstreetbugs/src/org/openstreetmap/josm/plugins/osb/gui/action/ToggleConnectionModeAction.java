package org.openstreetmap.josm.plugins.osb.gui.action;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.osb.ConfigKeys;
import org.openstreetmap.josm.plugins.osb.OsbPlugin;
import org.openstreetmap.josm.plugins.osb.gui.OsbDialog;

public class ToggleConnectionModeAction extends AbstractAction {

    private final OsbPlugin plugin;
    private final OsbDialog dialog;
    private final ActionQueue actionQueue;

    public static final String MSG_ONLINE = tr("Switch to online mode");
    public static final String MSG_OFFLINE = tr("Switch to offline mode");

    public ToggleConnectionModeAction(OsbDialog osbDialog, OsbPlugin osbPlugin) {
        super(MSG_OFFLINE);
        this.dialog = osbDialog;
        this.plugin = osbPlugin;
        this.actionQueue = osbDialog.getActionQueue();
    }

    public void actionPerformed(ActionEvent e) {
        boolean isOffline = !Main.pref.getBoolean(ConfigKeys.OSB_API_OFFLINE);

        // inform the dialog about the connection mode
        dialog.setConnectionMode(isOffline);

        // set the new value in the preferences
        Main.pref.put(ConfigKeys.OSB_API_OFFLINE, isOffline);

        // toggle the tooltip text
        if(e.getSource() != null && e.getSource() instanceof JToggleButton) {
            JToggleButton button = (JToggleButton) e.getSource();
            if(isOffline) {
                button.setToolTipText(MSG_ONLINE);
                if(Main.pref.getBoolean(ConfigKeys.OSB_BUTTON_LABELS)) {
                    button.setText(MSG_ONLINE);
                }
            } else {
                button.setToolTipText(MSG_OFFLINE);
                if(Main.pref.getBoolean(ConfigKeys.OSB_BUTTON_LABELS)) {
                    button.setText(MSG_OFFLINE);
                }
            }
        }


        if(!isOffline) {
            if(actionQueue.getSize() == 0) {
                dialog.hideQueuePanel();
                return;
            }

            // if we switch to online mode, ask if the queue should be processed
            int result = JOptionPane.showConfirmDialog(Main.parent,
                tr("You have unsaved changes in your queue. Do you want to submit them now?"),
                tr("OpenStreetBugs"),
                JOptionPane.YES_NO_OPTION);
            if(result == JOptionPane.YES_OPTION) {
                try {
                    actionQueue.processQueue();

                    // toggle queue panel visibility, if now error occurred
                    dialog.hideQueuePanel();

                    // refresh, if the api is enabled
                    if(!Main.pref.getBoolean(ConfigKeys.OSB_API_DISABLED)) {
                        plugin.updateData();
                    }
                } catch (Exception e1) {
                    System.err.println("Couldn't process action queue");
                    e1.printStackTrace();
                }
            }
        } else {
            dialog.showQueuePanel();
        }
    }
}

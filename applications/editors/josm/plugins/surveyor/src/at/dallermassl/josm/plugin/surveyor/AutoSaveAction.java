/**
 * Copyright by Christof Dallermassl
 * This program is free software and licensed under GPL.
 */
package at.dallermassl.josm.plugin.surveyor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import java.awt.event.KeyEvent;

import at.dallermassl.josm.plugin.surveyor.action.SetWaypointAction;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.tools.Shortcut;

import livegps.LiveGpsLayer;

/**
 * @author cdaller
 *
 */
public class AutoSaveAction extends JosmAction {
    private static final long serialVersionUID = -8608679323231116043L;
    private static final long AUTO_SAVE_PERIOD_SEC = 60; // once a minute
    public static final String GPS_FILE_NAME_PATTERN = "surveyor-{0,date,yyyyMMdd-HHmmss}.gpx";
    public static final String OSM_FILE_NAME_PATTERN = "surveyor-{0,date,yyyyMMdd-HHmmss}.osm";
    private boolean autoSave = false;
    private Timer gpsDataTimer;

    public AutoSaveAction() {
        super(tr("AutoSave LiveData"), "autosave.png", tr("Save captured data to file every minute."),
        Shortcut.registerShortcut("surveyor:autosave", tr("Tool: {0}", tr("AutoSave LiveData")),
        KeyEvent.VK_S, Shortcut.GROUP_DIRECT2+Shortcut.GROUPS_ALT1), true);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof AbstractButton) {
            autoSave = ((AbstractButton)e.getSource()).isSelected();
        }

        if(autoSave) {
            if(gpsDataTimer == null) {
                gpsDataTimer = new Timer();
            }
            TimerTask task;

            String gpxFilename = MessageFormat.format(GPS_FILE_NAME_PATTERN, new Date());
            task = new AutoSaveGpsLayerTimerTask(gpxFilename, LiveGpsLayer.LAYER_NAME);
            gpsDataTimer.schedule(task, 1000, AUTO_SAVE_PERIOD_SEC * 1000);

            String osmFilename = MessageFormat.format(OSM_FILE_NAME_PATTERN, new Date());
            task = new AutoSaveEditLayerTimerTask(osmFilename);
            gpsDataTimer.schedule(task, 5000, AUTO_SAVE_PERIOD_SEC * 1000);
        } else {
            if(gpsDataTimer != null) {
                gpsDataTimer.cancel();
            }
        }


    }
}

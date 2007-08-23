/**
 * Copyright by Christof Dallermassl
 * This program is free software and licensed under GPL.
 */
package at.dallermassl.josm.plugin.surveyor;

import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;

import at.dallermassl.josm.plugin.surveyor.action.SetWaypointAction;

import livegps.LiveGpsLayer;

/**
 * @author cdaller
 *
 */
public class AutoSaveAction extends AbstractAction {
    private static final long serialVersionUID = -8608679323231116043L;
    private static final long AUTO_SAVE_PERIOD_SEC = 60; // once a minute
    public static final String GPS_FILE_NAME_PATTERN = "surveyor-{0,date,yyyyMMdd-HHmmss}.gpx";
    public static final String OSM_FILE_NAME_PATTERN = "surveyor-{0,date,yyyyMMdd-HHmmss}.osm";
    private boolean autoSave = false;
    private Timer gpsDataTimer;
    
    
    public AutoSaveAction(String name) {
        super(name);
        // <FIXXME date="23.06.2007" author="cdaller">
        // TODO set accelerator key
        // </FIXXME> 
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
            task = new AutoSaveGpsAndMarkerLayerTimeTask(gpxFilename, 
                LiveGpsLayer.LAYER_NAME, SetWaypointAction.MARKER_LAYER_NAME);
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

/**
 * Copyright by Christof Dallermassl
 * This program is free software and licensed under GPL.
 */
package at.dallermassl.josm.plugin.surveyor;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JFrame;

import org.openstreetmap.josm.Main;

import livegps.LiveGpsData;

/**
 * Action that fires a {@link SurveyorAction} to the registered actions.
 *
 * @author cdaller
 *
 */
public class MetaAction extends AbstractAction {
    private static final long serialVersionUID = -1523524381092575809L;
    private List<SurveyorActionDescription> actions;
    private GpsDataSource gpsDataSource;
    private long lastActionCall = 0;
    public static final long MIN_TIME_DIFF = 500; // 500ms

    /**
     *
     */
    public MetaAction() {
    }

    /**
     * @param name
     */
    public MetaAction(String name) {
        super(name);
    }

    /**
     * @param name
     * @param icon
     */
    public MetaAction(String name, Icon icon) {
        super(name, icon);
        // TODO Auto-generated constructor stub
    }

    /**
     * @return the actions
     */
    public List<SurveyorActionDescription> getActions() {
        return this.actions;
    }

    /**
     * @param actions
     *            the actions to set
     */
    public void setActions(List<SurveyorActionDescription> actions) {
        this.actions = actions;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // check if action was called by repeating key presses too long pressed):
        long time = System.currentTimeMillis();
        if ((time - lastActionCall) < MIN_TIME_DIFF) {
            lastActionCall = time;
            return;
        }
        lastActionCall = time;

        // toggle on/off
        Boolean selected = (Boolean) getValue(ActionConstants.SELECTED_KEY);
        if (selected == null || selected == Boolean.FALSE) {
            selected = Boolean.TRUE;
        } else {
            selected = Boolean.FALSE;
        }
        putValue(ActionConstants.SELECTED_KEY, selected);

        LiveGpsData gpsData = gpsDataSource.getGpsData();
        if (gpsData != null && gpsData.isFix()) {
            double latitude = gpsData.getLatitude();
            double longitude = gpsData.getLongitude();
            GpsActionEvent gpsEvent = new GpsActionEvent(e, latitude, longitude);
            for (SurveyorActionDescription action : actions) {
                action.actionPerformed(gpsEvent);
            }
        } else {
            System.out.println("Surveyor: no gps data available!");
            // TEST for offline test only:
            if(Main.pref.getBoolean("surveyor.debug")) {
                for (SurveyorActionDescription action : actions) {
                    action.actionPerformed(new GpsActionEvent(e, 0, 0));
                }
            }
        }
        JFrame frame = SurveyorPlugin.getSurveyorFrame();
        if(frame != null && frame.isVisible()) {
            frame.toFront();
        }
    }

    /**
     * @param gpsDataSource
     */
    public void setGpsDataSource(GpsDataSource gpsDataSource) {
        this.gpsDataSource = gpsDataSource;
    }
}

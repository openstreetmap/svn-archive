/**
 * 
 */
package at.dallermassl.josm.plugin.surveyor;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import test.DialogClosingThread;


import livegps.LiveGpsData;

/**
 * Action that fires a {@link SurveyorAction} to the registered actions.
 * 
 * @author cdaller
 * 
 */
public class MetaAction extends AbstractAction {
    private List<SurveyorActionDescription> actions;
    private GpsDataSource gpsDataSource;
    private long lastActionCall = 0;
    public static final long MIN_TIME_DIFF = 500; // 500ms

    /**
     * 
     */
    public MetaAction() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param name
     */
    public MetaAction(String name) {
        super(name);
        // TODO Auto-generated constructor stub
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

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        // check if action was called by repeating key presses too long pressed):
        long time = System.currentTimeMillis();
        if ((time - lastActionCall) < MIN_TIME_DIFF) {
            lastActionCall = time;
// System.out.println("repeating key detected");
            return;
        }
        lastActionCall = time;
        // System.out.println("meta action '" + super.toString() + "' called");

        // toggle on/off
        Boolean selected = (Boolean) getValue(ActionConstants.SELECTED_KEY);
        if (selected == null || selected == Boolean.FALSE) {
            selected = Boolean.TRUE;
        } else {
            selected = Boolean.FALSE;
        }
        putValue(ActionConstants.SELECTED_KEY, selected);

        LiveGpsData gpsData = gpsDataSource.getGpsData();
        if (gpsData != null) {
            double latitude = gpsData.getLatitude();
            double longitude = gpsData.getLongitude();
            GpsActionEvent gpsEvent = new GpsActionEvent(e, latitude, longitude);
            for (SurveyorActionDescription action : actions) {
                action.actionPerformed(gpsEvent);
            }
        } else {
            System.out.println("Surveyor: no gps data available!");
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

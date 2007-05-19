/**
 * 
 */
package at.dallermassl.josm.plugin.surveyor;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
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

import at.dallermassl.josm.plugin.surveyor.action.DialogClosingThread;

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
    }

    public void openDialog(JFrame frame) {
        final JOptionPane optionPane = new JOptionPane("The only way to close this dialog is by\n"
                        + "pressing one of the following buttons.\n" + "Do you understand?",
            JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);

        final JDialog dialog = new JDialog(frame, "Click a button", true);
        dialog.setContentPane(optionPane);
        optionPane.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                String prop = e.getPropertyName();

                if (dialog.isVisible() && (e.getSource() == optionPane)
                                && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                    // If you were going to check something
                    // before closing the window, you'd do
                    // it here.
                    dialog.setVisible(false);
                }
            }
        });
        Thread closer = new DialogClosingThread(dialog);
        closer.start();
        dialog.pack();
        dialog.setVisible(true);
        

        System.out.println("value: " + optionPane.getValue().getClass());

//        int value = ((Integer) optionPane.getValue()).intValue();
//        if (value == JOptionPane.YES_OPTION) {
//            System.out.println("yes");
//        } else if (value == JOptionPane.NO_OPTION) {
//            System.out.println("no");
//        }

    }

    /**
     * @param gpsDataSource
     */
    public void setGpsDataSource(GpsDataSource gpsDataSource) {
        this.gpsDataSource = gpsDataSource;
    }
    
    public static void main(String[] args) {
      //1. Create the frame.
        JFrame frame = new JFrame("FrameDemo");

        //2. Optional: What happens when the frame closes?
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //3. Create components and put them in the frame.
        //...create emptyLabel...
        frame.getContentPane().add(new JLabel("test"), BorderLayout.CENTER);

        //4. Size the frame.
        frame.pack();
        frame.setSize(600,400);
        frame.setLocation(0,0);

        //5. Show it.
        frame.setVisible(true);
        System.out.println("after visible");
        new MetaAction().openDialog(frame);
    }

}

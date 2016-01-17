package public_transport;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Frame;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;

/**
 * Abstract superclass of {@link GTFSImporterDialog} and {@link StopImporterDialog}.
 */
public abstract class AbstractImporterDialog<T extends JosmAction> {

    private static final String[] stoptypes = new String[]{marktr("bus"), marktr("tram"), marktr("light_rail"), marktr("subway"), marktr("rail")};

    private final JDialog jDialog;
    protected final JTabbedPane tabbedPane;
    protected final JComboBox<TransText> cbStoptype;

    protected final JTextField tfGPSTimeStart;
    protected final JTextField tfStopwatchStart;
    protected final JTextField tfTimeWindow;
    protected final JTextField tfThreshold;

    public AbstractImporterDialog(T controller, String dialogTitle, String actionPrefix) {
        Frame frame = JOptionPane.getFrameForComponent(Main.parent);
        jDialog = new JDialog(frame, dialogTitle, false);
        tabbedPane = new JTabbedPane();
        jDialog.add(tabbedPane);
        
        cbStoptype = new JComboBox<>();
        cbStoptype.setEditable(false);
        for(String type : stoptypes)
            cbStoptype.addItem(new TransText(type));
        cbStoptype.setActionCommand(actionPrefix + ".settingsStoptype");
        cbStoptype.addActionListener(controller);
        
        tfGPSTimeStart = new JTextField("00:00:00", 15);
        tfGPSTimeStart.setActionCommand(actionPrefix + ".settingsGPSTimeStart");
        tfGPSTimeStart.addActionListener(controller);

        tfStopwatchStart = new JTextField("00:00:00", 15);
        tfStopwatchStart.setActionCommand(actionPrefix + ".settingsStopwatchStart");
        tfStopwatchStart.addActionListener(controller);

        tfTimeWindow = new JTextField("15", 4);
        tfTimeWindow.setActionCommand(actionPrefix + ".settingsTimeWindow");
        tfTimeWindow.addActionListener(controller);

        tfThreshold = new JTextField("20", 4);
        tfThreshold.setActionCommand(actionPrefix + ".settingsThreshold");
        tfThreshold.addActionListener(controller);

        initDialog(controller);
        
        jDialog.pack();
        jDialog.setLocationRelativeTo(frame);
    }
    
    protected abstract void initDialog(T controller);
    
    public void setTrackValid(boolean valid)
    {
      tabbedPane.setEnabledAt(2, valid);
    }

    public void setVisible(boolean visible)
    {
      jDialog.setVisible(visible);
    }

    public void setSettings
        (String gpsSyncTime, String stopwatchStart,
         double timeWindow, double threshold)
    {
      tfGPSTimeStart.setText(gpsSyncTime);
      tfStopwatchStart.setText(stopwatchStart);
      tfTimeWindow.setText(Double.toString(timeWindow));
      tfThreshold.setText(Double.toString(threshold));
    }

    public String getStoptype()
    {
      return ((TransText)cbStoptype.getSelectedItem()).text;
    }

    public boolean gpsTimeStartValid()
    {
      if (parseTime(tfGPSTimeStart.getText()) >= 0)
      {
        return true;
      }
      else
      {
        JOptionPane.showMessageDialog
        (null, tr("Can''t parse a time from this string."), tr("Invalid value"),
         JOptionPane.ERROR_MESSAGE);
        return false;
      }
    }
    
    public String getGpsTimeStart()
    {
      return tfGPSTimeStart.getText();
    }

    public void setGpsTimeStart(String s)
    {
      tfGPSTimeStart.setText(s);
    }

    public boolean stopwatchStartValid()
    {
      if (parseTime(tfStopwatchStart.getText()) >= 0)
      {
        return true;
      }
      else
      {
        JOptionPane.showMessageDialog
        (null, tr("Can''t parse a time from this string."), tr("Invalid value"),
         JOptionPane.ERROR_MESSAGE);
        return false;
      }
    }

    public String getStopwatchStart()
    {
      return tfStopwatchStart.getText();
    }

    public void setStopwatchStart(String s)
    {
      tfStopwatchStart.setText(s);
    }

    public double getTimeWindow()
    {
      return Double.parseDouble(tfTimeWindow.getText());
    }
    
    public double getThreshold()
    {
      return Double.parseDouble(tfThreshold.getText());
    }

    public static double parseTime(String s)
    {
      if ((s.charAt(2) != ':') || (s.charAt(2) != ':')
       || (s.length() < 8))
        return -1;
      int hour = Integer.parseInt(s.substring(0, 2));
      int minute = Integer.parseInt(s.substring(3, 5));
      double second = Double.parseDouble(s.substring(6, s.length()));
      if ((hour < 0) || (hour > 23) || (minute < 0) || (minute > 59)
       || (second < 0) || (second >= 60.0))
        return -1;
      return (second + minute*60 + hour*60*60);
    }
}

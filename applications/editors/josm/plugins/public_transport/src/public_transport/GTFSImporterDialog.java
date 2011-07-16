package public_transport;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.GpxTrackSegment;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.io.GpxReader;

import org.xml.sax.SAXException;

public class GTFSImporterDialog
{
  private JDialog jDialog = null;
  private JTabbedPane tabbedPane = null;
  private JComboBox cbStoptype = null;
  private JList tracksList = null;
  private JTextField tfGPSTimeStart = null;
  private JTextField tfStopwatchStart = null;
  private JTextField tfTimeWindow = null;
  private JTextField tfThreshold = null;
  private JTable stoplistTable = null;
  private JTable gtfsStopTable = null;
  private final String[] stoptypes = new String[]{marktr("bus"), marktr("tram"), marktr("light_rail"), marktr("subway"), marktr("rail")};

  public GTFSImporterDialog(GTFSImporterAction controller)
  {
    Frame frame = JOptionPane.getFrameForComponent(Main.parent);
    jDialog = new JDialog(frame, tr("Create Stops from GTFS"), false);
    tabbedPane = new JTabbedPane();
    JPanel tabSettings = new JPanel();
    tabbedPane.addTab(tr("Settings"), tabSettings);
    JPanel tabWaypoints = new JPanel();
    tabbedPane.addTab(tr("GTFS-Stops"), tabWaypoints);
    tabbedPane.setEnabledAt(0, false);
    tabbedPane.setEnabledAt(1, true);
    jDialog.add(tabbedPane);

    //Settings Tab
    JPanel contentPane = tabSettings;
    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints layoutCons = new GridBagConstraints();
    contentPane.setLayout(gridbag);

    JLabel label = new JLabel(tr("Type of stops to add"));

    layoutCons.gridx = 0;
    layoutCons.gridy = 0;
    layoutCons.gridwidth = 2;
    layoutCons.weightx = 0.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(label, layoutCons);
    contentPane.add(label);

    cbStoptype = new JComboBox();
    cbStoptype.setEditable(false);
    for(String type : stoptypes)
        cbStoptype.addItem(new TransText(type));
    cbStoptype.setActionCommand("gtfsImporter.settingsStoptype");
    cbStoptype.addActionListener(controller);

    layoutCons.gridx = 0;
    layoutCons.gridy = 1;
    layoutCons.gridwidth = 1;
    layoutCons.weightx = 0.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(cbStoptype, layoutCons);
    contentPane.add(cbStoptype);

    label = new JLabel(tr("Time on your GPS device"));

    layoutCons.gridx = 0;
    layoutCons.gridy = 2;
    layoutCons.gridwidth = 2;
    layoutCons.weightx = 0.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(label, layoutCons);
    contentPane.add(label);

    tfGPSTimeStart = new JTextField("00:00:00", 15);
    tfGPSTimeStart.setActionCommand("gtfsImporter.settingsGPSTimeStart");
    tfGPSTimeStart.addActionListener(controller);

    layoutCons.gridx = 0;
    layoutCons.gridy = 3;
    layoutCons.gridwidth = 1;
    layoutCons.weightx = 0.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(tfGPSTimeStart, layoutCons);
    contentPane.add(tfGPSTimeStart);

    /* I18n: Don't change the time format, you only may translate the letters */
    label = new JLabel(tr("HH:MM:SS.sss"));

    layoutCons.gridx = 1;
    layoutCons.gridy = 3;
    layoutCons.gridwidth = 1;
    layoutCons.weightx = 0.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(label, layoutCons);
    contentPane.add(label);

    label = new JLabel(tr("Time on your stopwatch"));

    layoutCons.gridx = 0;
    layoutCons.gridy = 4;
    layoutCons.gridwidth = 2;
    layoutCons.weightx = 0.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(label, layoutCons);
    contentPane.add(label);

    tfStopwatchStart = new JTextField("00:00:00", 15);
    tfStopwatchStart.setActionCommand("gtfsImporter.settingsStopwatchStart");
    tfStopwatchStart.addActionListener(controller);

    layoutCons.gridx = 0;
    layoutCons.gridy = 5;
    layoutCons.gridwidth = 1;
    layoutCons.weightx = 0.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(tfStopwatchStart, layoutCons);
    contentPane.add(tfStopwatchStart);

    /* I18n: Don't change the time format, you only may translate the letters */
    label = new JLabel(tr("HH:MM:SS.sss"));

    layoutCons.gridx = 1;
    layoutCons.gridy = 5;
    layoutCons.gridwidth = 1;
    layoutCons.weightx = 0.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(label, layoutCons);
    contentPane.add(label);

    label = new JLabel(tr("Time window"));

    layoutCons.gridx = 0;
    layoutCons.gridy = 6;
    layoutCons.gridwidth = 2;
    layoutCons.weightx = 0.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(label, layoutCons);
    contentPane.add(label);

    tfTimeWindow = new JTextField("15", 4);
    tfTimeWindow.setActionCommand("gtfsImporter.settingsTimeWindow");
    tfTimeWindow.addActionListener(controller);

    layoutCons.gridx = 0;
    layoutCons.gridy = 7;
    layoutCons.gridwidth = 1;
    layoutCons.weightx = 0.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(tfTimeWindow, layoutCons);
    contentPane.add(tfTimeWindow);

    label = new JLabel(tr("seconds"));

    layoutCons.gridx = 1;
    layoutCons.gridy = 7;
    layoutCons.gridwidth = 1;
    layoutCons.weightx = 0.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(label, layoutCons);
    contentPane.add(label);

    label = new JLabel(tr("Move Threshold"));

    layoutCons.gridx = 0;
    layoutCons.gridy = 8;
    layoutCons.gridwidth = 2;
    layoutCons.weightx = 0.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(label, layoutCons);
    contentPane.add(label);

    tfThreshold = new JTextField("20", 4);
    tfThreshold.setActionCommand("gtfsImporter.settingsThreshold");
    tfThreshold.addActionListener(controller);

    layoutCons.gridx = 0;
    layoutCons.gridy = 9;
    layoutCons.gridwidth = 1;
    layoutCons.weightx = 0.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(tfThreshold, layoutCons);
    contentPane.add(tfThreshold);

    label = new JLabel(tr("meters"));

    layoutCons.gridx = 1;
    layoutCons.gridy = 9;
    layoutCons.gridwidth = 1;
    layoutCons.weightx = 0.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(label, layoutCons);
    contentPane.add(label);

    JButton bSuggestStops = new JButton(tr("Suggest Stops"));
    bSuggestStops.setActionCommand("gtfsImporter.settingsSuggestStops");
    bSuggestStops.addActionListener(controller);

    layoutCons.gridx = 0;
    layoutCons.gridy = 10;
    layoutCons.gridwidth = 3;
    layoutCons.weightx = 1.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(bSuggestStops, layoutCons);
    contentPane.add(bSuggestStops);

    //Waypoints Tab
    contentPane = tabWaypoints;
    gridbag = new GridBagLayout();
    layoutCons = new GridBagConstraints();
    contentPane.setLayout(gridbag);
    contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put
        (KeyStroke.getKeyStroke("alt N"), "gtfsImporter.gtfsStopsFocusAdd");
    contentPane.getActionMap().put
    ("gtfsImporter.gtfsStopsFocusAdd", controller.getFocusAddAction());
/*    contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put
        (KeyStroke.getKeyStroke("alt S"), "gtfsImporter.focusShelterYes");
    contentPane.getActionMap().put
    ("gtfsImporter.focusShelterYes",
     controller.getFocusWaypointShelterAction("yes"));
    contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put
        (KeyStroke.getKeyStroke("alt T"), "gtfsImporter.focusShelterNo");
    contentPane.getActionMap().put
    ("gtfsImporter.focusShelterNo",
     controller.getFocusWaypointShelterAction("no"));
    contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put
        (KeyStroke.getKeyStroke("alt U"), "gtfsImporter.focusShelterImplicit");
    contentPane.getActionMap().put
    ("gtfsImporter.focusShelterImplicit",
     controller.getFocusWaypointShelterAction("implicit"));
    contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put
        (KeyStroke.getKeyStroke("alt D"), "gtfsImporter.gtfsStopsDelete");
    contentPane.getActionMap().put
    ("gtfsImporter.gtfsStopsDelete",
     controller.getFocusWaypointDeleteAction());*/

    gtfsStopTable = new JTable();
    JScrollPane tableSP = new JScrollPane(gtfsStopTable);

    layoutCons.gridx = 0;
    layoutCons.gridy = 0;
    layoutCons.gridwidth = 4;
    layoutCons.weightx = 1.0;
    layoutCons.weighty = 1.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(tableSP, layoutCons);
    contentPane.add(tableSP);

    JButton bFind = new JButton(tr("Find"));
    bFind.setActionCommand("gtfsImporter.gtfsStopsFind");
    bFind.addActionListener(controller);

    layoutCons.gridx = 0;
    layoutCons.gridy = 1;
    layoutCons.gridwidth = 1;
    layoutCons.weightx = 1.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(bFind, layoutCons);
    contentPane.add(bFind);

    JButton bShow = new JButton(tr("Show"));
    bShow.setActionCommand("gtfsImporter.gtfsStopsShow");
    bShow.addActionListener(controller);

    layoutCons.gridx = 0;
    layoutCons.gridy = 2;
    layoutCons.gridwidth = 1;
    layoutCons.weightx = 1.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(bShow, layoutCons);
    contentPane.add(bShow);

    JButton bMark = new JButton(tr("Mark"));
    bMark.setActionCommand("gtfsImporter.gtfsStopsMark");
    bMark.addActionListener(controller);

    layoutCons.gridx = 1;
    layoutCons.gridy = 1;
    layoutCons.gridheight = 2;
    layoutCons.gridwidth = 1;
    layoutCons.weightx = 1.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(bMark, layoutCons);
    contentPane.add(bMark);

    JButton bCatch = new JButton(tr("Catch"));
    bCatch.setActionCommand("gtfsImporter.gtfsStopsCatch");
    bCatch.addActionListener(controller);

    layoutCons.gridx = 2;
    layoutCons.gridy = 1;
    layoutCons.gridheight = 1;
    layoutCons.gridwidth = 1;
    layoutCons.weightx = 1.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(bCatch, layoutCons);
    contentPane.add(bCatch);

    JButton bJoin = new JButton(tr("Join"));
    bJoin.setActionCommand("gtfsImporter.gtfsStopsJoin");
    bJoin.addActionListener(controller);

    layoutCons.gridx = 2;
    layoutCons.gridy = 2;
    layoutCons.gridheight = 1;
    layoutCons.gridwidth = 1;
    layoutCons.weightx = 1.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(bJoin, layoutCons);
    contentPane.add(bJoin);

    JButton bAdd = new JButton(tr("Enable"));
    bAdd.setActionCommand("gtfsImporter.gtfsStopsAdd");
    bAdd.addActionListener(controller);

    layoutCons.gridx = 3;
    layoutCons.gridy = 1;
    layoutCons.gridheight = 1;
    layoutCons.gridwidth = 1;
    layoutCons.weightx = 1.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(bAdd, layoutCons);
    contentPane.add(bAdd);

    JButton bDelete = new JButton(tr("Disable"));
    bDelete.setActionCommand("gtfsImporter.gtfsStopsDelete");
    bDelete.addActionListener(controller);

    layoutCons.gridx = 3;
    layoutCons.gridy = 2;
    layoutCons.gridwidth = 1;
    layoutCons.weightx = 1.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(bDelete, layoutCons);
    contentPane.add(bDelete);

    jDialog.pack();
    jDialog.setLocationRelativeTo(frame);
  }

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

  public JTable getGTFSStopTable()
  {
    return gtfsStopTable;
  }

  public void setGTFSStopTableModel(GTFSStopTableModel model)
  {
    gtfsStopTable.setModel(model);
    int width = gtfsStopTable.getPreferredSize().width;
    gtfsStopTable.getColumnModel().getColumn(0).setPreferredWidth((int)(width * 0.3));
    gtfsStopTable.getColumnModel().getColumn(1).setPreferredWidth((int)(width * 0.6));
    gtfsStopTable.getColumnModel().getColumn(2).setPreferredWidth((int)(width * 0.1));
  }

  public static double parseTime(String s)
  {
    double result = 0;
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

/*  private class TracksLSL implements ListSelectionListener
  {
    GTFSImporterAction root = null;

    public TracksLSL(GTFSImporterAction sia)
    {
      root = sia;
    }

    public void valueChanged(ListSelectionEvent e)
    {
      int selectedPos = tracksList.getAnchorSelectionIndex();
      if (tracksList.isSelectedIndex(selectedPos))
    root.tracksSelectionChanged(selectedPos);
      else
    root.tracksSelectionChanged(-1);
    }
  };*/
}

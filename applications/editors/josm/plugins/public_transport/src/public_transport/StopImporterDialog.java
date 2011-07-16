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

public class StopImporterDialog
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
  private JTable waypointTable = null;

  private final String[] stoptypes = new String[]{marktr("bus"), marktr("tram"), marktr("light_rail"), marktr("subway"), marktr("rail")};

  public StopImporterDialog(StopImporterAction controller)
  {
    Frame frame = JOptionPane.getFrameForComponent(Main.parent);
    jDialog = new JDialog(frame, tr("Create Stops from GPX"), false);
    tabbedPane = new JTabbedPane();
    JPanel tabTracks = new JPanel();
    tabbedPane.addTab(tr("Tracks"), tabTracks);
    JPanel tabSettings = new JPanel();
    tabbedPane.addTab(tr("Settings"), tabSettings);
    JPanel tabStops = new JPanel();
    tabbedPane.addTab(tr("Stops"), tabStops);
    JPanel tabWaypoints = new JPanel();
    tabbedPane.addTab(tr("Waypoints"), tabWaypoints);
    tabbedPane.setEnabledAt(0, true);
    tabbedPane.setEnabledAt(1, true);
    tabbedPane.setEnabledAt(2, false);
    tabbedPane.setEnabledAt(3, true);
    jDialog.add(tabbedPane);

    //Tracks Tab
    JPanel contentPane = tabTracks;
    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints layoutCons = new GridBagConstraints();
    contentPane.setLayout(gridbag);

    JLabel label = new JLabel(tr("Tracks in this GPX file:"));

    layoutCons.gridx = 0;
    layoutCons.gridy = 0;
    layoutCons.gridwidth = 3;
    layoutCons.weightx = 0.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(label, layoutCons);
    contentPane.add(label);

    DefaultListModel tracksListModel = controller.getTracksListModel();
    tracksList = new JList(tracksListModel);
    JScrollPane rpListSP = new JScrollPane(tracksList);
    String[] data = {"1", "2", "3", "4", "5", "6"};
    tracksListModel.copyInto(data);
    tracksList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    tracksList.addListSelectionListener(new TracksLSL(controller));

    layoutCons.gridx = 0;
    layoutCons.gridy = 1;
    layoutCons.gridwidth = 3;
    layoutCons.weightx = 1.0;
    layoutCons.weighty = 1.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(rpListSP, layoutCons);
    contentPane.add(rpListSP);

    //Settings Tab
    contentPane = tabSettings;
    gridbag = new GridBagLayout();
    layoutCons = new GridBagConstraints();
    contentPane.setLayout(gridbag);

    label = new JLabel(tr("Type of stops to add"));

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
    cbStoptype.setActionCommand("stopImporter.settingsStoptype");
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
    tfGPSTimeStart.setActionCommand("stopImporter.settingsGPSTimeStart");
    tfGPSTimeStart.addActionListener(controller);

    layoutCons.gridx = 0;
    layoutCons.gridy = 3;
    layoutCons.gridwidth = 1;
    layoutCons.weightx = 0.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(tfGPSTimeStart, layoutCons);
    contentPane.add(tfGPSTimeStart);

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
    tfStopwatchStart.setActionCommand("stopImporter.settingsStopwatchStart");
    tfStopwatchStart.addActionListener(controller);

    layoutCons.gridx = 0;
    layoutCons.gridy = 5;
    layoutCons.gridwidth = 1;
    layoutCons.weightx = 0.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(tfStopwatchStart, layoutCons);
    contentPane.add(tfStopwatchStart);

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
    tfTimeWindow.setActionCommand("stopImporter.settingsTimeWindow");
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
    tfThreshold.setActionCommand("stopImporter.settingsThreshold");
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
    bSuggestStops.setActionCommand("stopImporter.settingsSuggestStops");
    bSuggestStops.addActionListener(controller);

    layoutCons.gridx = 0;
    layoutCons.gridy = 10;
    layoutCons.gridwidth = 3;
    layoutCons.weightx = 1.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(bSuggestStops, layoutCons);
    contentPane.add(bSuggestStops);

    //Stops Tab
    contentPane = tabStops;
    gridbag = new GridBagLayout();
    layoutCons = new GridBagConstraints();
    contentPane.setLayout(gridbag);
    contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put
        (KeyStroke.getKeyStroke("alt N"), "stopImporter.focusName");
    contentPane.getActionMap().put
    ("stopImporter.focusName", controller.getFocusTrackStoplistNameAction());
    contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put
        (KeyStroke.getKeyStroke("alt S"), "stopImporter.focusShelterYes");
    contentPane.getActionMap().put
    ("stopImporter.focusShelterYes",
     controller.getFocusTrackStoplistShelterAction("yes"));
    contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put
        (KeyStroke.getKeyStroke("alt T"), "stopImporter.focusShelterNo");
    contentPane.getActionMap().put
    ("stopImporter.focusShelterNo",
     controller.getFocusTrackStoplistShelterAction("no"));
    contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put
        (KeyStroke.getKeyStroke("alt U"), "stopImporter.focusShelterImplicit");
    contentPane.getActionMap().put
    ("stopImporter.focusShelterImplicit",
     controller.getFocusTrackStoplistShelterAction("implicit"));
    contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put
        (KeyStroke.getKeyStroke("alt D"), "stopImporter.stoplistDelete");
    contentPane.getActionMap().put
    ("stopImporter.stoplistDelete",
     controller.getFocusStoplistDeleteAction());

    stoplistTable = new JTable();
    JScrollPane tableSP = new JScrollPane(stoplistTable);

    layoutCons.gridx = 0;
    layoutCons.gridy = 0;
    layoutCons.gridwidth = 4;
    layoutCons.weightx = 1.0;
    layoutCons.weighty = 1.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(tableSP, layoutCons);
    contentPane.add(tableSP);

    JButton bFind = new JButton(tr("Find"));
    bFind.setActionCommand("stopImporter.stoplistFind");
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
    bShow.setActionCommand("stopImporter.stoplistShow");
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
    bMark.setActionCommand("stopImporter.stoplistMark");
    bMark.addActionListener(controller);

    layoutCons.gridx = 1;
    layoutCons.gridy = 1;
    layoutCons.gridheight = 1;
    layoutCons.gridwidth = 1;
    layoutCons.weightx = 1.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(bMark, layoutCons);
    contentPane.add(bMark);

    JButton bDetach = new JButton(tr("Detach"));
    bDetach.setActionCommand("stopImporter.stoplistDetach");
    bDetach.addActionListener(controller);

    layoutCons.gridx = 1;
    layoutCons.gridy = 2;
    layoutCons.gridheight = 1;
    layoutCons.gridwidth = 1;
    layoutCons.weightx = 1.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(bDetach, layoutCons);
    contentPane.add(bDetach);

    JButton bAdd = new JButton(tr("Add"));
    bAdd.setActionCommand("stopImporter.stoplistAdd");
    bAdd.addActionListener(controller);

    layoutCons.gridx = 2;
    layoutCons.gridy = 1;
    layoutCons.gridheight = 1;
    layoutCons.gridwidth = 1;
    layoutCons.weightx = 1.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(bAdd, layoutCons);
    contentPane.add(bAdd);

    JButton bDelete = new JButton(tr("Delete"));
    bDelete.setActionCommand("stopImporter.stoplistDelete");
    bDelete.addActionListener(controller);

    layoutCons.gridx = 2;
    layoutCons.gridy = 2;
    layoutCons.gridwidth = 1;
    layoutCons.weightx = 1.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(bDelete, layoutCons);
    contentPane.add(bDelete);

    JButton bSort = new JButton(tr("Sort"));
    bSort.setActionCommand("stopImporter.stoplistSort");
    bSort.addActionListener(controller);

    layoutCons.gridx = 3;
    layoutCons.gridy = 1;
    layoutCons.gridheight = 2;
    layoutCons.gridwidth = 1;
    layoutCons.weightx = 1.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(bSort, layoutCons);
    contentPane.add(bSort);

    //Waypoints Tab
    contentPane = tabWaypoints;
    gridbag = new GridBagLayout();
    layoutCons = new GridBagConstraints();
    contentPane.setLayout(gridbag);
    contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put
        (KeyStroke.getKeyStroke("alt N"), "stopImporter.focusName");
    contentPane.getActionMap().put
    ("stopImporter.focusName", controller.getFocusWaypointNameAction());
    contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put
        (KeyStroke.getKeyStroke("alt S"), "stopImporter.focusShelterYes");
    contentPane.getActionMap().put
    ("stopImporter.focusShelterYes",
     controller.getFocusWaypointShelterAction("yes"));
    contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put
        (KeyStroke.getKeyStroke("alt T"), "stopImporter.focusShelterNo");
    contentPane.getActionMap().put
    ("stopImporter.focusShelterNo",
     controller.getFocusWaypointShelterAction("no"));
    contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put
        (KeyStroke.getKeyStroke("alt U"), "stopImporter.focusShelterImplicit");
    contentPane.getActionMap().put
    ("stopImporter.focusShelterImplicit",
     controller.getFocusWaypointShelterAction("implicit"));
    contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put
        (KeyStroke.getKeyStroke("alt D"), "stopImporter.waypointsDelete");
    contentPane.getActionMap().put
    ("stopImporter.waypointsDelete",
     controller.getFocusWaypointDeleteAction());

    waypointTable = new JTable();
    tableSP = new JScrollPane(waypointTable);

    layoutCons.gridx = 0;
    layoutCons.gridy = 0;
    layoutCons.gridwidth = 3;
    layoutCons.weightx = 1.0;
    layoutCons.weighty = 1.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(tableSP, layoutCons);
    contentPane.add(tableSP);

    bFind = new JButton(tr("Find"));
    bFind.setActionCommand("stopImporter.waypointsFind");
    bFind.addActionListener(controller);

    layoutCons.gridx = 0;
    layoutCons.gridy = 1;
    layoutCons.gridwidth = 1;
    layoutCons.weightx = 1.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(bFind, layoutCons);
    contentPane.add(bFind);

    bShow = new JButton(tr("Show"));
    bShow.setActionCommand("stopImporter.waypointsShow");
    bShow.addActionListener(controller);

    layoutCons.gridx = 0;
    layoutCons.gridy = 2;
    layoutCons.gridwidth = 1;
    layoutCons.weightx = 1.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(bShow, layoutCons);
    contentPane.add(bShow);

    bMark = new JButton(tr("Mark"));
    bMark.setActionCommand("stopImporter.waypointsMark");
    bMark.addActionListener(controller);

    layoutCons.gridx = 1;
    layoutCons.gridy = 1;
    layoutCons.gridheight = 1;
    layoutCons.gridwidth = 1;
    layoutCons.weightx = 1.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(bMark, layoutCons);
    contentPane.add(bMark);

    bDetach = new JButton(tr("Detach"));
    bDetach.setActionCommand("stopImporter.waypointsDetach");
    bDetach.addActionListener(controller);

    layoutCons.gridx = 1;
    layoutCons.gridy = 2;
    layoutCons.gridheight = 1;
    layoutCons.gridwidth = 1;
    layoutCons.weightx = 1.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(bDetach, layoutCons);
    contentPane.add(bDetach);

    bAdd = new JButton(tr("Enable"));
    bAdd.setActionCommand("stopImporter.waypointsAdd");
    bAdd.addActionListener(controller);

    layoutCons.gridx = 2;
    layoutCons.gridy = 1;
    layoutCons.gridheight = 1;
    layoutCons.gridwidth = 1;
    layoutCons.weightx = 1.0;
    layoutCons.weighty = 0.0;
    layoutCons.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(bAdd, layoutCons);
    contentPane.add(bAdd);

    bDelete = new JButton(tr("Disable"));
    bDelete.setActionCommand("stopImporter.waypointsDelete");
    bDelete.addActionListener(controller);

    layoutCons.gridx = 2;
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

  public JTable getStoplistTable()
  {
    return stoplistTable;
  }

  public void setStoplistTableModel(TrackStoplistTableModel model)
  {
    stoplistTable.setModel(model);
    JComboBox comboBox = new JComboBox();
    comboBox.addItem(new TransText(null));
    comboBox.addItem(new TransText(marktr("yes")));
    comboBox.addItem(new TransText(marktr("no")));
    comboBox.addItem(new TransText(marktr("implicit")));
    stoplistTable.getColumnModel().getColumn(2)
    .setCellEditor(new DefaultCellEditor(comboBox));
    int width = stoplistTable.getPreferredSize().width;
    stoplistTable.getColumnModel().getColumn(0).setPreferredWidth((int)(width * 0.4));
    stoplistTable.getColumnModel().getColumn(1).setPreferredWidth((int)(width * 0.5));
    stoplistTable.getColumnModel().getColumn(2).setPreferredWidth((int)(width * 0.1));
  }

  public JTable getWaypointsTable()
  {
    return waypointTable;
  }

  public void setWaypointsTableModel(WaypointTableModel model)
  {
    waypointTable.setModel(model);
    JComboBox comboBox = new JComboBox();
    comboBox.addItem(new TransText(null));
    comboBox.addItem(new TransText(marktr("yes")));
    comboBox.addItem(new TransText(marktr("no")));
    comboBox.addItem(new TransText(marktr("implicit")));
    waypointTable.getColumnModel().getColumn(2)
    .setCellEditor(new DefaultCellEditor(comboBox));
    int width = waypointTable.getPreferredSize().width;
    waypointTable.getColumnModel().getColumn(0).setPreferredWidth((int)(width * 0.4));
    waypointTable.getColumnModel().getColumn(1).setPreferredWidth((int)(width * 0.5));
    waypointTable.getColumnModel().getColumn(2).setPreferredWidth((int)(width * 0.1));
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

  private class TracksLSL implements ListSelectionListener
  {
    StopImporterAction root = null;

    public TracksLSL(StopImporterAction sia)
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
  };
}

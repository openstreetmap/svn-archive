package public_transport;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
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

public class GTFSImporterAction extends JosmAction
{
  private static GTFSImporterDialog dialog = null;
  private static DefaultListModel tracksListModel = null;
  private static Vector< String > data = null;
  private static TrackReference currentTrack = null;
  private static GTFSStopTableModel gtfsStopTM = null;
  public boolean inEvent = false;

  public GTFSImporterAction()
  {
    super(tr("Create Stops from GTFS ..."), null,
      tr("Create Stops from a GTFS file"), null, false);
      putValue("toolbar", "publictransport/gtfsimporter");
      Main.toolbar.register(this);
  }

  public GTFSStopTableModel getGTFSStopTableModel()
  {
    return gtfsStopTM;
  }

  public GTFSImporterDialog getDialog()
  {
    return dialog;
  }

  public DefaultListModel getTracksListModel()
  {
    if (tracksListModel == null)
      tracksListModel = new DefaultListModel();
    return tracksListModel;
  }

  public TrackReference getCurrentTrack()
  {
    return currentTrack;
  }

  public void actionPerformed(ActionEvent event)
  {
    DataSet mainDataSet = Main.main.getCurrentDataSet();

    if (dialog == null)
      dialog = new GTFSImporterDialog(this);

    dialog.setVisible(true);

    if (tr("Create Stops from GTFS ...").equals(event.getActionCommand()))
    {
      String curDir = Main.pref.get("lastDirectory");
      if (curDir.equals(""))
      {
        curDir = ".";
      }
      JFileChooser fc = new JFileChooser(new File(curDir));
      fc.setDialogTitle(tr("Select GTFS file (stops.txt)"));
      fc.setMultiSelectionEnabled(false);

      int answer = fc.showOpenDialog(Main.parent);
      if (answer != JFileChooser.APPROVE_OPTION)
        return;

      if (!fc.getCurrentDirectory().getAbsolutePath().equals(curDir))
        Main.pref.put("lastDirectory", fc.getCurrentDirectory().getAbsolutePath());

      importData(fc.getSelectedFile());

      refreshData();
    }
/*    else if ("stopImporter.settingsGPSTimeStart".equals(event.getActionCommand()))
    {
      if ((!inEvent) && (dialog.gpsTimeStartValid()) && (currentTrack != null))
    Main.main.undoRedo.add(new TrackStoplistRelocateCommand(this));
    }
    else if ("stopImporter.settingsStopwatchStart".equals(event.getActionCommand()))
    {
      if ((!inEvent) && (dialog.stopwatchStartValid()) && (currentTrack != null))
    Main.main.undoRedo.add(new TrackStoplistRelocateCommand(this));
    }
    else if ("stopImporter.settingsTimeWindow".equals(event.getActionCommand()))
    {
      if (currentTrack != null)
    currentTrack.timeWindow = dialog.getTimeWindow();
    }
    else if ("stopImporter.settingsThreshold".equals(event.getActionCommand()))
    {
      if (currentTrack != null)
    currentTrack.threshold = dialog.getThreshold();
    }
    else if ("stopImporter.settingsSuggestStops".equals(event.getActionCommand()))
      Main.main.undoRedo.add(new TrackSuggestStopsCommand(this));
    else if ("stopImporter.stoplistFind".equals(event.getActionCommand()))
      findNodesInTable(dialog.getStoplistTable(), currentTrack.stoplistTM.getNodes());
    else if ("stopImporter.stoplistShow".equals(event.getActionCommand()))
      showNodesFromTable(dialog.getStoplistTable(), currentTrack.stoplistTM.getNodes());
    else if ("stopImporter.stoplistMark".equals(event.getActionCommand()))
      markNodesFromTable(dialog.getStoplistTable(), currentTrack.stoplistTM.getNodes());
    else if ("stopImporter.stoplistDetach".equals(event.getActionCommand()))
    {
      Main.main.undoRedo.add(new TrackStoplistDetachCommand(this));
      dialog.getStoplistTable().clearSelection();
    }*/
    else if ("gtfsImporter.gtfsStopsAdd".equals(event.getActionCommand()))
      Main.main.undoRedo.add(new GTFSAddCommand(this));
    else if ("gtfsImporter.gtfsStopsDelete".equals(event.getActionCommand()))
      Main.main.undoRedo.add(new GTFSDeleteCommand(this));
    else if ("gtfsImporter.gtfsStopsCatch".equals(event.getActionCommand()))
      Main.main.undoRedo.add(new GTFSCatchCommand(this));
    else if ("gtfsImporter.gtfsStopsJoin".equals(event.getActionCommand()))
      Main.main.undoRedo.add(new GTFSJoinCommand(this));
    else if ("gtfsImporter.gtfsStopsFind".equals(event.getActionCommand()))
      findNodesInTable(dialog.getGTFSStopTable(), gtfsStopTM.nodes);
    else if ("gtfsImporter.gtfsStopsShow".equals(event.getActionCommand()))
      showNodesFromTable(dialog.getGTFSStopTable(), gtfsStopTM.nodes);
    else if ("gtfsImporter.gtfsStopsMark".equals(event.getActionCommand()))
      markNodesFromTable(dialog.getGTFSStopTable(), gtfsStopTM.nodes);
  }

  private void importData(final File file)
  {
    try
    {
      FileReader is = new FileReader(file);
      final BufferedReader r = new BufferedReader(is);

      if (data == null)
    data = new Vector< String >();
      else
    data.clear();

      while (r.ready())
    data.add(r.readLine());
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, tr("File \"{0}\" does not exist", file.getName()));
    }
    catch (IOException e)
    {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, tr("IOException \"{0}\" occurred", e.toString()));
    }
  }

  private void refreshData()
  {
    if (data != null)
    {
      Vector< Node > existingStops = new Vector< Node >();

      if (Main.main.getCurrentDataSet() == null)
      {
        JOptionPane.showMessageDialog(null, tr("There exists no dataset."
        + " Try to download data from the server or open an OSM file."),
        tr("No data found"), JOptionPane.ERROR_MESSAGE);

        return;
      }
      else
      {
        Iterator< Node > iter =
        Main.main.getCurrentDataSet().getNodes().iterator();
        while (iter.hasNext())
        {
          Node node = iter.next();
          if ("bus_stop".equals(node.get("highway")))
            existingStops.add(node);
        }
      }

      Iterator< String > iter = data.iterator();
      if (iter.hasNext())
        gtfsStopTM = new GTFSStopTableModel(this, iter.next());
      else
      {
        JOptionPane.showMessageDialog
        (null, tr("The GTFS file was empty."), tr("No data found"),
        JOptionPane.ERROR_MESSAGE);

        return;
      }

      while (iter.hasNext())
      {
        String s = iter.next();
        gtfsStopTM.addRow(s, existingStops);
      }
      dialog.setGTFSStopTableModel(gtfsStopTM);
    }
    else
    {
      JOptionPane.showMessageDialog
      (null, tr("The GTFS file was empty."), tr("No data found"),
       JOptionPane.ERROR_MESSAGE);
    }
  }

//   public void tracksSelectionChanged(int selectedPos)
//   {
//     if (selectedPos >= 0)
//     {
//       currentTrack = ((TrackReference)tracksListModel.elementAt(selectedPos));
//       dialog.setTrackValid(true);
//
//       //Prepare Settings
//       dialog.setSettings
//    (currentTrack.gpsSyncTime, currentTrack.stopwatchStart,
//     currentTrack.timeWindow, currentTrack.threshold);
//
//       //Prepare Stoplist
//       dialog.setStoplistTableModel
//           (((TrackReference)tracksListModel.elementAt(selectedPos)).stoplistTM);
//     }
//     else
//     {
//       currentTrack = null;
//       dialog.setTrackValid(false);
//     }
//   }

  public static Node createNode(LatLon latLon, String id, String name)
  {
    Node node = new Node(latLon);
    node.put("highway", "bus_stop");
    node.put("stop_id", id);
    node.put("name", name);
    if (Main.main.getCurrentDataSet() == null)
    {
      JOptionPane.showMessageDialog(null, tr("There exists no dataset."
      + " Try to download data from the server or open an OSM file."),
      tr("No data found"), JOptionPane.ERROR_MESSAGE);

      return null;
    }
    Main.main.getCurrentDataSet().addPrimitive(node);
    return node;
  }

  /* returns a collection of all selected lines or
     a collection of all lines otherwise */
  public static Vector< Integer > getConsideredLines(JTable table)
  {
    int[] selectedLines = table.getSelectedRows();
    Vector< Integer > consideredLines = new Vector< Integer >();
    if (selectedLines.length > 0)
    {
      for (int i = 0; i < selectedLines.length; ++i)
        consideredLines.add(selectedLines[i]);
    }
    else
    {
      for (int i = 0; i < table.getRowCount(); ++i)
        consideredLines.add(new Integer(i));
    }
    return consideredLines;
  }

  /* marks the table items whose nodes are marked on the map */
  public static void findNodesInTable(JTable table, Vector< Node > nodes)
  {
    if (Main.main.getCurrentDataSet() == null)
      return;

    table.clearSelection();

    for (int i = 0; i < table.getRowCount(); ++i)
    {
      if ((nodes.elementAt(i) != null) &&
      (Main.main.getCurrentDataSet().isSelected(nodes.elementAt(i))))
        table.addRowSelectionInterval(i, i);
    }
  }

  /* shows the nodes that correspond to the marked lines in the table.
     If no lines are marked in the table, show all nodes from the vector */
  public static void showNodesFromTable(JTable table, Vector< Node > nodes)
  {
    BoundingXYVisitor box = new BoundingXYVisitor();
    Vector< Integer > consideredLines = getConsideredLines(table);
    for (int i = 0; i < consideredLines.size(); ++i)
    {
      int j = consideredLines.elementAt(i);
      if (nodes.elementAt(j) != null)
        nodes.elementAt(j).accept(box);
    }
    if (box.getBounds() == null)
      return;
    box.enlargeBoundingBox();
    Main.map.mapView.recalculateCenterScale(box);
  }

  /* marks the nodes that correspond to the marked lines in the table.
  If no lines are marked in the table, mark all nodes from the vector */
  public static void markNodesFromTable(JTable table, Vector< Node > nodes)
  {
    OsmPrimitive[] osmp = { null };
    Main.main.getCurrentDataSet().setSelected(osmp);
    Vector< Integer > consideredLines = getConsideredLines(table);
    for (int i = 0; i < consideredLines.size(); ++i)
    {
      int j = consideredLines.elementAt(i);
      if (nodes.elementAt(j) != null)
        Main.main.getCurrentDataSet().addSelected(nodes.elementAt(j));
    }
  }

  public static String timeOf(double t)
  {
    t -= Math.floor(t/24/60/60)*24*60*60;

    int hour = (int)Math.floor(t/60/60);
    t -=  Math.floor(t/60/60)*60*60;
    int minute = (int)Math.floor(t/60);
    t -=  Math.floor(t/60)*60;
    double second = t;

    Format format = new DecimalFormat("00");
    Format formatS = new DecimalFormat("00.###");
    return (format.format(hour) + ":" + format.format(minute) + ":"
    + formatS.format(second));
  }

  public Action getFocusAddAction()
  {
    return new FocusAddAction();
  }

  private class FocusAddAction extends AbstractAction
  {
    public void actionPerformed(ActionEvent e)
    {
      Main.main.undoRedo.add(new GTFSAddCommand(GTFSImporterAction.this));
      showNodesFromTable(dialog.getGTFSStopTable(), gtfsStopTM.nodes);
    }
  };

/*  public Action getFocusWaypointShelterAction(String shelter)
  {
    return new FocusWaypointShelterAction(shelter);
  }

  public Action getFocusWaypointDeleteAction()
  {
    return new AbstractAction()
    {
      public void actionPerformed(ActionEvent e)
      {
    JTable table = dialog.getWaypointsTable();
    int row = table.getEditingRow();
    if (row < 0)
      return;
    table.clearSelection();
    table.addRowSelectionInterval(row, row);
/*  Main.main.undoRedo.add
        (new WaypointsDisableCommand(GTFSImporterAction.this));*
      }
    };
  }

  public Action getFocusTrackStoplistNameAction()
  {
    return new FocusTrackStoplistNameAction();
  }

  public Action getFocusTrackStoplistShelterAction(String shelter)
  {
    return new FocusTrackStoplistShelterAction(shelter);
  }

  public Action getFocusStoplistDeleteAction()
  {
    return new AbstractAction()
    {
      public void actionPerformed(ActionEvent e)
      {
    JTable table = dialog.getStoplistTable();
    int row = table.getEditingRow();
    if (row < 0)
      return;
    table.clearSelection();
    table.addRowSelectionInterval(row, row);
/*  Main.main.undoRedo.add
        (new TrackStoplistDeleteCommand(GTFSImporterAction.this));*
      }
    };
  }

  private class FocusWaypointNameAction extends AbstractAction
  {
    public void actionPerformed(ActionEvent e)
    {
      JTable table = dialog.getWaypointsTable();
      showNodesFromTable(table, waypointTM.nodes);
      markNodesFromTable(table, waypointTM.nodes);
      int row = table.getEditingRow();
      if (row < 0)
    row = 0;
      waypointTM.inEvent = true;
      if (table.getCellEditor() != null)
      {
    if (!table.getCellEditor().stopCellEditing())
      table.getCellEditor().cancelCellEditing();
      }
      table.editCellAt(row, 1);
      table.getCellEditor().getTableCellEditorComponent
      (table, "", true, row, 1);
      waypointTM.inEvent = false;
    }
  };

  private class FocusWaypointShelterAction extends AbstractAction
  {
    private String defaultShelter = null;

    public FocusWaypointShelterAction(String defaultShelter)
    {
      this.defaultShelter = defaultShelter;
    }

    public void actionPerformed(ActionEvent e)
    {
      JTable table = dialog.getWaypointsTable();
      showNodesFromTable(table, waypointTM.nodes);
      markNodesFromTable(table, waypointTM.nodes);
      int row = table.getEditingRow();
      if (row < 0)
    row = 0;
      waypointTM.inEvent = true;
      if (table.getCellEditor() != null)
      {
    if (!table.getCellEditor().stopCellEditing())
      table.getCellEditor().cancelCellEditing();
      }
      table.editCellAt(row, 2);
      waypointTM.inEvent = false;
      table.getCellEditor().getTableCellEditorComponent
          (table, defaultShelter, true, row, 2);
    }
  };

  private class FocusTrackStoplistNameAction extends AbstractAction
  {
    public void actionPerformed(ActionEvent e)
    {
      JTable table = dialog.getStoplistTable();
      showNodesFromTable(table, currentTrack.stoplistTM.getNodes());
      markNodesFromTable(table, currentTrack.stoplistTM.getNodes());
      int row = table.getEditingRow();
      if (row < 0)
    row = 0;
      currentTrack.inEvent = true;
      if (table.getCellEditor() != null)
      {
    if (!table.getCellEditor().stopCellEditing())
      table.getCellEditor().cancelCellEditing();
      }
      table.editCellAt(row, 1);
      table.getCellEditor().getTableCellEditorComponent
          (table, "", true, row, 1);
      currentTrack.inEvent = false;
    }
  };

  private class FocusTrackStoplistShelterAction extends AbstractAction
  {
    private String defaultShelter = null;

    public FocusTrackStoplistShelterAction(String defaultShelter)
    {
      this.defaultShelter = defaultShelter;
    }

    public void actionPerformed(ActionEvent e)
    {
      JTable table = dialog.getStoplistTable();
      showNodesFromTable(table, currentTrack.stoplistTM.getNodes());
      markNodesFromTable(table, currentTrack.stoplistTM.getNodes());
      int row = table.getEditingRow();
      if (row < 0)
    row = 0;
      currentTrack.inEvent = true;
      if (table.getCellEditor() != null)
      {
    if (!table.getCellEditor().stopCellEditing())
      table.getCellEditor().cancelCellEditing();
      }
      table.editCellAt(row, 2);
      currentTrack.inEvent = false;
      table.getCellEditor().getTableCellEditorComponent
          (table, defaultShelter, true, row, 2);
    }
  };*/
}

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

public class StopImporterAction extends JosmAction
{  
  private class TrackReference
    implements Comparable< TrackReference >, TableModelListener
  {
    public GpxTrack track;
    public StoplistTableModel stoplistTM;
    public String stopwatchStart;
    public String gpsStartTime;
    public String gpsSyncTime;
    public double timeWindow;
    public double threshold;
    
    public TrackReference(GpxTrack track)
    {
      this.track = track;
      this.stoplistTM = new StoplistTableModel(this);
      this.stopwatchStart = "00:00:00";
      this.gpsStartTime = null;
      this.gpsSyncTime = null;
      if (track != null)
      {
	Iterator< GpxTrackSegment > siter = track.getSegments().iterator();
	while ((siter.hasNext()) && (this.gpsSyncTime == null))
	{
	  Iterator< WayPoint > witer = siter.next().getWayPoints().iterator();
	  if (witer.hasNext())
	  {
	    this.gpsStartTime = witer.next().getString("time");
	    if (this.gpsStartTime != null)
	      this.gpsSyncTime = this.gpsStartTime.substring(11, 19);
	  }
	}
	if (this.gpsSyncTime == null)
	{
	  JOptionPane.showMessageDialog
	      (null, "The GPX file doesn't contain valid trackpoints. "
	      + "Please use a GPX file that has trackpoints.", "GPX File Trouble",
	       JOptionPane.ERROR_MESSAGE);
	  
	  this.gpsStartTime = "1970-01-01T00:00:00Z";
	  this.gpsSyncTime = this.stopwatchStart;
	}
      }
      else
	this.gpsSyncTime = this.stopwatchStart;
      this.timeWindow = 20;
      this.threshold = 20;
    }
    
    public int compareTo(TrackReference tr)
    {
      String name = (String)track.getAttributes().get("name");
      String tr_name = (String)tr.track.getAttributes().get("name");
      if (name != null)
      {
	if (tr_name == null)
	  return -1;
	return name.compareTo(tr_name);
      }
      return 1;
    }
    
    public String toString()
    {
      String buf = (String)track.getAttributes().get("name");
      if (buf == null)
	return "unnamed";
      return buf;
    }
    
    public void tableChanged(TableModelEvent e)
    {
      if (e.getType() == TableModelEvent.UPDATE)
      {
	double time = StopImporterDialog.parseTime
	    ((String)stoplistTM.getValueAt(e.getFirstRow(), 0));
	if (time < 0)
	{
	  JOptionPane.showMessageDialog
	  (null, "Can't parse a time from this string.", "Invalid value",
	   JOptionPane.ERROR_MESSAGE);
	   return;
	}

	LatLon latLon = computeCoor(time);
	
	if (stoplistTM.nodes.elementAt(e.getFirstRow()) == null)
	{
	  Node node = createNode
	      (latLon, (String)stoplistTM.getValueAt(e.getFirstRow(), 1));
	  stoplistTM.nodes.set(e.getFirstRow(), node);
	}
	else
	{
	  Node node = new Node(stoplistTM.nodes.elementAt(e.getFirstRow()));
	  node.setCoor(latLon);
	  node.put("name", (String)stoplistTM.getValueAt(e.getFirstRow(), 1));
	  Command cmd = new ChangeCommand(stoplistTM.nodes.elementAt(e.getFirstRow()), node);
	  if (cmd != null) {
	    Main.main.undoRedo.add(cmd);
	  }
	}
      }
    }
    
    public LatLon computeCoor(double time)
    {
      double gpsSyncTime = StopImporterDialog.parseTime(this.gpsSyncTime);
      double dGpsStartTime = StopImporterDialog.parseTime(gpsStartTime);
      if (gpsSyncTime < dGpsStartTime - 12*60*60)
	gpsSyncTime += 24*60*60;
      double timeDelta = gpsSyncTime - StopImporterDialog.parseTime(stopwatchStart);
      time += timeDelta;
	
      WayPoint wayPoint = null;
      WayPoint lastWayPoint = null;
      double wayPointTime = 0;
      double lastWayPointTime = 0;
      Iterator< GpxTrackSegment > siter = track.getSegments().iterator();
      while (siter.hasNext())
      {
	Iterator< WayPoint > witer = siter.next().getWayPoints().iterator();
	while (witer.hasNext())
	{
	  wayPoint = witer.next();
	  String startTime = wayPoint.getString("time");
	  wayPointTime = StopImporterDialog.parseTime(startTime.substring(11, 19));
	  if (startTime.substring(11, 19).compareTo(gpsStartTime.substring(11, 19)) == -1)
	    wayPointTime += 24*60*60;
	  if (wayPointTime >= time)
	    break;
	  lastWayPoint = wayPoint;
	  lastWayPointTime = wayPointTime;
	}
	if (wayPointTime >= time)
	  break;
      }
	
      double lat = 0;
      if ((wayPointTime == lastWayPointTime) || (lastWayPoint == null))
	lat = wayPoint.getCoor().lat();
      else
	lat = wayPoint.getCoor().lat()
	    *(time - lastWayPointTime)/(wayPointTime - lastWayPointTime)
	    + lastWayPoint.getCoor().lat()
	    *(wayPointTime - time)/(wayPointTime - lastWayPointTime);
      double lon = 0;
      if ((wayPointTime == lastWayPointTime) || (lastWayPoint == null))
	lon = wayPoint.getCoor().lon();
      else
	lon = wayPoint.getCoor().lon()
	    *(time - lastWayPointTime)/(wayPointTime - lastWayPointTime)
	    + lastWayPoint.getCoor().lon()
	    *(wayPointTime - time)/(wayPointTime - lastWayPointTime);
      
      return new LatLon(lat, lon);
    }
    
    public void relocateNodes()
    {
      for (int i = 0; i < stoplistTM.nodes.size(); ++i)
      {
	Node node = stoplistTM.nodes.elementAt(i);
	if (node == null)
	  continue;
	
	double time = StopImporterDialog.parseTime
	      ((String)stoplistTM.getValueAt(i, 0));
	LatLon latLon = computeCoor(time);
	
	Node newNode = new Node(node);
	newNode.setCoor(latLon);
	Command cmd = new ChangeCommand(node, newNode);
	if (cmd != null)
	{
	  Main.main.undoRedo.add(cmd);
	}
      }
    }
    
    public void suggestStops()
    {
      Vector< WayPoint > wayPoints = new Vector< WayPoint >();
      Iterator< GpxTrackSegment > siter = track.getSegments().iterator();
      while (siter.hasNext())
      {
	Iterator< WayPoint > witer = siter.next().getWayPoints().iterator();
	while (witer.hasNext())
	  wayPoints.add(witer.next());
      }
      Vector< Double > wayPointsDist = new Vector< Double >(wayPoints.size());
      
      int i = 0;
      double time = -48*60*60;
      double dGpsStartTime = StopImporterDialog.parseTime(gpsStartTime);
      while ((i < wayPoints.size()) && (time < dGpsStartTime + timeWindow/2))
      {
	if (wayPoints.elementAt(i).getString("time") != null)
	  time = StopImporterDialog.parseTime(wayPoints.elementAt(i)
	      .getString("time").substring(11,19));
	if (time < dGpsStartTime)
	  time += 24*60*60;
	wayPointsDist.add(Double.valueOf(Double.POSITIVE_INFINITY));
	++i;
      }
      while (i < wayPoints.size())
      {
	int j = i;
	double time2 = time;
	while ((j > 0) && (time - timeWindow/2 < time2))
	{
	  --j;
	  if (wayPoints.elementAt(j).getString("time") != null)
	    time2 = StopImporterDialog.parseTime(wayPoints.elementAt(j)
		.getString("time").substring(11,19));
	  if (time2 < dGpsStartTime)
	    time2 += 24*60*60;
	}
	int k = i + 1;
	time2 = time;
	while ((k < wayPoints.size()) && (time + timeWindow/2 > time2))
	{
	  if (wayPoints.elementAt(k).getString("time") != null)
	    time2 = StopImporterDialog.parseTime(wayPoints.elementAt(k)
		.getString("time").substring(11,19));
	  if (time2 < dGpsStartTime)
	    time2 += 24*60*60;
	  ++k;
	}
	
	if (j < k)
	{
	  double dist = 0;
	  LatLon latLonI = wayPoints.elementAt(i).getCoor();
	  for (int l = j; l < k; ++l)
	  {
	    double distL = latLonI.greatCircleDistance(wayPoints.elementAt(l).getCoor());
	    if (distL > dist)
	      dist = distL;
	  }
	  wayPointsDist.add(Double.valueOf(dist));
	}
	else
	  wayPointsDist.add(Double.valueOf(Double.POSITIVE_INFINITY));
	
	if (wayPoints.elementAt(i).getString("time") != null)
	  time = StopImporterDialog.parseTime(wayPoints.elementAt(i)
	      .getString("time").substring(11,19));
	if (time < dGpsStartTime)
	  time += 24*60*60;
	++i;
      }
      
      Vector< Node > toDelete = new Vector< Node >();
      for (i = 0; i < stoplistTM.getRowCount(); ++i)
      {
	if ((Node)stoplistTM.nodes.elementAt(i) != null)
	  toDelete.add((Node)stoplistTM.nodes.elementAt(i));
      }
      if (!toDelete.isEmpty())
      {
	Command cmd = DeleteCommand.delete
	    (Main.main.getEditLayer(), toDelete);
	if (cmd == null)
	  return;
	Main.main.undoRedo.add(cmd);
      }
      stoplistTM.clear();
      
      LatLon lastStopCoor = null;
      for (i = 1; i < wayPoints.size()-1; ++i)
      {
	if (wayPointsDist.elementAt(i).doubleValue() >= threshold)
	  continue;
	if ((wayPointsDist.elementAt(i).compareTo(wayPointsDist.elementAt(i-1)) != -1)
		    || (wayPointsDist.elementAt(i).compareTo(wayPointsDist.elementAt(i+1)) != -1))
	  continue;
	
	LatLon latLon = wayPoints.elementAt(i).getCoor();
	if ((lastStopCoor != null) &&  (lastStopCoor.greatCircleDistance(latLon) < threshold))
	  continue;
	
	if (wayPoints.elementAt(i).getString("time") != null)
	{
	  time = StopImporterDialog.parseTime(wayPoints.elementAt(i)
	      .getString("time").substring(11,19));
	  double gpsSyncTime = StopImporterDialog.parseTime(this.gpsSyncTime);
	  if (gpsSyncTime < dGpsStartTime - 12*60*60)
	    gpsSyncTime += 24*60*60;
	  double timeDelta = gpsSyncTime - StopImporterDialog.parseTime(stopwatchStart);
	  time -= timeDelta;
	  stoplistTM.insertRow(-1, timeOf(time));
	  Node node = createNode(latLon, "");
	  stoplistTM.nodes.set(stoplistTM.getRowCount()-1, node);
	}
	
	lastStopCoor = latLon;
      }
    }
  };
  
  private class NodeSortEntry implements Comparable< NodeSortEntry >
  {
    public Node node = null;
    public String time = null;
    public String name = null;
    public double startTime = 0;
    
    public NodeSortEntry(Node node, String time, String name, double startTime)
    {
      this.node = node;
      this.time = time;
      this.name = name;
    }
    
    public int compareTo(NodeSortEntry nse)
    {
      double time = StopImporterDialog.parseTime(this.time);
      if (time - startTime > 12*60*60)
	time -= 24*60*60;
      
      double nseTime = StopImporterDialog.parseTime(nse.time);
      if (nseTime - startTime > 12*60*60)
	nseTime -= 24*60*60;
      
      if (time < nseTime)
	return -1;
      else if (time > nseTime)
	return 1;
      else
	return 0;
    }
  };
  
  private class StoplistTableModel extends DefaultTableModel
  {
    public Vector< Node > nodes = new Vector< Node >();
    
    public StoplistTableModel(TrackReference tr)
    {
      addColumn("Time");
      addColumn("Name");
      addTableModelListener(tr);
    }
    
    public boolean isCellEditable(int row, int column) {
      return true;
    }
    
    public void addRow(Object[] obj) {
      throw new UnsupportedOperationException();
    }
    
    public void insertRow(int insPos, Object[] obj) {
      throw new UnsupportedOperationException();
    }
    
    public void addRow(String time) {
      insertRow(-1, time);
    }
    
    public void insertRow(int insPos, String time)
    {
      insertRow(insPos, null, time, "");
    }
    
    public void insertRow(int insPos, Node node, String time, String name)
    {
      String[] buf = { "", "" };
      buf[0] = time;
      buf[1] = name;
      if (insPos == -1)
      {
	nodes.addElement(node);
	super.addRow(buf);
      }
      else
      {
	nodes.insertElementAt(node, insPos);
	super.insertRow(insPos, buf);
      }
    }
    
    public void clear()
    {
      nodes.clear();
      super.setRowCount(0);
    }
  };
  
  public class WaypointTableModel extends DefaultTableModel
      implements TableModelListener
  {
    public Vector< Node > nodes = new Vector< Node >();
    public Vector< LatLon > coors = new Vector< LatLon >();
    
    public WaypointTableModel()
    {
      addColumn("Time");
      addColumn("Stopname");
      addTableModelListener(this);
    }
    
    public boolean isCellEditable(int row, int column)
    {
      if (column == 1)
	return true;
      return false;
    }
    
    public void addRow(Object[] obj)
    {
      throw new UnsupportedOperationException();
    }
    
    public void insertRow(int insPos, Object[] obj)
    {
      throw new UnsupportedOperationException();
    }
    
    public void addRow(WayPoint wp)
    {
      insertRow(-1, wp);
    }
    
    public void insertRow(int insPos, WayPoint wp)
    {
      String[] buf = { "", "" };
      buf[0] = wp.getString("time");
      if (buf[0] == null)
	buf[0] = "";
      buf[1] = wp.getString("name");
      if (buf[1] == null)
	buf[1] = "";

      Node node = createNode(wp.getCoor(), buf[1]);
      
      if (insPos == -1)
      {
	nodes.addElement(node);
	coors.addElement(wp.getCoor());
	super.addRow(buf);
      }
      else
      {
	nodes.insertElementAt(node, insPos);
	coors.insertElementAt(wp.getCoor(), insPos);
	super.insertRow(insPos, buf);
      }
    }
    
    public void clear()
    {
      nodes.clear();
      super.setRowCount(0);
    }
  
    public void tableChanged(TableModelEvent e)
    {
      if (e.getType() == TableModelEvent.UPDATE)
      {
	if (nodes.elementAt(e.getFirstRow()) != null)
	{
	  Node node = nodes.elementAt(e.getFirstRow());
	  node.put("name", (String)getValueAt(e.getFirstRow(), 1));
	}
      }
    }
  };
  
  private static StopImporterDialog dialog = null;
  private static DefaultListModel tracksListModel = null;
  private static GpxData data = null;
  private static TrackReference currentTrack = null;
  private static WaypointTableModel waypointTM = null;
  
  public StopImporterAction()
  {
    super(tr("Create Stops from GPX ..."), null,
	  tr("Create Stops from a GPX file"), null, true);
  }

  public WaypointTableModel getWaypointTableModel()
  {
    return waypointTM;
  }
  
  public StopImporterDialog getDialog()
  {
    return dialog;
  }

  public DefaultListModel getTracksListModel()
  {
    if (tracksListModel == null)
      tracksListModel = new DefaultListModel();
    return tracksListModel;
  }
  
  public void actionPerformed(ActionEvent event)
  {
    DataSet mainDataSet = Main.main.getCurrentDataSet();
    
    if (dialog == null)
      dialog = new StopImporterDialog(this);
    
    dialog.setVisible(true);

    if (tr("Create Stops from GPX ...").equals(event.getActionCommand()))
    {
      String curDir = Main.pref.get("lastDirectory");
      if (curDir.equals(""))
      {
	curDir = ".";
      }
      JFileChooser fc = new JFileChooser(new File(curDir));
      fc.setDialogTitle("Select GPX file");  
      fc.setMultiSelectionEnabled(false);
      
      int answer = fc.showOpenDialog(Main.parent);
      if (answer != JFileChooser.APPROVE_OPTION)
	return;
      
      if (!fc.getCurrentDirectory().getAbsolutePath().equals(curDir))
	Main.pref.put("lastDirectory", fc.getCurrentDirectory().getAbsolutePath());
      
      importData(fc.getSelectedFile());
      
      refreshData();
    }
    else if ("stopImporter.settingsGPSTimeStart"
      .equals(event.getActionCommand()))
    {
      if (dialog.gpsTimeStartValid())
      {
	if (currentTrack != null)
	{
	  currentTrack.gpsSyncTime = dialog.getGpsTimeStart();
	  currentTrack.relocateNodes();
	}
      }
    }
    else if ("stopImporter.settingsStopwatchStart"
      .equals(event.getActionCommand()))
    {
      if (dialog.stopwatchStartValid())
      {
	if (currentTrack != null)
	{
	  currentTrack.stopwatchStart = dialog.getStopwatchStart();
	  currentTrack.relocateNodes();
	}
      }
      else
      {
      }
    }
    else if ("stopImporter.settingsTimeWindow"
      .equals(event.getActionCommand()))
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
    {
      currentTrack.suggestStops();
    }
    else if ("stopImporter.stoplistFind".equals(event.getActionCommand()))
    {
      if (Main.main.getCurrentDataSet() == null)
	return;
      
      dialog.getStoplistTable().clearSelection();
      
      for (int i = 0; i < currentTrack.stoplistTM.getRowCount(); ++i)
      {
	if ((currentTrack.stoplistTM.nodes.elementAt(i) != null) &&
		    (Main.main.getCurrentDataSet().isSelected(currentTrack.stoplistTM.nodes.elementAt(i))))
	  dialog.getStoplistTable().addRowSelectionInterval(i, i);
      }
    }
    else if ("stopImporter.stoplistShow".equals(event.getActionCommand()))
    {
      BoundingXYVisitor box = new BoundingXYVisitor();
      if (dialog.getStoplistTable().getSelectedRowCount() > 0)
      {
	for (int i = 0; i < currentTrack.stoplistTM.getRowCount(); ++i)
	{
	  if ((dialog.getStoplistTable().isRowSelected(i)) &&
		      (currentTrack.stoplistTM.nodes.elementAt(i) != null))
	  {
	    currentTrack.stoplistTM.nodes.elementAt(i).visit(box);
	  }
	}
      }
      else
      {
	for (int i = 0; i < currentTrack.stoplistTM.getRowCount(); ++i)
	{
	  if (currentTrack.stoplistTM.nodes.elementAt(i) != null)
	    currentTrack.stoplistTM.nodes.elementAt(i).visit(box);
	}
      }
      if (box.getBounds() == null)
	return;
      box.enlargeBoundingBox();
      Main.map.mapView.recalculateCenterScale(box);
    }
    else if ("stopImporter.stoplistMark".equals(event.getActionCommand()))
    {
      OsmPrimitive[] osmp = { null };
      Main.main.getCurrentDataSet().setSelected(osmp);
      if (dialog.getStoplistTable().getSelectedRowCount() > 0)
      {
	for (int i = 0; i < currentTrack.stoplistTM.getRowCount(); ++i)
	{
	  if ((dialog.getStoplistTable().isRowSelected(i)) &&
	    (currentTrack.stoplistTM.nodes.elementAt(i) != null))
	  {
	    Main.main.getCurrentDataSet().addSelected(currentTrack.stoplistTM.nodes.elementAt(i));
	  }
	}
      }
      else
      {
	for (int i = 0; i < currentTrack.stoplistTM.getRowCount(); ++i)
	{
	  if (currentTrack.stoplistTM.nodes.elementAt(i) != null)
	    Main.main.getCurrentDataSet().addSelected(currentTrack.stoplistTM.nodes.elementAt(i));
	}
      }
    }
    else if ("stopImporter.stoplistDetach".equals(event.getActionCommand()))
    {
      if (dialog.getStoplistTable().getSelectedRowCount() > 0)
      {
	for (int i = 0; i < currentTrack.stoplistTM.getRowCount(); ++i)
	{
	  if ((dialog.getStoplistTable().isRowSelected(i)) &&
		      (currentTrack.stoplistTM.nodes.elementAt(i) != null))
	  {
	    currentTrack.stoplistTM.nodes.set(i, null);
	  }
	}
      }
      else
      {
	for (int i = 0; i < currentTrack.stoplistTM.getRowCount(); ++i)
	{
	  if (currentTrack.stoplistTM.nodes.elementAt(i) != null)
	    currentTrack.stoplistTM.nodes.set(i, null);
	}
      }
      dialog.getStoplistTable().clearSelection();
    }
    else if ("stopImporter.stoplistAdd".equals(event.getActionCommand()))
    {
      int insPos = dialog.getStoplistTable().getSelectedRow();
      if (currentTrack != null)
	currentTrack.stoplistTM.insertRow(insPos, "00:00:00");
    }
    else if ("stopImporter.stoplistDelete".equals(event.getActionCommand()))
    {
      Vector< Node > toDelete = new Vector< Node >();
      if (currentTrack == null)
	return;
      for (int i = currentTrack.stoplistTM.getRowCount()-1; i >=0; --i)
      {
	if (dialog.getStoplistTable().isRowSelected(i))
	{
	  if ((Node)currentTrack.stoplistTM.nodes.elementAt(i) != null)
	    toDelete.add((Node)currentTrack.stoplistTM.nodes.elementAt(i));
	  currentTrack.stoplistTM.nodes.removeElementAt(i);
	  currentTrack.stoplistTM.removeRow(i);
	}
      }
      Command cmd = DeleteCommand.delete
          (Main.main.getEditLayer(), toDelete);
      if (cmd != null) {
	// cmd can be null if the user cancels dialogs DialogCommand displays
	Main.main.undoRedo.add(cmd);
      }
    }
    else if ("stopImporter.stoplistSort".equals(event.getActionCommand()))
    {
      int insPos = dialog.getStoplistTable().getSelectedRow();
      Vector< NodeSortEntry > nodesToSort = new Vector< NodeSortEntry >();
      if (currentTrack == null)
	return;
      if (dialog.getStoplistTable().getSelectedRowCount() > 0)
      {
	for (int i = currentTrack.stoplistTM.getRowCount()-1; i >=0; --i)
	{
	  if (dialog.getStoplistTable().isRowSelected(i))
	  {
	    nodesToSort.add(new NodeSortEntry
		(currentTrack.stoplistTM.nodes.elementAt(i),
		 (String)currentTrack.stoplistTM.getValueAt(i, 0),
		  (String)currentTrack.stoplistTM.getValueAt(i, 1),
		   StopImporterDialog.parseTime(currentTrack.stopwatchStart)));
	    currentTrack.stoplistTM.nodes.removeElementAt(i);
	    currentTrack.stoplistTM.removeRow(i);
	  }
	}
      }
      else
      {
	for (int i = 0; i < currentTrack.stoplistTM.getRowCount(); ++i)
	{
	  nodesToSort.add(new NodeSortEntry
	      (currentTrack.stoplistTM.nodes.elementAt(i),
	       (String)currentTrack.stoplistTM.getValueAt(i, 0),
		(String)currentTrack.stoplistTM.getValueAt(i, 1),
		 StopImporterDialog.parseTime(currentTrack.stopwatchStart)));
	}
	currentTrack.stoplistTM.clear();
      }
      
      Collections.sort(nodesToSort);
      
      Iterator< NodeSortEntry > iter = nodesToSort.iterator();
      while (iter.hasNext())
      {
	NodeSortEntry nse = iter.next();
	currentTrack.stoplistTM.insertRow
	    (insPos, nse.node, nse.time, nse.name);
	if (insPos >= 0)
	  ++insPos;
      }
    }
    else if ("stopImporter.waypointsFind".equals(event.getActionCommand()))
    {
      if (Main.main.getCurrentDataSet() == null)
	return;
      
      dialog.getWaypointsTable().clearSelection();
      
      for (int i = 0; i < waypointTM.getRowCount(); ++i)
      {
	if ((waypointTM.nodes.elementAt(i) != null) &&
		    (Main.main.getCurrentDataSet().isSelected(waypointTM.nodes.elementAt(i))))
	  dialog.getWaypointsTable().addRowSelectionInterval(i, i);
      }
    }
    else if ("stopImporter.waypointsShow".equals(event.getActionCommand()))
    {
      BoundingXYVisitor box = new BoundingXYVisitor();
      if (dialog.getWaypointsTable().getSelectedRowCount() > 0)
      {
	for (int i = 0; i < waypointTM.getRowCount(); ++i)
	{
	  if ((dialog.getWaypointsTable().isRowSelected(i)) &&
		      (waypointTM.nodes.elementAt(i) != null))
	  {
	    waypointTM.nodes.elementAt(i).visit(box);
	  }
	}
      }
      else
      {
	for (int i = 0; i < waypointTM.getRowCount(); ++i)
	{
	  if (waypointTM.nodes.elementAt(i) != null)
	    waypointTM.nodes.elementAt(i).visit(box);
	}
      }
      if (box.getBounds() == null)
	return;
      box.enlargeBoundingBox();
      Main.map.mapView.recalculateCenterScale(box);
    }
    else if ("stopImporter.waypointsMark".equals(event.getActionCommand()))
    {
      OsmPrimitive[] osmp = { null };
      Main.main.getCurrentDataSet().setSelected(osmp);
      if (dialog.getWaypointsTable().getSelectedRowCount() > 0)
      {
	for (int i = 0; i < waypointTM.getRowCount(); ++i)
	{
	  if ((dialog.getWaypointsTable().isRowSelected(i)) &&
		      (waypointTM.nodes.elementAt(i) != null))
	  {
	    Main.main.getCurrentDataSet().addSelected(waypointTM.nodes.elementAt(i));
	  }
	}
      }
      else
      {
	for (int i = 0; i < waypointTM.getRowCount(); ++i)
	{
	  if (waypointTM.nodes.elementAt(i) != null)
	    Main.main.getCurrentDataSet().addSelected(waypointTM.nodes.elementAt(i));
	}
      }
    }
    else if ("stopImporter.waypointsDetach".equals(event.getActionCommand()))
    {
      Main.main.undoRedo.add(new WaypointsDetachCommand(this));
      dialog.getWaypointsTable().clearSelection();
    }
    else if ("stopImporter.waypointsAdd".equals(event.getActionCommand()))
    {
      Main.main.undoRedo.add(new WaypointsEnableCommand(this));
    }
    else if ("stopImporter.waypointsDelete".equals(event.getActionCommand()))
    {
      Main.main.undoRedo.add(new WaypointsDisableCommand(this));
    }
    else if ("stopImporter.settingsStoptype".equals(event.getActionCommand()))
    {
      for (int i = 0; i < waypointTM.getRowCount(); ++i)
      {
	if ((Node)waypointTM.nodes.elementAt(i) != null)
        {
	  Node node = (Node)waypointTM.nodes.elementAt(i);
	  node.remove("highway");
	  node.remove("railway");
          if ("bus".equals(dialog.getStoptype()))
            node.put("highway", "bus_stop");
	  else if ("tram".equals(dialog.getStoptype()))
            node.put("railway", "tram_stop");
	  else if ("light_rail".equals(dialog.getStoptype()))
            node.put("railway", "station");
	  else if ("subway".equals(dialog.getStoptype()))
            node.put("railway", "station");
	  else if ("rail".equals(dialog.getStoptype()))
            node.put("railway", "station");
        }
      }
      for (int j = 0; j < tracksListModel.size(); ++j)
      {
	TrackReference track = (TrackReference)tracksListModel.elementAt(j);
	for (int i = 0; i < track.stoplistTM.getRowCount(); ++i)
	{
	  if ((Node)track.stoplistTM.nodes.elementAt(i) != null)
	  {
	    Node node = (Node)track.stoplistTM.nodes.elementAt(i);
	    node.remove("highway");
	    node.remove("railway");
	    if ("bus".equals(dialog.getStoptype()))
	      node.put("highway", "bus_stop");
	    else if ("tram".equals(dialog.getStoptype()))
	      node.put("railway", "tram_stop");
	    else if ("light_rail".equals(dialog.getStoptype()))
	      node.put("railway", "station");
	    else if ("subway".equals(dialog.getStoptype()))
	      node.put("railway", "station");
	    else if ("rail".equals(dialog.getStoptype()))
	      node.put("railway", "station");
	  }
	}
      }
    }
  }

  private void importData(final File file)
  {
    try 
    {
      InputStream is;
      if (file.getName().endsWith(".gpx.gz"))
	is = new GZIPInputStream(new FileInputStream(file));
      else
	is = new FileInputStream(file);
      // Workaround for SAX BOM bug
      // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6206835
      if (!((is.read() == 0xef) && (is.read() == 0xbb) && (is.read() == 0xbf)))
      {
	is.close();
	if (file.getName().endsWith(".gpx.gz"))
	  is = new GZIPInputStream(new FileInputStream(file));
	else
	  is = new FileInputStream(file);
      }
      final GpxReader r = new GpxReader(is);
      final boolean parsedProperly = r.parse(true);
      data = r.data;
      
      if (!parsedProperly)
      {
	JOptionPane.showMessageDialog(null, tr("Error occured while parsing gpx file {0}. Only part of the file will be available", file.getName()));
      }
    }
    catch (FileNotFoundException e) 
    {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, tr("File \"{0}\" does not exist", file.getName()));
    }
    catch (SAXException e)
    {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, tr("Parsing file \"{0}\" failed", file.getName()));
    }
    catch (IOException e)
    {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, tr("IOException \"{0}\" occurred", e.toString()));
    }
  }

  private void refreshData()
  {
    tracksListModel.clear();
    if (data != null)
    {
      Vector< TrackReference > trackRefs = new Vector< TrackReference >();
      Iterator< GpxTrack > trackIter = data.tracks.iterator();
      while (trackIter.hasNext())
      {
	GpxTrack track = trackIter.next();
	trackRefs.add(new TrackReference(track));
      }
      
      Collections.sort(trackRefs);

      Iterator< TrackReference > iter = trackRefs.iterator();
      while (iter.hasNext())
	tracksListModel.addElement(iter.next());
      
      waypointTM = new WaypointTableModel();
      Iterator< WayPoint > waypointIter = data.waypoints.iterator();
      while (waypointIter.hasNext())
      {
	WayPoint waypoint = waypointIter.next();
	waypointTM.addRow(waypoint);
      }
      dialog.getWaypointsTable().setModel(waypointTM);
    }
    else
    {
      JOptionPane.showMessageDialog
      (null, "The GPX file contained no tracks or waypoints.", "No data found",
       JOptionPane.ERROR_MESSAGE);
      
      System.out.println("Public Transport: StopImporter: No data found");
    }
  }
  
  public void tracksSelectionChanged(int selectedPos)
  {
    if (selectedPos >= 0)
    {
      currentTrack = ((TrackReference)tracksListModel.elementAt(selectedPos));
      dialog.setTrackValid(true);
      
      //Prepare Settings
      dialog.setSettings
	  (currentTrack.gpsSyncTime, currentTrack.stopwatchStart,
	   currentTrack.timeWindow, currentTrack.threshold);
      
      //Prepare Stoplist
      dialog.getStoplistTable().setModel
          (((TrackReference)tracksListModel.elementAt(selectedPos)).stoplistTM);
    }
    else
    {
      currentTrack = null;
      dialog.setTrackValid(false);
    }
  }

  private Node createNode(LatLon latLon, String name)
  {
    return createNode(latLon, dialog.getStoptype(), name);
  }
    
  public static Node createNode(LatLon latLon, String type, String name)
  {
    Node node = new Node(latLon);
    if ("bus".equals(type))
      node.put("highway", "bus_stop");
    else if ("tram".equals(type))
      node.put("railway", "tram_stop");
    else if ("light_rail".equals(type))
      node.put("railway", "station");
    else if ("subway".equals(type))
      node.put("railway", "station");
    else if ("rail".equals(type))
      node.put("railway", "station");
    node.put("name", name);
    if (Main.main.getCurrentDataSet() == null)
    {
      JOptionPane.showMessageDialog(null, "There exists no dataset."
	  + " Try to download data from the server or open an OSM file.",
   "No data found", JOptionPane.ERROR_MESSAGE);
      
      System.out.println("Public Transport: StopInserter: No data found");
	    
      return null;
    }
    Main.main.getCurrentDataSet().addPrimitive(node);
    return node;
  }
    
  private static String timeOf(double t)
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
}

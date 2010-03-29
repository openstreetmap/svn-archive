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

  private class TracksLSL implements ListSelectionListener
  {
    StopImporterAction root = null;
    
    public TracksLSL(StopImporterAction sia)
    {
      root = sia;
    }
    
    public void valueChanged(ListSelectionEvent e)
    {
      root.tracksSelectionChanged();
    }
  };
  
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
	double time = parseTime
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
      double gpsSyncTime = parseTime(this.gpsSyncTime);
      double dGpsStartTime = parseTime(gpsStartTime);
      if (gpsSyncTime < dGpsStartTime - 12*60*60)
	gpsSyncTime += 24*60*60;
      double timeDelta = gpsSyncTime - parseTime(stopwatchStart);
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
	  wayPointTime = parseTime(startTime.substring(11, 19));
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
	
	double time = parseTime
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
      double dGpsStartTime = parseTime(gpsStartTime);
      while ((i < wayPoints.size()) && (time < dGpsStartTime + timeWindow/2))
      {
	if (wayPoints.elementAt(i).getString("time") != null)
	  time = parseTime(wayPoints.elementAt(i).getString("time").substring(11,19));
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
	    time2 = parseTime(wayPoints.elementAt(j).getString("time").substring(11,19));
	  if (time2 < dGpsStartTime)
	    time2 += 24*60*60;
	}
	int k = i + 1;
	time2 = time;
	while ((k < wayPoints.size()) && (time + timeWindow/2 > time2))
	{
	  if (wayPoints.elementAt(k).getString("time") != null)
	    time2 = parseTime(wayPoints.elementAt(k).getString("time").substring(11,19));
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
	  time = parseTime(wayPoints.elementAt(i).getString("time").substring(11,19));
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
	  time = parseTime(wayPoints.elementAt(i).getString("time").substring(11,19));
	  double gpsSyncTime = parseTime(this.gpsSyncTime);
	  if (gpsSyncTime < dGpsStartTime - 12*60*60)
	    gpsSyncTime += 24*60*60;
	  double timeDelta = gpsSyncTime - parseTime(stopwatchStart);
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
      double time = parseTime(this.time);
      if (time - startTime > 12*60*60)
	time -= 24*60*60;
      
      double nseTime = parseTime(nse.time);
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
  
  private class WaypointTableModel extends DefaultTableModel
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
  
  private static JDialog jDialog = null;
  private static JTabbedPane tabbedPane = null;
  private static DefaultListModel tracksListModel = null;
  private static JComboBox cbStoptype = null;
  private static JList tracksList = null;
  private static JTextField tfGPSTimeStart = null;
  private static JTextField tfStopwatchStart = null;
  private static JTextField tfTimeWindow = null;
  private static JTextField tfThreshold = null;
  private static JTable stoplistTable = null;
  private static JTable waypointTable = null;
  private static GpxData data = null;
  private static TrackReference currentTrack = null;
  private static WaypointTableModel waypointTM = null;
  
  public StopImporterAction()
  {
    super(tr("Create Stops from GPX ..."), null,
	  tr("Create Stops from a GPX file"), null, true);
  }

  public void actionPerformed(ActionEvent event)
  {
    Frame frame = JOptionPane.getFrameForComponent(Main.parent);
    DataSet mainDataSet = Main.main.getCurrentDataSet();
    
    if (jDialog == null)
    {
      jDialog = new JDialog(frame, "Create Stops from GPX", false);
      tabbedPane = new JTabbedPane();
      JPanel tabTracks = new JPanel();
      tabbedPane.addTab(marktr("Tracks"), tabTracks);
      JPanel tabSettings = new JPanel();
      tabbedPane.addTab(marktr("Settings"), tabSettings);
      JPanel tabStops = new JPanel();
      tabbedPane.addTab(marktr("Stops"), tabStops);
      JPanel tabWaypoints = new JPanel();
      tabbedPane.addTab(marktr("Waypoints"), tabWaypoints);
      tabbedPane.setEnabledAt(0, true);
      tabbedPane.setEnabledAt(1, false);
      tabbedPane.setEnabledAt(2, false);
      tabbedPane.setEnabledAt(3, true);
      jDialog.add(tabbedPane);
      
      //Tracks Tab
      Container contentPane = tabTracks;
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layoutCons = new GridBagConstraints();
      contentPane.setLayout(gridbag);
      
      JLabel label = new JLabel("Tracks in this GPX file:");
      
      layoutCons.gridx = 0;
      layoutCons.gridy = 0;
      layoutCons.gridwidth = 3;
      layoutCons.weightx = 0.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(label, layoutCons);
      contentPane.add(label);
      
      tracksListModel = new DefaultListModel();
      tracksList = new JList(tracksListModel);
      JScrollPane rpListSP = new JScrollPane(tracksList);
      String[] data = {"1", "2", "3", "4", "5", "6"};
      tracksListModel.copyInto(data);
      tracksList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      tracksList.addListSelectionListener(new TracksLSL(this));
      
      layoutCons.gridx = 0;
      layoutCons.gridy = 1;
      layoutCons.gridwidth = 3;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 1.0;
      layoutCons.fill = GridBagConstraints.BOTH;      
      gridbag.setConstraints(rpListSP, layoutCons);
      contentPane.add(rpListSP);
      
      //Settings Tab
      /*Container*/ contentPane = tabSettings;
      /*GridBagLayout*/ gridbag = new GridBagLayout();
      /*GridBagConstraints*/ layoutCons = new GridBagConstraints();
      contentPane.setLayout(gridbag);
      
      /*JLabel*/ label = new JLabel("Type of stops to add");
      
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
      cbStoptype.addItem("bus");
      cbStoptype.addItem("tram");
      cbStoptype.addItem("light_rail");
      cbStoptype.addItem("subway");
      cbStoptype.addItem("rail");
      cbStoptype.setActionCommand("stopImporter.settingsStoptype");
      cbStoptype.addActionListener(this);
      
      layoutCons.gridx = 0;
      layoutCons.gridy = 1;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 0.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(cbStoptype, layoutCons);
      contentPane.add(cbStoptype);
      
      /*JLabel*/ label = new JLabel("Time on your GPS device");
      
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
      tfGPSTimeStart.addActionListener(this);
      
      layoutCons.gridx = 0;
      layoutCons.gridy = 3;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 0.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(tfGPSTimeStart, layoutCons);
      contentPane.add(tfGPSTimeStart);
      
      /*JLabel*/ label = new JLabel("HH:MM:SS.sss");
      
      layoutCons.gridx = 1;
      layoutCons.gridy = 3;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 0.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(label, layoutCons);
      contentPane.add(label);
            
      /*JLabel*/ label = new JLabel("Time on your stopwatch");
      
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
      tfStopwatchStart.addActionListener(this);
      
      layoutCons.gridx = 0;
      layoutCons.gridy = 5;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 0.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(tfStopwatchStart, layoutCons);
      contentPane.add(tfStopwatchStart);
      
      /*JLabel*/ label = new JLabel("HH:MM:SS.sss");
      
      layoutCons.gridx = 1;
      layoutCons.gridy = 5;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 0.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(label, layoutCons);
      contentPane.add(label);
      
      /*JLabel*/ label = new JLabel("Time window");
      
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
      tfTimeWindow.addActionListener(this);
      
      layoutCons.gridx = 0;
      layoutCons.gridy = 7;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 0.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(tfTimeWindow, layoutCons);
      contentPane.add(tfTimeWindow);
      
      /*JLabel*/ label = new JLabel("seconds");
      
      layoutCons.gridx = 1;
      layoutCons.gridy = 7;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 0.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(label, layoutCons);
      contentPane.add(label);
      
      /*JLabel*/ label = new JLabel("Move Threshold");
      
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
      tfThreshold.addActionListener(this);
      
      layoutCons.gridx = 0;
      layoutCons.gridy = 9;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 0.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(tfThreshold, layoutCons);
      contentPane.add(tfThreshold);
      
      /*JLabel*/ label = new JLabel("meters");
      
      layoutCons.gridx = 1;
      layoutCons.gridy = 9;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 0.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(label, layoutCons);
      contentPane.add(label);
      
      JButton bSuggestStops = new JButton("Suggest Stops");
      bSuggestStops.setActionCommand("stopImporter.settingsSuggestStops");
      bSuggestStops.addActionListener(this);
      
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
      
      JButton bFind = new JButton("Find");
      bFind.setActionCommand("stopImporter.stoplistFind");
      bFind.addActionListener(this);
      
      layoutCons.gridx = 0;
      layoutCons.gridy = 1;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(bFind, layoutCons);
      contentPane.add(bFind);
      
      JButton bShow = new JButton("Show");
      bShow.setActionCommand("stopImporter.stoplistShow");
      bShow.addActionListener(this);
      
      layoutCons.gridx = 0;
      layoutCons.gridy = 2;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(bShow, layoutCons);
      contentPane.add(bShow);
      
      JButton bMark = new JButton("Mark");
      bMark.setActionCommand("stopImporter.stoplistMark");
      bMark.addActionListener(this);
      
      layoutCons.gridx = 1;
      layoutCons.gridy = 1;
      layoutCons.gridheight = 1;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(bMark, layoutCons);
      contentPane.add(bMark);
      
      JButton bDetach = new JButton("Detach");
      bDetach.setActionCommand("stopImporter.stoplistDetach");
      bDetach.addActionListener(this);
      
      layoutCons.gridx = 1;
      layoutCons.gridy = 2;
      layoutCons.gridheight = 1;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(bDetach, layoutCons);
      contentPane.add(bDetach);
      
      JButton bAdd = new JButton("Add");
      bAdd.setActionCommand("stopImporter.stoplistAdd");
      bAdd.addActionListener(this);
      
      layoutCons.gridx = 2;
      layoutCons.gridy = 1;
      layoutCons.gridheight = 1;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(bAdd, layoutCons);
      contentPane.add(bAdd);
      
      JButton bDelete = new JButton("Delete");
      bDelete.setActionCommand("stopImporter.stoplistDelete");
      bDelete.addActionListener(this);
      
      layoutCons.gridx = 2;
      layoutCons.gridy = 2;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(bDelete, layoutCons);
      contentPane.add(bDelete);
      
      JButton bSort = new JButton("Sort");
      bSort.setActionCommand("stopImporter.stoplistSort");
      bSort.addActionListener(this);
      
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
      
      waypointTable = new JTable();
      /*JScrollPane*/ tableSP = new JScrollPane(waypointTable);
      
      layoutCons.gridx = 0;
      layoutCons.gridy = 0;
      layoutCons.gridwidth = 3;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 1.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(tableSP, layoutCons);
      contentPane.add(tableSP);
      
      /*JButton*/ bFind = new JButton("Find");
      bFind.setActionCommand("stopImporter.waypointsFind");
      bFind.addActionListener(this);
      
      layoutCons.gridx = 0;
      layoutCons.gridy = 1;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(bFind, layoutCons);
      contentPane.add(bFind);
      
      /*JButton*/ bShow = new JButton("Show");
      bShow.setActionCommand("stopImporter.waypointsShow");
      bShow.addActionListener(this);
      
      layoutCons.gridx = 0;
      layoutCons.gridy = 2;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(bShow, layoutCons);
      contentPane.add(bShow);
      
      /*JButton*/ bMark = new JButton("Mark");
      bMark.setActionCommand("stopImporter.waypointsMark");
      bMark.addActionListener(this);
      
      layoutCons.gridx = 1;
      layoutCons.gridy = 1;
      layoutCons.gridheight = 1;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(bMark, layoutCons);
      contentPane.add(bMark);
      
      /*JButton*/ bDetach = new JButton("Detach");
      bDetach.setActionCommand("stopImporter.waypointsDetach");
      bDetach.addActionListener(this);
      
      layoutCons.gridx = 1;
      layoutCons.gridy = 2;
      layoutCons.gridheight = 1;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(bDetach, layoutCons);
      contentPane.add(bDetach);
      
      /*JButton*/ bAdd = new JButton("Enable");
      bAdd.setActionCommand("stopImporter.waypointsAdd");
      bAdd.addActionListener(this);
      
      layoutCons.gridx = 2;
      layoutCons.gridy = 1;
      layoutCons.gridheight = 1;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(bAdd, layoutCons);
      contentPane.add(bAdd);
      
      /*JButton*/ bDelete = new JButton("Disable");
      bDelete.setActionCommand("stopImporter.waypointsDelete");
      bDelete.addActionListener(this);
      
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

    jDialog.setVisible(true);

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
      
/*      Iterator< GpxTrack > iter = data.tracks.iterator();
      while (iter.hasNext())
      {
	GpxTrack track = iter.next();
	System.out.println("");
	System.out.println(track.getAttributes().get("name"));
	Iterator< GpxTrackSegment > siter = track.getSegments().iterator();
	while (siter.hasNext())
	{
	  Iterator< WayPoint > witer = siter.next().getWayPoints().iterator();
	  while (witer.hasNext())
	  {
	    System.out.println(witer.next());
	  }
	}
      }*/
    }
    else if ("stopImporter.settingsGPSTimeStart"
      .equals(event.getActionCommand()))
    {
      if (parseTime(tfGPSTimeStart.getText()) >= 0)
      {
	if (currentTrack != null)
	{
	  currentTrack.gpsSyncTime = tfGPSTimeStart.getText();
	  currentTrack.relocateNodes();
	}
      }
      else
      {
	JOptionPane.showMessageDialog
	(null, "Can't parse a time from this string.", "Invalid value",
	 JOptionPane.ERROR_MESSAGE);
      }
    }
    else if ("stopImporter.settingsStopwatchStart"
      .equals(event.getActionCommand()))
    {
      if (parseTime(tfStopwatchStart.getText()) >= 0)
      {
	if (currentTrack != null)
	{
	  currentTrack.stopwatchStart = tfStopwatchStart.getText();
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
	currentTrack.timeWindow = Double.parseDouble(tfTimeWindow.getText());
    }
    else if ("stopImporter.settingsThreshold".equals(event.getActionCommand()))
    {
      if (currentTrack != null)
	currentTrack.threshold = Double.parseDouble(tfThreshold.getText());
    }
    else if ("stopImporter.settingsSuggestStops".equals(event.getActionCommand()))
    {
      currentTrack.suggestStops();
    }
    else if ("stopImporter.stoplistFind".equals(event.getActionCommand()))
    {
      if (Main.main.getCurrentDataSet() == null)
	return;
      
      stoplistTable.clearSelection();
      
      for (int i = 0; i < currentTrack.stoplistTM.getRowCount(); ++i)
      {
	if ((currentTrack.stoplistTM.nodes.elementAt(i) != null) &&
		    (Main.main.getCurrentDataSet().isSelected(currentTrack.stoplistTM.nodes.elementAt(i))))
	  stoplistTable.addRowSelectionInterval(i, i);
      }
    }
    else if ("stopImporter.stoplistShow".equals(event.getActionCommand()))
    {
      BoundingXYVisitor box = new BoundingXYVisitor();
      if (stoplistTable.getSelectedRowCount() > 0)
      {
	for (int i = 0; i < currentTrack.stoplistTM.getRowCount(); ++i)
	{
	  if ((stoplistTable.isRowSelected(i)) &&
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
      if (stoplistTable.getSelectedRowCount() > 0)
      {
	for (int i = 0; i < currentTrack.stoplistTM.getRowCount(); ++i)
	{
	  if ((stoplistTable.isRowSelected(i)) &&
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
      if (stoplistTable.getSelectedRowCount() > 0)
      {
	for (int i = 0; i < currentTrack.stoplistTM.getRowCount(); ++i)
	{
	  if ((stoplistTable.isRowSelected(i)) &&
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
      stoplistTable.clearSelection();
    }
    else if ("stopImporter.stoplistAdd".equals(event.getActionCommand()))
    {
      int insPos = stoplistTable.getSelectedRow();
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
	if (stoplistTable.isRowSelected(i))
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
      int insPos = stoplistTable.getSelectedRow();
      Vector< NodeSortEntry > nodesToSort = new Vector< NodeSortEntry >();
      if (currentTrack == null)
	return;
      if (stoplistTable.getSelectedRowCount() > 0)
      {
	for (int i = currentTrack.stoplistTM.getRowCount()-1; i >=0; --i)
	{
	  if (stoplistTable.isRowSelected(i))
	  {
	    nodesToSort.add(new NodeSortEntry
		(currentTrack.stoplistTM.nodes.elementAt(i),
		 (String)currentTrack.stoplistTM.getValueAt(i, 0),
		  (String)currentTrack.stoplistTM.getValueAt(i, 1),
		   parseTime(currentTrack.stopwatchStart)));
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
		 parseTime(currentTrack.stopwatchStart)));
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
      
      waypointTable.clearSelection();
      
      for (int i = 0; i < waypointTM.getRowCount(); ++i)
      {
	if ((waypointTM.nodes.elementAt(i) != null) &&
		    (Main.main.getCurrentDataSet().isSelected(waypointTM.nodes.elementAt(i))))
	  waypointTable.addRowSelectionInterval(i, i);
      }
    }
    else if ("stopImporter.waypointsShow".equals(event.getActionCommand()))
    {
      BoundingXYVisitor box = new BoundingXYVisitor();
      if (waypointTable.getSelectedRowCount() > 0)
      {
	for (int i = 0; i < waypointTM.getRowCount(); ++i)
	{
	  if ((waypointTable.isRowSelected(i)) &&
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
      if (waypointTable.getSelectedRowCount() > 0)
      {
	for (int i = 0; i < waypointTM.getRowCount(); ++i)
	{
	  if ((waypointTable.isRowSelected(i)) &&
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
      if (waypointTable.getSelectedRowCount() > 0)
      {
	for (int i = 0; i < waypointTM.getRowCount(); ++i)
	{
	  if ((waypointTable.isRowSelected(i)) &&
		      (waypointTM.nodes.elementAt(i) != null))
	  {
	    waypointTM.nodes.set(i, null);
	  }
	}
      }
      else
      {
	for (int i = 0; i < waypointTM.getRowCount(); ++i)
	{
	  if (waypointTM.nodes.elementAt(i) != null)
	    waypointTM.nodes.set(i, null);
	}
      }
      waypointTable.clearSelection();
    }
    else if ("stopImporter.waypointsAdd".equals(event.getActionCommand()))
    {
      if (waypointTable.getSelectedRowCount() > 0)
      {
	for (int i = 0; i < waypointTM.getRowCount(); ++i)
	{
	  if ((waypointTable.isRowSelected(i)) &&
		      (waypointTM.nodes.elementAt(i) == null))
	  {
	    Node node = createNode(waypointTM.coors.elementAt(i), (String)waypointTM.getValueAt(i, 1));
	    waypointTM.nodes.set(i, node);
	    Main.main.getCurrentDataSet().addSelected(waypointTM.nodes.elementAt(i));
	  }
	}
      }
      else
      {
	for (int i = 0; i < waypointTM.getRowCount(); ++i)
	{
	  if (waypointTM.nodes.elementAt(i) == null)
	    Main.main.getCurrentDataSet().addSelected(waypointTM.nodes.elementAt(i));
	}
      }
    }
    else if ("stopImporter.waypointsDelete".equals(event.getActionCommand()))
    {
      Vector< Node > toDelete = new Vector< Node >();
      for (int i = waypointTM.getRowCount()-1; i >=0; --i)
      {
	if (waypointTable.isRowSelected(i))
	{
	  if ((Node)waypointTM.nodes.elementAt(i) != null)
	    toDelete.add((Node)waypointTM.nodes.elementAt(i));
	  waypointTM.nodes.set(i, null);
	}
      }
      Command cmd = DeleteCommand.delete
	  (Main.main.getEditLayer(), toDelete);
      if (cmd != null) {
	// cmd can be null if the user cancels dialogs DialogCommand displays
	Main.main.undoRedo.add(cmd);
      }
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
          if ("bus".equals((String)cbStoptype.getSelectedItem()))
            node.put("highway", "bus_stop");
          else if ("tram".equals((String)cbStoptype.getSelectedItem()))
            node.put("railway", "tram_stop");
          else if ("light_rail".equals((String)cbStoptype.getSelectedItem()))
            node.put("railway", "station");
          else if ("subway".equals((String)cbStoptype.getSelectedItem()))
            node.put("railway", "station");
          else if ("rail".equals((String)cbStoptype.getSelectedItem()))
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
	    if ("bus".equals((String)cbStoptype.getSelectedItem()))
	      node.put("highway", "bus_stop");
	    else if ("tram".equals((String)cbStoptype.getSelectedItem()))
	      node.put("railway", "tram_stop");
	    else if ("light_rail".equals((String)cbStoptype.getSelectedItem()))
	      node.put("railway", "station");
	    else if ("subway".equals((String)cbStoptype.getSelectedItem()))
	      node.put("railway", "station");
	    else if ("rail".equals((String)cbStoptype.getSelectedItem()))
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
      waypointTable.setModel(waypointTM);
    }
    else
    {
      JOptionPane.showMessageDialog
      (null, "The GPX file contained no tracks or waypoints.", "No data found",
       JOptionPane.ERROR_MESSAGE);
      
      System.out.println("Public Transport: StopImporter: No data found");
    }
  }
  
  private void tracksSelectionChanged()
  {
    int selectedPos = tracksList.getAnchorSelectionIndex();
    if (tracksList.isSelectedIndex(selectedPos))
    {
      currentTrack = ((TrackReference)tracksListModel.elementAt(selectedPos));
      tabbedPane.setEnabledAt(1, true);
      tabbedPane.setEnabledAt(2, true);
      
      //Prepare Settings
      tfGPSTimeStart.setText(currentTrack.gpsSyncTime);
      tfStopwatchStart.setText(currentTrack.stopwatchStart);
      tfTimeWindow.setText(Double.toString(currentTrack.timeWindow));
      tfThreshold.setText(Double.toString(currentTrack.threshold));
      
      //Prepare Stoplist
      stoplistTable.setModel
          (((TrackReference)tracksListModel.elementAt(selectedPos)).stoplistTM);
    }
    else
    {
      currentTrack = null;
      tabbedPane.setEnabledAt(1, false);
      tabbedPane.setEnabledAt(2, false);
    }
  }

  private Node createNode(LatLon latLon, String name)
  {
    Node node = new Node(latLon);
    if ("bus".equals((String)cbStoptype.getSelectedItem()))
      node.put("highway", "bus_stop");
    else if ("tram".equals((String)cbStoptype.getSelectedItem()))
      node.put("railway", "tram_stop");
    else if ("light_rail".equals((String)cbStoptype.getSelectedItem()))
      node.put("railway", "station");
    else if ("subway".equals((String)cbStoptype.getSelectedItem()))
      node.put("railway", "station");
    else if ("rail".equals((String)cbStoptype.getSelectedItem()))
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
    
  private static double parseTime(String s)
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

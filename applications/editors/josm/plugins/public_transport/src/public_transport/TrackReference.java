package public_transport;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Iterator;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.GpxTrackSegment;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.data.osm.Node;

public class TrackReference
    implements Comparable< TrackReference >, TableModelListener
{
  public GpxTrack track;
  public TrackStoplistTableModel stoplistTM;
  public String stopwatchStart;
  public String gpsStartTime;
  public String gpsSyncTime;
  public double timeWindow;
  public double threshold;
  private StopImporterAction controller = null;
    
  public TrackReference(GpxTrack track, StopImporterAction controller)
  {
    this.track = track;
    this.stoplistTM = new TrackStoplistTableModel(this);
    this.stopwatchStart = "00:00:00";
    this.gpsStartTime = null;
    this.gpsSyncTime = null;
    this.controller = controller;
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
  
  public GpxTrack getGpxTrack()
  {
    return track;
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
    if ((e.getType() == TableModelEvent.UPDATE) && (e.getFirstRow() >= 0))
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
	Node node = controller.createNode
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
	stoplistTM.insertRow(-1, StopImporterAction.timeOf(time));
	Node node = controller.createNode(latLon, "");
	stoplistTM.nodes.set(stoplistTM.getRowCount()-1, node);
      }
	
      lastStopCoor = latLon;
    }
  }
};

package public_transport;

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
  public boolean inEvent = false;

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
        (null, tr("The GPX file doesn''t contain valid trackpoints. "
        + "Please use a GPX file that has trackpoints."), tr("GPX File Trouble"),
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
      return tr("unnamed");
    return buf;
  }

  public void tableChanged(TableModelEvent e)
  {
    if ((e.getType() == TableModelEvent.UPDATE) && (e.getFirstRow() >= 0))
    {
      if (inEvent)
    return;

      double time = StopImporterDialog.parseTime
        ((String)stoplistTM.getValueAt(e.getFirstRow(), 0));
      if (time < 0)
      {
    stoplistTM.setValueAt
        (stoplistTM.timeAt(e.getFirstRow()), e.getFirstRow(), 0);
    JOptionPane.showMessageDialog
        (null, tr("Can''t parse a time from this string."), tr("Invalid value"),
         JOptionPane.ERROR_MESSAGE);
    return;
      }

      Main.main.undoRedo.add(new TrackStoplistNameCommand
              (this, e.getFirstRow()));
      stoplistTM.setTimeAt
      (e.getFirstRow(), (String)stoplistTM.getValueAt(e.getFirstRow(), 0));
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
    for (int i = 0; i < stoplistTM.getNodes().size(); ++i)
    {
      Node node = stoplistTM.nodeAt(i);
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
};

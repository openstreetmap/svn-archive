package public_transport;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.DefaultListModel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.gpx.GpxTrackSegment;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

public class TrackSuggestStopsCommand extends Command
{
  private TrackStoplistTableModel stoplistTM = null;
  private String type = null;
  private String stopwatchStart;
  private String gpsStartTime;
  private String gpsSyncTime;
  private double timeWindow;
  private double threshold;
  private Collection< GpxTrackSegment > segments = null;
  private Vector< Vector< Object > > tableDataModel = null;
  private Vector< Node > nodes = null;
  private Vector< String > times = null;

  public TrackSuggestStopsCommand(StopImporterAction controller)
  {
    if (controller.getCurrentTrack() == null)
      return;
    stoplistTM = controller.getCurrentTrack().stoplistTM;
    type = controller.getDialog().getStoptype();
    stopwatchStart = controller.getCurrentTrack().stopwatchStart;
    gpsStartTime = controller.getCurrentTrack().gpsStartTime;
    gpsSyncTime = controller.getCurrentTrack().gpsSyncTime;
    timeWindow = controller.getCurrentTrack().timeWindow;
    threshold = controller.getCurrentTrack().threshold;
    segments = controller.getCurrentTrack().getGpxTrack().getSegments();
  }

  @SuppressWarnings("unchecked")
  public boolean executeCommand()
  {
    if (stoplistTM == null)
      return false;
    tableDataModel = (Vector< Vector< Object > >)stoplistTM.getDataVector()
    .clone();
    nodes = (Vector< Node >)stoplistTM.getNodes().clone();
    times = (Vector< String >)stoplistTM.getTimes().clone();

    for (int i = 0; i < stoplistTM.getNodes().size(); ++i)
    {
      Node node = stoplistTM.nodeAt(i);
      if (node == null)
        continue;
      Main.main.getCurrentDataSet().removePrimitive(node);
      node.setDeleted(true);
    }
    stoplistTM.clear();

    Vector< WayPoint > wayPoints = new Vector< WayPoint >();
    Iterator< GpxTrackSegment > siter = segments.iterator();
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
        Node node = StopImporterAction.createNode(latLon, type, "");
        stoplistTM.insertRow(-1, node, StopImporterAction.timeOf(time), "", new TransText(null));
      }

      lastStopCoor = latLon;
    }

    return true;
  }

  public void undoCommand()
  {
    if (stoplistTM == null)
      return;
    for (int i = 0; i < stoplistTM.getNodes().size(); ++i)
    {
      Node node = stoplistTM.nodeAt(i);
      if (node == null)
        continue;
      Main.main.getCurrentDataSet().removePrimitive(node);
      node.setDeleted(true);
    }

    stoplistTM.setDataVector(tableDataModel);
    stoplistTM.setNodes(nodes);
    stoplistTM.setTimes(times);

    for (int i = 0; i < stoplistTM.getNodes().size(); ++i)
    {
      Node node = stoplistTM.nodeAt(i);
      if (node == null)
        continue;
      node.setDeleted(false);
      Main.main.getCurrentDataSet().addPrimitive(node);
    }
  }

  public void fillModifiedData
    (Collection< OsmPrimitive > modified, Collection< OsmPrimitive > deleted,
     Collection< OsmPrimitive > added)
  {
  }

  @Override public String getDescriptionText()
  {
    return tr("Public Transport: Suggest stops");
  }

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
};

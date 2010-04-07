package public_transport;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

import java.util.Collection;
import java.util.Vector;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

public class TrackStoplistNameCommand extends Command
{
  private int workingLine = 0;
  private TrackReference trackref = null;
  private String oldName = null;
  private String name = null;
  private String oldTime = null;
  private String time = null;
  private LatLon oldLatLon = null;
  
  @SuppressWarnings("unchecked")
  public TrackStoplistNameCommand
    (TrackReference trackref, int workingLine, String time, String name)
  {
    this.trackref = trackref;
    this.workingLine = workingLine;
    Node node = trackref.stoplistTM.nodeAt(workingLine);
    if (node != null)
    {
      oldName = node.get("name");
      oldTime = trackref.stoplistTM.timeAt(workingLine);
      oldLatLon = (LatLon)node.getCoor().clone();
      System.out.println("Setze oldLatLon: " + oldLatLon);
    }
    this.name = name;
    this.time = time;
  }
  
  public boolean executeCommand()
  {
    Node node = trackref.stoplistTM.nodeAt(workingLine);
    if (node != null)
    {
      node.put("name", name);
      double dTime = StopImporterDialog.parseTime(time);
      node.setCoor(trackref.computeCoor(dTime));
      trackref.inEvent = true;
      trackref.stoplistTM.setValueAt(time, workingLine, 0);
      trackref.stoplistTM.setValueAt(name, workingLine, 1);
      trackref.inEvent = false;
    }
    return true;
  }
  
  public void undoCommand()
  {
    Node node = trackref.stoplistTM.nodeAt(workingLine);
    if (node != null)
    {
      node.put("name", oldName);
      System.out.println("Verwende oldLatLon: " + oldLatLon);
      node.setCoor(oldLatLon);
      trackref.inEvent = true;
      trackref.stoplistTM.setValueAt(oldTime, workingLine, 0);
      trackref.stoplistTM.setValueAt(oldName, workingLine, 1);
      trackref.inEvent = false;
    }
  }
  
  public void fillModifiedData
    (Collection< OsmPrimitive > modified, Collection< OsmPrimitive > deleted,
     Collection< OsmPrimitive > added)
  {
  }
  
  public MutableTreeNode description()
  {
    return new DefaultMutableTreeNode("public_transport.TrackStoplist.Edit");
  }
};

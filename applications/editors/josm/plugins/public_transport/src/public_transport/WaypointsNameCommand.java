package public_transport;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

import java.util.Collection;
import java.util.Vector;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

public class WaypointsNameCommand extends Command
{
  private int workingLine = 0;
  private WaypointTableModel waypointTM = null;
  private String oldName = null;
  private String name = null;
  
  public WaypointsNameCommand(WaypointTableModel waypointTM, int workingLine, String name)
  {
    this.waypointTM = waypointTM;
    this.workingLine = workingLine;
    if (waypointTM.nodes.elementAt(workingLine) != null)
      oldName = waypointTM.nodes.elementAt(workingLine).get("name");
    this.name = name;
  }
  
  public boolean executeCommand()
  {
    if (waypointTM.nodes.elementAt(workingLine) != null)
    {
      waypointTM.nodes.elementAt(workingLine).put("name", name);
      waypointTM.inEvent = true;
      waypointTM.setValueAt(name, workingLine, 1);
      waypointTM.inEvent = false;
    }
    return true;
  }
  
  public void undoCommand()
  {
    if (waypointTM.nodes.elementAt(workingLine) != null)
    {
      waypointTM.nodes.elementAt(workingLine).put("name", oldName);
      waypointTM.inEvent = true;
      waypointTM.setValueAt(oldName, workingLine, 1);
      waypointTM.inEvent = false;
    }
  }
  
  public void fillModifiedData
    (Collection< OsmPrimitive > modified, Collection< OsmPrimitive > deleted,
     Collection< OsmPrimitive > added)
  {
  }
  
  public MutableTreeNode description()
  {
    return new DefaultMutableTreeNode("public_transport.Waypoints.EditName");
  }
};

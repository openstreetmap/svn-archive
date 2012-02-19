package public_transport;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

import java.util.Collection;
import java.util.Vector;

public class WaypointsDetachCommand extends Command
{
  private Vector< Integer > workingLines = null;
  private Vector< Node > nodesForUndo = null;
  private WaypointTableModel waypointTM = null;

  public WaypointsDetachCommand(StopImporterAction controller)
  {
    waypointTM = controller.getWaypointTableModel();
    workingLines = new Vector< Integer >();
    nodesForUndo = new Vector< Node >();

    // use either selected lines or all lines if no line is selected
    int[] selectedLines = controller.getDialog().getWaypointsTable().getSelectedRows();
    Vector< Integer > consideredLines = new Vector< Integer >();
    if (selectedLines.length > 0)
    {
      for (int i = 0; i < selectedLines.length; ++i)
    consideredLines.add(selectedLines[i]);
    }
    else
    {
      for (int i = 0; i < waypointTM.getRowCount(); ++i)
    consideredLines.add(new Integer(i));
    }

    // keep only lines where a node can be added
    for (int i = 0; i < consideredLines.size(); ++i)
    {
      if (waypointTM.nodes.elementAt(consideredLines.elementAt(i)) != null)
    workingLines.add(consideredLines.elementAt(i));
    }
  }

  public boolean executeCommand()
  {
    nodesForUndo.clear();
    for (int i = 0; i < workingLines.size(); ++i)
    {
      int j = workingLines.elementAt(i).intValue();
      Node node = waypointTM.nodes.elementAt(j);
      nodesForUndo.add(node);
      waypointTM.nodes.set(j, null);
    }
    return true;
  }

  public void undoCommand()
  {
    for (int i = 0; i < workingLines.size(); ++i)
    {
      int j = workingLines.elementAt(i).intValue();
      Node node = nodesForUndo.elementAt(i);
      waypointTM.nodes.set(j, node);
    }
  }

  public void fillModifiedData
    (Collection< OsmPrimitive > modified, Collection< OsmPrimitive > deleted,
     Collection< OsmPrimitive > added)
  {
  }

  @Override public String getDescriptionText()
  {
    return tr("Public Transport: Detach waypoints");
  }
};

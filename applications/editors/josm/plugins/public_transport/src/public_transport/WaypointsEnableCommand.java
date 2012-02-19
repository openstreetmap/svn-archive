package public_transport;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

import java.util.Collection;
import java.util.Vector;

public class WaypointsEnableCommand extends Command
{
  private Vector< Integer > workingLines = null;
  private WaypointTableModel waypointTM = null;
  private String type = null;

  public WaypointsEnableCommand(StopImporterAction controller)
  {
    waypointTM = controller.getWaypointTableModel();
    type = controller.getDialog().getStoptype();
    workingLines = new Vector< Integer >();

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
      if (waypointTM.nodes.elementAt(consideredLines.elementAt(i)) == null)
        workingLines.add(consideredLines.elementAt(i));
    }
  }

  public boolean executeCommand()
  {
    for (int i = 0; i < workingLines.size(); ++i)
    {
      int j = workingLines.elementAt(i).intValue();
      Node node = StopImporterAction.createNode
        (waypointTM.coors.elementAt(j), type, (String)waypointTM.getValueAt(j, 1));
      TransText shelter = (TransText)waypointTM.getValueAt(j, 2);
      node.put("shelter", shelter.text);
      waypointTM.nodes.set(j, node);
    }
    return true;
  }

  public void undoCommand()
  {
    for (int i = 0; i < workingLines.size(); ++i)
    {
      int j = workingLines.elementAt(i).intValue();
      Node node = waypointTM.nodes.elementAt(j);
      waypointTM.nodes.set(j, null);
      if (node == null)
        continue;
      Main.main.getCurrentDataSet().removePrimitive(node);
      node.setDeleted(true);
    }
  }

  public void fillModifiedData
    (Collection< OsmPrimitive > modified, Collection< OsmPrimitive > deleted,
     Collection< OsmPrimitive > added)
  {
  }

  @Override public String getDescriptionText()
  {
    return tr("Public Transport: Enable waypoints");
  }
};

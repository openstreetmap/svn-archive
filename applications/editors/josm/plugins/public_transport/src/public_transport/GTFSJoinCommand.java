package public_transport;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

public class GTFSJoinCommand extends Command
{
  private Vector< Integer > workingLines = null;
  private Node undoMapNode = null;
  private Node undoTableNode = null;
  private GTFSStopTableModel gtfsStopTM = null;
  private String type = null;

  public GTFSJoinCommand(GTFSImporterAction controller)
  {
    gtfsStopTM = controller.getGTFSStopTableModel();
    workingLines = new Vector< Integer >();

    // use either selected lines or all lines if no line is selected
    int[] selectedLines = controller.getDialog().getGTFSStopTable().getSelectedRows();
    if (selectedLines.length != 1)
      return;
    workingLines.add(selectedLines[0]);
  }

  public boolean executeCommand()
  {
    if (workingLines.size() != 1)
      return false;
    Node dest = null;
    Iterator< Node > iter =
        Main.main.getCurrentDataSet().getSelectedNodes().iterator();
    int j = workingLines.elementAt(0);
    while (iter.hasNext())
    {
      Node n = iter.next();
      if ((n != null) && (n.equals(gtfsStopTM.nodes.elementAt(j))))
    continue;
      if (dest != null)
    return false;
      dest = n;
    }
    if (dest == null)
      return false;
    undoMapNode = new Node(dest);

    Node node = gtfsStopTM.nodes.elementAt(j);
    undoTableNode = node;
    if (node != null)
    {
      Main.main.getCurrentDataSet().removePrimitive(node);
      node.setDeleted(true);
    }

    dest.put("highway", "bus_stop");
    dest.put("stop_id", (String)gtfsStopTM.getValueAt(j, 0));
    if (dest.get("name") == null)
      dest.put("name", (String)gtfsStopTM.getValueAt(j, 1));
    gtfsStopTM.nodes.set(j, dest);
    type = (String)gtfsStopTM.getValueAt(j, 2);
    gtfsStopTM.setValueAt(tr("moved"), j, 2);

    return true;
  }

  public void undoCommand()
  {
    if (workingLines.size() != 1)
      return;
    int j = workingLines.elementAt(0);

    Node node = gtfsStopTM.nodes.elementAt(j);
    if (node != null)
    {
      Main.main.getCurrentDataSet().removePrimitive(node);
      node.setDeleted(true);
    }

    if (undoMapNode != null)
    {
      undoMapNode.setDeleted(false);
      Main.main.getCurrentDataSet().addPrimitive(undoMapNode);
    }
    if (undoTableNode != null)
    {
      undoTableNode.setDeleted(false);
      Main.main.getCurrentDataSet().addPrimitive(undoTableNode);
    }
    gtfsStopTM.nodes.set(j, undoTableNode);
    gtfsStopTM.setValueAt(type, j, 2);
  }

  public void fillModifiedData
    (Collection< OsmPrimitive > modified, Collection< OsmPrimitive > deleted,
     Collection< OsmPrimitive > added)
  {
  }

  @Override public String getDescriptionText()
  {
    return tr("Public Transport: Join GTFS stops");
  }
};

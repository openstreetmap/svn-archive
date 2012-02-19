package public_transport;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

import java.util.Collection;
import java.util.Vector;
import javax.swing.DefaultListModel;

public class SettingsStoptypeCommand extends Command
{
  private class HighwayRailway
  {
    public HighwayRailway(Node node)
    {
      this.node = node;
      highway = node.get("highway");
      railway = node.get("railway");
    }

    public Node node;
    public String highway;
    public String railway;
  };

  private Vector< HighwayRailway > oldStrings = null;
  private WaypointTableModel waypointTM = null;
  private DefaultListModel tracksListModel = null;
  private String type = null;

  public SettingsStoptypeCommand(StopImporterAction controller)
  {
    waypointTM = controller.getWaypointTableModel();
    tracksListModel = controller.getTracksListModel();
    type = controller.getDialog().getStoptype();
    oldStrings = new Vector< HighwayRailway >();
  }

  public boolean executeCommand()
  {
    oldStrings.clear();
    for (int i = 0; i < waypointTM.getRowCount(); ++i)
    {
      if ((Node)waypointTM.nodes.elementAt(i) != null)
      {
        Node node = (Node)waypointTM.nodes.elementAt(i);
        oldStrings.add(new HighwayRailway(node));
        StopImporterAction.setTagsWrtType(node, type);
      }
    }
    for (int j = 0; j < tracksListModel.size(); ++j)
    {
      TrackReference track = (TrackReference)tracksListModel.elementAt(j);
      for (int i = 0; i < track.stoplistTM.getRowCount(); ++i)
      {
        if (track.stoplistTM.nodeAt(i) != null)
        {
          Node node = track.stoplistTM.nodeAt(i);
          oldStrings.add(new HighwayRailway(node));
          StopImporterAction.setTagsWrtType(node, type);
        }
      }
    }
    return true;
  }

  public void undoCommand()
  {
    for (int i = 0; i < oldStrings.size(); ++i)
    {
      HighwayRailway hr = oldStrings.elementAt(i);
      hr.node.put("highway", hr.highway);
      hr.node.put("railway", hr.railway);
    }
  }

  public void fillModifiedData
    (Collection< OsmPrimitive > modified, Collection< OsmPrimitive > deleted,
     Collection< OsmPrimitive > added)
  {
  }

  @Override public String getDescriptionText()
  {
    return tr("Public Transport: Change stop type");
  }

};

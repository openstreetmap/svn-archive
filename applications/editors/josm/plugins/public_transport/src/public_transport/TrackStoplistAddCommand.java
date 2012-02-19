package public_transport;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

import java.util.Collection;
import java.util.Vector;

public class TrackStoplistAddCommand extends Command
{
  private int workingLine;
  private TrackStoplistTableModel stoplistTM = null;

  public TrackStoplistAddCommand(StopImporterAction controller)
  {
    stoplistTM = controller.getCurrentTrack().stoplistTM;
    workingLine = controller.getDialog().getStoplistTable().getSelectedRow();
  }

  public boolean executeCommand()
  {
    stoplistTM.insertRow(workingLine, "00:00:00");
    return true;
  }

  public void undoCommand()
  {
    int workingLine = this.workingLine;
    if (workingLine < 0)
      workingLine = stoplistTM.getRowCount()-1;
    stoplistTM.removeRow(workingLine);
  }

  public void fillModifiedData
    (Collection< OsmPrimitive > modified, Collection< OsmPrimitive > deleted,
     Collection< OsmPrimitive > added)
  {
  }

  @Override public String getDescriptionText()
  {
    return tr("Public Transport: Add track stop");
  }
};

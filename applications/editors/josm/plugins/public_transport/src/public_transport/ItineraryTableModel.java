package public_transport;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;

public class ItineraryTableModel extends DefaultTableModel
    implements TableModelListener
{
  public Vector<Way> ways = new Vector<Way>();
  public boolean inEvent = false;

  public boolean isCellEditable(int row, int column)
  {
    if (column != 1)
      return false;
    if (ways.elementAt(row) == null)
      return false;
    return true;
  }

  public void addRow(Object[] obj)
  {
    ways.addElement(null);
    super.addRow(obj);
  }

  public void insertRow(int insPos, Object[] obj)
  {
    if (insPos == -1)
    {
      ways.addElement(null);
      super.addRow(obj);
    }
    else
    {
      ways.insertElementAt(null, insPos);
      super.insertRow(insPos, obj);
    }
  }

  public void addRow(Way way, String role)
  {
    insertRow(-1, way, role);
  }

  public void insertRow(int insPos, Way way, String role)
  {
    String[] buf = { "", "" };
    String curName = way.get("name");
    if (way.isIncomplete())
      buf[0] = tr("[incomplete]");
    else if (way.getNodesCount() < 1)
      buf[0] = tr("[empty way]");
    else if (curName != null)
      buf[0] = curName;
    else
      buf[0] = tr("[ID] {0}", (new Long(way.getId())).toString());
    buf[1] = role;
    if (insPos == -1)
    {
      ways.addElement(way);
      super.addRow(buf);
    }
    else
    {
      ways.insertElementAt(way, insPos);
      super.insertRow(insPos, buf);
    }
  }

  public void clear()
  {
    ways.clear();
    super.setRowCount(0);
  }

  public void cleanupGaps()
  {
    inEvent = true;
    Node lastNode = null;

    for (int i = 0; i < getRowCount(); ++i)
    {
      if (ways.elementAt(i) == null)
      {
        ++i;
        if (i >= getRowCount())
          break;
      }
      while ((ways.elementAt(i) == null) &&
      ((i == 0) || (ways.elementAt(i-1) == null)))
      {
        ways.removeElementAt(i);
        removeRow(i);
        if (i >= getRowCount())
          break;
      }
      if (i >= getRowCount())
        break;

      boolean gapRequired = gapNecessary
      (ways.elementAt(i), (String)(getValueAt(i, 1)), lastNode);
      if ((i > 0) && (!gapRequired) && (ways.elementAt(i-1) == null))
      {
        ways.removeElementAt(i-1);
        removeRow(i-1);
        --i;
      }
      else if ((i > 0) && gapRequired && (ways.elementAt(i-1) != null))
      {
        String[] buf = { "", "" };
        buf[0] = tr("[gap]");
        insertRow(i, buf);
        ++i;
      }
      lastNode = getLastNode(ways.elementAt(i), (String)(getValueAt(i, 1)));
    }
    while ((getRowCount() > 0) &&
      (ways.elementAt(getRowCount()-1) == null))
    {
      ways.removeElementAt(getRowCount()-1);
      removeRow(getRowCount()-1);
    }
    inEvent = false;
  }

  public void tableChanged(TableModelEvent e)
  {
    if (e.getType() == TableModelEvent.UPDATE)
    {
      if (inEvent)
        return;
      cleanupGaps();
      RoutePatternAction.rebuildWays();
    }
  }

  private Node getLastNode(Way way, String role)
  {
    if ((way == null) || (way.isIncomplete()) || (way.getNodesCount() < 1))
      return null;
    else
    {
      if ("backward".equals(role))
        return way.getNode(0);
      else
        return way.getNode(way.getNodesCount() - 1);
    }
  }

  private boolean gapNecessary(Way way, String role, Node lastNode)
  {
    if ((way != null) && (!(way.isIncomplete())) && (way.getNodesCount() >= 1))
    {
      Node firstNode = null;
      if ("backward".equals(role))
        firstNode = way.getNode(way.getNodesCount() - 1);
      else
        firstNode = way.getNode(0);
      if ((lastNode != null) && (!lastNode.equals(firstNode)))
        return true;
    }
    return false;
  }
};

package public_transport;

// import static org.openstreetmap.josm.tools.I18n.marktr;
// import static org.openstreetmap.josm.tools.I18n.tr;
//
// import java.awt.BorderLayout;
// import java.awt.Container;
// import java.awt.Dimension;
// import java.awt.Frame;
// import java.awt.GridBagConstraints;
// import java.awt.GridBagLayout;
// import java.awt.event.ActionEvent;
// import java.util.Collection;
// import java.util.Collections;
// import java.util.Iterator;
// import java.util.LinkedList;
// import java.util.List;
// import java.util.ListIterator;
// import java.util.Map;
// import java.util.TreeMap;
// import java.util.TreeSet;
import java.util.Vector;
//
// import javax.swing.DefaultCellEditor;
// import javax.swing.DefaultListModel;
// import javax.swing.JButton;
// import javax.swing.JCheckBox;
// import javax.swing.JComboBox;
// import javax.swing.JDialog;
// import javax.swing.JLabel;
// import javax.swing.JList;
// import javax.swing.JOptionPane;
// import javax.swing.JPanel;
// import javax.swing.JScrollPane;
// import javax.swing.JTabbedPane;
// import javax.swing.JTable;
// import javax.swing.JTextField;
// import javax.swing.ListSelectionModel;
// import javax.swing.event.ListSelectionEvent;
// import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
// import javax.swing.table.TableCellEditor;
//
// import org.openstreetmap.josm.Main;
// import org.openstreetmap.josm.actions.JosmAction;
// import org.openstreetmap.josm.actions.mapmode.DeleteAction;
// import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
// import org.openstreetmap.josm.data.osm.OsmPrimitive;
// import org.openstreetmap.josm.data.osm.Relation;
// import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
// import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
// import org.openstreetmap.josm.gui.ExtendedDialog;
// import org.openstreetmap.josm.tools.GBC;
// import org.openstreetmap.josm.tools.Shortcut;
// import org.openstreetmap.josm.tools.UrlLabel;

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
      buf[0] = "[incomplete]";
    else if (way.getNodesCount() < 1)
      buf[0] = "[empty way]";
    else if (curName != null)
      buf[0] = curName;
    else
      buf[0] = "[ID] " + (new Long(way.getId())).toString();
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
    buf[0] = "[gap]";
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

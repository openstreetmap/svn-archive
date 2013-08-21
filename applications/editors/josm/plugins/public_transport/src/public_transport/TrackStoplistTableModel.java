package public_transport;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import org.openstreetmap.josm.data.osm.Node;

public class TrackStoplistTableModel extends DefaultTableModel
{
  private Vector< Node > nodes = null;
  private Vector< String > times = null;
  private static Vector< String > columns = null;

  public TrackStoplistTableModel(TrackReference tr)
  {
    if (columns == null)
    {
      columns = new Vector< String >();
      columns.add(tr("Time"));
      columns.add(tr("Name"));
      columns.add(tr("Shelter"));
    }
    nodes = new Vector< Node >();
    times = new Vector< String >();

    setColumnIdentifiers(columns);
    addTableModelListener(tr);
  }

  public boolean isCellEditable(int row, int column) {
    return true;
  }

  public void addRow(Object[] obj) {
    throw new UnsupportedOperationException();
  }

  public void insertRow(int insPos, Object[] obj) {
    throw new UnsupportedOperationException();
  }

  public void addRow(String time) {
    insertRow(-1, time);
  }

  public void insertRow(int insPos, String time)
  {
    insertRow(insPos, null, time, "", new TransText(null));
  }

  public void removeRow(int pos)
  {
    super.removeRow(pos);
    nodes.removeElementAt(pos);
    times.removeElementAt(pos);
  }

  public Node nodeAt(int i)
  {
    return nodes.elementAt(i);
  }

  public void setNodeAt(int i, Node node)
  {
    nodes.set(i, node);
  }

  public final Vector< Node > getNodes()
  {
    return nodes;
  }

  public void setNodes(Vector< Node > nodes)
  {
    this.nodes = nodes;
  }

  public String timeAt(int i)
  {
    return times.elementAt(i);
  }

  public void setTimeAt(int i, String time)
  {
    times.set(i, time);
  }

  public final Vector< String > getTimes()
  {
    return times;
  }

  public void setTimes(Vector< String > times)
  {
    this.times = times;
  }

  public void insertRow
      (int insPos, Node node, String time, String name, TransText shelter)
  {
    Object[] buf = { time, name, shelter };
    if (insPos == -1)
    {
      nodes.addElement(node);
      times.addElement(time);
      super.addRow(buf);
    }
    else
    {
      nodes.insertElementAt(node, insPos);
      times.insertElementAt(time, insPos);
      super.insertRow(insPos, buf);
    }
  }

  public void clear()
  {
    nodes.clear();
    times.clear();
    super.setRowCount(0);
  }

  public void setDataVector(Vector< Vector< Object > > dataVector)
  {
    setDataVector(dataVector, columns);
  }
};

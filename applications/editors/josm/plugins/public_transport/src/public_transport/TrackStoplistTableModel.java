package public_transport;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Vector;
import javax.swing.table.DefaultTableModel;

import org.openstreetmap.josm.data.osm.Node;

public class TrackStoplistTableModel extends DefaultTableModel
{
  public Vector< Node > nodes = new Vector< Node >();
  public Vector< String > columns = null;
    
  public TrackStoplistTableModel(TrackReference tr)
  {
    if (columns == null)
    {
      columns = new Vector< String >();
      columns.add("Time");
      columns.add("Name");
    }
      
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
    insertRow(insPos, null, time, "");
  }
    
  public void insertRow(int insPos, Node node, String time, String name)
  {
    String[] buf = { "", "" };
    buf[0] = time;
    buf[1] = name;
    if (insPos == -1)
    {
      nodes.addElement(node);
      super.addRow(buf);
    }
    else
    {
      nodes.insertElementAt(node, insPos);
      super.insertRow(insPos, buf);
    }
  }
    
  public void clear()
  {
    nodes.clear();
    super.setRowCount(0);
  }
    
  public void setDataVector(Vector< Vector< Object > > dataVector)
  {
    setDataVector(dataVector, columns);
  }
};

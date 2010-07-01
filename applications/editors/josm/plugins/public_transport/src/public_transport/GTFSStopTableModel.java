package public_transport;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Iterator;
import java.util.Vector;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.data.osm.DataSource;
import org.openstreetmap.josm.data.osm.Node;

public class GTFSStopTableModel extends DefaultTableModel
      implements TableModelListener
{
  private GTFSImporterAction controller = null;
  public Vector< Node > nodes = new Vector< Node >();
  public Vector< LatLon > coors = new Vector< LatLon >();
  private int idCol = -1;
  private int nameCol = -1;
  private int latCol = -1;
  private int lonCol = -1;
  
  public GTFSStopTableModel(GTFSImporterAction controller,
			    String columnConfig)
  {
    int pos = columnConfig.indexOf(',');
    int oldPos = 0;
    int i = 0;
    while (pos > -1)
    {
      String title = columnConfig.substring(oldPos, pos);
      if ("stop_id".equals(title))
	idCol = i;
      else if ("stop_name".equals(title))
	nameCol = i;
      else if ("stop_lat".equals(title))
	latCol = i;
      else if ("stop_lon".equals(title))
	lonCol = i;
      ++i;
      oldPos = pos + 1;
      pos = columnConfig.indexOf(',', oldPos);
    }
    String title = columnConfig.substring(oldPos);
    if ("stop_id".equals(title))
      idCol = i;
    else if ("stop_name".equals(title))
      nameCol = i;
    else if ("stop_lat".equals(title))
      latCol = i;
    else if ("stop_lon".equals(title))
      lonCol = i;
    
    this.controller = controller;
    addColumn("Id");
    addColumn("Name");
    addColumn("State");
    addTableModelListener(this);
  }
    
  public boolean isCellEditable(int row, int column)
  {
    return false;
  }
    
  public void addRow(Object[] obj)
  {
    throw new UnsupportedOperationException();
  }
    
  public void insertRow(int insPos, Object[] obj)
  {
    throw new UnsupportedOperationException();
  }
    
  public void addRow(String s)
  {
    insertRow(-1, s, new Vector< Node >());
  }
  
  public void addRow(String s, Vector< Node > existingStops)
  {
    insertRow(-1, s, existingStops);
  }
  
  public void insertRow(int insPos, String s, Vector< Node > existingStops)
  {
    String[] buf = { "", "", "pending" };
    int pos = s.indexOf(',');
    int oldPos = 0;
    int i = 0;
    double lat = 0;
    double lon = 0;
    while (pos > -1)
    {
      if (i == idCol)
	buf[0] = s.substring(oldPos, pos);
      else if (i == nameCol)
	buf[1] = s.substring(oldPos, pos);
      else if (i == latCol)
	lat = Double.parseDouble(s.substring(oldPos, pos));
      else if (i == lonCol)
	lon = Double.parseDouble(s.substring(oldPos, pos));
      ++i;
      oldPos = pos + 1;
      pos = s.indexOf(',', oldPos);
    }
    if (i == idCol)
      buf[0] = s.substring(oldPos);
    else if (i == nameCol)
      buf[1] = s.substring(oldPos);
    else if (i == latCol)
      lat = Double.parseDouble(s.substring(oldPos));
    else if (i == lonCol)
      lon = Double.parseDouble(s.substring(oldPos));
    
    LatLon coor = new LatLon(lat, lon);
    
    if (Main.main.getCurrentDataSet() != null)
    {
      boolean inside = false;
      Iterator< DataSource > iter =
          Main.main.getCurrentDataSet().dataSources.iterator();
      while (iter.hasNext())
      {
	if (iter.next().bounds.contains(coor))
	{
	  inside = true;
	  break;
	}
      }
      if (!inside)
	buf[2] = "outside";
    }
    
    boolean nearBusStop = false;
    Iterator< Node > iter = existingStops.iterator();
    while (iter.hasNext())
    {
      Node node = iter.next();
      if (coor.greatCircleDistance(node.getCoor()) < 1000)
      {
	nearBusStop = true;
	break;
      }
    }
    
    if (insPos == -1)
    {
      if ((nearBusStop) || !("pending".equals(buf[2])))
	nodes.addElement(null);
      else
      {
	Node node = GTFSImporterAction.createNode(coor, buf[0], buf[1]);
	nodes.addElement(node);
	buf[2] = "added";
      }
      coors.addElement(coor);
      super.addRow(buf);
    }
    else
    {
      if ((nearBusStop) || !("pending".equals(buf[2])))
	nodes.insertElementAt(null, insPos);
      else
      {
	Node node = GTFSImporterAction.createNode(coor, buf[0], buf[1]);
	nodes.insertElementAt(node, insPos);
	buf[2] = "added";
      }
      coors.insertElementAt(coor, insPos);
      super.insertRow(insPos, buf);
    }
  }
    
  public void clear()
  {
    nodes.clear();
    super.setRowCount(0);
  }
  
  public void tableChanged(TableModelEvent e)
  {
  }
};

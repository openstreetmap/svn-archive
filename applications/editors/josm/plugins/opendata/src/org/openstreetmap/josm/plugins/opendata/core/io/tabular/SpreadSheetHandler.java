// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.tabular;

import java.util.Map;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.core.io.tabular.SpreadSheetReader.CoordinateColumns;

public interface SpreadSheetHandler {

	public void setSheetNumber(int n);
	
	public int getSheetNumber();

	public void setHandlesProjection(boolean handle);
	
	public boolean handlesProjection();

	public LatLon getCoor(EastNorth en, String[] fields);

	public void setLineNumber(int n);
	
	public int getLineNumber();

	public void setXCol(int i);

	public void setYCol(int i);
	
	public int getXCol();

	public int getYCol();

    public void nodesAdded(DataSet ds, Map<CoordinateColumns, Node> nodes, String[] fields, int lineNumber);
}

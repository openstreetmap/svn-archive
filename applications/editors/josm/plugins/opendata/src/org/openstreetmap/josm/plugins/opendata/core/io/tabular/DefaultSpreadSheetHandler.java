// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.tabular;

import java.util.Map;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.core.io.tabular.SpreadSheetReader.CoordinateColumns;

public class DefaultSpreadSheetHandler implements SpreadSheetHandler {

	private int sheetNumber = -1;
	private int lineNumber = -1;
	private boolean handlesProjection = false;
	
	private int xCol = -1;
	private int yCol = -1;
	
	@Override
	public int getSheetNumber() {
		return sheetNumber;
	}

	@Override
	public void setSheetNumber(int n) {
		sheetNumber = n;
	}
	
	@Override
	public boolean handlesProjection() {
		return handlesProjection;
	}

	@Override
	public void setHandlesProjection(boolean handle) {
		handlesProjection = handle;
	}

	@Override
	public LatLon getCoor(EastNorth en, String[] fields) {
		return null;
	}

	@Override
	public void setLineNumber(int n) {
		lineNumber = n;
	}

	@Override
	public int getLineNumber() {
		return lineNumber;
	}

	@Override
	public void setXCol(int i) {
		xCol = i;
	}

	@Override
	public void setYCol(int i) {
		yCol = i;
	}

	@Override
	public int getXCol() {
		return xCol;
	}

	@Override
	public int getYCol() {
		return yCol;
	}

    @Override
    public void nodesAdded(DataSet ds, Map<CoordinateColumns, Node> nodes, String[] header, int lineNumber) {
        // To be overriden if needed
    }
}

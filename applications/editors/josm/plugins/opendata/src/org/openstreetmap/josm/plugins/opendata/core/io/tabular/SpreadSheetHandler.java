// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.tabular;

import java.util.Map;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.core.io.tabular.SpreadSheetReader.CoordinateColumns;

public interface SpreadSheetHandler {

    void setSheetNumber(int n);

    int getSheetNumber();

    void setHandlesProjection(boolean handle);

    boolean handlesProjection();

    LatLon getCoor(EastNorth en, String[] fields);

    void setLineNumber(int n);

    int getLineNumber();

    void setXCol(int i);

    void setYCol(int i);

    int getXCol();

    int getYCol();

    void nodesAdded(DataSet ds, Map<CoordinateColumns, Node> nodes, String[] fields, int lineNumber);
}

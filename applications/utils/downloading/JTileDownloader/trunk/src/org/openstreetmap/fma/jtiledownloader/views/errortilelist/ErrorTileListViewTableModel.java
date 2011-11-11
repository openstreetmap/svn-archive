/*
 * Copyright 2008, Friedrich Maier
 * 
 * This file is part of JTileDownloader.
 * (see http://wiki.openstreetmap.org/index.php/JTileDownloader)
 *
 * JTileDownloader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JTileDownloader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy (see file COPYING.txt) of the GNU 
 * General Public License along with JTileDownloader.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.openstreetmap.fma.jtiledownloader.views.errortilelist;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

import org.openstreetmap.fma.jtiledownloader.datatypes.TileDownloadError;

public class ErrorTileListViewTableModel
    extends AbstractTableModel
{
    private static final long serialVersionUID = 1L;
    private ArrayList _data;

    /**
     * @param errorTileList
     */
    public ErrorTileListViewTableModel(ArrayList<TileDownloadError> errorTileList)
    {
        _data = new ArrayList();
        int count = 0;
        for (TileDownloadError tde : errorTileList)
        {
            ArrayList rowData = new ArrayList();
            rowData.add(Integer.toString(++count));
            rowData.add(tde.getTile());
            rowData.add(tde.getResult().getMessage());
            _data.add(rowData);
        }

    }

    /**
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount()
    {
        return 3;
    }

    /**
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount()
    {
        return _data == null ? 0 : _data.size();
    }

    /**
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int row, int column)
    {
        if (_data == null)
        {
            return null;
        }

        ArrayList rowData = (ArrayList) _data.get(row);
        
        return rowData == null ? null : rowData.get(column);
    }
}

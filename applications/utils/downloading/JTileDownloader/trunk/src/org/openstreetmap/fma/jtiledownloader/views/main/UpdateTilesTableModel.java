package org.openstreetmap.fma.jtiledownloader.views.main;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.openstreetmap.fma.jtiledownloader.datatypes.UpdateTileList;

/**
 * Copyright 2008, Friedrich Maier 
 * 
 * This file is part of JTileDownloader. 
 * (see http://wiki.openstreetmap.org/index.php/JTileDownloader)
 *
 *    JTileDownloader is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    JTileDownloader is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy (see file COPYING.txt) of the GNU 
 *    General Public License along with JTileDownloader.  
 *    If not, see <http://www.gnu.org/licenses/>.
 */
public class UpdateTilesTableModel
    extends AbstractTableModel
{
    private static final long serialVersionUID = 1L;
    private Vector _data;

    /**
     * @param errorTileList
     */
    public UpdateTilesTableModel(Vector updateTileList)
    {
        _data = new Vector();
        int count = 0;

        if (updateTileList == null || updateTileList.size() == 0)
        {
            return;
        }

        for (Enumeration enumeration = updateTileList.elements(); enumeration.hasMoreElements();)
        {
            UpdateTileList utl = (UpdateTileList) enumeration.nextElement();
            count++;
            Vector rowData = new Vector();
            rowData.add(utl.getZoomLevel());
            rowData.add("" + utl.getFileCount());
            _data.add(rowData);
        }
    }

    /**
     * @see javax.swing.table.TableModel#getColumnCount()
     * {@inheritDoc}
     */
    public int getColumnCount()
    {
        return 2;
    }

    /**
     * @see javax.swing.table.TableModel#getRowCount()
     * {@inheritDoc}
     */
    public int getRowCount()
    {
        if (_data == null)
        {
            return 0;
        }
        return _data.size();
    }

    /**
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     * {@inheritDoc}
     */
    public Object getValueAt(int row, int column)
    {
        if (_data == null)
        {
            return null;
        }

        Vector rowData = (Vector) _data.elementAt(row);
        if (rowData == null)
        {
            return null;
        }

        return rowData.elementAt(column);
    }
}

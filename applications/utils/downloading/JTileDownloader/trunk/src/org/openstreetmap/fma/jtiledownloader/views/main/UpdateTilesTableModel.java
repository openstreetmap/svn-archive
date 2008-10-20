package org.openstreetmap.fma.jtiledownloader.views.main;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.openstreetmap.fma.jtiledownloader.datatypes.UpdateTileList;

/**
 * developed by fma (http://wiki.openstreetmap.org/index.php/User:Fma)
 * license: http://creativecommons.org/licenses/by-nc-nd/3.0/
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

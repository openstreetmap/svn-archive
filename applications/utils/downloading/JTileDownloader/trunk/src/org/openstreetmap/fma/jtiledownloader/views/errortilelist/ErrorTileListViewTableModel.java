package org.openstreetmap.fma.jtiledownloader.views.errortilelist;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.openstreetmap.fma.jtiledownloader.datatypes.TileDownloadError;

/**
 * developed by fma (http://wiki.openstreetmap.org/index.php/User:Fma)
 * license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
public class ErrorTileListViewTableModel
    extends AbstractTableModel
{
    private static final long serialVersionUID = 1L;
    private Vector _data;

    /**
     * @param errorTileList
     */
    public ErrorTileListViewTableModel(Vector errorTileList)
    {
        _data = new Vector();
        int count = 0;
        for (Enumeration enumeration = errorTileList.elements(); enumeration.hasMoreElements();)
        {
            TileDownloadError tde = (TileDownloadError) enumeration.nextElement();
            count++;
            Vector rowData = new Vector();
            rowData.add("" + count);
            rowData.add(tde.getTile());
            rowData.add(tde.getResult().getMessage());
            _data.add(rowData);
        }

    }

    /**
     * @see javax.swing.table.TableModel#getColumnCount()
     * {@inheritDoc}
     */
    public int getColumnCount()
    {
        return 3;
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

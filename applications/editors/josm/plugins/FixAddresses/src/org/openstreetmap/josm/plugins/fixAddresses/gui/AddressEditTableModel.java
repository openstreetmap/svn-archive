// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer;
import org.openstreetmap.josm.plugins.fixAddresses.IAddressEditContainerListener;
import org.openstreetmap.josm.plugins.fixAddresses.IOSMEntity;

/**
 * Address edit table model.
 */
@SuppressWarnings("serial")
public abstract class AddressEditTableModel extends DefaultTableModel implements
        IAddressEditContainerListener {

    protected AddressEditContainer addressContainer;
    protected int sortCol = 0;
    protected boolean isSortAsc = true;

    public AddressEditTableModel(AddressEditContainer addressContainer) {
        super();
        this.addressContainer = addressContainer;
        addressContainer.addChangedListener(this);
    }

    @Override
    public void containerChanged(AddressEditContainer container) {
        if (SwingUtilities.isEventDispatchThread()) {
            fireTableDataChanged(); // update model
        } else {
            SwingUtilities.invokeLater(() -> fireTableDataChanged());
        }
    }

    @Override
    public void entityChanged(IOSMEntity entity) {
        int row = getRowOfEntity(entity);
        if (row != -1) { // valid row? -> update model
            fireTableRowsUpdated(row, row);
        } // else we don't do anything
    }

    /**
     * Gets the node entity for the given row or null; if row contains no entity.
     *
     * @param row
     *            The row to get the entity object for.
     * @return the node entity for the given row or null; if row contains no entity
     */
    public abstract IOSMEntity getEntityOfRow(int row);

    /**
     * Gets the row for the given node entity or -1; if the model does not contain the entity.
     *
     * @param entity
     *            The entity to get the row for.
     * @return the row for the given node entity or -1; if the model does not contain the entity
     */
    public abstract int getRowOfEntity(IOSMEntity entity);

    /**
     * Sorts the model data by the given column.
     *
     * @param column
     *            the column
     * @param ascending
     *            the ascending
     */
    protected abstract void sortByColumn(int column, boolean ascending);


    /**
     * The listener interface for receiving column events.
     * The class that is interested in processing a column
     * event implements this interface, and the object created
     * with that class is registered with a component using the
     * component's {@code addColumnListener} method. When
     * the column event occurs, that object's appropriate
     * method is invoked.
     */
    class ColumnListener extends MouseAdapter {
        protected JTable table;

        /**
         * Instantiates a new column listener.
         *
         * @param t the t
         */
        ColumnListener(JTable t) {
            table = t;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            TableColumnModel colModel = table.getColumnModel();
            int columnModelIndex = colModel.getColumnIndexAtX(e.getX());
            int modelIndex = colModel.getColumn(columnModelIndex)
                    .getModelIndex();

            if (modelIndex < 0) {
                return;
            }
            // Same column? If yes, flip order
            if (sortCol == modelIndex) {
                isSortAsc = !isSortAsc;
            } else {
                sortCol = modelIndex;
            }

            for (int i = 0; i < colModel.getColumnCount(); i++) {
                TableColumn column = colModel.getColumn(i);
                column.setHeaderValue(getColumnName(column.getModelIndex()));
            }
            table.getTableHeader().repaint();

            //Collections.sort(addressContainer, new MyComparator(isSortAsc));

            sortByColumn(sortCol, isSortAsc);
            table.tableChanged(new TableModelEvent(AddressEditTableModel.this));
            table.repaint();
        }
    }

    /**
     * Internal base class to sort items by different columns.
     * @param <E> item type
     */
    protected abstract static class ColumnSorter<E> implements Comparator<E> {
        private int column;
        private boolean ascending;

        /**
         * Instantiates a new address sorter.
         *
         * @param column the column to sort by
         * @param ascending if sort mode is ascending or not
         */
        public ColumnSorter(int column, boolean ascending) {
            super();
            this.column = column;
            this.ascending = ascending;
        }

        /**
         * Gets the index of the column to sort.
         *
         * @return the column
         */
        protected int getColumn() {
            return column;
        }

        /**
         * Checks if sort mode is ascending or not.
         *
         * @return true, if is ascending
         */
        protected boolean isAscending() {
            return ascending;
        }

        @Override
        public abstract int compare(E o1, E o2);
    }
}

// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses.gui;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trc;

import java.util.Collections;

import org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer;
import org.openstreetmap.josm.plugins.fixAddresses.IOSMEntity;
import org.openstreetmap.josm.plugins.fixAddresses.OSMAddress;

public class IncompleteAddressesTableModel extends AddressEditTableModel  {
    /**
     *
     */
    private static final long serialVersionUID = -5951629033395186324L;

    // TODO: Add "state" column, if required
    private static final int NUMBER_OF_COLUMNS = 5;
    private static final String[] COLUMN_NAMES = new String[]{tr("Country"), trc("address", "City" /* fix #8140 */), tr("Postcode"), tr("Street"), tr("Number")};
    private static final Class<?>[] COLUMN_CLASSES = new Class<?>[]{
        String.class, String.class, String.class, String.class, String.class, String.class};


    /**
     * Instantiates a new incomplete addresses table model.
     *
     * @param addressContainer the address container used for display
     */
    public IncompleteAddressesTableModel(AddressEditContainer addressContainer) {
        super(addressContainer);
    }

    @Override
    public int getColumnCount() {
        return NUMBER_OF_COLUMNS;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public int getRowCount() {
        if (addressContainer == null || addressContainer.getIncompleteAddresses() == null) {
            return 0;
        }
        return addressContainer.getNumberOfIncompleteAddresses();
    }

    @Override
    public Object getValueAt(int row, int column) {
        OSMAddress aNode = (OSMAddress) getEntityOfRow(row);

        if (aNode == null) {
            return null;
        }

        switch (column) {
        case 0:
            return aNode.getCountry();
        case 1:
            return aNode.getCity();
        case 2:
            return aNode.getPostalCode();
        case 3:
            return aNode.getStreetName();
        case 4:
            return aNode.getHouseNumber();
        default:
            throw new RuntimeException("Invalid column index: " + column);
        }

    }

    @Override
    public Class<?> getColumnClass(int arg0) {
        return COLUMN_CLASSES[arg0];
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    @Override
    public IOSMEntity getEntityOfRow(int row) {
        if (addressContainer == null || addressContainer.getIncompleteAddresses() == null) {
            return null;
        }
        if (row < 0 || row >= addressContainer.getNumberOfIncompleteAddresses()) {
            return null;
        }
        return addressContainer.getIncompleteAddresses().get(row);
    }

    @Override
    public int getRowOfEntity(IOSMEntity entity) {
        if (addressContainer == null || addressContainer.getIncompleteAddresses() == null) {
            return -1;
        }

        return addressContainer.getIncompleteAddresses().indexOf(entity);
    }

    @Override
    protected void sortByColumn(int column, boolean ascending) {
        if (addressContainer.getNumberOfIncompleteAddresses() == 0) return;

        Collections.sort(addressContainer.getIncompleteAddresses(),
                new IncompleteAddressModelSorter(column, ascending));
    }

    /**
     * Internal class StreetModelSorter.
     */
    class IncompleteAddressModelSorter extends ColumnSorter<OSMAddress> {

        /**
         * Instantiates a new incomplete address model sorter.
         *
         * @param column the column to sort
         * @param asc sort ascending
         */
        public IncompleteAddressModelSorter(int column, boolean asc) {
            super(column, asc);
        }

        @Override
        public int compare(OSMAddress arg0, OSMAddress arg1) {
            int cc = 0;

            switch (getColumn()) {
            case 0:
                cc=arg0.getCountry().compareTo(arg1.getCountry());
                break;
            case 1:
                cc=arg0.getCity().compareTo(arg1.getCity());
                break;
            case 2:
                cc=arg0.getPostalCode().compareTo(arg1.getPostalCode());
                break;
            case 3:
                cc= arg0.getStreetName().compareTo(arg1.getStreetName());
                break;
            case 4:
                cc=arg0.getHouseNumber().compareTo(arg1.getHouseNumber());
                break;
            default:
                throw new RuntimeException("Invalid column index: " + getColumn());
            }

            if (!isAscending()) {
                cc = -cc;
            }

            return cc;
        }
    }


}

// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses.gui;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trc;

import java.util.Collections;

import org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer;
import org.openstreetmap.josm.plugins.fixAddresses.IOSMEntity;
import org.openstreetmap.josm.plugins.fixAddresses.OSMAddress;

/**
 * Provides a table model to show unresolved addresses.
 *
 * @author Oliver Wieland <oliver.wieland@online.de>
 *
 */

public class UnresolvedAddressesTableModel extends AddressEditTableModel {

    private static final int NUMBER_OF_COLUMNS = 5;
    private static final String[] COLUMN_NAMES = new String[] { tr("Street"),
            tr("Number"), trc("address", "City" /* fix #8140 */), tr("Postcode"), tr("Name") };

    private static final Class<?>[] COLUMN_CLASSES = new Class<?>[] {
            String.class, String.class, String.class, String.class,
            String.class };

    /**
     *
     */
    private static final long serialVersionUID = 424009321818130586L;

    /**
     * @param addressContainer
     */
    public UnresolvedAddressesTableModel(AddressEditContainer addressContainer) {
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
        if (addressContainer == null
                || addressContainer.getUnresolvedAddresses() == null) {
            return 0;
        }
        return addressContainer.getNumberOfUnresolvedAddresses();
    }

    @Override
    public Object getValueAt(int row, int column) {
        OSMAddress aNode = (OSMAddress) getEntityOfRow(row);

        if (aNode == null) {
            return null;
        }

        switch (column) {
        case 0:
            return aNode.getStreetName();
        case 1:
            return aNode.getHouseNumber();
        case 2:
            return aNode.getCity();
        case 3:
            return aNode.getPostalCode();
        case 4:
            return aNode.getName();
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
        if (addressContainer == null
                || addressContainer.getUnresolvedAddresses() == null) {
            return null;
        }
        if (row < 0 || row >= addressContainer.getNumberOfUnresolvedAddresses()) {
            return null;
        }
        return addressContainer.getUnresolvedAddresses().get(row);
    }

    @Override
    public int getRowOfEntity(IOSMEntity entity) {
        if (addressContainer == null
                || addressContainer.getUnresolvedAddresses() == null) {
            return -1;
        }

        return addressContainer.getUnresolvedAddresses().indexOf(entity);
    }

    @Override
    protected void sortByColumn(int column, boolean ascending) {
        if (addressContainer.getNumberOfUnresolvedAddresses() == 0)
            return;

        Collections.sort(addressContainer.getUnresolvedAddresses(),
                new UnresolvedAddressModelSorter(column, ascending));
    }

    /**
     * Internal class StreetModelSorter.
     */
    class UnresolvedAddressModelSorter extends ColumnSorter<OSMAddress> {

        public UnresolvedAddressModelSorter(int column, boolean asc) {
            super(column, asc);
        }

        @Override
        public int compare(OSMAddress arg0, OSMAddress arg1) {
            int cc = 0;
            switch (getColumn()) {
            case 0:
                cc = arg0.getStreetName().compareTo(arg1.getStreetName());
                break;
            case 1:
                cc = arg0.getHouseNumber().compareTo(arg1.getHouseNumber());
                break;
            case 2:
                cc = arg0.getCity().compareTo(arg1.getCity());
                break;
            case 3:
                cc = arg0.getPostalCode().compareTo(arg1.getPostalCode());
                break;
            case 4:
                cc = arg0.getName().compareTo(arg1.getName());
                break;
            default:
                throw new RuntimeException("Invalid column index: "
                        + getColumn());
            }

            if (!isAscending()) {
                cc = -cc;
            }

            return cc;
        }
    }
}

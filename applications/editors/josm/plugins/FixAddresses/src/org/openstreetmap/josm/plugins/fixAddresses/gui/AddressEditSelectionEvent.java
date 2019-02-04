// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses.gui;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;

import org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer;
import org.openstreetmap.josm.plugins.fixAddresses.OSMAddress;
import org.openstreetmap.josm.plugins.fixAddresses.OSMStreet;

/**
 * Address Edit selection event
 */
public class AddressEditSelectionEvent extends ActionEvent {

    private static final long serialVersionUID = -93034483427803409L;
    private JTable streetTable;
    private JTable unresolvedAddressTable;
    private JTable incompleteAddressTable;
    private AddressEditContainer addressContainer;

    private List<OSMAddress> unresolvedCache;
    private List<OSMAddress> incompleteCache;

    /**
     * Creates a new 'AddressEditSelectionEvent'.
     * @param source The event source.
     * @param selStreet The street table component.
     * @param unresolvedAddresses The unresolved addresses table component.
     * @param incompleteAddresses The incomplete addresses table component.
     * @param container The address container instance holding the entities for streets and addresses.
     */
    public AddressEditSelectionEvent(Object source, JTable selStreet, JTable unresolvedAddresses, JTable incompleteAddresses,
            AddressEditContainer container) {
        super(source, -1, "");
        this.streetTable = selStreet;
        this.unresolvedAddressTable = unresolvedAddresses;
        this.incompleteAddressTable = incompleteAddresses;
        this.addressContainer = container;
    }

    /**
     * Gets the street table component.
     * @return the street table component
     */
    public JTable getStreetTable() {
        return streetTable;
    }

    /**
     * Gets the 'unresolved addresses' table component.
     * @return the 'unresolved addresses' table component
     */
    public JTable getUnresolvedAddressTable() {
        return unresolvedAddressTable;
    }

    /**
     * @return the incompleteAddressTable
     */
    protected JTable getIncompleteAddressTable() {
        return incompleteAddressTable;
    }

    /**
     * Gets the address container.
     *
     * @return the address container
     */
    public AddressEditContainer getAddressContainer() {
        return addressContainer;
    }

    /**
     * Gets the selected street of the street table.
     * @return the selected street of the street table
     */
    public OSMStreet getSelectedStreet() {
        if (streetTable != null && addressContainer != null && addressContainer.getStreetList() != null) {
            int selRows = streetTable.getSelectedRow();

            if (selRows < 0 || selRows >= addressContainer.getNumberOfStreets()) {
                return null;
            }

            return addressContainer.getStreetList().get(selRows);
        }
        return null;
    }

    /**
     * Checks for addresses.
     *
     * @return true, if successful
     */
    public boolean hasAddresses() {
        return hasIncompleteAddresses() || hasUnresolvedAddresses();
    }

    /**
     * Checks for incomplete addresses.
     *
     * @return true, if successful
     */
    public boolean hasIncompleteAddresses() {
        return getSelectedIncompleteAddresses() != null;
    }

    /**
     * Checks for unresolved addresses.
     *
     * @return true, if successful
     */
    public boolean hasUnresolvedAddresses() {
        return getSelectedUnresolvedAddresses() != null;
    }

    /**
     * Checks for addresses with guesses.
     *
     * @return true, if successful
     */
    public boolean hasAddressesWithGuesses() {
        if (hasIncompleteAddresses()) {
            for (OSMAddress addr : getSelectedIncompleteAddresses()) {
                if (addr.hasGuesses()) {
                    return true;
                }
            }
        }

        if (hasUnresolvedAddresses()) {
            for (OSMAddress addr : getSelectedUnresolvedAddresses()) {
                if (addr.hasGuesses()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Gets the list containing the selected items of the 'unresolved addresses ' table.
     * @return the list containing the selected items of the 'unresolved addresses ' table
     */
    public List<OSMAddress> getSelectedUnresolvedAddresses() {
        if (unresolvedAddressTable != null &&
                addressContainer != null &&
                unresolvedCache == null) {

            int[] selRows = unresolvedAddressTable.getSelectedRows();

            unresolvedCache = new ArrayList<>();
            for (int i = 0; i < selRows.length; i++) {
                if (selRows[i] >= 0 && selRows[i] < addressContainer.getNumberOfUnresolvedAddresses()) {
                    unresolvedCache.add(addressContainer.getUnresolvedAddresses().get(selRows[i]));
                }
            }
            return unresolvedCache;
        } else {
            return unresolvedCache;
        }
    }

    /**
     * Gets the selected incomplete addresses.
     *
     * @return the selected incomplete addresses
     */
    public List<OSMAddress> getSelectedIncompleteAddresses() {
        if (incompleteAddressTable != null &&
                addressContainer != null &&
                incompleteCache == null) {

            int[] selRows = incompleteAddressTable.getSelectedRows();

            incompleteCache = new ArrayList<>();
            for (int i = 0; i < selRows.length; i++) {
                if (selRows[i] >= 0 && selRows[i] < addressContainer.getNumberOfIncompleteAddresses()) {
                    incompleteCache.add(addressContainer.getIncompleteAddresses().get(selRows[i]));
                }
            }
            return incompleteCache;
        } else {
            return incompleteCache; // equals null, if no data is present
        }
    }
}

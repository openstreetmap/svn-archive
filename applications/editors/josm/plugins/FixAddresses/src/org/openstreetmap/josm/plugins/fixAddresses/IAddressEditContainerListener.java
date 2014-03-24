// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses;

public interface IAddressEditContainerListener {
    /**
     * Notifies clients that the container has been changed.
     * @param container
     */
    public void containerChanged(AddressEditContainer container);

    /**
     * Notifies clients that an entity has been changed.
     */
    public void entityChanged(IOSMEntity node);
}

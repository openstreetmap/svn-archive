// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses;

/**
 * Address edit container listener.
 */
public interface IAddressEditContainerListener {
    /**
     * Notifies clients that the container has been changed.
     * @param container container
     */
    void containerChanged(AddressEditContainer container);

    /**
     * Notifies clients that an entity has been changed.
     * @param node OSM entity
     */
    void entityChanged(IOSMEntity node);
}

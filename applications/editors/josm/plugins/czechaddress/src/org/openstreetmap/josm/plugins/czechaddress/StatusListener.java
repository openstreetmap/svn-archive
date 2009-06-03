package org.openstreetmap.josm.plugins.czechaddress;

import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.AddressElement;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.Database;

/**
 * Listener capable of sensing plugin status.
 *
 * <p>There are several types of events in the plugin, which can happen during
 * its lifetime. This interface is intended for classes that want to be
 * aware of such changes.</p>
 *
 * <p>Currently there is one single class capable of broadcasting messages to
 * {@link StatusListener}s, which is the {@link CzechAddressPlugin}. When anyone
 * wants to notify all listeners about a change in the plugin's status, the
 * {@link CzechAddressPlugin}{@code .broadcastStatusChanged()} method.</p>
 *
 * <p>For registering to the global broadcasting system, use
 * {@link CzechAddressPlugin}{@code .addStatusListener()} or
 * {@code removeStatusListener()}.
 *
 * @see CzechAddressPlugin
 * 
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 */
public interface StatusListener {

    /**
     * The user's choice of location in the database has changed.
     *
     * <p>The new location can be obtained by
     * {@link CzechAddressPlugin}{@code .getLocation()}.</p>
     *
     * @see AddressElement
     */
    static final int MESSAGE_LOCATION_CHANGED  = 1;

    /**
     * All parsers have finished their parsing.
     *
     * <p>The current database can be obtained by
     * {@link CzechAddressPlugin}{@code .getDatabase()}.</p>
     *
     * @see CzechAddressPlugin
     * @see Database
     */
    static final int MESSAGE_DATABASE_LOADED   = 2;

    /**
     * Called when status of the plugin has changed.
     *
     * <p>This method is called whenever someone calls
     * {@link CzechAddressPlugin}{@code .broadcastStatusChanged()}.</p>
     *
     * <p>If you are implementing this interface and this method is not
     * being called, make sure you registered to the messaging system by
     * {@link CzechAddressPlugin}{@code .addStatusListener()}.</p>
     *
     * <p>Parameter {@code message} takes values from {@code MESSAGE_*} fields
     * of this class.
     *
     * @param message the unique ID of the message
     */
    void pluginStatusChanged(int message);
}

// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.eventbus;

import java.util.logging.Level;

import org.openstreetmap.josm.eventbus.EventBus.DirectExecutor;
import org.openstreetmap.josm.tools.Logging;

/**
 * The unique JOSM event bus.
 */
public class JosmEventBus {

    private static final EventBus BUS = new EventBus("JOSM", DirectExecutor.INSTANCE, Dispatcher.perThreadDispatchQueue(),
            (exception, context) -> Logging.logWithStackTrace(Level.SEVERE, exception, "Event bus error in {0}:", context));

    private JosmEventBus() {
        // Hide default constructor
    }

    /**
     * Returns the unique JOSM event bus.
     * @return the unique JOSM event bus
     */
    public static EventBus getBus() {
        return BUS;
    }
    
    /**
     * Registers all subscriber methods on {@code object} to receive events.
     *
     * @param object object whose subscriber methods should be registered.
     * @see EventBus#register
     */
    public static void register(Object object) {
        BUS.register(object);
    }

    /**
     * Unregisters all subscriber methods on a registered {@code object}.
     *
     * @param object object whose subscriber methods should be unregistered.
     * @throws IllegalArgumentException if the object was not previously registered.
     * @see EventBus#unregister
     */
    public static void unregister(Object object) {
        BUS.unregister(object);
    }

    /**
     * Posts an event to all registered subscribers. This method will return successfully after the
     * event has been posted to all subscribers, and regardless of any exceptions thrown by
     * subscribers.
     *
     * <p>If no subscribers have been subscribed for {@code event}'s class, and {@code event} is not
     * already a {@link DeadEvent}, it will be wrapped in a DeadEvent and reposted.
     *
     * @param event event to post.
     * @see EventBus#post
     */
    public static void post(Object event) {
        BUS.post(event);
    }
}

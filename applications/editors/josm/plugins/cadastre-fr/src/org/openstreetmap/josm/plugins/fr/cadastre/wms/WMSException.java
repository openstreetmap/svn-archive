// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.wms;

/**
 * Exception thrown in case of WMS error.
 */
public class WMSException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code WMSException} with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param  message the detail message. The detail message is saved for
     *         later retrieval by the {@link #getMessage()} method.
     */
    public WMSException(String message) {
        super(message);
    }
}

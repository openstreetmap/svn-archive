// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.terracer;

/**
 * The Class TerracerRuntimeException indicates errors from the Terracer Plugin.
 *
 * @author casualwalker - Copyright 2009 CloudMade Ltd
 */
public class TerracerRuntimeException extends RuntimeException {

    /**
     * Default constructor.
     */
    public TerracerRuntimeException() {
        super();
    }

    public TerracerRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public TerracerRuntimeException(String message) {
        super(message);
    }

    public TerracerRuntimeException(Throwable cause) {
        super(cause);
    }

}

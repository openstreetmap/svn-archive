// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

public class GeoCrsException extends Exception {

    public GeoCrsException() {
        super();
    }

    public GeoCrsException(String message, Throwable cause) {
        super(message, cause);
    }

    public GeoCrsException(String message) {
        super(message);
    }

    public GeoCrsException(Throwable cause) {
        super(cause);
    }
}

// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.modules;

public class ModuleListParseException extends Exception {
    public ModuleListParseException() {
        super();
    }

    public ModuleListParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModuleListParseException(String message) { // NO_UCD
        super(message);
    }

    public ModuleListParseException(Throwable cause) {
        super(cause);
    }
}

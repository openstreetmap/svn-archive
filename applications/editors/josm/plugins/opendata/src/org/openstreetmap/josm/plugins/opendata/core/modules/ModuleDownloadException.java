// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.modules;

public class ModuleDownloadException extends Exception {

    public ModuleDownloadException() {
        super();
    }

    public ModuleDownloadException(String message, Throwable cause) { // NO_UCD
        super(message, cause);
    }

    public ModuleDownloadException(String message) {
        super(message);
    }

    public ModuleDownloadException(Throwable cause) {
        super(cause);
    }
}

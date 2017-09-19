// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.wms;

public class WMSException extends Exception {
    private String message;
    private static final long serialVersionUID = 1L;

    public WMSException(String message) {
        super();
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

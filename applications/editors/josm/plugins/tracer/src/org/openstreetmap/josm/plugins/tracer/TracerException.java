/**
 * Tracer - plugin for JOSM
 * Jan Bilak
 * This program is free software and licensed under GPL.
 */

package org.openstreetmap.josm.plugins.tracer;

import static org.openstreetmap.josm.tools.I18n.tr;

class TracerException extends Exception {

    public TracerException() {
        super(tr("An unknown error has occurred"));
    }

    public TracerException(String err) {
        super(err);
    }
}

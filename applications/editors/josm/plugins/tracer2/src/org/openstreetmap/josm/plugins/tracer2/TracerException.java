// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.tracer2;

import static org.openstreetmap.josm.tools.I18n.tr;

class TracerException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 4404064875119981715L;

    TracerException() {
        super(tr("An unknown error has occurred"));
    }

    TracerException(String err) {
        super(err);
    }
}

// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.lakewalker;

import static org.openstreetmap.josm.tools.I18n.tr;

class LakewalkerException extends Exception {
    LakewalkerException() {
        super(tr("An unknown error has occurred"));
    }

    LakewalkerException(String err) {
        super(err);
    }
}

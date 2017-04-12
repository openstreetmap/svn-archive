// License: GPL. For details, see LICENSE file.
package org.insignificant.josm.plugins.imagewaypoint.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import org.insignificant.josm.plugins.imagewaypoint.ImageEntries;
import org.openstreetmap.josm.actions.JosmAction;

public final class NextAction extends JosmAction {

    public NextAction() {
        super(tr("Next"), null, tr("Next image"), null, false);
    }

    @Override
    public void actionPerformed(final ActionEvent actionEvent) {
        if (ImageEntries.getInstance().hasNext()) {
            ImageEntries.getInstance().next();
        }
    }
}

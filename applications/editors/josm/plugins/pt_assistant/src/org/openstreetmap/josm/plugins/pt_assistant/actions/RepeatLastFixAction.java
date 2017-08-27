// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.pt_assistant.PTAssistantPlugin;
import org.openstreetmap.josm.plugins.pt_assistant.validation.SegmentChecker;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Carries out the changes after the Repeat last fix button has been pressed
 *
 * @author darya
 *
 */
public class RepeatLastFixAction extends JosmAction {

    private static final long serialVersionUID = 2681464946469047054L;

    /**
     * Default constructor
     */
    public RepeatLastFixAction() {
        super(tr("Repeat last fix"), new ImageProvider("presets/transport", "bus.svg"), tr("Repeat last fix"),
                null, false, "repeatLastFix", false);
    }

    /**
     * Applies the fixes, resets the last fix attribute
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        if (!isEnabled() || !MainApplication.isDisplayingMapView()) {
            return;
        }

        SegmentChecker.carryOutRepeatLastFix(PTAssistantPlugin.getLastFix());

        PTAssistantPlugin.setLastFix(null);

    }

}

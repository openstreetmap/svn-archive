// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.pt_assistant.PTAssistantPlugin;
import org.openstreetmap.josm.plugins.pt_assistant.validation.SegmentChecker;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

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
                Shortcut.registerShortcut("Repeat last fix", tr("Repeat last fix"), KeyEvent.VK_E, Shortcut.NONE),
                false, "repeatLastFix", false);

    }

    /**
     * Applies the fixes, resets the last fix attribute
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        if (!isEnabled() || !Main.isDisplayingMapView()) {
            return;
        }

        SegmentChecker.carryOutRepeatLastFix(PTAssistantPlugin.getLastFix());

        PTAssistantPlugin.setLastFix(null);

    }

}

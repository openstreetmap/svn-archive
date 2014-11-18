// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.preferences.PreferenceDialog;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.gui.OdPreferenceSetting;
import org.openstreetmap.josm.tools.Shortcut;

public class OpenPreferencesActions extends JosmAction {
    
    public OpenPreferencesActions() {
        super(tr("OpenData preferences"), OdConstants.ICON_CORE_24, null, 
                Shortcut.registerShortcut("opendata_open_preferences", tr("OpenData preferences"),
                KeyEvent.CHAR_UNDEFINED, Shortcut.NONE), true, false);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        final PreferenceDialog p = new PreferenceDialog(Main.parent);
        p.selectPreferencesTabByClass(OdPreferenceSetting.class);
        p.setVisible(true);
    }
}

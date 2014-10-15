// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.preferences.PreferenceDialog;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.gui.OdPreferenceSetting;
import org.openstreetmap.josm.tools.ImageProvider;

public class OpenPreferencesActions extends JosmAction {
    
    public OpenPreferencesActions() {
        super(false);
        putValue(NAME, tr("OpenData preferences"));
        putValue(SMALL_ICON, ImageProvider.get("dialogs", OdConstants.ICON_CORE_24));
        putValue("toolbar", "opendata_open_preferences");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        final PreferenceDialog p = new PreferenceDialog(Main.parent);
        p.selectPreferencesTabByClass(OdPreferenceSetting.class);
        p.setVisible(true);
    }
}

// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.actions;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.preferences.PreferenceDialog;
import org.openstreetmap.josm.plugins.fr.cadastre.preferences.CadastrePreferenceSetting;

public class MenuActionOpenPreferences extends JosmAction {
    private static final long serialVersionUID = 1L;

    public static final String NAME = marktr("Preferences");

    /**
     * Constructs a new {@code MenuActionOpenPreferences}.
     */
    public MenuActionOpenPreferences() {
        super(tr(NAME), "cadastre_small", tr("Open Cadastre Preferences"), null, false, "cadastrefr/openpreferences", true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        PreferenceDialog p = new PreferenceDialog(MainApplication.getMainFrame());
        p.selectPreferencesTabByClass(CadastrePreferenceSetting.class);
        p.setVisible(true);
    }
}

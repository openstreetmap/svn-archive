// License: GPL. For details, see LICENSE file.
package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.preferences.PreferenceDialog;

public class MenuActionOpenPreferences extends JosmAction {
    private static final long serialVersionUID = 1L;

    public static String name = marktr("Preferences");

    public MenuActionOpenPreferences() {
        super(tr(name), "cadastre_small", tr("Open Cadastre Preferences"), null, false, "cadastrefr/openpreferences", true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        PreferenceDialog p = new PreferenceDialog(Main.parent);
        p.selectPreferencesTabByClass(CadastrePreferenceSetting.class);
        p.setVisible(true);
    }
}

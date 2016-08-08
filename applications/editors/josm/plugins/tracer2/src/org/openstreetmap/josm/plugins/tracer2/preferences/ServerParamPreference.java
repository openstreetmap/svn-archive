// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.tracer2.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagConstraints;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.plugins.tracer2.TracerPlugin;
import org.openstreetmap.josm.tools.GBC;

public class ServerParamPreference extends DefaultTabPreferenceSetting {

    TracerPlugin m_oPlugin;

    public ServerParamPreference(TracerPlugin plugin) {
        super("tracer2", tr("Tracer2") + " - " + tr("Preferences"), tr("Modify list of parameter for server request."));

        m_oPlugin = plugin;
    }

    @Override
    public void addGui(PreferenceTabbedPane gui) {
        JPanel p = gui.createPreferenceTab(this);
        ServerParamPanel spp = new ServerParamPanel(m_oPlugin.m_oParamList);
        spp.refresh();
        JScrollPane sp = new JScrollPane(spp);
        p.add(sp, GBC.eol().fill(GridBagConstraints.BOTH));
    }

    @Override
    public boolean ok() {
        m_oPlugin.m_oParamList.save();
        return false;
    }

}

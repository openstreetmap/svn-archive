// License: GPL. For details, see LICENSE file.
package org.wikipedia.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceSettingFactory;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.gui.preferences.SubPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.TabPreferenceSetting;
import org.openstreetmap.josm.gui.widgets.HistoryComboBox;
import org.openstreetmap.josm.tools.GBC;
import org.wikipedia.io.WikosmDownloadReader;

/**
 * Preferences related to Wikosm API servers.
 */
public class WikosmServerPreference implements SubPreferenceSetting {

    private final HistoryComboBox wikosmServer = new HistoryComboBox();

    /**
     * Factory used to create a new {@link WikosmServerPreference}.
     */
    public static class Factory implements PreferenceSettingFactory {
        @Override
        public PreferenceSetting createPreferenceSetting() {
            return new WikosmServerPreference();
        }
    }

    @Override
    public TabPreferenceSetting getTabPreferenceSetting(PreferenceTabbedPane gui) {
        return gui.getServerPreference();
    }

    @Override
    public void addGui(PreferenceTabbedPane gui) {
        final JPanel panel = new JPanel(new GridBagLayout());

        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(new JLabel(tr("Server: ")), GBC.std().insets(5, 5, 5, 5));
        panel.add(wikosmServer, GBC.eop().fill(GBC.HORIZONTAL));
        wikosmServer.setPossibleItems(WikosmDownloadReader.WIKOSM_SERVER_HISTORY.get());
        wikosmServer.setText(WikosmDownloadReader.WIKOSM_SERVER.get());

        panel.add(Box.createVerticalGlue(), GBC.eol().fill());

        getTabPreferenceSetting(gui).addSubTab(this, tr("Wikidata+OSM server"), panel);
    }

    @Override
    public boolean ok() {
        WikosmDownloadReader.WIKOSM_SERVER.put(wikosmServer.getText());
        WikosmDownloadReader.WIKOSM_SERVER_HISTORY.put(wikosmServer.getHistory());
        return false;
    }

    @Override
    public boolean isExpert() {
        return true;
    }
}

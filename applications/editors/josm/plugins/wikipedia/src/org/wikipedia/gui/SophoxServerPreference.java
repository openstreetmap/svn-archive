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
import org.wikipedia.io.SophoxDownloadReader;

/**
 * Preferences related to Sophox API servers.
 */
public class SophoxServerPreference implements SubPreferenceSetting {

    private final HistoryComboBox SophoxServer = new HistoryComboBox();

    /**
     * Factory used to create a new {@link SophoxServerPreference}.
     */
    public static class Factory implements PreferenceSettingFactory {
        @Override
        public PreferenceSetting createPreferenceSetting() {
            return new SophoxServerPreference();
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
        panel.add(SophoxServer, GBC.eop().fill(GBC.HORIZONTAL));
        SophoxServer.setPossibleItems(SophoxDownloadReader.SOPHOX_SERVER_HISTORY.get());
        SophoxServer.setText(SophoxDownloadReader.SOPHOX_SERVER.get());

        panel.add(Box.createVerticalGlue(), GBC.eol().fill());

        getTabPreferenceSetting(gui).addSubTab(this, tr("Wikidata+OSM server"), panel);
    }

    @Override
    public boolean ok() {
        SophoxDownloadReader.SOPHOX_SERVER.put(SophoxServer.getText());
        SophoxDownloadReader.SOPHOX_SERVER_HISTORY.put(SophoxServer.getHistory());
        return false;
    }

    @Override
    public boolean isExpert() {
        return true;
    }
}

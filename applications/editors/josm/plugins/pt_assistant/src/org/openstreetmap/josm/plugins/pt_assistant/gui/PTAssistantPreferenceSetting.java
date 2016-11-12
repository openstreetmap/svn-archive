// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.gui;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.gui.preferences.SubPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.TabPreferenceSetting;
import org.openstreetmap.josm.tools.I18n;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Displays the settings of the pt_assistant plugin under Preferences
 * @author darya
 *
 */
public class PTAssistantPreferenceSetting implements SubPreferenceSetting {

    private final JCheckBox downloadIncompleteMembers = new JCheckBox(I18n.tr("Download incomplete route relation members"));
    private final JCheckBox stopArea = new JCheckBox(I18n.tr("Include stop_area tests"));

    /**
     * Setting up the pt_assistant preference tab
     */
    @Override
    public void addGui(PreferenceTabbedPane gui) {

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 5));

        mainPanel.add(downloadIncompleteMembers);
        mainPanel.add(stopArea);

        downloadIncompleteMembers.setSelected(Main.pref.getBoolean("pt_assistant.download-incomplete", false));
        stopArea.setSelected(Main.pref.getBoolean("pt_assistant.stop-area-tests", true));

        synchronized (gui.getDisplayPreference().getTabPane()) {
            gui.getValidatorPreference().addSubTab(this, "PT_Assistant", new JScrollPane(mainPanel));
            gui.getValidatorPreference().getTabPane().setIconAt(gui.getValidatorPreference().getTabPane().getTabCount() - 1,
                    new ImageProvider("presets/transport", "bus.svg").get());

        }

    }

    @Override
    public boolean isExpert() {
        return false;
    }

    /**
     * Action to be performed when the OK button is pressed
     */
    @Override
    public boolean ok() {
        Main.pref.put("pt_assistant.download-incomplete", this.downloadIncompleteMembers.isSelected());
        Main.pref.put("pt_assistant.stop-area-tests", this.stopArea.isSelected());
        return false;
    }

    @Override
    public TabPreferenceSetting getTabPreferenceSetting(PreferenceTabbedPane gui) {
        return gui.getDisplayPreference();
    }

}

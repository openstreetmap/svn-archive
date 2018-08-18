// License: GPL. See LICENSE file for details.
package org.openstreetmap.josm.plugins.pointinfo;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.GBC;

/**
 * Plugin preferences.
 */
public class PointInfoPreference extends DefaultTabPreferenceSetting {

    private final JComboBox<String> module = new JComboBox<>();
    private final JCheckBox autoMode = new JCheckBox(tr("Automatically detect the module"));

    /**
     * Constructs a new {@code PointInfoPreference}.
     */
    public PointInfoPreference() {
        super("pointinfo", tr("Point information settings"), tr("Settings for the point information plugin."), true);
    }

    @Override
    public String getIconName() {
        return "info-sml.png";
    }

    @Override
    public void addGui(PreferenceTabbedPane gui) {
        JPanel panel = new JPanel(new GridBagLayout());
        // autoMode
        autoMode.setSelected(Config.getPref().getBoolean("plugin.pointinfo.automode", false));
        autoMode.setToolTipText(tr("Try to guess the appropriate module from the location."
                + " If it fails, use the module selected below."));
        panel.add(autoMode, GBC.eol().insets(0, 0, 0, 0));
        // module
        for (String modName : PointInfoPlugin.getModules()) {
            module.addItem(modName);
        }
        module.setSelectedItem(Config.getPref().get("plugin.pointinfo.module", "RUIAN"));
        module.setToolTipText(tr("The module called to get the point information."));
        panel.add(new JLabel(tr("Module")), GBC.std());
        panel.add(module, GBC.eol().fill(GridBagConstraints.HORIZONTAL).insets(5, 0, 0, 5));
        panel.add(Box.createVerticalGlue(), GBC.eol().fill(GridBagConstraints.VERTICAL));
        createPreferenceTabWithScrollPane(gui, panel);
    }

    @Override
    public boolean ok() {
        Config.getPref().putBoolean("plugin.pointinfo.automode", autoMode.isSelected());
        Config.getPref().put("plugin.pointinfo.module", (String) module.getSelectedItem());
        return false;
    }
}

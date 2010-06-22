package ext_tools.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagConstraints;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.tools.GBC;

import ext_tools.ExtToolsPlugin;

public class ExtToolsPreference implements PreferenceSetting {

    ExtToolsPlugin plugin;

    public ExtToolsPreference(ExtToolsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void addGui(PreferenceTabbedPane gui) {
        JPanel p = gui.createPreferenceTab("ext", tr("External tools"),
                tr("Use external scripts in JOSM"));
        MyToolsPanel tp = new MyToolsPanel(plugin.myTools);
        tp.refresh();
        JScrollPane sp = new JScrollPane(tp);
        p.add(sp, GBC.eol().fill(GridBagConstraints.BOTH));
    }

    @Override
    public boolean ok() {
        plugin.myTools.save();
        plugin.repoTools.save();
        return false;
    }

}

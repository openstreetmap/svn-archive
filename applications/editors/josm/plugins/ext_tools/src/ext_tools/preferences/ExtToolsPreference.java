package ext_tools.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagConstraints;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.tools.GBC;

import ext_tools.ExtToolsPlugin;

public class ExtToolsPreference extends DefaultTabPreferenceSetting {

    ExtToolsPlugin plugin;

    public ExtToolsPreference(ExtToolsPlugin plugin) {
        super("ext", tr("External tools"), tr("Use external scripts in JOSM"));
        this.plugin = plugin;
    }

    @Override
    public void addGui(PreferenceTabbedPane gui) {
        JPanel p = gui.createPreferenceTab(this);
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

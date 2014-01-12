package ext_tools;

import java.io.File;

import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

import ext_tools.preferences.ExtToolsPreference;

public class ExtToolsPlugin extends Plugin {

    public static ExtToolsPlugin plugin;

    public final ToolsInformation myTools;
    public final ToolsInformation repoTools;

    public ExtToolsPlugin(PluginInformation info) {
        super(info);

        plugin = this;

        File plugindir = new File(this.getPluginDir());
        if (!plugindir.exists())
            plugindir.mkdirs();

        myTools = new ToolsInformation(new File(plugindir, "tools.cfg").getAbsolutePath());
        repoTools = new ToolsInformation(new File(plugindir, "repo.cfg").getAbsolutePath());

        for (ExtTool tool : myTools.getToolsList()) {
            tool.setEnabled(true);
        }
    }

    @Override
    public PreferenceSetting getPreferenceSetting() {
        return new ExtToolsPreference(this);
    }
}

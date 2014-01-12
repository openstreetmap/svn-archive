package org.openstreetmap.josm.plugins.tageditor;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class TagEditorPlugin extends Plugin {
    LaunchAction action;
    
    /**
     * constructor 
     */
    public TagEditorPlugin(PluginInformation info) {
        super(info);
        action = new LaunchAction();
        MainMenu.add(Main.main.menu.editMenu, action);
    }
}

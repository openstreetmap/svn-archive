package org.openstreetmap.josm.plugins.tofix;

import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class TofixPlugin extends Plugin {

    private IconToggleButton btn;
    protected static TofixDialog tofixDialog;
 
    public TofixPlugin(PluginInformation info) {
        super(info);
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (newFrame != null) {
            newFrame.addToggleDialog(tofixDialog = new TofixDialog());
        }
    }
}

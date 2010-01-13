package org.openstreetmap.josm.plugins.slippymap;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * Main class for the slippy map plugin.
 *
 * @author Frederik Ramm <frederik@remote.org>
 *
 */
public class SlippyMapPlugin extends Plugin
{
    public SlippyMapPlugin(PluginInformation info)
    {
    	super(info);
    }

    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame)
    {
        if (newFrame != null){
            SlippyMapLayer smlayer;
            smlayer = new SlippyMapLayer();
            Main.main.addLayer(smlayer);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openstreetmap.josm.plugins.Plugin#getPreferenceSetting()
     */
    @Override
    public PreferenceSetting getPreferenceSetting()
    {
        return new SlippyMapPreferenceSetting();
    }

}

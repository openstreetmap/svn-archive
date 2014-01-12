// License: WTFPL
package geochat;

import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * Create chat panel.
 * 
 * @author zverik
 */
public class GeoChatPlugin extends Plugin {
    public GeoChatPlugin( PluginInformation info ) {
        super(info);
    }
    
    @Override
    public void mapFrameInitialized( MapFrame oldFrame, MapFrame newFrame ) {
        if( oldFrame == null && newFrame != null ) {
            newFrame.addToggleDialog(new GeoChatPanel());
        }
    }
}
